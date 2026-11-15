package com.ruoyi.flowable.service.impl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.ai.core.AiClient;
import com.ruoyi.ai.core.AiResult;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.flowable.domain.FlowAiGenTask;
import com.ruoyi.flowable.domain.FlowForm;
import com.ruoyi.flowable.domain.FlowModel;
import com.ruoyi.flowable.mapper.FlowAiGenTaskMapper;
import com.ruoyi.flowable.service.IFlowAiGenService;
import com.ruoyi.flowable.service.IFlowFormService;
import com.ruoyi.flowable.service.IFlowModelService;
import com.ruoyi.system.service.ISysDeptService;
import com.ruoyi.system.service.ISysRoleService;
import org.flowable.engine.RepositoryService;

/**
 * AI流程生成 Service 实现
 *
 * 设计要点：
 * 1. AI只输出结构化JSON，BPMN由后端模板拼装，避免模型直接写XML导致语法错误；
 * 2. 结构化结果必须经过字段/节点白名单校验后才允许落地；
 * 3. 生成结果默认只建草稿（不部署），由管理员在流程模型/表单管理中确认后自行部署；
 * 4. 办理人由AI从系统角色/部门目录中选择，后端按名称映射为ID，未匹配项输出告警。
 *
 * @author ruoyi
 */
@Service
public class FlowAiGenServiceImpl implements IFlowAiGenService
{
    private static final Logger log = LoggerFactory.getLogger(FlowAiGenServiceImpl.class);

    /** AI模块标识 */
    private static final String AI_MODULE = "flowable";

    /** AI功能标识 */
    private static final String AI_FEATURE = "flow_generate";

    /** AI功能标识：截图/图片方案的文字识别 */
    private static final String AI_OCR_FEATURE = "flow_ocr";

    /** 图片识别提示词（业务侧默认值，可被 sys_config ai.prompt.flowable.flow_ocr 覆盖） */
    private static final String OCR_PROMPT = "请识别并输出图片中的全部文字，保持原阅读顺序，不要总结和解释。";

    /** 送模图片的原始体积上限（超过先压缩，避免请求体过大） */
    private static final long MAX_IMAGE_BYTES = 3L * 1024 * 1024;

    /** 送模图片的最长边（像素），超过则等比缩放 */
    private static final int MAX_IMAGE_EDGE = 2200;

    /** 方案文本送模上限（超出部分截断，避免超出上下文） */
    private static final int MAX_SOURCE_CHARS = 14000;

    /** 保存的原文上限 */
    private static final int MAX_STORE_CHARS = 60000;

    /** AI输出上限 */
    private static final int MAX_AI_OUTPUT_CHARS = 60000;

    /** 流程结构送模的输出上限 */
    private static final int PROCESS_MAX_TOKENS = 3000;

    /** 表单字段送模的输出上限 */
    private static final int FORM_MAX_TOKENS = 3500;

    /** 分段解析等待上限（秒） */
    private static final long AI_PART_TIMEOUT_SECONDS = 150L;

    /** 流程结构与表单字段并行解析线程池（守护线程，避免阻塞应用退出） */
    private static final ExecutorService AI_PART_EXECUTOR = Executors.newFixedThreadPool(2, r -> {
        Thread thread = new Thread(r, "flow-ai-gen-part");
        thread.setDaemon(true);
        return thread;
    });

    private static final String STATUS_PARSING = "0";
    private static final String STATUS_CONFIRM = "1";
    private static final String STATUS_GENERATED = "2";
    private static final String STATUS_FAILED = "3";

    private static final Set<String> ALLOWED_EXT = new HashSet<String>();

    /** 图片格式：走AI多模态识别文字 */
    private static final Set<String> IMAGE_EXT = new HashSet<String>();

    static
    {
        ALLOWED_EXT.add("pdf");
        ALLOWED_EXT.add("docx");
        ALLOWED_EXT.add("md");
        ALLOWED_EXT.add("markdown");
        ALLOWED_EXT.add("txt");

        IMAGE_EXT.add("png");
        IMAGE_EXT.add("jpg");
        IMAGE_EXT.add("jpeg");
        IMAGE_EXT.add("bmp");
        IMAGE_EXT.add("webp");
        ALLOWED_EXT.addAll(IMAGE_EXT);
    }

    @Autowired
    private FlowAiGenTaskMapper flowAiGenTaskMapper;

    @Autowired
    private AiClient aiClient;

    @Autowired
    private IFlowFormService flowFormService;

    @Autowired
    private IFlowModelService flowModelService;

    @Autowired
    private ISysRoleService sysRoleService;

    @Autowired
    private ISysDeptService sysDeptService;

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public List<FlowAiGenTask> selectFlowAiGenTaskList(FlowAiGenTask query)
    {
        return flowAiGenTaskMapper.selectFlowAiGenTaskList(query);
    }

    @Override
    public FlowAiGenTask selectFlowAiGenTaskById(Long taskId)
    {
        return flowAiGenTaskMapper.selectFlowAiGenTaskById(taskId);
    }

    @Override
    public Long uploadAndAnalyze(MultipartFile file, String taskName, String remark)
    {
        if (file == null || file.isEmpty())
        {
            throw new ServiceException("请上传流程开发方案文件");
        }
        String originalName = file.getOriginalFilename() == null ? "方案" : file.getOriginalFilename();
        String ext = extensionOf(originalName);
        if (!ALLOWED_EXT.contains(ext))
        {
            throw new ServiceException("仅支持 pdf、docx、md、txt、png、jpg、jpeg、bmp、webp 格式的流程方案文件");
        }
        FlowAiGenTask task = new FlowAiGenTask();
        task.setTaskName(StringUtils.isNotEmpty(taskName) ? taskName.trim() : stripExtension(originalName));
        task.setRemark(StringUtils.isNotEmpty(remark) ? truncate(remark.trim(), 500) : null);
        task.setFileName(originalName);
        task.setFileType(ext);
        task.setStatus(STATUS_PARSING);
        task.setNodeCount(0);
        task.setFieldCount(0);
        task.setCreateBy(SecurityUtils.getUsername());
        flowAiGenTaskMapper.insertFlowAiGenTask(task);

        try
        {
            File saved = saveUploadFile(file, originalName);
            task.setFilePath(saved.getAbsolutePath());
            String text = extractText(saved, ext);
            if (StringUtils.isEmpty(text) || text.trim().length() < 30)
            {
                throw new ServiceException("未能从流程方案中提取到有效文字，请确认文件内容清晰且包含流程描述（图片方案请保证文字可辨认）");
            }
            task.setSourceText(truncate(text, MAX_STORE_CHARS));
            analyzeAndSave(task);
        }
        catch (Exception e)
        {
            markFailed(task, e.getMessage() == null ? e.toString() : e.getMessage());
        }
        return task.getTaskId();
    }

    @Override
    public FlowAiGenTask reanalyze(Long taskId, String remark)
    {
        FlowAiGenTask task = flowAiGenTaskMapper.selectFlowAiGenTaskById(taskId);
        if (task == null)
        {
            throw new ServiceException("任务不存在或已删除");
        }
        if (StringUtils.isEmpty(task.getSourceText()))
        {
            throw new ServiceException("任务没有可用的方案原文，请重新上传方案文件");
        }
        // 备注说明允许在重新解析时修改，不传则沿用已保存内容
        if (remark != null)
        {
            task.setRemark(truncate(remark.trim(), 500));
        }
        task.setStatus(STATUS_PARSING);
        task.setErrorMsg("");
        flowAiGenTaskMapper.updateFlowAiGenTask(task);
        analyzeAndSave(task);
        return flowAiGenTaskMapper.selectFlowAiGenTaskById(taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> generate(Long taskId, String resultJson)
    {
        FlowAiGenTask task = flowAiGenTaskMapper.selectFlowAiGenTaskById(taskId);
        if (task == null)
        {
            throw new ServiceException("任务不存在或已删除");
        }
        String source = StringUtils.isNotEmpty(resultJson) ? resultJson : task.getResultJson();
        if (StringUtils.isEmpty(source))
        {
            throw new ServiceException("没有可生成的结构化结果，请先完成AI解析");
        }
        try
        {
            JSONObject raw = JSON.parseObject(source);
            JSONObject result = normalizeResult(raw, task.getTaskName());
            result.put("confirmed", Boolean.TRUE);
            result.put("unresolved", raw.getJSONArray("unresolved") == null
                    ? new JSONArray() : raw.getJSONArray("unresolved"));

            String processName = result.getString("processName");
            String processKey = uniqueModelKey(result.getString("processKey"));
            result.put("processKey", processKey);
            JSONObject form = result.getJSONObject("form");

            // 1. 创建表单并建物理数据表（含明细表）
            FlowForm flowForm = new FlowForm();
            flowForm.setFormName(processName + "表单");
            flowForm.setFormDesc("由AI根据流程方案《" + firstNonEmpty(task.getFileName(), task.getTaskName())
                    + "》生成，请确认字段后使用");
            flowForm.setCategory(result.getString("category"));
            flowForm.setProcessKey(processKey);
            flowForm.setFieldsJson(form.getJSONArray("fields").toJSONString());
            flowForm.setLayoutJson(form.getJSONObject("layout").toJSONString());
            flowFormService.insertFlowForm(flowForm);
            flowFormService.createTable(flowForm.getFormId());
            FlowForm savedForm = flowFormService.selectFlowFormByFormId(flowForm.getFormId());

            // 2. 创建流程模型（空模型）并写入生成的BPMN
            FlowModel model = new FlowModel();
            model.setModelName(processName);
            model.setModelKey(processKey);
            model.setCategory(result.getString("category"));
            model.setDescription(firstNonEmpty(result.getString("description"), "由AI根据流程方案自动生成")
                    + "（AI生成草稿，部署前请确认流程结构与办理人）");
            model.setFormId(flowForm.getFormId());
            String modelId = flowModelService.insertModel(model);

            FlowModel update = new FlowModel();
            update.setModelId(modelId);
            update.setBpmnXml(buildBpmn(result));
            flowModelService.updateModel(update);

            // 3. 回写任务
            task.setProcessName(processName);
            task.setProcessKey(processKey);
            task.setCategory(result.getString("category"));
            task.setResultJson(result.toJSONString());
            task.setStatus(STATUS_GENERATED);
            task.setErrorMsg("");
            task.setModelId(modelId);
            task.setFormId(flowForm.getFormId());
            task.setFormKey(savedForm == null ? null : savedForm.getFormKey());
            task.setNodeCount(result.getJSONArray("nodes").size());
            task.setFieldCount(form.getJSONArray("fields").size());
            task.setUpdateBy(SecurityUtils.getUsername());
            flowAiGenTaskMapper.updateFlowAiGenTask(task);

            Map<String, Object> data = new LinkedHashMap<String, Object>();
            data.put("taskId", taskId);
            data.put("modelId", modelId);
            data.put("modelName", processName);
            data.put("processKey", processKey);
            data.put("formId", flowForm.getFormId());
            data.put("formKey", savedForm == null ? null : savedForm.getFormKey());
            data.put("formName", processName + "表单");
            data.put("nodeCount", task.getNodeCount());
            data.put("fieldCount", task.getFieldCount());
            return data;
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            log.error("AI流程生成落地失败，taskId={}", taskId, e);
            throw new ServiceException("生成流程草稿失败：" + e.getMessage());
        }
    }

    @Override
    public int deleteFlowAiGenTaskByIds(Long[] taskIds)
    {
        return flowAiGenTaskMapper.deleteFlowAiGenTaskByIds(taskIds);
    }

    // ==================== AI解析 ====================

    /** 调用AI解析并保存结构化结果（流程结构、表单字段两段并行解析后合并） */
    private void analyzeAndSave(FlowAiGenTask task)
    {
        try
        {
            String processPrompt = buildProcessPrompt(task.getSourceText(), task.getRemark());
            String formPrompt = buildFormPrompt(task.getSourceText(), task.getRemark());
            long startAt = System.currentTimeMillis();

            Future<PartOutput> processFuture = AI_PART_EXECUTOR
                    .submit(() -> callForJson(processPrompt, PROCESS_MAX_TOKENS, "流程结构"));
            Future<PartOutput> formFuture = AI_PART_EXECUTOR
                    .submit(() -> callForJson(formPrompt, FORM_MAX_TOKENS, "表单字段"));

            PartOutput processOut;
            PartOutput formOut;
            try
            {
                processOut = processFuture.get(AI_PART_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                formOut = formFuture.get(AI_PART_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            }
            catch (ExecutionException e)
            {
                processFuture.cancel(true);
                formFuture.cancel(true);
                Throwable cause = e.getCause() == null ? e : e.getCause();
                markFailed(task, cause.getMessage() == null ? cause.toString() : cause.getMessage());
                return;
            }
            catch (TimeoutException e)
            {
                processFuture.cancel(true);
                formFuture.cancel(true);
                markFailed(task, "AI解析超时（已等待" + AI_PART_TIMEOUT_SECONDS + "秒），请精简方案内容或稍后重试");
                return;
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                markFailed(task, "AI解析被中断，请重试");
                return;
            }
            log.info("AI流程分段解析完成，taskId={}，耗时{}ms", task.getTaskId(), System.currentTimeMillis() - startAt);

            // 合并两段结果：流程结构 + 表单字段
            JSONObject merged = new JSONObject();
            merged.putAll(processOut.json);
            merged.remove("fields");
            merged.remove("sections");
            merged.remove("detailTables");
            JSONObject formPart = formOut.json.getJSONObject("form");
            if (formPart != null)
            {
                merged.put("form", formPart);
            }
            else
            {
                merged.put("fields", formOut.json.getJSONArray("fields"));
                merged.put("sections", formOut.json.getJSONArray("sections"));
                merged.put("detailTables", formOut.json.getJSONArray("detailTables"));
            }

            JSONObject result = normalizeResult(merged, task.getTaskName());
            task.setAiRawOutput(truncate("【流程结构】\n" + firstNonEmpty(processOut.raw, "")
                    + "\n\n【表单字段】\n" + firstNonEmpty(formOut.raw, ""), MAX_AI_OUTPUT_CHARS));
            task.setResultJson(result.toJSONString());
            task.setProcessName(result.getString("processName"));
            task.setProcessKey(result.getString("processKey"));
            task.setCategory(result.getString("category"));
            task.setNodeCount(result.getJSONArray("nodes").size());
            task.setFieldCount(result.getJSONObject("form").getJSONArray("fields").size());
            task.setStatus(STATUS_CONFIRM);
            task.setErrorMsg("");
            task.setUpdateBy(SecurityUtils.getUsername());
            flowAiGenTaskMapper.updateFlowAiGenTask(task);
        }
        catch (Exception e)
        {
            log.error("AI解析流程方案失败，taskId={}", task.getTaskId(), e);
            markFailed(task, e.getMessage() == null ? e.toString() : e.getMessage());
        }
    }

    private void markFailed(FlowAiGenTask task, String message)
    {
        task.setStatus(STATUS_FAILED);
        task.setErrorMsg(truncate(message, 900));
        task.setUpdateBy(SecurityUtils.getUsername());
        flowAiGenTaskMapper.updateFlowAiGenTask(task);
    }

    /** 组装流程结构提示词：角色/部门目录 + 方案原文 + 备注 + 输出契约 */
    private String buildProcessPrompt(String sourceText, String remark)
    {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是企业流程设计专家。请阅读下面的《流程开发方案》，只抽取“审批流程结构”（节点与连线），不要输出表单字段。\n\n");
        appendRemark(prompt, remark);
        prompt.append("【系统可用角色】（nodes[].participants[].type=role 时，name 必须原样取自此处）\n")
                .append(roleCatalog()).append("\n\n");
        prompt.append("【系统可用部门】（participants[].type=dept 或 deptLeader 时，name 必须原样取自此处）\n")
                .append(deptCatalog()).append("\n\n");
        prompt.append("【流程开发方案】\n").append(truncate(sourceText, MAX_SOURCE_CHARS)).append("\n\n");
        prompt.append("【输出要求】\n")
                .append("1. 严格只输出一个 JSON 对象，禁止输出 markdown 代码块、注释或解释文字。\n")
                .append("2. 只输出 processName、processKey、category、description、nodes、flows 六个字段，禁止输出任何表单字段。\n")
                .append("3. nodes 按流程执行顺序排列，必须且只能有 1 个 start 与 1 个 end。\n")
                .append("4. type 取值：start、userTask、exclusiveGateway、end。\n")
                .append("5. userTask 的 participants 最多 3 个，只能从上面的角色/部门目录里选；无法确定时给空数组。\n")
                .append("6. flows 用节点 id 连接；exclusiveGateway 的多条出线用 condition 填 UEL 表达式，例如 ${amount > 1000}。\n")
                .append("7. 方案中未说明的分支条件不要编造，按顺序连线即可。\n\n");
        prompt.append("【JSON结构示例】\n")
                .append("{\"processName\":\"采购申请\",\"processKey\":\"purchase_apply\",\"category\":\"\",")
                .append("\"description\":\"采购申请审批流程\",")
                .append("\"nodes\":[")
                .append("{\"id\":\"start\",\"type\":\"start\",\"name\":\"开始\"},")
                .append("{\"id\":\"dept_approve\",\"type\":\"userTask\",\"name\":\"部门经理审批\",")
                .append("\"participants\":[{\"type\":\"role\",\"name\":\"部门经理\"}]},")
                .append("{\"id\":\"amount_gateway\",\"type\":\"exclusiveGateway\",\"name\":\"金额判断\"},")
                .append("{\"id\":\"end\",\"type\":\"end\",\"name\":\"结束\"}],")
                .append("\"flows\":[{\"from\":\"start\",\"to\":\"dept_approve\"},")
                .append("{\"from\":\"dept_approve\",\"to\":\"amount_gateway\"},")
                .append("{\"from\":\"amount_gateway\",\"to\":\"end\",\"condition\":\"${amount <= 1000}\"}]}");
        return prompt.toString();
    }

    /** 组装表单字段提示词：方案原文 + 备注 + 输出契约 */
    private String buildFormPrompt(String sourceText, String remark)
    {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是企业表单设计专家。请阅读下面的《流程开发方案》，只抽取“流程表单字段与布局”，不要输出流程节点与连线。\n\n");
        appendRemark(prompt, remark);
        prompt.append("【流程开发方案】\n").append(truncate(sourceText, MAX_SOURCE_CHARS)).append("\n\n");
        prompt.append("【输出要求】\n")
                .append("1. 严格只输出一个 JSON 对象，禁止输出 markdown 代码块、注释或解释文字。\n")
                .append("2. 只输出 form 对象（含 fields、sections、detailTables），禁止输出流程节点与连线。\n")
                .append("3. 表单字段 field 必须小写字母开头、仅含小写字母/数字/下划线；form 取值：")
                .append("text、textarea、number、date、datetime、select、radio、checkbox、attach、browse。\n")
                .append("4. 附件字段 form=attach；人员字段 form=browse、browseType=user；部门字段 form=browse、browseType=dept。\n")
                .append("5. 明细子表：字段增加 \"table\":\"dt1\"（第二张用 dt2），明细字段不要放进 sections；")
                .append("同时在 detailTables 中给出明细表名。\n")
                .append("6. sections 只放主表字段，每行 span 合计不超过 24（常规字段 12，文本域/附件/明细 24）；")
                .append("所有主表字段都必须出现在 sections 中。\n")
                .append("7. 方案里明确要求必填的字段设置 required=true；不要编造方案中不存在的业务字段。\n\n");
        prompt.append("【JSON结构示例】\n")
                .append("{\"form\":{\"sections\":[{\"title\":\"基本信息\",\"rows\":[[")
                .append("{\"field\":\"apply_reason\",\"span\":24},{\"field\":\"apply_amount\",\"span\":12}]}]},")
                .append("\"fields\":[")
                .append("{\"field\":\"apply_reason\",\"label\":\"申请事由\",\"form\":\"textarea\",\"required\":true},")
                .append("{\"field\":\"apply_amount\",\"label\":\"申请金额\",\"form\":\"number\",\"required\":true},")
                .append("{\"field\":\"apply_attach\",\"label\":\"附件\",\"form\":\"attach\"},")
                .append("{\"field\":\"item_name\",\"label\":\"物品名称\",\"form\":\"text\",\"table\":\"dt1\"}]},")
                .append("\"detailTables\":[{\"table\":\"dt1\",\"title\":\"采购明细\"}]}}");
        return prompt.toString();
    }

    /** 备注说明：用户补充的生成要求，优先于方案原文 */
    private void appendRemark(StringBuilder prompt, String remark)
    {
        if (StringUtils.isNotEmpty(remark) && StringUtils.isNotEmpty(remark.trim()))
        {
            prompt.append("【用户补充要求】（优先满足，与方案冲突时以补充要求为准）\n")
                    .append(truncate(remark.trim(), 600)).append("\n\n");
        }
    }

    /** 系统角色目录文本 */
    private String roleCatalog()
    {
        StringBuilder roles = new StringBuilder();
        try
        {
            List<SysRole> roleList = sysRoleService.selectRoleAll();
            int count = 0;
            for (SysRole role : roleList)
            {
                if (StringUtils.isEmpty(role.getRoleName()))
                {
                    continue;
                }
                if (count++ > 0)
                {
                    roles.append("、");
                }
                roles.append(role.getRoleName());
                if (StringUtils.isNotEmpty(role.getRoleKey()))
                {
                    roles.append("(").append(role.getRoleKey()).append(")");
                }
                if (count >= 120)
                {
                    break;
                }
            }
        }
        catch (Exception e)
        {
            log.warn("加载角色目录失败：{}", e.getMessage());
        }
        return roles.length() == 0 ? "（无）" : roles.toString();
    }

    /** 系统部门目录文本 */
    private String deptCatalog()
    {
        StringBuilder depts = new StringBuilder();
        try
        {
            List<SysDept> deptList = sysDeptService.selectDeptList(new SysDept());
            int count = 0;
            for (SysDept dept : deptList)
            {
                if (StringUtils.isEmpty(dept.getDeptName()))
                {
                    continue;
                }
                if (count++ > 0)
                {
                    depts.append("、");
                }
                depts.append(dept.getDeptName());
                if (count >= 150)
                {
                    break;
                }
            }
        }
        catch (Exception e)
        {
            log.warn("加载部门目录失败：{}", e.getMessage());
        }
        return depts.length() == 0 ? "（无）" : depts.toString();
    }

    /** 单段AI解析：要求输出JSON，非法JSON时纠偏一次 */
    private PartOutput callForJson(String prompt, int maxTokens, String partName)
    {
        AiResult ai = aiClient.chat(AI_MODULE, AI_FEATURE, prompt, maxTokens);
        String rawOutput = ai.getContent();
        JSONObject parsed = ai.isSuccess() ? tryParseJson(rawOutput) : null;
        if (ai.isSuccess() && parsed == null)
        {
            String fixPrompt = prompt + "\n\n【纠偏要求】上一次输出无法解析为JSON对象，请严格只输出一个JSON对象，"
                    + "不要输出markdown代码块和任何解释。上一次输出片段：\n"
                    + truncate(rawOutput, 600);
            AiResult retry = aiClient.chat(AI_MODULE, AI_FEATURE, fixPrompt, maxTokens);
            if (retry.isSuccess())
            {
                rawOutput = retry.getContent();
                parsed = tryParseJson(rawOutput);
            }
        }
        if (parsed == null)
        {
            throw new ServiceException(partName + "解析失败："
                    + (ai.isSuccess() ? "AI返回内容无法解析为JSON，请重试" : "AI调用失败：" + firstNonEmpty(ai.getError(), "未知错误")));
        }
        return new PartOutput(parsed, rawOutput);
    }

    /** 分段AI解析结果（结构化JSON + 原始输出） */
    private static final class PartOutput
    {
        private final JSONObject json;
        private final String raw;

        private PartOutput(JSONObject json, String raw)
        {
            this.json = json;
            this.raw = raw;
        }
    }

    // ==================== 结构化校验与归一化 ====================

    /**
     * 校验并归一化AI输出（同时用于生成前的二次校验，保证人工修改后的结果依然合法）
     */
    private JSONObject normalizeResult(JSONObject raw, String fallbackName)
    {
        if (raw == null)
        {
            throw new ServiceException("AI结果为空");
        }
        JSONObject process = raw.getJSONObject("process");
        JSONObject formNode = raw.getJSONObject("form");
        List<String> warnings = new ArrayList<String>();

        // ---- 流程基础信息 ----
        String processName = firstNonEmpty(
                process == null ? null : process.getString("name"),
                raw.getString("processName"), raw.getString("name"), fallbackName, "AI生成流程");
        processName = truncate(processName.trim(), 100);
        String keySource = firstNonEmpty(
                process == null ? null : process.getString("key"),
                raw.getString("processKey"), raw.getString("key"), processName);
        String processKey = sanitizeKey(keySource);
        String category = firstNonEmpty(raw.getString("category"),
                process == null ? null : process.getString("category"), "");
        String description = firstNonEmpty(raw.getString("description"),
                process == null ? null : process.getString("description"), processName + "流程");

        // ---- 节点 ----
        JSONArray rawNodes = raw.getJSONArray("nodes");
        if (rawNodes == null && process != null)
        {
            rawNodes = process.getJSONArray("nodes");
        }
        if (rawNodes == null || rawNodes.isEmpty())
        {
            throw new ServiceException("AI未识别出流程节点，请补充方案中的审批步骤后重试");
        }
        Map<String, SysRole> roleIndex = buildRoleIndex();
        Map<String, SysDept> deptIndex = buildDeptIndex();
        JSONArray nodes = new JSONArray();
        Map<String, String> idByName = new HashMap<String, String>();
        Set<String> usedIds = new HashSet<String>();
        JSONArray unresolved = new JSONArray();
        boolean hasStartNode = false;
        boolean hasEndNode = false;
        for (int i = 0; i < rawNodes.size(); i++)
        {
            JSONObject in = rawNodes.getJSONObject(i);
            if (in == null)
            {
                continue;
            }
            String type = normalizeNodeType(in.getString("type"));
            if (type == null)
            {
                warnings.add("已忽略不支持的节点类型：" + in.getString("type"));
                continue;
            }
            // 开始/结束节点必须唯一，重复的降级为审批节点，保证可部署
            if ("startEvent".equals(type) && hasStartNode)
            {
                type = "userTask";
                warnings.add("AI输出了多个开始节点，多余的已转为审批节点");
            }
            else if ("endEvent".equals(type) && hasEndNode)
            {
                type = "userTask";
                warnings.add("AI输出了多个结束节点，多余的已转为审批节点");
            }
            if ("startEvent".equals(type))
            {
                hasStartNode = true;
            }
            else if ("endEvent".equals(type))
            {
                hasEndNode = true;
            }
            String name = firstNonEmpty(in.getString("name"), in.getString("label"),
                    "userTask".equals(type) ? "审批节点" : type);
            String id = uniqueNodeId(sanitizeId(in.getString("id")), type, i + 1, usedIds);
            JSONObject node = new JSONObject();
            node.put("id", id);
            node.put("type", type);
            node.put("name", name);
            if ("userTask".equals(type))
            {
                JSONArray parts = normalizeParticipants(in, roleIndex, deptIndex, unresolved, name);
                node.put("participants", parts);
            }
            nodes.add(node);
            idByName.put(name, id);
            if (StringUtils.isNotEmpty(in.getString("id")))
            {
                idByName.put(in.getString("id"), id);
            }
        }
        if (nodes.isEmpty())
        {
            throw new ServiceException("AI未识别出有效流程节点");
        }
        ensureStartEnd(nodes, warnings);

        // ---- 连线 ----
        JSONArray rawFlows = raw.getJSONArray("flows");
        if (rawFlows == null && process != null)
        {
            rawFlows = process.getJSONArray("flows");
        }
        JSONArray flows = new JSONArray();
        Set<String> ids = new LinkedHashSet<String>();
        for (int i = 0; i < nodes.size(); i++)
        {
            ids.add(nodes.getJSONObject(i).getString("id"));
        }
        if (rawFlows != null)
        {
            for (int i = 0; i < rawFlows.size(); i++)
            {
                JSONObject in = rawFlows.getJSONObject(i);
                if (in == null)
                {
                    continue;
                }
                String from = resolveNodeId(firstNonEmpty(in.getString("from"), in.getString("source"),
                        in.getString("sourceRef")), ids, idByName);
                String to = resolveNodeId(firstNonEmpty(in.getString("to"), in.getString("target"),
                        in.getString("targetRef")), ids, idByName);
                if (from == null || to == null || from.equals(to))
                {
                    continue;
                }
                JSONObject flow = new JSONObject();
                flow.put("from", from);
                flow.put("to", to);
                String condition = in.getString("condition");
                if (StringUtils.isNotEmpty(condition))
                {
                    flow.put("condition", condition.trim());
                }
                flows.add(flow);
            }
        }
        repairFlows(nodes, flows, warnings);

        // ---- 表单 ----
        JSONArray rawFields = raw.getJSONArray("fields");
        if (rawFields == null && formNode != null)
        {
            rawFields = formNode.getJSONArray("fields");
        }
        if (rawFields == null || rawFields.isEmpty())
        {
            throw new ServiceException("AI未识别出表单字段，请补充方案中的表单要素后重试");
        }
        List<JSONObject> fields = normalizeFields(rawFields, warnings);
        if (fields.isEmpty())
        {
            throw new ServiceException("AI未识别出合法表单字段");
        }
        JSONArray rawSections = raw.getJSONArray("sections");
        if (rawSections == null && formNode != null)
        {
            rawSections = formNode.getJSONArray("sections");
        }
        JSONArray rawDetails = raw.getJSONArray("detailTables");
        if (rawDetails == null && formNode != null)
        {
            rawDetails = formNode.getJSONArray("detailTables");
        }
        JSONObject layout = buildLayout(fields, rawSections, rawDetails);

        JSONObject result = new JSONObject();
        result.put("processName", processName);
        result.put("processKey", processKey);
        result.put("category", category);
        result.put("description", description);
        result.put("nodes", nodes);
        result.put("flows", flows);
        JSONObject formOut = new JSONObject();
        JSONArray fieldArray = new JSONArray();
        for (JSONObject f : fields)
        {
            fieldArray.add(f);
        }
        formOut.put("fields", fieldArray);
        formOut.put("layout", layout);
        result.put("form", formOut);
        result.put("unresolved", unresolved);
        result.put("warnings", toJSONArray(warnings));
        return result;
    }

    private Map<String, SysRole> buildRoleIndex()
    {
        Map<String, SysRole> map = new HashMap<String, SysRole>();
        try
        {
            List<SysRole> roles = sysRoleService.selectRoleAll();
            for (SysRole role : roles)
            {
                if (StringUtils.isNotEmpty(role.getRoleName()))
                {
                    map.put(role.getRoleName().trim().toLowerCase(), role);
                }
                if (StringUtils.isNotEmpty(role.getRoleKey()))
                {
                    map.put(role.getRoleKey().trim().toLowerCase(), role);
                }
            }
        }
        catch (Exception e)
        {
            log.warn("加载角色目录失败：{}", e.getMessage());
        }
        return map;
    }

    private Map<String, SysDept> buildDeptIndex()
    {
        Map<String, SysDept> map = new HashMap<String, SysDept>();
        try
        {
            List<SysDept> depts = sysDeptService.selectDeptList(new SysDept());
            for (SysDept dept : depts)
            {
                if (StringUtils.isNotEmpty(dept.getDeptName()))
                {
                    map.put(dept.getDeptName().trim().toLowerCase(), dept);
                }
            }
        }
        catch (Exception e)
        {
            log.warn("加载部门目录失败：{}", e.getMessage());
        }
        return map;
    }

    /** 办理人归一化：名称 → 系统角色/部门ID */
    private JSONArray normalizeParticipants(JSONObject in, Map<String, SysRole> roleIndex,
            Map<String, SysDept> deptIndex, JSONArray unresolved, String nodeName)
    {
        JSONArray out = new JSONArray();
        Set<String> seen = new HashSet<String>();
        JSONArray parts = in.getJSONArray("participants");
        if (parts == null)
        {
            parts = new JSONArray();
            String simple = firstNonEmpty(in.getString("assigneeName"), in.getString("roleName"),
                    in.getString("assignee"), in.getString("candidateGroups"));
            if (StringUtils.isNotEmpty(simple))
            {
                JSONObject p = new JSONObject();
                p.put("type", inferParticipantType(simple));
                p.put("name", simple);
                parts.add(p);
            }
        }
        for (int i = 0; i < parts.size(); i++)
        {
            Object item = parts.get(i);
            String type;
            String name;
            if (item instanceof String)
            {
                type = inferParticipantType((String) item);
                name = (String) item;
            }
            else
            {
                JSONObject p = parts.getJSONObject(i);
                if (p == null)
                {
                    continue;
                }
                name = firstNonEmpty(p.getString("name"), p.getString("label"), p.getString("id"));
                type = normalizeParticipantType(firstNonEmpty(p.getString("type"),
                        p.getString("participantType"), p.getString("kind")), name);
            }
            if (StringUtils.isEmpty(name))
            {
                continue;
            }
            String key = name.trim().toLowerCase();
            if (!seen.add(type + ":" + key))
            {
                continue;
            }
            JSONObject resolved = null;
            if ("role".equals(type))
            {
                SysRole role = roleIndex.get(key);
                if (role == null)
                {
                    role = fuzzyRole(roleIndex, key);
                }
                if (role != null)
                {
                    resolved = participant("role", String.valueOf(role.getRoleId()), role.getRoleName());
                }
            }
            else
            {
                SysDept dept = deptIndex.get(key);
                if (dept == null)
                {
                    dept = fuzzyDept(deptIndex, key);
                }
                if (dept != null)
                {
                    resolved = participant(type, String.valueOf(dept.getDeptId()), dept.getDeptName());
                }
            }
            if (resolved != null)
            {
                out.add(resolved);
            }
            else
            {
                unresolved.add(nodeName + "：" + name);
            }
        }
        return out;
    }

    private SysRole fuzzyRole(Map<String, SysRole> index, String key)
    {
        for (Map.Entry<String, SysRole> e : index.entrySet())
        {
            if (e.getKey().contains(key) || key.contains(e.getKey()))
            {
                return e.getValue();
            }
        }
        return null;
    }

    private SysDept fuzzyDept(Map<String, SysDept> index, String key)
    {
        for (Map.Entry<String, SysDept> e : index.entrySet())
        {
            if (e.getKey().contains(key) || key.contains(e.getKey()))
            {
                return e.getValue();
            }
        }
        return null;
    }

    private JSONObject participant(String type, String id, String name)
    {
        JSONObject p = new JSONObject();
        p.put("type", type);
        JSONArray ids = new JSONArray();
        ids.add(id);
        p.put("ids", ids);
        p.put("name", name);
        return p;
    }

    private String inferParticipantType(String name)
    {
        String n = name == null ? "" : name;
        if (n.contains("部") || n.contains("科") || n.contains("中心") || n.contains("车间")
                || n.contains("公司") || n.contains("组织"))
        {
            return n.contains("领导") || n.contains("负责人") || n.contains("经理") ? "deptLeader" : "dept";
        }
        return "role";
    }

    private String normalizeParticipantType(String type, String name)
    {
        String t = type == null ? "" : type.trim().toLowerCase();
        if ("role".equals(t) || "角色".equals(t) || "岗位角色".equals(t))
        {
            return "role";
        }
        if ("dept".equals(t) || "department".equals(t) || "部门".equals(t) || "组织".equals(t))
        {
            return "dept";
        }
        if ("deptleader".equals(t) || "dept_leader".equals(t) || "部门领导".equals(t)
                || "部门负责人".equals(t) || "leader".equals(t))
        {
            return "deptLeader";
        }
        return inferParticipantType(name);
    }

    /** 字段归一化：类型白名单、字段名合法化、明细表编号、默认长度等 */
    private List<JSONObject> normalizeFields(JSONArray rawFields, List<String> warnings)
    {
        List<JSONObject> list = new ArrayList<JSONObject>();
        Set<String> used = new HashSet<String>();
        int mainOrder = 0;
        Map<String, Integer> dtOrder = new HashMap<String, Integer>();
        for (int i = 0; i < rawFields.size(); i++)
        {
            JSONObject in = rawFields.getJSONObject(i);
            if (in == null)
            {
                continue;
            }
            String label = firstNonEmpty(in.getString("label"), in.getString("title"),
                    in.getString("name"), in.getString("field"));
            if (StringUtils.isEmpty(label))
            {
                continue;
            }
            String table = normalizeTable(in.getString("table"));
            String field = normalizeFieldName(firstNonEmpty(in.getString("field"), in.getString("name")),
                    label, i + 1);
            String unique = table + "." + field;
            int suffix = 2;
            while (used.contains(unique))
            {
                String candidate = field + "_" + suffix;
                unique = table + "." + candidate;
                field = candidate;
                suffix++;
            }
            used.add(unique);

            String form = normalizeFormType(firstNonEmpty(in.getString("form"), in.getString("type"), "text"));
            JSONObject f = new JSONObject();
            f.put("field", field);
            f.put("label", truncate(label, 100));
            f.put("form", form);
            f.put("table", table);
            f.put("required", in.getBooleanValue("required") || "true".equalsIgnoreCase(in.getString("required")));
            f.put("dtype", normalizeDtype(form, in.getString("dtype")));
            if ("main".equals(table))
            {
                f.put("order", ++mainOrder);
            }
            else
            {
                Integer o = dtOrder.get(table) == null ? 0 : dtOrder.get(table);
                o = o + 1;
                dtOrder.put(table, o);
                f.put("order", o);
            }
            if ("textarea".equals(form))
            {
                f.put("length", 2000);
                f.put("rows", 4);
            }
            else if ("text".equals(form))
            {
                f.put("length", 200);
            }
            else if ("select".equals(form) || "radio".equals(form) || "checkbox".equals(form))
            {
                f.put("length", 200);
                f.put("selectOptions", normalizeOptions(in));
            }
            else if ("attach".equals(form))
            {
                f.put("length", 1000);
                f.put("attachType", "all");
                f.put("attachLimit", 5);
            }
            else if ("browse".equals(form))
            {
                f.put("length", 500);
                String browseType = in.getString("browseType");
                f.put("browseType", "dept".equalsIgnoreCase(browseType) ? "dept" : "user");
            }
            else if ("number".equals(form))
            {
                f.put("dtype", "float");
                f.put("scale", 2);
            }
            else if ("date".equals(form))
            {
                f.put("dtype", "date");
            }
            else if ("datetime".equals(form))
            {
                f.put("dtype", "datetime");
            }
            list.add(f);
        }
        return list;
    }

    private String normalizeFormType(String raw)
    {
        String t = raw == null ? "text" : raw.trim().toLowerCase();
        if ("input".equals(t) || "单行文本".equals(t) || "文本".equals(t))
        {
            return "text";
        }
        if ("多行文本".equals(t) || "multiline".equals(t))
        {
            return "textarea";
        }
        if ("数字".equals(t) || "int".equals(t) || "float".equals(t) || "decimal".equals(t))
        {
            return "number";
        }
        if ("日期".equals(t))
        {
            return "date";
        }
        if ("日期时间".equals(t) || "time".equals(t))
        {
            return "datetime";
        }
        if ("下拉".equals(t) || "下拉框".equals(t))
        {
            return "select";
        }
        if ("单选".equals(t))
        {
            return "radio";
        }
        if ("多选".equals(t))
        {
            return "checkbox";
        }
        if ("附件".equals(t) || "file".equals(t) || "upload".equals(t))
        {
            return "attach";
        }
        if ("人员".equals(t) || "部门".equals(t) || "browseuser".equals(t) || "browsedept".equals(t))
        {
            return "browse";
        }
        if ("textarea".equals(t) || "number".equals(t) || "date".equals(t) || "datetime".equals(t)
                || "select".equals(t) || "radio".equals(t) || "checkbox".equals(t)
                || "attach".equals(t) || "browse".equals(t) || "text".equals(t))
        {
            return t;
        }
        return "text";
    }

    private String normalizeDtype(String form, String dtype)
    {
        if ("date".equals(form))
        {
            return "date";
        }
        if ("datetime".equals(form))
        {
            return "datetime";
        }
        if ("number".equals(form))
        {
            return "number".equals(dtype) || "int".equals(dtype) ? "int" : "float";
        }
        return StringUtils.isEmpty(dtype) ? "text" : dtype;
    }

    private JSONArray normalizeOptions(JSONObject in)
    {
        JSONArray options = in.getJSONArray("selectOptions");
        if (options == null)
        {
            options = in.getJSONArray("options");
        }
        JSONArray out = new JSONArray();
        if (options != null)
        {
            for (int i = 0; i < options.size(); i++)
            {
                Object item = options.get(i);
                JSONObject opt = new JSONObject();
                if (item instanceof JSONObject)
                {
                    JSONObject o = (JSONObject) item;
                    String label = firstNonEmpty(o.getString("label"), o.getString("text"), o.getString("value"));
                    String value = firstNonEmpty(o.getString("value"), label);
                    if (StringUtils.isEmpty(label))
                    {
                        continue;
                    }
                    opt.put("label", label);
                    opt.put("value", value);
                }
                else
                {
                    String v = String.valueOf(item);
                    if (StringUtils.isEmpty(v))
                    {
                        continue;
                    }
                    opt.put("label", v);
                    opt.put("value", v);
                }
                out.add(opt);
            }
        }
        if (out.isEmpty())
        {
            String raw = in.getString("options");
            if (StringUtils.isNotEmpty(raw) && !raw.trim().startsWith("["))
            {
                for (String v : raw.split("[,，、;；/]"))
                {
                    if (StringUtils.isNotEmpty(v.trim()))
                    {
                        JSONObject opt = new JSONObject();
                        opt.put("label", v.trim());
                        opt.put("value", v.trim());
                        out.add(opt);
                    }
                }
            }
        }
        return out;
    }

    /** 构建 section 布局；明细表以 detailTable 单元格形式追加 */
    private JSONObject buildLayout(List<JSONObject> fields, JSONArray rawSections, JSONArray rawDetails)
    {
        Map<String, JSONObject> mainFields = new LinkedHashMap<String, JSONObject>();
        Set<String> detailTables = new LinkedHashSet<String>();
        for (JSONObject f : fields)
        {
            if ("main".equals(f.getString("table")))
            {
                mainFields.put(f.getString("field"), f);
            }
            else
            {
                detailTables.add(f.getString("table"));
            }
        }
        JSONArray sections = new JSONArray();
        Set<String> placed = new HashSet<String>();
        if (rawSections != null)
        {
            for (int i = 0; i < rawSections.size(); i++)
            {
                Object item = rawSections.get(i);
                if (!(item instanceof JSONObject))
                {
                    continue;
                }
                JSONObject in = (JSONObject) item;
                JSONArray rowsIn = in.getJSONArray("rows");
                JSONArray rowsOut = new JSONArray();
                if (rowsIn != null)
                {
                    for (int r = 0; r < rowsIn.size(); r++)
                    {
                        Object rowItem = rowsIn.get(r);
                        JSONArray cellsIn = null;
                        if (rowItem instanceof JSONArray)
                        {
                            cellsIn = (JSONArray) rowItem;
                        }
                        else if (rowItem instanceof JSONObject)
                        {
                            cellsIn = ((JSONObject) rowItem).getJSONArray("fields");
                            if (cellsIn == null)
                            {
                                cellsIn = ((JSONObject) rowItem).getJSONArray("cells");
                            }
                        }
                        if (cellsIn == null)
                        {
                            continue;
                        }
                        JSONArray cellsOut = new JSONArray();
                        for (int c = 0; c < cellsIn.size(); c++)
                        {
                            Object cellItem = cellsIn.get(c);
                            String fieldName = cellItem instanceof JSONObject
                                    ? firstNonEmpty(((JSONObject) cellItem).getString("field"),
                                            ((JSONObject) cellItem).getString("name"))
                                    : String.valueOf(cellItem);
                            JSONObject field = mainFields.get(fieldName);
                            if (field == null || placed.contains(fieldName))
                            {
                                continue;
                            }
                            JSONObject cell = new JSONObject();
                            cell.put("field", fieldName);
                            cell.put("span", cellSpan(field, cellItem));
                            cellsOut.add(cell);
                            placed.add(fieldName);
                        }
                        if (!cellsOut.isEmpty())
                        {
                            rowsOut.add(cellsOut);
                        }
                    }
                }
                if (!rowsOut.isEmpty())
                {
                    JSONObject section = new JSONObject();
                    section.put("title", firstNonEmpty(in.getString("title"), "基本信息"));
                    section.put("rows", rowsOut);
                    sections.add(section);
                }
            }
        }
        // 补齐AI漏排的主表字段
        JSONArray missingRows = new JSONArray();
        for (Map.Entry<String, JSONObject> e : mainFields.entrySet())
        {
            if (placed.contains(e.getKey()))
            {
                continue;
            }
            JSONObject cell = new JSONObject();
            cell.put("field", e.getKey());
            cell.put("span", cellSpan(e.getValue(), null));
            JSONArray missingRow = new JSONArray();
            missingRow.add(cell);
            missingRows.add(missingRow);
            placed.add(e.getKey());
        }
        if (!missingRows.isEmpty())
        {
            JSONObject section = new JSONObject();
            section.put("title", sections.isEmpty() ? "基本信息" : "其他信息");
            section.put("rows", missingRows);
            sections.add(section);
        }
        // 明细表区块
        if (!detailTables.isEmpty())
        {
            Map<String, String> titles = new HashMap<String, String>();
            if (rawDetails != null)
            {
                for (int i = 0; i < rawDetails.size(); i++)
                {
                    JSONObject d = rawDetails.getJSONObject(i);
                    if (d != null && StringUtils.isNotEmpty(d.getString("table")))
                    {
                        titles.put(normalizeTable(d.getString("table")), d.getString("title"));
                    }
                }
            }
            JSONArray rows = new JSONArray();
            for (String dt : detailTables)
            {
                JSONObject cell = new JSONObject();
                cell.put("detailTable", dt);
                cell.put("span", 24);
                String title = titles.get(dt);
                if (StringUtils.isNotEmpty(title))
                {
                    cell.put("detailTitle", title);
                }
                JSONArray detailRow = new JSONArray();
                detailRow.add(cell);
                rows.add(detailRow);
            }
            JSONObject section = new JSONObject();
            section.put("title", "明细信息");
            section.put("rows", rows);
            sections.add(section);
        }
        if (sections.isEmpty())
        {
            JSONObject section = new JSONObject();
            section.put("title", "基本信息");
            JSONArray rows = new JSONArray();
            JSONArray current = new JSONArray();
            int used = 0;
            for (JSONObject f : mainFields.values())
            {
                JSONObject cell = new JSONObject();
                cell.put("field", f.getString("field"));
                cell.put("span", cellSpan(f, null));
                current.add(cell);
                used += cell.getIntValue("span");
                if (used >= 24)
                {
                    rows.add(current);
                    current = new JSONArray();
                    used = 0;
                }
            }
            if (!current.isEmpty())
            {
                rows.add(current);
            }
            section.put("rows", rows);
            sections.add(section);
        }
        JSONObject layout = new JSONObject();
        layout.put("mode", "section");
        layout.put("sections", sections);
        return layout;
    }

    private int cellSpan(JSONObject field, Object rawCell)
    {
        String form = field.getString("form");
        if ("textarea".equals(form) || "attach".equals(form))
        {
            return 24;
        }
        if (rawCell instanceof JSONObject)
        {
            int span = ((JSONObject) rawCell).getIntValue("span");
            if (span >= 6 && span <= 24)
            {
                return span;
            }
        }
        return 12;
    }

    private String normalizeTable(String table)
    {
        if (StringUtils.isEmpty(table))
        {
            return "main";
        }
        String t = table.trim().toLowerCase();
        if ("main".equals(t) || "主表".equals(t))
        {
            return "main";
        }
        if (t.matches("^dt[1-9]$"))
        {
            return t;
        }
        if (t.matches("^detail[1-9]$"))
        {
            return "dt" + t.substring(6);
        }
        if (t.matches("^明细[1-9]$"))
        {
            return "dt" + t.substring(2);
        }
        if ("明细".equals(t) || "detail".equals(t) || "明细表".equals(t))
        {
            return "dt1";
        }
        return "main";
    }

    private String normalizeFieldName(String raw, String label, int index)
    {
        String name = raw == null ? "" : raw.trim();
        if (StringUtils.isEmpty(name))
        {
            name = label == null ? "" : label.trim();
        }
        // 中文标签无法直接作为列名，使用序号兜底
        if (!name.matches(".*[a-zA-Z].*"))
        {
            return "field_" + index;
        }
        name = name.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
        name = name.replaceAll("[^a-z0-9_]", "_");
        name = name.replaceAll("_+", "_").replaceAll("^_+|_+$", "");
        if (StringUtils.isEmpty(name))
        {
            return "field_" + index;
        }
        if (!name.matches("^[a-z].*"))
        {
            name = "f_" + name;
        }
        if (name.length() > 40)
        {
            name = name.substring(0, 40);
        }
        if (!name.matches("^[a-z][a-z0-9_]{0,39}$"))
        {
            return "field_" + index;
        }
        return name;
    }

    private String normalizeNodeType(String raw)
    {
        if (StringUtils.isEmpty(raw))
        {
            return "userTask";
        }
        String t = raw.trim().toLowerCase();
        if ("start".equals(t) || "startevent".equals(t) || "开始".equals(t) || "开始节点".equals(t))
        {
            return "startEvent";
        }
        if ("end".equals(t) || "endevent".equals(t) || "结束".equals(t) || "结束节点".equals(t))
        {
            return "endEvent";
        }
        if ("usertask".equals(t) || "task".equals(t) || "审批".equals(t) || "审批节点".equals(t)
                || "user".equals(t))
        {
            return "userTask";
        }
        if ("exclusivegateway".equals(t) || "gateway".equals(t) || "网关".equals(t)
                || "条件网关".equals(t) || "排他网关".equals(t))
        {
            return "exclusiveGateway";
        }
        return null;
    }

    private String sanitizeId(String raw)
    {
        if (StringUtils.isEmpty(raw))
        {
            return "";
        }
        String id = raw.trim().replaceAll("[^A-Za-z0-9_\\-]", "");
        if (StringUtils.isEmpty(id))
        {
            return "";
        }
        if (!id.matches("^[A-Za-z_].*"))
        {
            id = "n_" + id;
        }
        return id.length() > 40 ? id.substring(0, 40) : id;
    }

    private String uniqueNodeId(String id, String type, int index, Set<String> used)
    {
        String prefix = "startEvent".equals(type) ? "StartEvent" : "endEvent".equals(type)
                ? "EndEvent" : "exclusiveGateway".equals(type) ? "Gateway" : "Task";
        String candidate = StringUtils.isNotEmpty(id) ? id : prefix + "_" + index;
        String base = candidate;
        int suffix = 2;
        while (used.contains(candidate))
        {
            candidate = base + "_" + suffix;
            suffix++;
        }
        used.add(candidate);
        return candidate;
    }

    private String resolveNodeId(String ref, Set<String> ids, Map<String, String> idByName)
    {
        if (StringUtils.isEmpty(ref))
        {
            return null;
        }
        String r = ref.trim();
        if (ids.contains(r))
        {
            return r;
        }
        return idByName.get(r);
    }

    /** 补开始/结束节点，并修正断链 */
    private void ensureStartEnd(JSONArray nodes, List<String> warnings)
    {
        int startCount = 0;
        int endCount = 0;
        for (int i = 0; i < nodes.size(); i++)
        {
            String t = nodes.getJSONObject(i).getString("type");
            if ("startEvent".equals(t))
            {
                startCount++;
            }
            else if ("endEvent".equals(t))
            {
                endCount++;
            }
        }
        if (startCount == 0)
        {
            JSONObject start = new JSONObject();
            start.put("id", "StartEvent_1");
            start.put("type", "startEvent");
            start.put("name", "开始");
            ensureNodeIdUnique(start, nodes);
            nodes.add(0, start);
            warnings.add("AI未输出开始节点，已自动补充");
        }
        if (endCount == 0)
        {
            JSONObject end = new JSONObject();
            end.put("id", "EndEvent_1");
            end.put("type", "endEvent");
            end.put("name", "结束");
            ensureNodeIdUnique(end, nodes);
            nodes.add(end);
            warnings.add("AI未输出结束节点，已自动补充");
        }
    }

    private void ensureNodeIdUnique(JSONObject node, JSONArray nodes)
    {
        Set<String> ids = new HashSet<String>();
        for (int i = 0; i < nodes.size(); i++)
        {
            ids.add(nodes.getJSONObject(i).getString("id"));
        }
        String id = node.getString("id");
        String base = id;
        int suffix = 2;
        while (ids.contains(id))
        {
            id = base + "_" + suffix;
            suffix++;
        }
        node.put("id", id);
    }

    /** 断链修复：保证除开始外都有入线、除结束外都有出线 */
    private void repairFlows(JSONArray nodes, JSONArray flows, List<String> warnings)
    {
        Set<String> ids = new LinkedHashSet<String>();
        Set<String> hasIn = new HashSet<String>();
        Set<String> hasOut = new HashSet<String>();
        for (int i = 0; i < nodes.size(); i++)
        {
            ids.add(nodes.getJSONObject(i).getString("id"));
        }
        for (int i = 0; i < flows.size(); i++)
        {
            JSONObject f = flows.getJSONObject(i);
            hasOut.add(f.getString("from"));
            hasIn.add(f.getString("to"));
        }
        if (flows.isEmpty())
        {
            for (int i = 0; i < nodes.size() - 1; i++)
            {
                JSONObject f = new JSONObject();
                f.put("from", nodes.getJSONObject(i).getString("id"));
                f.put("to", nodes.getJSONObject(i + 1).getString("id"));
                flows.add(f);
            }
            warnings.add("AI未输出流程连线，已按节点顺序自动串联");
            return;
        }
        List<JSONObject> list = new ArrayList<JSONObject>();
        for (int i = 0; i < nodes.size(); i++)
        {
            list.add(nodes.getJSONObject(i));
        }
        for (int i = 1; i < list.size(); i++)
        {
            JSONObject node = list.get(i);
            String id = node.getString("id");
            if ("startEvent".equals(node.getString("type")) || hasIn.contains(id))
            {
                continue;
            }
            JSONObject prev = list.get(i - 1);
            String prevId = prev.getString("id");
            if (!"endEvent".equals(prev.getString("type")))
            {
                JSONObject f = new JSONObject();
                f.put("from", prevId);
                f.put("to", id);
                flows.add(f);
                hasOut.add(prevId);
                hasIn.add(id);
                warnings.add("已自动补齐断开的连线：" + prev.getString("name") + " → " + node.getString("name"));
            }
        }
        for (int i = 0; i < list.size() - 1; i++)
        {
            JSONObject node = list.get(i);
            String id = node.getString("id");
            if ("endEvent".equals(node.getString("type")) || hasOut.contains(id))
            {
                continue;
            }
            JSONObject next = list.get(i + 1);
            String nextId = next.getString("id");
            if (!"startEvent".equals(next.getString("type")))
            {
                JSONObject f = new JSONObject();
                f.put("from", id);
                f.put("to", nextId);
                flows.add(f);
                hasOut.add(id);
                hasIn.add(nextId);
                warnings.add("已自动补齐断开的连线：" + node.getString("name") + " → " + next.getString("name"));
            }
        }
    }

    // ==================== BPMN构建 ====================

    /** 由结构化结果拼装可部署的BPMN XML（含图形坐标） */
    private String buildBpmn(JSONObject result)
    {
        String key = result.getString("processKey");
        String name = escapeXml(result.getString("processName"));
        JSONArray nodes = result.getJSONArray("nodes");
        JSONArray flows = result.getJSONArray("flows");

        Map<String, JSONObject> nodeMap = new LinkedHashMap<String, JSONObject>();
        for (int i = 0; i < nodes.size(); i++)
        {
            JSONObject n = nodes.getJSONObject(i);
            nodeMap.put(n.getString("id"), n);
        }
        // 连线先分配ID，便于网关默认流引用
        List<JSONObject> flowList = new ArrayList<JSONObject>();
        Map<String, String> defaultFlow = new HashMap<String, String>();
        for (int i = 0; i < flows.size(); i++)
        {
            JSONObject f = flows.getJSONObject(i);
            String from = f.getString("from");
            String to = f.getString("to");
            if (!nodeMap.containsKey(from) || !nodeMap.containsKey(to))
            {
                continue;
            }
            JSONObject item = new JSONObject();
            item.put("id", "Flow_" + (flowList.size() + 1));
            item.put("from", from);
            item.put("to", to);
            item.put("condition", f.getString("condition"));
            flowList.add(item);
            JSONObject gateway = nodeMap.get(from);
            if (gateway != null && "exclusiveGateway".equals(gateway.getString("type"))
                    && StringUtils.isEmpty(f.getString("condition")) && !defaultFlow.containsKey(from))
            {
                defaultFlow.put(from, item.getString("id"));
            }
        }

        StringBuilder xml = new StringBuilder(8192);
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" ")
                .append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ")
                .append("xmlns:flowable=\"http://flowable.org/bpmn\" ")
                .append("xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" ")
                .append("xmlns:omgdc=\"http://www.omg.org/spec/DD/20100524/DC\" ")
                .append("xmlns:omgdi=\"http://www.omg.org/spec/DD/20100524/DI\" ")
                .append("targetNamespace=\"http://flowable.org/bpmn\">\n");
        xml.append("  <process id=\"").append(escapeXml(key)).append("\" name=\"").append(name)
                .append("\" isExecutable=\"true\">\n");

        // 节点
        for (JSONObject n : nodeMap.values())
        {
            String type = n.getString("type");
            String id = escapeXml(n.getString("id"));
            String nName = escapeXml(n.getString("name"));
            if ("startEvent".equals(type))
            {
                xml.append("    <startEvent id=\"").append(id).append("\" name=\"").append(nName).append("\"/>\n");
            }
            else if ("endEvent".equals(type))
            {
                xml.append("    <endEvent id=\"").append(id).append("\" name=\"").append(nName).append("\"/>\n");
            }
            else if ("exclusiveGateway".equals(type))
            {
                String def = defaultFlow.get(n.getString("id"));
                xml.append("    <exclusiveGateway id=\"").append(id).append("\" name=\"").append(nName).append("\"");
                if (StringUtils.isNotEmpty(def))
                {
                    xml.append(" default=\"").append(escapeXml(def)).append("\"");
                }
                xml.append("/>\n");
            }
            else
            {
                xml.append("    <userTask id=\"").append(id).append("\" name=\"").append(nName).append("\"");
                JSONArray parts = n.getJSONArray("participants");
                if (parts != null && !parts.isEmpty())
                {
                    JSONObject cfg = new JSONObject();
                    cfg.put("handleMode", "candidate");
                    cfg.put("performMode", "claim");
                    cfg.put("participants", parts);
                    cfg.put("fallbackUserIds", new JSONArray());
                    xml.append(" flowable:participantConfig=\"")
                            .append(escapeXml(cfg.toJSONString())).append("\"");
                }
                xml.append("/>\n");
            }
        }
        // 连线
        for (JSONObject f : flowList)
        {
            String condition = f.getString("condition");
            if (StringUtils.isEmpty(condition))
            {
                xml.append("    <sequenceFlow id=\"").append(f.getString("id"))
                        .append("\" sourceRef=\"").append(escapeXml(f.getString("from")))
                        .append("\" targetRef=\"").append(escapeXml(f.getString("to"))).append("\"/>\n");
            }
            else
            {
                String expr = condition.trim();
                if (!expr.startsWith("${") && !expr.startsWith("#{"))
                {
                    expr = "${" + expr + "}";
                }
                expr = expr.replace("]]>", "]] >");
                xml.append("    <sequenceFlow id=\"").append(f.getString("id"))
                        .append("\" sourceRef=\"").append(escapeXml(f.getString("from")))
                        .append("\" targetRef=\"").append(escapeXml(f.getString("to"))).append("\">\n");
                xml.append("      <conditionExpression xsi:type=\"tFormalExpression\"><![CDATA[")
                        .append(expr).append("]]></conditionExpression>\n");
                xml.append("    </sequenceFlow>\n");
            }
        }
        xml.append("  </process>\n");

        // 图形段：按顺序横向排布，保证设计器可打开
        Map<String, int[]> geometry = new HashMap<String, int[]>();
        int x = 160;
        for (JSONObject n : nodeMap.values())
        {
            String type = n.getString("type");
            int w = "userTask".equals(type) ? 120 : 80;
            int h = "exclusiveGateway".equals(type) ? 60 : 80;
            int y = "exclusiveGateway".equals(type) ? 160 : 150;
            geometry.put(n.getString("id"), new int[] { x, y, w, h });
            x += w + 90;
        }
        xml.append("  <bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">\n");
        xml.append("    <bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"").append(escapeXml(key)).append("\">\n");
        for (JSONObject n : nodeMap.values())
        {
            int[] g = geometry.get(n.getString("id"));
            xml.append("      <bpmndi:BPMNShape id=\"").append(escapeXml(n.getString("id"))).append("_di\" bpmnElement=\"")
                    .append(escapeXml(n.getString("id"))).append("\">\n");
            xml.append("        <omgdc:Bounds x=\"").append(g[0]).append("\" y=\"").append(g[1])
                    .append("\" width=\"").append(g[2]).append("\" height=\"").append(g[3]).append("\"/>\n");
            xml.append("      </bpmndi:BPMNShape>\n");
        }
        for (JSONObject f : flowList)
        {
            int[] sg = geometry.get(f.getString("from"));
            int[] tg = geometry.get(f.getString("to"));
            if (sg == null || tg == null)
            {
                continue;
            }
            xml.append("      <bpmndi:BPMNEdge id=\"").append(f.getString("id")).append("_di\" bpmnElement=\"")
                    .append(f.getString("id")).append("\">\n");
            xml.append("        <omgdi:waypoint x=\"").append(sg[0] + sg[2]).append("\" y=\"")
                    .append(sg[1] + sg[3] / 2).append("\"/>\n");
            xml.append("        <omgdi:waypoint x=\"").append(tg[0]).append("\" y=\"")
                    .append(tg[1] + tg[3] / 2).append("\"/>\n");
            xml.append("      </bpmndi:BPMNEdge>\n");
        }
        xml.append("    </bpmndi:BPMNPlane>\n");
        xml.append("  </bpmndi:BPMNDiagram>\n");
        xml.append("</definitions>");
        return xml.toString();
    }

    // ==================== 文件与字符串工具 ====================

    private File saveUploadFile(MultipartFile file, String originalName) throws Exception
    {
        File dir = new File(RuoYiConfig.getProfile(), "ai/flow-plan");
        if (!dir.exists() && !dir.mkdirs())
        {
            throw new ServiceException("创建方案文件目录失败");
        }
        String safeName = originalName.replaceAll("[\\\\/:*?\"<>|]", "_");
        File dest = new File(dir, System.currentTimeMillis() + "_" + safeName);
        file.transferTo(dest.getAbsoluteFile());
        return dest;
    }

    private String extractText(File file, String ext) throws Exception
    {
        if (IMAGE_EXT.contains(ext))
        {
            return ocrImage(file);
        }
        if ("pdf".equals(ext))
        {
            try (FileInputStream in = new FileInputStream(file); PDDocument pdf = PDDocument.load(in))
            {
                return new PDFTextStripper().getText(pdf);
            }
        }
        if ("docx".equals(ext))
        {
            try (FileInputStream in = new FileInputStream(file);
                    XWPFDocument docx = new XWPFDocument(in);
                    XWPFWordExtractor extractor = new XWPFWordExtractor(docx))
            {
                return extractor.getText();
            }
        }
        byte[] bytes = Files.readAllBytes(file.toPath());
        String text = new String(bytes, StandardCharsets.UTF_8);
        if (text.indexOf('\uFFFD') >= 0)
        {
            text = new String(bytes, Charset.forName("GBK"));
        }
        return text;
    }

    private String extensionOf(String fileName)
    {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase();
    }

    /**
     * 图片方案识别：把截图转成文字，后续仍走同一套结构化解析流程。
     * 这样 reanalyze/历史任务无需保留图片，只依赖已落库的 sourceText。
     */
    private String ocrImage(File file) throws Exception
    {
        if (file.length() > 20L * 1024 * 1024)
        {
            throw new ServiceException("图片文件过大（超过20MB），请压缩到10MB以内后重新上传");
        }
        String dataUrl = buildImageDataUrl(file);
        AiResult ai = aiClient.chatVision(AI_MODULE, AI_OCR_FEATURE, OCR_PROMPT, dataUrl, 4000, 120);
        if (!ai.isSuccess() || StringUtils.isEmpty(ai.getContent()))
        {
            throw new ServiceException("图片方案识别失败：" + firstNonEmpty(ai.getError(), "AI未返回识别结果"));
        }
        return ai.getContent();
    }

    /** 构造内联图片数据URL；体积过大或格式兼容性差时转JPEG压缩 */
    private String buildImageDataUrl(File file) throws Exception
    {
        byte[] raw = Files.readAllBytes(file.toPath());
        String ext = extensionOf(file.getName());
        String mime = imageMime(ext);
        boolean compressible = "bmp".equals(ext) || "webp".equals(ext);
        if (raw.length <= MAX_IMAGE_BYTES && !compressible)
        {
            return "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(raw);
        }
        BufferedImage image;
        try (ByteArrayInputStream in = new ByteArrayInputStream(raw))
        {
            image = ImageIO.read(in);
        }
        if (image == null)
        {
            // JDK无法解码的格式（如webp）按原格式直传，交给大模型识别
            return "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(raw);
        }
        int maxEdge = Math.max(image.getWidth(), image.getHeight());
        if (maxEdge > MAX_IMAGE_EDGE)
        {
            double scale = (double) MAX_IMAGE_EDGE / maxEdge;
            int width = Math.max(1, (int) Math.round(image.getWidth() * scale));
            int height = Math.max(1, (int) Math.round(image.getHeight() * scale));
            BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = scaled.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
            g.drawImage(image, 0, 0, width, height, null);
            g.dispose();
            image = scaled;
        }
        return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(writeJpeg(image));
    }

    private byte[] writeJpeg(BufferedImage image) throws Exception
    {
        BufferedImage rgb = image;
        if (image.getType() != BufferedImage.TYPE_INT_RGB)
        {
            rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = rgb.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
            g.drawImage(image, 0, 0, null);
            g.dispose();
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext())
        {
            ImageIO.write(rgb, "png", out);
            return out.toByteArray();
        }
        ImageWriter writer = writers.next();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(out))
        {
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.85f);
            writer.write(null, new IIOImage(rgb, null, null), param);
        }
        finally
        {
            writer.dispose();
        }
        return out.toByteArray();
    }

    private String imageMime(String ext)
    {
        if ("jpg".equals(ext) || "jpeg".equals(ext))
        {
            return "image/jpeg";
        }
        if ("bmp".equals(ext))
        {
            return "image/bmp";
        }
        if ("webp".equals(ext))
        {
            return "image/webp";
        }
        return "image/png";
    }

    private String stripExtension(String fileName)
    {
        int dot = fileName.lastIndexOf('.');
        return dot <= 0 ? fileName : fileName.substring(0, dot);
    }

    private JSONObject tryParseJson(String content)
    {
        if (StringUtils.isEmpty(content))
        {
            return null;
        }
        String text = content.trim();
        if (text.startsWith("```"))
        {
            int firstLine = text.indexOf('\n');
            if (firstLine > 0)
            {
                text = text.substring(firstLine + 1);
            }
            int fence = text.lastIndexOf("```");
            if (fence >= 0)
            {
                text = text.substring(0, fence);
            }
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start)
        {
            return null;
        }
        try
        {
            return JSON.parseObject(text.substring(start, end + 1));
        }
        catch (Exception e)
        {
            log.warn("AI输出JSON解析失败：{}", e.getMessage());
            return null;
        }
    }

    private String sanitizeKey(String raw)
    {
        String key = raw == null ? "" : raw.trim().toLowerCase();
        key = key.replaceAll("[^a-z0-9_]", "_").replaceAll("_+", "_").replaceAll("^_+|_+$", "");
        if (StringUtils.isEmpty(key) || !key.matches("^[a-z].*"))
        {
            key = "flow_ai_" + new SimpleDateFormat("MMddHHmmss").format(new Date());
        }
        if (key.length() > 50)
        {
            key = key.substring(0, 50);
        }
        return key;
    }

    /** 流程Key唯一化，避免覆盖已存在的模型 */
    private String uniqueModelKey(String baseKey)
    {
        String key = baseKey;
        int suffix = 2;
        while (repositoryService.createModelQuery().modelKey(key).count() > 0)
        {
            String tail = "_" + suffix;
            key = (baseKey.length() + tail.length() > 50
                    ? baseKey.substring(0, 50 - tail.length()) : baseKey) + tail;
            suffix++;
            if (suffix > 99)
            {
                key = baseKey.substring(0, Math.min(40, baseKey.length())) + "_"
                        + new SimpleDateFormat("MMddHHmmss").format(new Date());
                break;
            }
        }
        return key;
    }

    private JSONArray toJSONArray(List<String> values)
    {
        JSONArray array = new JSONArray();
        for (String v : values)
        {
            array.add(v);
        }
        return array;
    }

    private String escapeXml(String value)
    {
        if (value == null)
        {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }

    private String firstNonEmpty(String... values)
    {
        if (values == null)
        {
            return null;
        }
        for (String v : values)
        {
            if (v != null && v.trim().length() > 0)
            {
                return v.trim();
            }
        }
        return null;
    }

    private String truncate(String value, int max)
    {
        if (value == null)
        {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}

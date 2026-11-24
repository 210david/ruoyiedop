package com.ruoyi.pjm.controller;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.ai.core.AiClient;
import com.ruoyi.ai.core.AiResult;
import com.ruoyi.ai.util.AiJsonParser;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.pjm.domain.PjmWbsTask;
import com.ruoyi.pjm.service.IPjmWbsTaskService;

/**
 * WBS 任务 Controller
 */
@RestController
@RequestMapping("/pjm/task")
public class PjmWbsTaskController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(PjmWbsTaskController.class);

    @Autowired
    private IPjmWbsTaskService wbsTaskService;

    @Autowired
    private AiClient aiClient;

    /** 平铺列表（分页） */
    @PreAuthorize("@ss.hasPermi('pjm:task:list')")
    @GetMapping("/list")
    public TableDataInfo list(PjmWbsTask task)
    {
        startPage();
        List<PjmWbsTask> list = wbsTaskService.selectTaskList(task);
        return getDataTable(list);
    }

    /** 树结构（按项目） */
    @PreAuthorize("@ss.hasPermi('pjm:task:list')")
    @GetMapping("/tree/{projectId}")
    public AjaxResult tree(@PathVariable Long projectId)
    {
        return AjaxResult.success(wbsTaskService.selectTaskTree(projectId));
    }

    @PreAuthorize("@ss.hasPermi('pjm:task:query')")
    @GetMapping(value = "/{taskId}")
    public AjaxResult getInfo(@PathVariable("taskId") Long taskId)
    {
        return AjaxResult.success(wbsTaskService.selectTaskById(taskId));
    }

    @PreAuthorize("@ss.hasPermi('pjm:task:add')")
    @Log(title = "WBS任务", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PjmWbsTask task)
    {
        return toAjax(wbsTaskService.insertTask(task));
    }

    @PreAuthorize("@ss.hasPermi('pjm:task:edit')")
    @Log(title = "WBS任务", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PjmWbsTask task)
    {
        return toAjax(wbsTaskService.updateTask(task));
    }

    @PreAuthorize("@ss.hasPermi('pjm:task:remove')")
    @Log(title = "WBS任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{taskIds}")
    public AjaxResult remove(@PathVariable Long[] taskIds)
    {
        return toAjax(wbsTaskService.deleteTaskByIds(taskIds));
    }

    // ==================== AI 解析投标文件生成 WBS ====================

    /**
     * AI 解析投标/合同附件 → 生成 WBS 任务分解草稿（不入库，前端确认后调 batchImport）
     *
     * @param file       上传的附件（支持 PDF/DOCX/XLSX/XLS/CSV/TXT，可选）
     * @param fileUrls   已上传到服务器的附件 URL 列表（逗号分隔，如 /profile/upload/2026/xx/xx.pdf）
     * @param manualText 手动粘贴的文本摘要（优先级最高）
     * @param source     boq=工程量清单（表格优先、逐项精确） / doc=投标文件全文（章节归纳），默认 doc
     * @param fileExt    上传文件的扩展名（multipart 文件名含中文时前端可显式传入，避免编码问题）
     */
    @PreAuthorize("@ss.hasPermi('pjm:task:add')")
    @Log(title = "AI解析WBS任务", businessType = BusinessType.OTHER)
    @PostMapping("/ai/parse")
    public AjaxResult aiParse(@RequestParam(value = "file", required = false) MultipartFile file,
                              @RequestParam(value = "fileUrls", required = false) String fileUrls,
                              @RequestParam(value = "manualText", required = false) String manualText,
                              @RequestParam(value = "source", required = false) String source,
                              @RequestParam(value = "fileExt", required = false) String fileExt)
    {
        try
        {
            boolean boqMode = "boq".equalsIgnoreCase(source);
            String text = "";
            if (StringUtils.isNotEmpty(manualText))
            {
                text = manualText;
            }
            else if (file != null && !file.isEmpty())
            {
                text = extractTextFromFile(file, fileExt);
            }
            else if (StringUtils.isNotEmpty(fileUrls))
            {
                text = extractTextFromUrls(fileUrls);
            }
            else
            {
                return AjaxResult.error("请上传附件、选择已上传附件或输入文本摘要");
            }

            if (text.trim().length() < 20)
            {
                return AjaxResult.error("附件文本内容过少，无法提取有效信息");
            }

            if (boqMode)
            {
                // 清单模式：用户已单独上传清单，不再做章节定向截断（清单本身就是全部有用内容）
                if (text.length() > 18000)
                {
                    text = text.substring(0, 18000);
                }
            }
            else
            {
                // 截断保护：大文档不做盲目截断，优先保留「工程量清单/工作范围」等关键章节
                text = focusRelevantText(text);
            }

            String prompt = boqMode ? buildBoqPrompt(text) : buildWbsPrompt(text);
            AiResult ai = aiClient.chatCached("pjm", boqMode ? "wbs_parse_boq" : "wbs_parse", prompt, 4000);

            if (!ai.isSuccess())
            {
                return AjaxResult.error("AI解析失败：" + (ai.getError() != null ? ai.getError() : "模型无响应"));
            }

            JSONObject json = AiJsonParser.parse(ai.getContent());
            JSONArray tasks = json.getJSONArray("tasks");
            if (tasks == null || tasks.isEmpty())
            {
                return AjaxResult.error(boqMode
                        ? "AI 未从该清单文件中识别出清单项，请确认文件是否包含「序号/项目名称/单位/工程量」等清单列"
                        : "AI 未从附件中识别出可分解的任务条目，请改用「工程量清单」模式直接上传清单文件");
            }

            AjaxResult result = AjaxResult.success(json);
            result.put("fileTextLen", text.length());
            result.put("taskCount", tasks.size());
            result.put("sourceMode", boqMode ? "boq" : "doc");
            return result;
        }
        catch (ServiceException e)
        {
            return AjaxResult.error(e.getMessage());
        }
        catch (Exception e)
        {
            log.error("AI解析WBS附件异常", e);
            return AjaxResult.error("AI解析服务异常：" + e.getMessage());
        }
    }

    /**
     * 批量导入 AI 草稿为 WBS 任务（前端确认后调用；按前端返回的层级结构写入）
     * 请求体：{ projectId, tasks: [{ taskName, taskType, parentKey, description, planStartDate, planEndDate, ... }] }
     * parentKey 为草稿内的临时引用，导入时映射为真实 taskId
     */
    @PreAuthorize("@ss.hasPermi('pjm:task:add')")
    @Log(title = "WBS任务批量导入", businessType = BusinessType.INSERT)
    @PostMapping("/ai/batchImport")
    public AjaxResult batchImport(@RequestBody JSONObject body)
    {
        Long projectId = body.getLong("projectId");
        JSONArray tasks = body.getJSONArray("tasks");
        if (projectId == null || tasks == null || tasks.isEmpty())
        {
            return AjaxResult.error("参数不完整：projectId 和 tasks 不能为空");
        }
        try
        {
            int count = importTasks(projectId, tasks);
            return AjaxResult.success("成功导入 " + count + " 条 WBS 任务", count);
        }
        catch (Exception e)
        {
            log.error("WBS任务批量导入异常", e);
            return AjaxResult.error("批量导入失败：" + e.getMessage());
        }
    }

    /** 按 parentKey 顺序导入：父任务先入库，子任务引用真实 taskId */
    private int importTasks(Long projectId, JSONArray tasks) throws Exception
    {
        // key → 已入库 taskId（父任务先入库，子任务再引用）
        java.util.Map<String, Long> keyMap = new java.util.HashMap<>();
        int count = 0;
        for (int i = 0; i < tasks.size(); i++)
        {
            JSONObject t = tasks.getJSONObject(i);
            String taskName = t.getString("taskName");
            if (StringUtils.isEmpty(taskName))
            {
                continue;
            }
            PjmWbsTask task = new PjmWbsTask();
            task.setProjectId(projectId);
            task.setTaskName(taskName.trim());
            String taskType = t.getString("taskType");
            // 字典 pjm_wbs_task_type：缺省按「工作项」(6)；兼容历史值 1/2 原样保留
            task.setTaskType(StringUtils.isEmpty(taskType) ? "6" : taskType);
            task.setDescription(t.getString("description"));
            task.setPlanStartDate(parseDate(t.getString("planStartDate")));
            task.setPlanEndDate(parseDate(t.getString("planEndDate")));
            // 清单字段（清单模式下 AI 会返回；非清单模式为 null，不影响原有行为）
            task.setQuantity(parseDecimal(t.getString("quantity")));
            task.setUnit(t.getString("unit"));
            task.setUnitPrice(parseDecimal(t.getString("unitPrice")));
            task.setAmount(parseDecimal(t.getString("amount")));

            String parentKey = t.getString("parentKey");
            if (StringUtils.isNotEmpty(parentKey) && keyMap.containsKey(parentKey))
            {
                task.setParentTaskId(keyMap.get(parentKey));
            }

            wbsTaskService.insertTask(task);
            // 草稿自身的 key（nodeId 或数组下标）→ 真实 taskId，供子任务引用
            String selfKey = t.getString("nodeId");
            if (StringUtils.isEmpty(selfKey))
            {
                selfKey = String.valueOf(i);
            }
            keyMap.put(selfKey, task.getTaskId());
            keyMap.put(String.valueOf(i), task.getTaskId());
            count++;
        }
        return count;
    }

    /**
     * 大文档定向截断：全文超长时不盲目取前 N 字，而是定位 WBS 相关章节。
     * 策略：一级关键词（工程量清单/分部分项）优先保留——清单是 WBS 叶子任务的主要来源；
     * 剩余预算再给二级关键词（承包范围/协议书概况等）。
     */
    private String focusRelevantText(String text)
    {
        final int LIMIT = 12000;
        if (text.length() <= LIMIT)
        {
            return text;
        }
        String[] lines = text.split("\n");
        // 一级关键词：工程量清单类（最高优先级）；二级关键词：范围/概况类
        final String[] TIER1_KEYWORDS = { "工程量清单", "分部分项", "分项工程清单" };
        final String[] TIER2_KEYWORDS = { "承包范围", "工作范围", "施工内容", "协议书", "工程概况" };
        // 清单作为附件位于文档末尾，目录页也会出现同名标题——只取最后一次命中，避免吃掉预算
        List<int[]> tier1 = lastKeywordRange(lines, TIER1_KEYWORDS, 300);
        List<int[]> tier2 = findKeywordRanges(lines, TIER2_KEYWORDS);

        StringBuilder sb = new StringBuilder();
        int budget = LIMIT;
        budget = appendRanges(sb, lines, tier1, budget);
        if (budget > 0)
        {
            appendRanges(sb, lines, tier2, budget);
        }
        return sb.length() > 0 ? sb.toString() : text.substring(0, LIMIT);
    }

    /** 取关键词在全文中最后一次命中的位置，向后保留 window 行（合并重叠后返回） */
    private List<int[]> lastKeywordRange(String[] lines, String[] keywords, int window)
    {
        int lastHit = -1;
        for (int i = 0; i < lines.length; i++)
        {
            for (String kw : keywords)
            {
                if (lines[i].contains(kw))
                {
                    lastHit = i;
                    break;
                }
            }
        }
        List<int[]> merged = new ArrayList<>();
        if (lastHit >= 0)
        {
            merged.add(new int[] { lastHit, Math.min(lines.length, lastHit + window) });
        }
        return merged;
    }

    /** 扫描行数组，返回命中关键词的保留区间 [start,end)，并合并重叠 */
    private List<int[]> findKeywordRanges(String[] lines, String[] keywords)
    {
        List<int[]> ranges = new ArrayList<>();
        for (int i = 0; i < lines.length; i++)
        {
            for (String kw : keywords)
            {
                if (lines[i].contains(kw))
                {
                    // 从命中行起保留一段（章节标题+表格内容通常连续数百行）
                    int end = Math.min(lines.length, i + 400);
                    ranges.add(new int[] { i, end });
                    break;
                }
            }
        }
        List<int[]> merged = new ArrayList<>();
        for (int[] r : ranges)
        {
            if (!merged.isEmpty() && r[0] <= merged.get(merged.size() - 1)[1])
            {
                int[] last = merged.get(merged.size() - 1);
                last[1] = Math.max(last[1], r[1]);
            }
            else
            {
                merged.add(r);
            }
        }
        return merged;
    }

    /** 按预算拼接区间内容，返回剩余预算 */
    private int appendRanges(StringBuilder sb, String[] lines, List<int[]> ranges, int budget)
    {
        for (int[] r : ranges)
        {
            if (budget <= 0)
            {
                break;
            }
            StringBuilder chunk = new StringBuilder();
            for (int i = r[0]; i < r[1] && chunk.length() < budget; i++)
            {
                chunk.append(lines[i]).append('\n');
            }
            String piece = chunk.toString();
            if (piece.length() > budget)
            {
                piece = piece.substring(0, budget);
            }
            sb.append(piece);
            budget -= piece.length();
        }
        return budget;
    }

    /** 解析数字字符串（支持千分位逗号与中文全角空格），非法返回 null */
    private java.math.BigDecimal parseDecimal(String s)
    {
        if (StringUtils.isEmpty(s))
        {
            return null;
        }
        try
        {
            String v = s.trim().replace(",", "").replace("，", "").replace(" ", "");
            if (v.isEmpty())
            {
                return null;
            }
            return new java.math.BigDecimal(v);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /** 解析 YYYY-MM-DD 日期字符串，格式不合法返回 null */
    private Date parseDate(String s)    {
        if (StringUtils.isEmpty(s))
        {
            return null;
        }
        try
        {
            return new java.text.SimpleDateFormat("yyyy-MM-dd").parse(s.trim());
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /** 构造「工程量清单」专用提示词：逐行精确提取，表格已结构化，重点防列错位 */
    private String buildBoqPrompt(String text)
    {
        return "你是一名资深建筑工程项目管理专员。以下是从【工程量清单】文件中提取的内容（可能来自 Excel 或 PDF 表格，"
             + "行结构为「序号 | 项目编码 | 项目名称 | 项目特征 | 计量单位 | 工程量 | 综合单价 | 合价」）。\n\n"
             + "=== 清单内容 ===\n" + text + "\n\n"
             + "=== 提取规则 ===\n"
             + "1. 每一个清单行（有序号/项目编码的）生成一个叶子任务，逐条提取，不得合并、不得遗漏。\n"
             + "2. taskName 使用清单的「项目名称」原文（可加上项目特征的关键限定，如「挡墙C25混凝土浇筑」）。\n"
             + "3. 严禁编造清单中不存在的任务；清单有几行就输出几条叶子任务。\n"
             + "4. description 写入该行的工程量、单位、综合单价、合价，格式：工程量XXX.XXX<单位>，综合单价XX.XX元/<单位>，合价XXXX.XX元。数字必须与清单原值一致。\n"
             + "5. 严格区分单价与合价：用「综合单价 × 工程量 ≈ 合价」自检；若不符，检查是否把合价当成了单价（合价/工程量可反推单价）。\n"
             + "6. 按清单的「分部/章节」或工程性质归组为父任务（如：拆除工程、挡墙工程、道路工程、附属工程）；"
             + "若清单本身有分部标题行（如「A.1 土石方工程」），直接用它作父任务名称。\n"
             + "7. 若清单含措施项目、规费税金等非实体项，归入「措施及其他项目」父任务。\n\n"
             + "=== 请返回严格的 JSON（不要 markdown 代码块） ===\n"
             + "{\n"
             + "  \"summary\": \"一句话概括，如：清单共28行，识别28个清单项并归入4个分部\",\n"
             + "  \"tasks\": [\n"
             + "    {\n"
             + "      \"nodeId\": \"n1\",\n"
             + "      \"parentKey\": null 或父任务 nodeId,\n"
             + "      \"taskName\": \"清单项目名称\",\n"
             + "      \"taskType\": \"字典 pjm_wbs_task_type：3=分部工程（本项目为阶段/分部父任务）或 6=工作项（具体清单项）；清单叶子一律 6\",\n"
             + "      \"description\": \"工程量/单价/合价原文数据\",\n"
             + "      \"quantity\": \"工程量数值字符串，如 476.70\",\n"
             + "      \"unit\": \"计量单位，如 m²/m³/m/t/项\",\n"
             + "      \"unitPrice\": \"综合单价数值字符串，如 266.06\",\n"
             + "      \"amount\": \"合价数值字符串，如 126830.80\",\n"
             + "      \"planStartDate\": null,\n"
             + "      \"planEndDate\": null\n"
             + "    }\n"
             + "  ]\n"
             + "}\n"
             + "tasks 必须父任务在前（先父后子），最多120条。quantity/unit/unitPrice/amount 提取不到填 null，不要猜。";
    }

    /** 构造 WBS 解析提示词（要求严格 JSON；只允许从原文提取，禁止编造） */
    private String buildWbsPrompt(String text)
    {
        return "你是一名资深建筑工程项目管理专员。以下是从投标文件/分包合同附件中提取的文本，请从中识别可分解的工程任务条目，"
             + "生成 WBS 任务分解草稿（只生成草稿，不执行）。\n\n"
             + "=== 附件内容 ===\n" + text + "\n\n"
             + "=== 提取规则 ===\n"
             + "1. 优先从「工程量清单/分部分项清单/施工内容/工作范围」等章节提取条目，每条清单项对应一个叶子任务。\n"
             + "2. 可按工程性质将叶子任务归组为阶段级父任务（如：拆除工程、安装工程、设备安装调试、收尾验收），父任务名称必须概括且不超过12字。\n"
             + "2a. 清单项按原清单顺序归组；同组任务在原文中相邻的优先同组；未识别归属的单项可作顶级任务。\n"
             + "3. 严格依据原文，禁止编造清单中不存在的任务；任务名称保留原文中的工程项名称。\n"
             + "4. 数量、单位、单价等清单数据放入 description 字段（格式如：工程量476.700m²，合同单价266.06元/m²）。\n"
             + "4a. 严格区分「单价」与「合价」：PDF转文本后表格列会错位，请用勾稽关系校验——若你提取的单价×数量≈某行合价，说明单价正确；若提取值×数量远大于合价或与合价相等，说明你把合价当成了单价，须重新定位单价列。单价数量级异常（如单价远超同类项）时以合价÷数量反推校正。\n"
             + "5. 如原文有明确工期条款，可给出计划起止日期（YYYY-MM-DD）；无法确定填 null。\n\n"
             + "=== 请返回严格的 JSON 格式（不要加 markdown 代码块标记），字段如下 ===\n"
             + "{\n"
             + "  \"summary\": \"一句话概括解析结果，如：共识别14个分项，归入3个阶段\",\n"
             + "  \"tasks\": [\n"
             + "    {\n"
             + "      \"nodeId\": \"n1\",\n"
             + "      \"parentKey\": null 或父任务 nodeId,\n"
             + "      \"taskName\": \"任务名称\",\n"
             + "      \"taskType\": \"按字典 pjm_wbs_task_type 取值：1单项工程 2单位工程 3分部工程 4分项工程 5检验批 6工作项 7里程碑 9其他；"
             + "清单叶子任务默认 6(工作项)，阶段/分部父任务默认 3(分部工程)，无工程量的验收节点用 7(里程碑)\",\n"
             + "      \"description\": \"工程量/单位/单价等原文数据摘要\",\n"
             + "      \"planStartDate\": \"YYYY-MM-DD 或 null\",\n"
             + "      \"planEndDate\": \"YYYY-MM-DD 或 null\"\n"
             + "    }\n"
             + "  ]\n"
             + "}\n"
             + "tasks 数组必须父任务在前（先父后子），最多50条。";
    }

    // ==================== 文本提取（与 PjmProjectController 同模式） ====================

    /** 从服务器已上传附件 URL 列表读取本地文件并提取文本（仅允许 /profile 前缀，防目录穿越） */
    private String extractTextFromUrls(String fileUrls) throws Exception
    {
        StringBuilder sb = new StringBuilder();
        for (String u : fileUrls.split(","))
        {
            String url = u.trim();
            if (url.isEmpty())
            {
                continue;
            }
            if (!url.startsWith("/profile/") || url.contains(".."))
            {
                log.warn("跳过非法附件URL：{}", url);
                continue;
            }
            String localPath = com.ruoyi.common.config.RuoYiConfig.getProfile() + url.substring("/profile".length());
            java.io.File f = new java.io.File(localPath);
            if (!f.exists() || !f.isFile())
            {
                log.warn("附件不存在：{}", localPath);
                continue;
            }
            String ext = "";
            int dot = f.getName().lastIndexOf('.');
            if (dot >= 0)
            {
                ext = f.getName().substring(dot + 1).toLowerCase();
            }

            // 落盘文件为加密格式（EDOPENC 魔数 + AES + GZIP），解析前须先解密还原原始内容
            byte[] raw = java.nio.file.Files.readAllBytes(f.toPath());
            byte[] content = raw;
            if (raw.length > 8 && "EDOPENC".equals(new String(raw, 0, 7, java.nio.charset.StandardCharsets.UTF_8)))
            {
                content = com.ruoyi.common.utils.file.FileStorageUtils.decryptAndDecompress(raw);
            }

            String part = "";
            try (InputStream in = new java.io.ByteArrayInputStream(content))
            {
                if ("pdf".equals(ext))
                {
                    String pdfText = extractPdfText(in);
                    if (pdfText == null)
                    {
                        log.warn("PDF 文本提取失败，跳过该附件：{}", f.getName());
                        continue;
                    }
                    part = pdfText;
                }
                else if ("docx".equals(ext))
                {
                    try (XWPFDocument docx = new XWPFDocument(in);
                         XWPFWordExtractor extractor = new XWPFWordExtractor(docx))
                    {
                        part = extractor.getText();
                    }
                }
                else if ("xlsx".equals(ext) || "xls".equals(ext))
                {
                    part = extractExcelText(in, "xls".equals(ext));
                }
                else if ("csv".equals(ext))
                {
                    part = extractCsvText(new String(content, "UTF-8"));
                }
                else if (ext.isEmpty() || "txt".equals(ext) || "md".equals(ext)
                        || "json".equals(ext) || "xml".equals(ext))
                {
                    part = new String(content, "UTF-8");
                }
                else
                {
                    log.warn("附件类型[{}]不支持文本提取，跳过：{}", ext, f.getName());
                    continue;
                }
            }
            if (part != null && !part.trim().isEmpty())
            {
                sb.append("\n<<<附件 [").append(f.getName()).append("]>>>\n");
                sb.append(part.trim()).append("\n");
            }
        }
        if (sb.length() == 0)
        {
            throw new ServiceException("附件解析失败：文件可能已损坏或为扫描件/图片型 PDF，无法提取文本。"
                    + "请用 WPS/Office 将其另存为标准 PDF 后重新上传，或改用「粘贴文本摘要」方式解析");
        }
        return sb.toString();
    }

    /** 提取 PDF 文本；损坏/加密/非标准时返回 null */
    private String extractPdfText(InputStream in)
    {
        try (PDDocument pdf = PDDocument.load(in))
        {
            return new PDFTextStripper().getText(pdf);
        }
        catch (Exception e)
        {
            log.warn("PDF 解析失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 从上传文件提取文本（PDF / DOCX / XLSX / XLS / TXT / CSV）
     *
     * @param hintExt 前端显式传入的扩展名（multipart 文件名在部分浏览器/编码环境下会丢失中文，
     *                或文件名无扩展名时作为兜底；如 "xlsx"）
     */
    private String extractTextFromFile(MultipartFile file, String hintExt) throws Exception
    {
        String orig = file.getOriginalFilename();
        String ext = "";
        if (orig != null && orig.contains("."))
        {
            ext = orig.substring(orig.lastIndexOf('.') + 1).toLowerCase();
        }
        if (StringUtils.isEmpty(ext) && StringUtils.isNotEmpty(hintExt))
        {
            ext = hintExt.trim().toLowerCase();
            if (ext.startsWith("."))
            {
                ext = ext.substring(1);
            }
        }
        try (InputStream in = file.getInputStream())
        {
            if ("pdf".equals(ext))
            {
                String pdfText = extractPdfText(in);
                if (pdfText == null)
                {
                    throw new ServiceException("PDF 文件解析失败：文件可能已损坏或为扫描件/图片型 PDF，无法提取文本。"
                            + "请用 WPS/Office 将其另存为标准 PDF 后重新上传，或直接上传 Excel 版工程量清单");
                }
                return pdfText;
            }
            if ("docx".equals(ext))
            {
                try (XWPFDocument docx = new XWPFDocument(in);
                     XWPFWordExtractor extractor = new XWPFWordExtractor(docx))
                {
                    return extractor.getText();
                }
            }
            // Excel 清单：逐表逐行转成「列名=值」的紧凑文本，便于模型按列精确取值
            if ("xlsx".equals(ext) || "xls".equals(ext))
            {
                return extractExcelText(in, "xls".equals(ext));
            }
            if ("csv".equals(ext))
            {
                return extractCsvText(new String(file.getBytes(), "UTF-8"));
            }
            if (ext.isEmpty() || "txt".equals(ext) || "md".equals(ext)
                    || "json".equals(ext) || "xml".equals(ext))
            {
                return new String(file.getBytes(), "UTF-8");
            }
            throw new ServiceException("不支持自动解析该文件类型（." + ext + "），请上传 Excel/PDF 版工程量清单或粘贴文本摘要");
        }
    }

    /** Excel → 结构化文本：每个 sheet 输出「第N行: 列A=值 | 列B=值 ...」，保留空白列位置避免错位 */
    private String extractExcelText(InputStream in, boolean legacyXls) throws Exception
    {
        StringBuilder sb = new StringBuilder();
        org.apache.poi.ss.usermodel.Workbook wb = legacyXls
                ? new org.apache.poi.hssf.usermodel.HSSFWorkbook(in)
                : new org.apache.poi.xssf.usermodel.XSSFWorkbook(in);
        try
        {
            org.apache.poi.ss.usermodel.DataFormatter fmt = new org.apache.poi.ss.usermodel.DataFormatter();
            for (int si = 0; si < wb.getNumberOfSheets(); si++)
            {
                org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheetAt(si);
                sb.append("\n=== 工作表：").append(sheet.getSheetName()).append(" ===\n");
                for (org.apache.poi.ss.usermodel.Row row : sheet)
                {
                    StringBuilder line = new StringBuilder();
                    int lastCol = row.getLastCellNum();
                    for (int c = 0; c < lastCol; c++)
                    {
                        org.apache.poi.ss.usermodel.Cell cell = row.getCell(c);
                        String v = cell == null ? "" : fmt.formatCellValue(cell).trim();
                        if (line.length() > 0)
                        {
                            line.append(" | ");
                        }
                        line.append(excelColName(c)).append('=').append(v);
                    }
                    if (line.length() > 0 && !line.toString().replace("|", "").replace("=", "").replace(" ", "").isEmpty())
                    {
                        sb.append("第").append(row.getRowNum() + 1).append("行: ").append(line).append('\n');
                    }
                    // 单个清单表通常不超过数百行，超出预算直接停
                    if (sb.length() > 30000)
                    {
                        sb.append("...(超出预算，已截断)\n");
                        return sb.toString();
                    }
                }
            }
        }
        finally
        {
            wb.close();
        }
        return sb.toString();
    }

    /** 列序号转 Excel 列名（0→A, 25→Z, 26→AA） */
    private String excelColName(int idx)
    {
        StringBuilder name = new StringBuilder();
        int n = idx;
        do
        {
            name.insert(0, (char) ('A' + (n % 26)));
            n = n / 26 - 1;
        }
        while (n >= 0);
        return name.toString();
    }

    /** CSV → 同上格式，行内逗号/引号已按标准规则切分 */
    private String extractCsvText(String raw)
    {
        StringBuilder sb = new StringBuilder();
        String[] lines = raw.split("\r?\n");
        for (int i = 0; i < lines.length; i++)
        {
            if (lines[i].trim().isEmpty())
            {
                continue;
            }
            String[] cells = lines[i].split(",", -1);
            StringBuilder line = new StringBuilder();
            for (int c = 0; c < cells.length; c++)
            {
                if (line.length() > 0)
                {
                    line.append(" | ");
                }
                line.append(excelColName(c)).append('=').append(cells[c].replace("\"", "").trim());
            }
            sb.append("第").append(i + 1).append("行: ").append(line).append('\n');
            if (sb.length() > 30000)
            {
                sb.append("...(超出预算，已截断)\n");
                break;
            }
        }
        return sb.toString();
    }
}

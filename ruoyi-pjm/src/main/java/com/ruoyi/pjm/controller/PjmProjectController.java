package com.ruoyi.pjm.controller;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
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
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.pjm.domain.PjmProject;
import com.ruoyi.pjm.service.IPjmProjectService;

/**
 * 项目立项 Controller
 */
@RestController
@RequestMapping("/pjm/project")
public class PjmProjectController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(PjmProjectController.class);

    @Autowired
    private IPjmProjectService pjmProjectService;

    @Autowired
    private AiClient aiClient;

    @PreAuthorize("@ss.hasPermi('pjm:project:list')")
    @GetMapping("/list")
    public TableDataInfo list(PjmProject pjmProject)
    {
        startPage();
        List<PjmProject> list = pjmProjectService.selectProjectList(pjmProject);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('pjm:project:export')")
    @Log(title = "项目立项", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, PjmProject pjmProject)
    {
        List<PjmProject> list = pjmProjectService.selectProjectList(pjmProject);
        ExcelUtil<PjmProject> util = new ExcelUtil<PjmProject>(PjmProject.class);
        util.exportExcel(response, list, "项目立项数据");
    }

    @PreAuthorize("@ss.hasPermi('pjm:project:query')")
    @GetMapping(value = "/{projectId}")
    public AjaxResult getInfo(@PathVariable("projectId") Long projectId)
    {
        return AjaxResult.success(pjmProjectService.selectProjectById(projectId));
    }

    @PreAuthorize("@ss.hasPermi('pjm:project:add')")
    @Log(title = "项目立项", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PjmProject pjmProject)
    {
        return toAjax(pjmProjectService.insertProject(pjmProject));
    }

    @PreAuthorize("@ss.hasPermi('pjm:project:edit')")
    @Log(title = "项目立项", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PjmProject pjmProject)
    {
        return toAjax(pjmProjectService.updateProject(pjmProject));
    }

    @PreAuthorize("@ss.hasPermi('pjm:project:remove')")
    @Log(title = "项目立项", businessType = BusinessType.DELETE)
    @DeleteMapping("/{projectIds}")
    public AjaxResult remove(@PathVariable Long[] projectIds)
    {
        return toAjax(pjmProjectService.deleteProjectByIds(projectIds));
    }

    /** 审批项目 */
    @PreAuthorize("@ss.hasPermi('pjm:project:audit')")
    @Log(title = "项目立项", businessType = BusinessType.UPDATE)
    @PutMapping("/audit/{projectId}")
    public AjaxResult audit(@PathVariable Long projectId,
                            @RequestParam String status,
                            @RequestParam(required = false) String auditOpinion)
    {
        return toAjax(pjmProjectService.auditProject(projectId, status, auditOpinion));
    }

    /** 提交审批（草稿 / 驳回 → 待审批） */
    @PreAuthorize("@ss.hasPermi('pjm:project:edit')")
    @Log(title = "项目立项", businessType = BusinessType.UPDATE)
    @PutMapping("/submit/{projectId}")
    public AjaxResult submit(@PathVariable Long projectId)
    {
        return toAjax(pjmProjectService.auditProject(projectId, "1", "提交审批"));
    }

    /** 项目结项归档 */
    @PreAuthorize("@ss.hasPermi('pjm:project:close')")
    @Log(title = "项目结项", businessType = BusinessType.UPDATE)
    @PutMapping("/close/{projectId}")
    public AjaxResult close(@PathVariable Long projectId)
    {
        return toAjax(pjmProjectService.closeProject(projectId));
    }

    /** ★ 手动刷新采购汇总 */
    @PreAuthorize("@ss.hasPermi('pjm:project:edit')")
    @PutMapping("/refresh/{projectId}")
    public AjaxResult refresh(@PathVariable Long projectId)
    {
        pjmProjectService.refreshPurchaseSummary(projectId);
        return AjaxResult.success();
    }

    /** ★ 项目采购汇总（读接口） */
    @PreAuthorize("@ss.hasPermi('pjm:project:query')")
    @GetMapping("/summary/{projectId}")
    public AjaxResult summary(@PathVariable Long projectId)
    {
        return AjaxResult.success(pjmProjectService.selectPurchaseSummary(projectId));
    }

    /** ★ 从项目/任务发起采购 */
    @PreAuthorize("@ss.hasPermi('pjm:project:purchase')")
    @Log(title = "项目立项", businessType = BusinessType.INSERT)
    @PostMapping("/purchase")
    public AjaxResult buildPurchase(@RequestBody Map<String, Object> body)
    {
        Long projectId = Long.valueOf(body.get("projectId").toString());
        Long taskId = body.get("taskId") != null ? Long.valueOf(body.get("taskId").toString()) : null;
        String title = (String) body.get("title");
        String purpose = (String) body.get("purpose");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> details = (List<Map<String, Object>>) body.get("details");
        Long requestId = pjmProjectService.buildPurchaseRequest(projectId, taskId, title, purpose, details);
        return AjaxResult.success(requestId);
    }

    /** ★ 项目采购-库存检查：按物料批量返回各仓库可用库存 */
    @PreAuthorize("@ss.hasPermi('pjm:project:query')")
    @PostMapping("/purchase/stockCheck")
    public AjaxResult stockCheck(@RequestBody Map<String, Object> body)
    {
        @SuppressWarnings("unchecked")
        List<Object> raw = (List<Object>) body.get("materialIds");
        List<Long> materialIds = new java.util.ArrayList<>();
        if (raw != null)
        {
            for (Object o : raw)
            {
                if (o != null) materialIds.add(Long.valueOf(o.toString()));
            }
        }
        return AjaxResult.success(pjmProjectService.stockCheck(materialIds));
    }

    /** ★ 项目采购-执行：有库存走出库单，无库存走采购申请（正常采购入库再出库/直送项目点） */
    @PreAuthorize("@ss.hasPermi('pjm:project:purchase')")
    @Log(title = "项目采购执行", businessType = BusinessType.INSERT)
    @PostMapping("/purchase/execute")
    public AjaxResult executePurchase(@RequestBody Map<String, Object> body)
    {
        return AjaxResult.success(pjmProjectService.executeProjectPurchase(body));
    }

    // ==================== ★ AI 解析接口 ====================

    /**
     * AI 解析附件 → 工程概况 + 采购清单 + 项目团队 JSON
     *
     * @param file           上传的附件（支持 PDF/DOCX/TXT，其他类型请用 manualText）
     * @param attachmentType 附件类型（1招标文件 2投标文件 3项目图纸 4项目保险）
     * @param manualText     手动输入的文本摘要（优先级最高）
     * @param fileUrls       已上传到服务器的附件URL列表（逗号分隔，如 /profile/upload/xxx.pdf），按URL读本地文件提取文本
     */
    @PreAuthorize("@ss.hasPermi('pjm:project:add')")
    @Log(title = "AI解析项目附件", businessType = BusinessType.OTHER)
    @PostMapping("/ai/parse")
    public AjaxResult aiParse(@RequestParam(value = "file", required = false) MultipartFile file,
                              @RequestParam(value = "attachmentType", required = false) String attachmentType,
                              @RequestParam(value = "manualText", required = false) String manualText,
                              @RequestParam(value = "fileUrls", required = false) String fileUrls)
    {
        try
        {
            String text = "";
            if (StringUtils.isNotEmpty(manualText))
            {
                text = manualText;
            }
            else if (file != null && !file.isEmpty())
            {
                text = extractTextFromFile(file);
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

            // 截断保护（大文档取前 8000 字）
            if (text.length() > 8000)
            {
                text = text.substring(0, 8000);
            }

            String typeLabel = typeLabel(attachmentType);
            String prompt = buildPrompt(typeLabel, text);
            AiResult ai = aiClient.chatCached("pjm", "project_parse", prompt, 2000);

            if (!ai.isSuccess())
            {
                return AjaxResult.error("AI解析失败：" + (ai.getError() != null ? ai.getError() : "模型无响应"));
            }

            JSONObject json = AiJsonParser.parse(ai.getContent());
            if (json.isEmpty())
            {
                return AjaxResult.error("AI返回结果解析失败，请重试或手动录入");
            }

            AjaxResult result = AjaxResult.success(json);
            result.put("rawText", ai.getContent().length() > 500 ? ai.getContent().substring(0, 500) + "..." : ai.getContent());
            result.put("fileTextLen", text.length());
            return result;
        }
        catch (ServiceException e)
        {
            return AjaxResult.error(e.getMessage());
        }
        catch (Exception e)
        {
            log.error("AI解析附件异常", e);
            return AjaxResult.error("AI解析服务异常：" + e.getMessage());
        }
    }

    // ==================== AI 辅助方法 ====================

    private String typeLabel(String t)
    {
        if (t == null) return "项目附件";
        switch (t)
        {
            case "1": return "招标文件";
            case "2": return "投标文件";
            case "3": return "项目图纸";
            case "4": return "项目保险";
            default: return "项目附件";
        }
    }

    /**
     * 从服务器已上传附件 URL 列表读取本地文件并提取文本。
     * URL 形如 /profile/upload/2026/11/02/xxx.pdf → 本地路径 = RuoYiConfig.getProfile() + /upload/...
     */
    private String extractTextFromUrls(String fileUrls) throws Exception
    {
        StringBuilder sb = new StringBuilder();
        String[] urls = fileUrls.split(",");
        int idx = 1;
        boolean pdfParseFailed = false;
        for (String u : urls)
        {
            String url = u.trim();
            if (url.isEmpty()) continue;
            // 仅允许 /profile 前缀，防目录穿越
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
            if (dot >= 0) ext = f.getName().substring(dot + 1).toLowerCase();

            // 落盘文件为加密格式（EDOPENC 魔数 + AES + GZIP），AI 解析前须先解密还原原始内容
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
                        // 单个 PDF 损坏/非标准不中断整体解析，继续处理其余附件
                        pdfParseFailed = true;
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
                else if (ext.isEmpty() || "txt".equals(ext) || "md".equals(ext)
                        || "csv".equals(ext) || "json".equals(ext) || "xml".equals(ext))
                {
                    part = new String(java.nio.file.Files.readAllBytes(f.toPath()), "UTF-8");
                }
                else
                {
                    log.warn("附件类型[{}]不支持文本提取，跳过：{}", ext, f.getName());
                    continue;
                }
            }
            if (part != null && !part.trim().isEmpty())
            {
                sb.append("\n<<<附件").append(idx++).append(" [").append(f.getName()).append("]>>>\n");
                sb.append(part.trim()).append("\n");
            }
        }
        // 一个字都没提出来且存在解析失败的 PDF 时，给出可操作的中文提示而不是原始异常
        if (sb.length() == 0 && pdfParseFailed)
        {
            throw new ServiceException("PDF 文件解析失败：文件可能已损坏或为扫描件/图片型 PDF，无法提取文本。"
                    + "请用 WPS/Office 将其另存为标准 PDF 后重新上传，或切换到「粘贴文本摘要」方式解析");
        }
        return sb.toString();
    }

    /**
     * 提取 PDF 文本；文件损坏、加密或非标准（如 trailer 缺 Root 对象）时返回 null，由调用方决定降级策略。
     */
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

    /** 从附件提取文本（PDF / DOCX / TXT） */
    private String extractTextFromFile(MultipartFile file) throws Exception
    {
        String orig = file.getOriginalFilename();
        String ext = "";
        if (orig != null && orig.contains("."))
        {
            ext = orig.substring(orig.lastIndexOf('.') + 1).toLowerCase();
        }

        try (InputStream in = file.getInputStream())
        {
            if ("pdf".equals(ext))
            {
                String pdfText = extractPdfText(in);
                if (pdfText == null)
                {
                    throw new ServiceException("PDF 文件解析失败：文件可能已损坏或为扫描件/图片型 PDF，无法提取文本。"
                            + "请用 WPS/Office 将其另存为标准 PDF 后重新上传，或切换到「粘贴文本摘要」方式解析");
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
            // txt / md / csv / json 等
            if (ext.isEmpty() || "txt".equals(ext) || "md".equals(ext)
                    || "csv".equals(ext) || "json".equals(ext) || "xml".equals(ext))
            {
                return new String(file.getBytes(), "UTF-8");
            }
            // 图片、扫描 PDF 等——直接返回空，让用户手动填摘要
            throw new ServiceException("不支持自动解析该文件类型（." + ext + "），请粘贴文本摘要后再解析");
        }
    }

    /** 构造 AI 提示词（要求严格 JSON 格式） */
    private String buildPrompt(String typeLabel, String text)
    {
        return "你是一名资深建筑工程项目立项专员。以下是从【" + typeLabel + "】附件中提取的文本内容，请从中识别并提取以下信息，按指定 JSON 格式返回。\n\n"
             + "=== 附件内容 ===\n" + text + "\n\n"
             + "=== 请返回严格的 JSON 格式（不要加 markdown 代码块标记），字段如下 ===\n"
             + "{\n"
             + "  \"project\": {\n"
             + "    \"projectName\": \"项目名称（从附件中提取的全称，如未找到留空）\",\n"
             + "    \"ownerUnit\": \"建设单位/业主名称\",\n"
             + "    \"buildUnit\": \"施工单位/承建单位名称\",\n"
             + "    \"supervisorUnit\": \"监理单位名称\",\n"
             + "    \"designUnit\": \"设计单位名称\",\n"
             + "    \"customer\": \"客户/甲方（如与建设单位不同）\",\n"
             + "    \"projectAddress\": \"工程地点/建设地址\",\n"
             + "    \"buildScale\": \"建设规模（如总建筑面积/栋数/层数等概要）\",\n"
             + "    \"contractAmount\": 合同总额（数字，单位元；如：12500000.00，未找到填 null）,\n"
             + "    \"budgetAmount\": 预算金额（数字，单位元；未找到填 null）,\n"
             + "    \"contractDuration\": 合同工期（数字，单位日历天；未找到填 null）,\n"
             + "    \"startDate\": \"计划开工日期 YYYY-MM-DD 格式\",\n"
             + "    \"endDate\": \"计划竣工日期 YYYY-MM-DD 格式\",\n"
             + "    \"qualityStandard\": \"质量标准（如优良/合格/BIM 要求等）\",\n"
             + "    \"description\": \"项目描述/范围摘要（不超过 200 字）\",\n"
             + "    \"projectType\": \"项目类型代码，1=房屋建筑,2=市政工程,3=机电安装,4=装饰装修,5=园林绿化,6=基础设施,7=EPC总承包\"\n"
             + "  },\n"
             + "  \"procurement\": [\n"
             + "    {\n"
             + "      \"itemName\": \"物料/设备/服务名称\",\n"
             + "      \"specModel\": \"规格型号\",\n"
             + "      \"unit\": \"单位\",\n"
             + "      \"qty\": 数量（数字）,\n"
             + "      \"unitPrice\": 单价（数字，元）\n"
             + "    }\n"
             + "  ],\n"
             + "  \"team\": [\n"
             + "    {\n"
             + "      \"userName\": \"成员姓名（必填，从附件中识别的项目人员姓名）\",\n"
             + "      \"roleType\": \"角色代码：1=项目经理,2=技术负责人,3=施工员,4=安全员,5=质量员,6=资料员,7=监理；如无法判断填 3\",\n"
             + "      \"deptName\": \"所属部门/单位名称（如附件中提到）\",\n"
             + "      \"joinDate\": \"加入日期 YYYY-MM-DD（未提到填 null）\"\n"
             + "    }\n"
             + "  ]\n"
             + "}\n\n"
             + "注意：\n"
             + "1. 未找到的字段填空字符串或 null，不要编造信息。\n"
             + "2. 若附件为图纸或保险类文档无法提取以上信息，返回 project 为空对象 + procurement 为空数组 + team 为空数组即可。\n"
             + "3. procurement 数组如无明确采购清单可留空数组 []；若有多条，全部列出。\n"
             + "4. team 数组：重点识别附件中出现的项目经理、技术负责人、施工员、安全员、质量员、资料员、监理等项目关键岗位人员；如有人名但岗位不明，roleType 填 3；如无明确人员名单可留空数组 []。\n"
             + "5. 只返回 JSON，不要任何解释性文字。";
    }
}


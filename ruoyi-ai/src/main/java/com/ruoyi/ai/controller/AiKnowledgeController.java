package com.ruoyi.ai.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;
import com.ruoyi.ai.domain.AiKnowledgeDoc;
import com.ruoyi.ai.domain.AiQaHistory;
import com.ruoyi.ai.service.IAiKnowledgeService;
import com.ruoyi.ai.mapper.AiKnowledgeDocMapper;
import com.ruoyi.ai.mapper.AiQaHistoryMapper;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.config.RuoYiConfig;

/**
 * AI知识库（RAG） Controller
 * 文档上传/解析/列表/删除 + 知识库问答
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/ai/knowledge")
public class AiKnowledgeController extends BaseController
{
    @Autowired
    private IAiKnowledgeService knowledgeService;

    @Autowired
    private AiKnowledgeDocMapper docMapper;

    @Autowired
    private AiQaHistoryMapper historyMapper;

    /**
     * 查询知识库文档列表
     */
    @PreAuthorize("@ss.hasPermi('ai:knowledge:list')")
    @GetMapping("/list")
    public TableDataInfo list(AiKnowledgeDoc doc)
    {
        startPage();
        List<AiKnowledgeDoc> list = docMapper.selectDocList(doc);
        return getDataTable(list);
    }

    /**
     * 上传并解析文档
     */
    @PreAuthorize("@ss.hasPermi('ai:knowledge:add')")
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file, @RequestParam(defaultValue = "platform") String module)
    {
        return AjaxResult.success("上传解析成功", knowledgeService.uploadAndParse(file, module));
    }

    /**
     * 删除文档（连同切片）
     */
    @PreAuthorize("@ss.hasPermi('ai:knowledge:remove')")
    @DeleteMapping("/{docIds}")
    public AjaxResult remove(@PathVariable Long[] docIds)
    {
        for (Long docId : docIds)
        {
            docMapper.deleteChunksByDocId(docId);
        }
        return toAjax(docMapper.deleteDocByIds(docIds));
    }

    /**
     * 知识库问答（完成后自动保存历史记录，按用户隔离）
     */
    @PreAuthorize("@ss.hasPermi('ai:knowledge:qa')")
    @PostMapping("/qa")
    public AjaxResult qa(@RequestBody Map<String, String> body)
    {
        String question = body.get("question");
        if (question == null || question.trim().isEmpty())
        {
            return AjaxResult.error("请输入问题");
        }
        String module = body.get("module");
        Map<String, Object> result = knowledgeService.answer(module, question);
        // 异步落库失败不影响回答返回；保存历史按用户隔离，仅本人可查
        try
        {
            AiQaHistory h = new AiQaHistory();
            h.setUserId(getUserId());
            h.setUserName(getUsername());
            h.setModule(module == null ? "" : module);
            h.setQuestion(question.length() > 1000 ? question.substring(0, 1000) : question);
            h.setAnswer(String.valueOf(result.get("answer")));
            Object sources = result.get("sources");
            h.setSourcesJson(sources == null ? "[]" : com.alibaba.fastjson2.JSON.toJSONString(sources));
            Object mode = result.get("retrievalMode");
            h.setRetrievalMode(mode == null ? "" : mode.toString());
            historyMapper.insertHistory(h);
        }
        catch (Exception e)
        {
            logger.warn("保存问答历史失败（不影响回答）: {}", e.getMessage());
        }
        return AjaxResult.success(result);
    }

    /**
     * 查询本人问答历史（按用户隔离，每人只能看到自己的记录）
     */
    @PreAuthorize("@ss.hasPermi('ai:knowledge:qa')")
    @GetMapping("/history")
    public AjaxResult history(@RequestParam(defaultValue = "20") int pageSize,
                              @RequestParam(defaultValue = "1") int pageNum,
                              @RequestParam(required = false) String question)
    {
        AiQaHistory query = new AiQaHistory();
        query.setUserId(getUserId());
        query.setQuestion(question);
        query.setPageSize(Math.min(Math.max(pageSize, 1), 100));
        query.setOffset((Math.max(pageNum, 1) - 1) * query.getPageSize());
        List<AiQaHistory> list = historyMapper.selectHistoryByUser(query);
        return AjaxResult.success(list);
    }

    /**
     * 清空本人问答历史
     */
    @PreAuthorize("@ss.hasPermi('ai:knowledge:qa')")
    @DeleteMapping("/history")
    public AjaxResult clearHistory()
    {
        return toAjax(historyMapper.deleteHistoryByUser(getUserId()));
    }

    /**
     * 知识文档基本信息（来源点击查看用，不返回存储路径）
     */
    @PreAuthorize("@ss.hasPermi('ai:knowledge:query')")
    @GetMapping("/doc/{docId}")
    public AjaxResult docInfo(@PathVariable Long docId)
    {
        AiKnowledgeDoc doc = docMapper.selectDocById(docId);
        if (doc == null)
        {
            return AjaxResult.error("文档不存在或已被删除");
        }
        Map<String, Object> info = new java.util.HashMap<>();
        info.put("docId", doc.getDocId());
        info.put("docName", doc.getDocName());
        info.put("module", doc.getModule());
        info.put("fileType", doc.getFileType());
        info.put("status", doc.getStatus());
        info.put("chunkCount", doc.getChunkCount());
        info.put("createBy", doc.getCreateBy());
        info.put("createTime", doc.getCreateTime());
        return AjaxResult.success(info);
    }

    /**
     * 按文档名称查找（历史来源无docId时兜底）
     */
    @PreAuthorize("@ss.hasPermi('ai:knowledge:query')")
    @GetMapping("/docByName")
    public AjaxResult docByName(@RequestParam("name") String name)
    {
        AiKnowledgeDoc doc = docMapper.selectDocByName(name);
        if (doc == null)
        {
            return AjaxResult.error("未找到该文档，可能已被删除或重命名");
        }
        Map<String, Object> info = new java.util.HashMap<>();
        info.put("docId", doc.getDocId());
        info.put("docName", doc.getDocName());
        info.put("module", doc.getModule());
        info.put("fileType", doc.getFileType());
        info.put("status", doc.getStatus());
        info.put("chunkCount", doc.getChunkCount());
        info.put("createBy", doc.getCreateBy());
        info.put("createTime", doc.getCreateTime());
        return AjaxResult.success(info);
    }

    /**
     * 知识文档全部切片内容（原文查看用，按切片顺序）
     */
    @PreAuthorize("@ss.hasPermi('ai:knowledge:query')")
    @GetMapping("/chunks/{docId}")
    public AjaxResult docChunks(@PathVariable Long docId)
    {
        return AjaxResult.success(docMapper.selectChunksByDocId(docId));
    }

    /**
     * Office 文档在线预览（服务端转 PDF 后返回，供 iframe 内嵌渲染）
     *
     * 说明：前端 docx-preview 对复杂表格渲染保真度差，改由服务端 LibreOffice 转 PDF。
     * 首次预览转换并缓存（uploadPath/convert/{docId}.pdf），之后直接命中缓存。
     * 转换失败/不支持转换时返回 500，前端自动回退原有渲染方式。
     */
    @PreAuthorize("@ss.hasPermi('ai:knowledge:query')")
    @GetMapping("/preview/{docId}")
    public void previewDoc(@PathVariable Long docId, HttpServletResponse response)
    {
        try
        {
            AiKnowledgeDoc doc = docMapper.selectDocById(docId);
            if (doc == null)
            {
                writeJsonError(response, "文档不存在或已被删除");
                return;
            }
            if (!com.ruoyi.ai.core.OfficeConvertUtils.isConvertible(doc.getFileType()))
            {
                writeJsonError(response, "该格式不支持服务端转换预览");
                return;
            }
            java.nio.file.Path srcPath = java.nio.file.Paths.get(RuoYiConfig.getProfile(), doc.getFilePath());
            java.io.File srcFile = srcPath.toFile();
            if (!srcFile.exists())
            {
                writeJsonError(response, "原文件已丢失，请重新上传");
                return;
            }
            java.io.File pdf = com.ruoyi.ai.core.OfficeConvertUtils.getOrCreatePdf(docId, srcFile);
            if (pdf == null || !pdf.exists())
            {
                writeJsonError(response, "文档转换预览失败，请下载原文件查看");
                return;
            }
            response.setContentType("application/pdf");
            // inline 内嵌展示，非下载
            String encodedName = java.net.URLEncoder.encode(doc.getDocName() + ".pdf", "UTF-8").replace("+", "%20");
            response.setHeader("Content-Disposition", "inline; filename*=UTF-8''" + encodedName);
            response.setContentLengthLong(pdf.length());
            // 允许前端 iframe 内嵌
            response.setHeader("X-Frame-Options", "SAMEORIGIN");
            try (java.io.InputStream in = java.nio.file.Files.newInputStream(pdf.toPath());
                 java.io.OutputStream out = response.getOutputStream())
            {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0)
                {
                    out.write(buf, 0, n);
                }
            }
        }
        catch (Exception e)
        {
            logger.error("文档转换预览失败 docId={}: {}", docId, e.getMessage());
        }
    }

    /** 输出 JSON 错误（预览接口为流式响应，不能直接返回 AjaxResult） */
    private void writeJsonError(HttpServletResponse response, String msg)
    {
        try
        {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"msg\":\"" + msg + "\",\"code\":500}");
        }
        catch (Exception ignored)
        {
            // 响应已提交时忽略
        }
    }

    /**
     * 下载知识文档原文件
     */
    @PreAuthorize("@ss.hasPermi('ai:knowledge:query')")
    @GetMapping("/download/{docId}")
    public void downloadDoc(@PathVariable Long docId, HttpServletResponse response)
    {
        try
        {
            AiKnowledgeDoc doc = docMapper.selectDocById(docId);
            if (doc == null)
            {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"msg\":\"文档不存在或已被删除\",\"code\":500}");
                return;
            }
            java.nio.file.Path path = java.nio.file.Paths.get(RuoYiConfig.getProfile(), doc.getFilePath());
            java.io.File file = path.toFile();
            if (!file.exists())
            {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"msg\":\"原文件已丢失，请重新上传\",\"code\":500}");
                return;
            }
            response.setContentType("application/octet-stream");
            String encodedName = java.net.URLEncoder.encode(doc.getDocName() + "." + doc.getFileType(), "UTF-8").replace("+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedName);
            response.setContentLengthLong(file.length());
            try (java.io.InputStream in = java.nio.file.Files.newInputStream(path);
                 java.io.OutputStream out = response.getOutputStream())
            {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0)
                {
                    out.write(buf, 0, n);
                }
            }
        }
        catch (Exception e)
        {
            logger.error("知识文档下载失败: {}", e.getMessage());
        }
    }
}

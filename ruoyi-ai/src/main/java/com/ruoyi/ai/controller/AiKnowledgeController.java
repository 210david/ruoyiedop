package com.ruoyi.ai.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.ai.domain.AiKnowledgeDoc;
import com.ruoyi.ai.service.IAiKnowledgeService;
import com.ruoyi.ai.mapper.AiKnowledgeDocMapper;
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
     * 知识库问答
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
        return AjaxResult.success(knowledgeService.answer(module, question));
    }
}

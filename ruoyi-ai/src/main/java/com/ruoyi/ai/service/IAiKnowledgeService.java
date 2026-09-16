package com.ruoyi.ai.service;

import java.util.Map;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.ai.domain.AiKnowledgeDoc;

/**
 * AI知识库（RAG） Service接口
 *
 * @author ruoyi
 */
public interface IAiKnowledgeService
{
    /** 上传并解析文档（抽取文本→切片→向量化→入库），同步处理，返回docId */
    public Long uploadAndParse(MultipartFile file, String module);

    /** 知识库问答：检索Top-K切片 → LLM生成答案（附引用来源） */
    public Map<String, Object> answer(String module, String question);
}

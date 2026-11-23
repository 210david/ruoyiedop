package com.ruoyi.ai.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.ai.domain.AiKnowledgeDoc;

/**
 * AI知识库文档 Mapper接口
 *
 * @author ruoyi
 */
public interface AiKnowledgeDocMapper
{
    /** 新增文档 */
    public int insertDoc(AiKnowledgeDoc doc);

    /** 修改文档 */
    public int updateDoc(AiKnowledgeDoc doc);

    /** 删除文档（含物理切片级联删除） */
    public int deleteDocByIds(Long[] docIds);

    /** 根据ID查询 */
    public AiKnowledgeDoc selectDocById(Long docId);

    /** 查询列表 */
    public List<AiKnowledgeDoc> selectDocList(AiKnowledgeDoc doc);

    /** 更新解析状态与切片数 */
    public int updateParseResult(@Param("docId") Long docId, @Param("status") String status, @Param("chunkCount") int chunkCount);

    /** 新增切片（向量JSON） */
    public int insertChunk(@Param("docId") Long docId, @Param("chunkIndex") int chunkIndex,
            @Param("content") String content, @Param("embedding") String embedding);

    /** 删除文档全部切片 */
    public int deleteChunksByDocId(Long docId);

    /** 加载文档切片（未配置embedding时关键词检索用） */
    public List<java.util.Map<String, Object>> selectChunksForSearch(@Param("module") String module);

    /** 按ID加载切片（带embedding，向量检索用） */
    public List<java.util.Map<String, Object>> selectChunksWithEmbedding(@Param("module") String module);

    /** 按文档加载全部切片（原文查看用，按切片顺序） */
    public List<java.util.Map<String, Object>> selectChunksByDocId(@Param("docId") Long docId);

    /** 检索结果附带文档绑定的功能入口（命名入口直连用） */
    public List<java.util.Map<String, Object>> selectChunksWithNav(@Param("module") String module);

    /** 按文档名称精确查找（来源无docId时兜底） */
    public AiKnowledgeDoc selectDocByName(@Param("docName") String docName);

    /** 取文档绑定的功能入口菜单ID（未绑定返回 null） */
    public Long selectNavMenuIdByDocId(@Param("docId") Long docId);

    /** 设置文档关联的功能入口菜单（navMenuId 传 null 表示清空绑定） */
    public int updateNavMenuId(@Param("docId") Long docId, @Param("navMenuId") Long navMenuId);
}

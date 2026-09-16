package com.ruoyi.ai.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * AI知识库文档 ai_knowledge_doc
 * 主数据/共享资源类：数据权限豁免（知识库按模块挂载，全公司共享检索）
 *
 * @author ruoyi
 */
public class AiKnowledgeDoc extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 文档ID */
    private Long docId;

    /** 所属模块（dms/qms/safety/hr/platform等） */
    @Excel(name = "所属模块")
    private String module;

    /** 文档名称 */
    @Excel(name = "文档名称")
    private String docName;

    /** 文件类型（pdf/docx/md/txt） */
    @Excel(name = "文件类型")
    private String fileType;

    /** 存储路径 */
    private String filePath;

    /** 状态（0已解析 1解析中 2解析失败） */
    @Excel(name = "状态", readConverterExp = "0=已解析,1=解析中,2=解析失败")
    private String status;

    /** 切片数 */
    @Excel(name = "切片数")
    private Integer chunkCount;

    public Long getDocId() { return docId; }
    public void setDocId(Long docId) { this.docId = docId; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getDocName() { return docName; }
    public void setDocName(String docName) { this.docName = docName; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getChunkCount() { return chunkCount; }
    public void setChunkCount(Integer chunkCount) { this.chunkCount = chunkCount; }
}

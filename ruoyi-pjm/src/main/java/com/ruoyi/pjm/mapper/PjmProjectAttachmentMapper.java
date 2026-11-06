package com.ruoyi.pjm.mapper;

import java.util.List;
import com.ruoyi.pjm.domain.PjmProjectAttachment;

/**
 * 项目立项附件 Mapper
 */
public interface PjmProjectAttachmentMapper
{
    public PjmProjectAttachment selectPjmProjectAttachmentById(Long attachmentId);

    public List<PjmProjectAttachment> selectPjmProjectAttachmentList(PjmProjectAttachment attachment);

    /** 按项目ID查询 */
    public List<PjmProjectAttachment> selectByProjectId(Long projectId);

    /** 按项目ID和类型查询 */
    public List<PjmProjectAttachment> selectByProjectIdAndType(Long projectId, String attachmentType);

    public int insertPjmProjectAttachment(PjmProjectAttachment attachment);

    public int updatePjmProjectAttachment(PjmProjectAttachment attachment);

    public int deletePjmProjectAttachmentByIds(Long[] attachmentIds);

    public int deletePjmProjectAttachmentById(Long attachmentId);

    /** 按项目ID全部删除 */
    public int deleteByProjectId(Long projectId);
}

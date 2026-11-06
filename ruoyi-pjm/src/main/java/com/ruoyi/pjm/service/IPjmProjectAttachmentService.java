package com.ruoyi.pjm.service;

import java.util.List;
import com.ruoyi.pjm.domain.PjmProjectAttachment;

/**
 * 项目立项附件 Service 接口
 */
public interface IPjmProjectAttachmentService
{
    public PjmProjectAttachment selectPjmProjectAttachmentById(Long attachmentId);

    public List<PjmProjectAttachment> selectPjmProjectAttachmentList(PjmProjectAttachment attachment);

    public List<PjmProjectAttachment> selectByProjectId(Long projectId);

    public List<PjmProjectAttachment> selectByProjectIdAndType(Long projectId, String attachmentType);

    public int insertPjmProjectAttachment(PjmProjectAttachment attachment);

    public int updatePjmProjectAttachment(PjmProjectAttachment attachment);

    public int deletePjmProjectAttachmentByIds(Long[] attachmentIds);

    public int deletePjmProjectAttachmentById(Long attachmentId);

    public int deleteByProjectId(Long projectId);
}

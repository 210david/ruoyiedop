package com.ruoyi.pjm.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.pjm.domain.PjmProjectAttachment;
import com.ruoyi.pjm.mapper.PjmProjectAttachmentMapper;
import com.ruoyi.pjm.service.IPjmProjectAttachmentService;

/**
 * 项目立项附件 Service 实现
 */
@Service
public class PjmProjectAttachmentServiceImpl implements IPjmProjectAttachmentService
{
    @Autowired
    private PjmProjectAttachmentMapper attachmentMapper;

    @Override
    public PjmProjectAttachment selectPjmProjectAttachmentById(Long attachmentId)
    {
        return attachmentMapper.selectPjmProjectAttachmentById(attachmentId);
    }

    @Override
    public List<PjmProjectAttachment> selectPjmProjectAttachmentList(PjmProjectAttachment attachment)
    {
        return attachmentMapper.selectPjmProjectAttachmentList(attachment);
    }

    @Override
    public List<PjmProjectAttachment> selectByProjectId(Long projectId)
    {
        return attachmentMapper.selectByProjectId(projectId);
    }

    @Override
    public List<PjmProjectAttachment> selectByProjectIdAndType(Long projectId, String attachmentType)
    {
        return attachmentMapper.selectByProjectIdAndType(projectId, attachmentType);
    }

    @Override
    public int insertPjmProjectAttachment(PjmProjectAttachment attachment)
    {
        if (attachment.getDelFlag() == null) attachment.setDelFlag("0");
        return attachmentMapper.insertPjmProjectAttachment(attachment);
    }

    @Override
    public int updatePjmProjectAttachment(PjmProjectAttachment attachment)
    {
        return attachmentMapper.updatePjmProjectAttachment(attachment);
    }

    @Override
    public int deletePjmProjectAttachmentByIds(Long[] attachmentIds)
    {
        return attachmentMapper.deletePjmProjectAttachmentByIds(attachmentIds);
    }

    @Override
    public int deletePjmProjectAttachmentById(Long attachmentId)
    {
        return attachmentMapper.deletePjmProjectAttachmentById(attachmentId);
    }

    @Override
    public int deleteByProjectId(Long projectId)
    {
        return attachmentMapper.deleteByProjectId(projectId);
    }
}

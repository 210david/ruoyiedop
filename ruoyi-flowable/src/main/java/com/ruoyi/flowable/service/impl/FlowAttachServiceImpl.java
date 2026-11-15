package com.ruoyi.flowable.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.flowable.domain.FlowAttach;
import com.ruoyi.flowable.mapper.FlowAttachMapper;
import com.ruoyi.flowable.service.IFlowAttachService;

/**
 * 流程附件Service实现
 *
 * @author ruoyi
 */
@Service
public class FlowAttachServiceImpl implements IFlowAttachService
{
    @Autowired
    private FlowAttachMapper flowAttachMapper;

    @Override
    public List<FlowAttach> selectByInstance(String instanceId)
    {
        if (StringUtils.isEmpty(instanceId))
        {
            throw new ServiceException("流程实例标识不能为空");
        }
        return flowAttachMapper.selectByInstance(instanceId);
    }

    @Override
    public int addAttach(FlowAttach attach)
    {
        if (attach == null || StringUtils.isEmpty(attach.getInstanceId()))
        {
            throw new ServiceException("流程实例标识不能为空");
        }
        if (StringUtils.isEmpty(attach.getFileName()) || StringUtils.isEmpty(attach.getFileUrl()))
        {
            throw new ServiceException("附件文件名与地址不能为空");
        }
        Long userId = SecurityUtils.getUserId();
        attach.setUploaderId(userId);
        attach.setUploaderName(currentUserName());
        return flowAttachMapper.insertAttach(attach);
    }

    @Override
    public int deleteAttach(Long attachId)
    {
        if (attachId == null)
        {
            throw new ServiceException("附件ID不能为空");
        }
        Long userId = SecurityUtils.getUserId();
        // 管理员可删任意附件，其他人只能删自己上传的
        Long ownerLimit = SecurityUtils.isAdmin(userId) ? null : userId;
        int rows = flowAttachMapper.deleteAttach(attachId, ownerLimit);
        if (rows == 0)
        {
            throw new ServiceException("附件不存在或您无权删除他人上传的附件");
        }
        return rows;
    }

    private String currentUserName()
    {
        try
        {
            SysUser user = SecurityUtils.getLoginUser().getUser();
            if (user != null)
            {
                return StringUtils.isNotEmpty(user.getNickName()) ? user.getNickName() : user.getUserName();
            }
        }
        catch (Exception ignored)
        {
        }
        return SecurityUtils.getUsername();
    }
}

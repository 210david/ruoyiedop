package com.ruoyi.mms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.mms.domain.MmsResource;
import com.ruoyi.mms.mapper.MmsResourceMapper;
import com.ruoyi.mms.service.IMmsResourceService;

/**
 * 产能单元 Service实现
 *
 * @author ruoyi
 */
@Service
public class MmsResourceServiceImpl implements IMmsResourceService
{
    @Autowired
    private MmsResourceMapper resourceMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<MmsResource> selectResourceList(MmsResource resource)
    {
        return resourceMapper.selectResourceList(resource);
    }

    @Override
    public MmsResource selectResourceById(Long ResourceId)
    {
        return resourceMapper.selectResourceById(ResourceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertResource(MmsResource resource)
    {
        // 状态默认启用
        if (StringUtils.isEmpty(resource.getStatus()))
        {
            resource.setStatus("0");
        }
        // 必填校验
        if (StringUtils.isEmpty(resource.getResourceName()))
        {
            throw new ServiceException("产能单元名称不能为空");
        }
        if (StringUtils.isEmpty(resource.getResourceType()))
        {
            throw new ServiceException("请选择资源类型");
        }
        if (StringUtils.isEmpty(resource.getStatus()))
        {
            throw new ServiceException("请选择状态");
        }
        if (StringUtils.isEmpty(resource.getLineName()))
        {
            throw new ServiceException("请选择所属产线");
        }
        // 自动生成资源编码（通过编号规则 mms_resource 生成）
        if (StringUtils.isEmpty(resource.getResourceCode()))
        {
            resource.setResourceCode(mkNumberRuleService.generateNumber("mms_resource"));
        }
        resource.setDelFlag("0");
        resource.setCreateBy(SecurityUtils.getUsername());
        resource.setCreateTime(DateUtils.getNowDate());
        resource.setUpdateBy(SecurityUtils.getUsername());
        resource.setUpdateTime(DateUtils.getNowDate());
        return resourceMapper.insertResource(resource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateResource(MmsResource resource)
    {
        resource.setUpdateBy(SecurityUtils.getUsername());
        resource.setUpdateTime(DateUtils.getNowDate());
        return resourceMapper.updateResource(resource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteResourceByIds(Long[] ResourceIds)
    {
        return resourceMapper.deleteResourceByIds(ResourceIds);
    }
}

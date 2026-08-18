package com.ruoyi.mms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
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
        resource.setDelFlag("0");
        return resourceMapper.insertResource(resource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateResource(MmsResource resource)
    {
        return resourceMapper.updateResource(resource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteResourceByIds(Long[] ResourceIds)
    {
        return resourceMapper.deleteResourceByIds(ResourceIds);
    }
}

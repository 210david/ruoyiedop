package com.ruoyi.mms.service;

import java.util.List;
import com.ruoyi.mms.domain.MmsResource;

/**
 * 产能单元 Service接口
 *
 * @author ruoyi
 */
public interface IMmsResourceService
{
    public List<MmsResource> selectResourceList(MmsResource resource);
    public MmsResource selectResourceById(Long ResourceId);
    public int insertResource(MmsResource resource);
    public int updateResource(MmsResource resource);
    public int deleteResourceByIds(Long[] ResourceIds);
}

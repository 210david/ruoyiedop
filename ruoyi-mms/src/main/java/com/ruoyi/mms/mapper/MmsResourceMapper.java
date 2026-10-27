package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsResource;

public interface MmsResourceMapper {
    public List<MmsResource> selectResourceList(MmsResource resource);
    public MmsResource selectResourceById(Long resourceId);
    public int insertResource(MmsResource resource);
    public int updateResource(MmsResource resource);
    public int deleteResourceByIds(Long[] resourceIds);
}
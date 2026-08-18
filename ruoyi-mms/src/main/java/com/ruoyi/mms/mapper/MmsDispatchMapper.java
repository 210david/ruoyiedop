package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsDispatch;

public interface MmsDispatchMapper
{
    public List<MmsDispatch> selectDispatchList(MmsDispatch dispatch);
    public MmsDispatch selectDispatchById(Long dispatchId);
    public int insertDispatch(MmsDispatch dispatch);
    public int updateDispatch(MmsDispatch dispatch);
    public int deleteDispatchByIds(Long[] dispatchIds);
}

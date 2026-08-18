package com.ruoyi.mms.service;

import java.util.List;
import com.ruoyi.mms.domain.MmsDispatch;

/**
 * 派工管理 Service接口
 *
 * 派工状态机：
 * 0(待开工) → 1(进行中) → 2(已完成)
 *              ↓
 *          3(已取消)
 *
 * @author ruoyi
 */
public interface IMmsDispatchService
{
    /** 标准CRUD */
    public List<MmsDispatch> selectDispatchList(MmsDispatch dispatch);
    public MmsDispatch selectDispatchById(Long dispatchId);
    public int insertDispatch(MmsDispatch dispatch);
    public int updateDispatch(MmsDispatch dispatch);
    public int deleteDispatchByIds(Long[] dispatchIds);

    /** 派工开工：0(待开工) → 1(进行中) */
    public int startDispatch(Long dispatchId);

    /** 派工完工：1(进行中) → 2(已完成) */
    public int finishDispatch(Long dispatchId);

    /** 派工取消：0(待开工)/1(进行中) → 3(已取消) */
    public int cancelDispatch(Long dispatchId);
}

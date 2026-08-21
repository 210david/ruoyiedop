package com.ruoyi.mms.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.mms.domain.MmsDispatch;
import com.ruoyi.mms.mapper.MmsDispatchMapper;
import com.ruoyi.mms.service.IMmsDispatchService;

/**
 * 派工管理 Service实现
 *
 * 派工状态机：
 * 0(待开工) → 1(进行中) → 2(已完成)
 *              ↓
 *          3(已取消)
 *
 * @author ruoyi
 */
@Service
public class MmsDispatchServiceImpl implements IMmsDispatchService
{
    @Autowired
    private MmsDispatchMapper dispatchMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    // ========== 标准 CRUD ==========

    @Override
    public List<MmsDispatch> selectDispatchList(MmsDispatch dispatch)
    {
        return dispatchMapper.selectDispatchList(dispatch);
    }

    @Override
    public MmsDispatch selectDispatchById(Long dispatchId)
    {
        return dispatchMapper.selectDispatchById(dispatchId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertDispatch(MmsDispatch dispatch)
    {
        if (StringUtils.isEmpty(dispatch.getStatus()))
        {
            dispatch.setStatus("0");
        }
        dispatch.setDelFlag("0");
        if (StringUtils.isEmpty(dispatch.getDispatchNo()))
        {
            dispatch.setDispatchNo(mkNumberRuleService.generateNumber("DI"));
        }
        if (dispatch.getGoodQty() == null)
        {
            dispatch.setGoodQty(BigDecimal.ZERO);
        }
        if (dispatch.getDefectQty() == null)
        {
            dispatch.setDefectQty(BigDecimal.ZERO);
        }
        dispatch.setCreateBy(SecurityUtils.getUsername());
        dispatch.setCreateTime(DateUtils.getNowDate());
        return dispatchMapper.insertDispatch(dispatch);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDispatch(MmsDispatch dispatch)
    {
        MmsDispatch existing = dispatchMapper.selectDispatchById(dispatch.getDispatchId());
        if (existing != null && !"0".equals(existing.getStatus()))
        {
            throw new ServiceException("当前状态不允许修改派工单信息");
        }
        dispatch.setUpdateBy(SecurityUtils.getUsername());
        return dispatchMapper.updateDispatch(dispatch);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDispatchByIds(Long[] dispatchIds)
    {
        for (Long id : dispatchIds)
        {
            MmsDispatch d = dispatchMapper.selectDispatchById(id);
            if (d != null && !"0".equals(d.getStatus()) && !"3".equals(d.getStatus()))
            {
                throw new ServiceException("派工单[" + d.getDispatchNo() + "]非待开工/已取消状态，不允许删除");
            }
        }
        return dispatchMapper.deleteDispatchByIds(dispatchIds);
    }

    // ========== 业务操作 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int startDispatch(Long dispatchId)
    {
        MmsDispatch d = getAndCheckDispatch(dispatchId);
        if (!"0".equals(d.getStatus()))
        {
            throw new ServiceException("派工单[" + d.getDispatchNo() + "]当前状态为" + statusName(d.getStatus()) + "，只有待开工状态可开工");
        }
        d.setStatus("1");
        d.setActualStart(new Date());
        d.setUpdateBy(SecurityUtils.getUsername());
        return dispatchMapper.updateDispatch(d);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int finishDispatch(Long dispatchId)
    {
        MmsDispatch d = getAndCheckDispatch(dispatchId);
        if (!"1".equals(d.getStatus()))
        {
            throw new ServiceException("派工单[" + d.getDispatchNo() + "]当前状态为" + statusName(d.getStatus()) + "，只有进行中状态可完工");
        }
        d.setStatus("2");
        d.setActualEnd(new Date());
        d.setUpdateBy(SecurityUtils.getUsername());
        return dispatchMapper.updateDispatch(d);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelDispatch(Long dispatchId)
    {
        MmsDispatch d = getAndCheckDispatch(dispatchId);
        if ("2".equals(d.getStatus()) || "3".equals(d.getStatus()))
        {
            throw new ServiceException("派工单[" + d.getDispatchNo() + "]当前状态为" + statusName(d.getStatus()) + "，不允许取消");
        }
        d.setStatus("3");
        d.setUpdateBy(SecurityUtils.getUsername());
        return dispatchMapper.updateDispatch(d);
    }

    // ========== 私有辅助方法 ==========

    private MmsDispatch getAndCheckDispatch(Long dispatchId)
    {
        MmsDispatch d = dispatchMapper.selectDispatchById(dispatchId);
        if (d == null)
        {
            throw new ServiceException("派工单不存在或已删除");
        }
        return d;
    }

    private String statusName(String status)
    {
        switch (status)
        {
            case "0": return "待开工";
            case "1": return "进行中";
            case "2": return "已完成";
            case "3": return "已取消";
            default: return "未知(" + status + ")";
        }
    }
}

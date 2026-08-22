package com.ruoyi.mms.service;

import java.math.BigDecimal;
import java.util.Date;
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

    /**
     * 派工开工：0(待开工) → 1(进行中)
     * @param dispatchId 派工单ID
     * @param operatorName 操作人员（为空时默认取当前登录用户）
     * @param teamId 班组ID
     * @param teamName 班组名称
     */
    public int startDispatch(Long dispatchId, String operatorName, Long teamId, String teamName);

    /**
     * 派工完工：1(进行中) → 2(已完成)
     * 完工时需填写合格数量、不良数量、实际开始/结束时间，系统自动：
     * 1. 回写派工单 goodQty/defectQty/actualStart/actualEnd
     * 2. 生成报工记录（已审核状态）
     * 3. 联动更新工单完工数量和状态
     *
     * @param dispatchId 派工单ID
     * @param goodQty 合格数量
     * @param defectQty 不良数量
     * @param remark 完工备注（不良原因等）
     * @param actualStart 实际开始时间（为空时保留开工时记录的时间）
     * @param actualEnd 实际结束时间（为空时取当前系统时间）
     */
    public int finishDispatch(Long dispatchId, BigDecimal goodQty, BigDecimal defectQty, String remark, Date actualStart, Date actualEnd);

    /** 派工取消：0(待开工)/1(进行中) → 3(已取消) */
    public int cancelDispatch(Long dispatchId);
}

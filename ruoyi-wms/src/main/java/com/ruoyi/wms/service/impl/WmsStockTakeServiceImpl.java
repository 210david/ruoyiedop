package com.ruoyi.wms.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.WmsInventory;
import com.ruoyi.wms.domain.WmsStockTake;
import com.ruoyi.wms.domain.WmsStockTakeDetail;
import com.ruoyi.wms.domain.WmsStockTakeSnapshot;
import com.ruoyi.wms.domain.WmsStockTakeApproveLog;
import com.ruoyi.wms.mapper.WmsInventoryMapper;
import com.ruoyi.wms.mapper.WmsStockTakeMapper;
import com.ruoyi.wms.service.IWmsStockTakeService;
import com.ruoyi.wms.service.IWmsInventoryService;
import com.ruoyi.mk.service.IMkNumberRuleService;

@Service
public class WmsStockTakeServiceImpl implements IWmsStockTakeService
{
    @Autowired
    private WmsStockTakeMapper wmsStockTakeMapper;

    @Autowired
    private WmsInventoryMapper wmsInventoryMapper;

    @Autowired
    private IWmsInventoryService wmsInventoryService;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<WmsStockTake> selectStockTakeList(WmsStockTake take)
    {
        return wmsStockTakeMapper.selectStockTakeList(take);
    }

    @Override
    public WmsStockTake selectStockTakeById(Long takeId)
    {
        WmsStockTake take = wmsStockTakeMapper.selectStockTakeById(takeId);
        if (take != null)
        {
            take.setDetailList(wmsStockTakeMapper.selectStockTakeDetailByTakeId(takeId));
            take.setApproveLogList(wmsStockTakeMapper.selectApproveLogByTakeId(takeId));
        }
        return take;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertStockTake(WmsStockTake take)
    {
        if (StringUtils.isEmpty(take.getTakeNo()))
        {
            take.setTakeNo(mkNumberRuleService.generateNumber("wms_stocktake"));
        }
        take.setDelFlag("0");
        if (take.getStatus() == null)
        {
            take.setStatus("0");
        }
        int rows = wmsStockTakeMapper.insertStockTake(take);
        // generate detail from inventory snapshot
        WmsInventory queryInv = new WmsInventory();
        queryInv.setWarehouseId(take.getWarehouseId());
        queryInv.setAreaId(take.getAreaId());
        List<WmsInventory> invList = wmsInventoryMapper.selectInventoryList(queryInv);

        // 根据盘点类型进行差异化处理
        String takeType = take.getTakeType();
        if ("1".equals(takeType))
        {
            // 抽盘：按抽样比例随机选取
            int ratio = take.getSampleRatio() != null ? take.getSampleRatio() : 100;
            if (ratio < 100)
            {
                Collections.shuffle(invList);
                int sampleSize = Math.max(1, (int) Math.ceil(invList.size() * ratio / 100.0));
                invList = invList.subList(0, sampleSize);
            }
        }
        else if ("2".equals(takeType))
        {
            // 循环盘：按循环批次号分组，只盘当前批次
            int cycleNo = take.getCycleNo() != null ? take.getCycleNo() : 1;
            // 按 material_id 取模分组，cycleNo 为 1~N
            List<WmsInventory> cycleList = new ArrayList<>();
            // 默认分4个循环批次
            int totalCycles = 4;
            for (int i = 0; i < invList.size(); i++)
            {
                if (i % totalCycles == (cycleNo - 1) % totalCycles)
                {
                    cycleList.add(invList.get(i));
                }
            }
            invList = cycleList;
        }
        // 全盘（takeType=0）：使用全部库存，无需过滤

        for (WmsInventory inv : invList)
        {
            // snapshot
            WmsStockTakeSnapshot snapshot = new WmsStockTakeSnapshot();
            snapshot.setTakeId(take.getTakeId());
            snapshot.setMaterialId(inv.getMaterialId());
            snapshot.setLocationId(inv.getLocationId());
            snapshot.setBatchNo(inv.getBatchNo());
            snapshot.setSnapshotQty(inv.getQty());
            wmsStockTakeMapper.insertSnapshot(snapshot);

            // detail
            WmsStockTakeDetail detail = new WmsStockTakeDetail();
            detail.setTakeId(take.getTakeId());
            detail.setMaterialId(inv.getMaterialId());
            detail.setLocationId(inv.getLocationId());
            detail.setBatchNo(inv.getBatchNo());
            detail.setBookQty(inv.getQty());
            detail.setStatus("0");
            detail.setDelFlag("0");
            wmsStockTakeMapper.insertStockTakeDetail(detail);
        }
        return rows;
    }

    @Override
    public int updateStockTake(WmsStockTake take)
    {
        return wmsStockTakeMapper.updateStockTake(take);
    }

    @Override
    public int deleteStockTakeByIds(Long[] takeIds)
    {
        return wmsStockTakeMapper.deleteStockTakeByIds(takeIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int startTake(Long takeId)
    {
        WmsStockTake take = wmsStockTakeMapper.selectStockTakeById(takeId);
        if (take == null || !"0".equals(take.getStatus()))
        {
            throw new ServiceException("盘点单状态不正确");
        }
        take.setStatus("1");
        take.setStartTime(new Date());
        return wmsStockTakeMapper.updateStockTake(take);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitDetail(Long takeId, Long detailId, BigDecimal actualQty, String diffReason)
    {
        WmsStockTakeDetail detail = null;
        List<WmsStockTakeDetail> details = wmsStockTakeMapper.selectStockTakeDetailByTakeId(takeId);
        for (WmsStockTakeDetail d : details)
        {
            if (d.getDetailId().equals(detailId))
            {
                detail = d;
                break;
            }
        }
        if (detail == null)
        {
            throw new ServiceException("盘点明细不存在");
        }
        detail.setActualQty(actualQty);
        detail.setDiffQty(actualQty.subtract(detail.getBookQty()));
        detail.setDiffReason(diffReason);
        // 有差异时差异原因必填
        if (detail.getDiffQty().compareTo(BigDecimal.ZERO) != 0 && StringUtils.isEmpty(diffReason))
        {
            throw new ServiceException("存在差异（差异数量：" + detail.getDiffQty() + "），请填写差异原因");
        }
        detail.setStatus("1");
        wmsStockTakeMapper.updateStockTakeDetail(detail);
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSubmitDetail(Long takeId, List<WmsStockTakeDetail> detailList)
    {
        WmsStockTake take = wmsStockTakeMapper.selectStockTakeById(takeId);
        if (take == null || !"1".equals(take.getStatus()))
        {
            throw new ServiceException("盘点单状态不正确，无法录入");
        }
        // 获取全部明细用于匹配
        List<WmsStockTakeDetail> existingDetails = wmsStockTakeMapper.selectStockTakeDetailByTakeId(takeId);
        Map<Long, WmsStockTakeDetail> detailMap = new HashMap<>();
        for (WmsStockTakeDetail d : existingDetails)
        {
            detailMap.put(d.getDetailId(), d);
        }
        int count = 0;
        for (WmsStockTakeDetail input : detailList)
        {
            if (input.getDetailId() == null || input.getActualQty() == null)
            {
                continue;
            }
            WmsStockTakeDetail detail = detailMap.get(input.getDetailId());
            if (detail == null)
            {
                continue;
            }
            detail.setActualQty(input.getActualQty());
            detail.setDiffQty(input.getActualQty().subtract(detail.getBookQty()));
            detail.setDiffReason(input.getDiffReason());
            // 有差异时差异原因必填
            if (detail.getDiffQty().compareTo(BigDecimal.ZERO) != 0 && StringUtils.isEmpty(input.getDiffReason()))
            {
                throw new ServiceException("物料[" + detail.getMaterialCode() + "]存在差异（差异数量：" + detail.getDiffQty() + "），请填写差异原因");
            }
            detail.setStatus("1");
            wmsStockTakeMapper.updateStockTakeDetail(detail);
            count++;
        }
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importDetail(Long takeId, List<WmsStockTakeDetail> importList)
    {
        WmsStockTake take = wmsStockTakeMapper.selectStockTakeById(takeId);
        if (take == null || !"1".equals(take.getStatus()))
        {
            throw new ServiceException("盘点单状态不正确，无法录入");
        }
        if (importList == null || importList.isEmpty())
        {
            throw new ServiceException("导入数据不能为空");
        }
        // 获取全部明细，按物料编码+库位编码+批次号建立索引
        List<WmsStockTakeDetail> existingDetails = wmsStockTakeMapper.selectStockTakeDetailByTakeId(takeId);
        Map<String, WmsStockTakeDetail> detailMap = new HashMap<>();
        for (WmsStockTakeDetail d : existingDetails)
        {
            String key = buildMatchKey(d.getMaterialCode(), d.getLocationCode(), d.getBatchNo());
            detailMap.put(key, d);
        }
        int successCount = 0;
        int failCount = 0;
        StringBuilder failMsg = new StringBuilder();
        for (WmsStockTakeDetail importItem : importList)
        {
            String materialCode = importItem.getMaterialCode();
            if (StringUtils.isEmpty(materialCode) || importItem.getActualQty() == null)
            {
                failCount++;
                failMsg.append("<br/>物料编码或实盘数量为空，跳过");
                continue;
            }
            String key = buildMatchKey(materialCode, importItem.getLocationCode(), importItem.getBatchNo());
            WmsStockTakeDetail detail = detailMap.get(key);
            if (detail == null)
            {
                failCount++;
                failMsg.append("<br/>物料编码[").append(materialCode).append("]");
                if (StringUtils.isNotEmpty(importItem.getLocationCode()))
                {
                    failMsg.append("库位[").append(importItem.getLocationCode()).append("]");
                }
                if (StringUtils.isNotEmpty(importItem.getBatchNo()))
                {
                    failMsg.append("批次[").append(importItem.getBatchNo()).append("]");
                }
                failMsg.append("未匹配到盘点明细");
                continue;
            }
            detail.setActualQty(importItem.getActualQty());
            detail.setDiffQty(importItem.getActualQty().subtract(detail.getBookQty()));
            detail.setDiffReason(importItem.getDiffReason());
            // 有差异时差异原因必填
            if (detail.getDiffQty().compareTo(BigDecimal.ZERO) != 0 && StringUtils.isEmpty(importItem.getDiffReason()))
            {
                failCount++;
                failMsg.append("<br/>物料编码[").append(materialCode).append("]");
                if (StringUtils.isNotEmpty(importItem.getLocationCode()))
                {
                    failMsg.append("库位[").append(importItem.getLocationCode()).append("]");
                }
                if (StringUtils.isNotEmpty(importItem.getBatchNo()))
                {
                    failMsg.append("批次[").append(importItem.getBatchNo()).append("]");
                }
                failMsg.append("存在差异（差异数量：").append(detail.getDiffQty()).append("），未填写差异原因");
                continue;
            }
            detail.setStatus("1");
            wmsStockTakeMapper.updateStockTakeDetail(detail);
            successCount++;
        }
        StringBuilder result = new StringBuilder();
        result.append("导入完成：成功").append(successCount).append("条");
        if (failCount > 0)
        {
            result.append("，失败").append(failCount).append("条");
            result.append(failMsg.toString());
        }
        return result.toString();
    }

    private String buildMatchKey(String materialCode, String locationCode, String batchNo)
    {
        return materialCode + "|" + (locationCode == null ? "" : locationCode) + "|" + (batchNo == null ? "" : batchNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitForApproval(Long takeId)
    {
        WmsStockTake take = wmsStockTakeMapper.selectStockTakeById(takeId);
        if (take == null || !"1".equals(take.getStatus()))
        {
            throw new ServiceException("盘点单状态不正确，无法提交审批");
        }
        // 检查是否所有明细都已录入
        List<WmsStockTakeDetail> details = wmsStockTakeMapper.selectStockTakeDetailByTakeId(takeId);
        for (WmsStockTakeDetail d : details)
        {
            if (!"1".equals(d.getStatus()) && !"2".equals(d.getStatus()))
            {
                throw new ServiceException("存在未录入的盘点明细，请全部录入后再提交审批");
            }
            // 有差异的明细必须填写差异原因
            if (d.getDiffQty() != null && d.getDiffQty().compareTo(BigDecimal.ZERO) != 0 && StringUtils.isEmpty(d.getDiffReason()))
            {
                throw new ServiceException("物料[" + d.getMaterialCode() + "]存在差异（差异数量：" + d.getDiffQty() + "），请填写差异原因后再提交审批");
            }
        }
        take.setStatus("2");
        take.setEndTime(new Date());
        return wmsStockTakeMapper.updateStockTake(take);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int approveTake(Long takeId, String approveOpinion)
    {
        WmsStockTake take = wmsStockTakeMapper.selectStockTakeById(takeId);
        if (take == null || !"2".equals(take.getStatus()))
        {
            throw new ServiceException("盘点单状态不正确，无法审批");
        }
        String username = SecurityUtils.getUsername();
        List<WmsStockTakeDetail> details = wmsStockTakeMapper.selectStockTakeDetailByTakeId(takeId);
        for (WmsStockTakeDetail d : details)
        {
            if (d.getDiffQty() != null && d.getDiffQty().compareTo(BigDecimal.ZERO) != 0)
            {
                if (d.getDiffQty().compareTo(BigDecimal.ZERO) > 0)
                {
                    // 盘盈
                    wmsInventoryService.addInventory(d.getMaterialId(), take.getWarehouseId(),
                            d.getLocationId(), d.getBatchNo(), d.getDiffQty().abs(), "4", "stocktake",
                            take.getTakeNo(), username, null, null);
                }
                else
                {
                    // 盘亏
                    wmsInventoryService.reduceInventory(d.getMaterialId(), take.getWarehouseId(),
                            d.getLocationId(), d.getBatchNo(), d.getDiffQty().abs(), "5", "stocktake",
                            take.getTakeNo(), username);
                }
                d.setStatus("2");
                wmsStockTakeMapper.updateStockTakeDetail(d);
            }
        }
        take.setStatus("3");
        take.setApproveBy(username);
        take.setApproveTime(new Date());
        take.setApproveOpinion(approveOpinion);
        // 插入审批日志
        WmsStockTakeApproveLog log = new WmsStockTakeApproveLog();
        log.setTakeId(takeId);
        log.setApproveBy(username);
        log.setApproveTime(new Date());
        log.setApproveAction("pass");
        log.setApproveOpinion(approveOpinion);
        wmsStockTakeMapper.insertApproveLog(log);
        return wmsStockTakeMapper.updateStockTake(take);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int rejectTake(Long takeId, String approveOpinion)
    {
        WmsStockTake take = wmsStockTakeMapper.selectStockTakeById(takeId);
        if (take == null || !"2".equals(take.getStatus()))
        {
            throw new ServiceException("盘点单状态不正确，无法驳回");
        }
        // 驳回后退回到"盘点中"状态，允许盘点人员根据驳回意见修改后重新提交
        take.setStatus("1");
        take.setApproveBy(SecurityUtils.getUsername());
        take.setApproveTime(new Date());
        take.setApproveOpinion(approveOpinion);
        // 插入审批日志
        WmsStockTakeApproveLog log = new WmsStockTakeApproveLog();
        log.setTakeId(takeId);
        log.setApproveBy(SecurityUtils.getUsername());
        log.setApproveTime(new Date());
        log.setApproveAction("reject");
        log.setApproveOpinion(approveOpinion);
        wmsStockTakeMapper.insertApproveLog(log);
        // 将明细状态重置为未提交，让盘点人员重新核实
        List<WmsStockTakeDetail> details = wmsStockTakeMapper.selectStockTakeDetailByTakeId(takeId);
        for (WmsStockTakeDetail d : details)
        {
            if ("1".equals(d.getStatus()))
            {
                d.setStatus("0");
                wmsStockTakeMapper.updateStockTakeDetail(d);
            }
        }
        return wmsStockTakeMapper.updateStockTake(take);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int voidTake(Long takeId)
    {
        WmsStockTake take = wmsStockTakeMapper.selectStockTakeById(takeId);
        if (take == null)
        {
            throw new ServiceException("盘点单不存在");
        }
        // 只有待盘点和盘点中可以作废
        if (!"0".equals(take.getStatus()) && !"1".equals(take.getStatus()))
        {
            throw new ServiceException("当前状态不允许作废");
        }
        take.setStatus("4");
        take.setEndTime(new Date());
        return wmsStockTakeMapper.updateStockTake(take);
    }
}

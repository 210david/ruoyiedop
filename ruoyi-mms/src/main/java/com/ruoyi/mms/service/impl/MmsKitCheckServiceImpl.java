package com.ruoyi.mms.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.mms.domain.MmsKitCheck;
import com.ruoyi.mms.domain.MmsKitCheckDetail;
import com.ruoyi.mms.mapper.MmsKitCheckMapper;
import com.ruoyi.mms.service.IMmsKitCheckService;

/**
 * 齐套检查 Service实现
 *
 * 齐套检查逻辑：
 * 1. 根据工单关联的BOM展开物料需求
 * 2. 对比库存可用量计算缺口
 * 3. 计算齐套率 = 满足项数 / 总项数 * 100%
 * 4. 全部满足 → 1(已通过)，否则 → 2(缺料)
 *
 * @author ruoyi
 */
@Service
public class MmsKitCheckServiceImpl implements IMmsKitCheckService
{
    @Autowired
    private MmsKitCheckMapper kitCheckMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<MmsKitCheck> selectKitCheckList(MmsKitCheck kitCheck)
    {
        return kitCheckMapper.selectKitCheckList(kitCheck);
    }

    @Override
    public MmsKitCheck selectKitCheckById(Long kitId)
    {
        return kitCheckMapper.selectKitCheckById(kitId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertKitCheck(MmsKitCheck kitCheck)
    {
        // 自动生成齐套检查单号
        if (StringUtils.isEmpty(kitCheck.getKitNo()))
        {
            kitCheck.setKitNo(mkNumberRuleService.generateNumber("mms_kit_check"));
        }
        // 默认状态为待检查
        if (StringUtils.isEmpty(kitCheck.getStatus()))
        {
            kitCheck.setStatus("0");
        }
        kitCheck.setDelFlag("0");
        kitCheck.setCreateBy(SecurityUtils.getUsername());
        return kitCheckMapper.insertKitCheck(kitCheck);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateKitCheck(MmsKitCheck kitCheck)
    {
        // 已通过/缺料状态的检查不允许修改
        MmsKitCheck existing = kitCheckMapper.selectKitCheckById(kitCheck.getKitId());
        if (existing != null && ("1".equals(existing.getStatus()) || "2".equals(existing.getStatus())))
        {
            throw new ServiceException("齐套检查[" + existing.getKitNo() + "]已执行，不允许修改");
        }
        kitCheck.setUpdateBy(SecurityUtils.getUsername());
        return kitCheckMapper.updateKitCheck(kitCheck);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteKitCheckByIds(Long[] kitIds)
    {
        // 已通过的检查不允许删除
        for (Long id : kitIds)
        {
            MmsKitCheck check = kitCheckMapper.selectKitCheckById(id);
            if (check != null && "1".equals(check.getStatus()))
            {
                throw new ServiceException("齐套检查[" + check.getKitNo() + "]已通过，不允许删除");
            }
        }
        return kitCheckMapper.deleteKitCheckByIds(kitIds);
    }

    @Override
    public List<MmsKitCheckDetail> selectKitCheckDetailByKitId(Long kitId)
    {
        return kitCheckMapper.selectKitCheckDetailByKitId(kitId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int executeKitCheck(Long kitId)
    {
        MmsKitCheck kitCheck = kitCheckMapper.selectKitCheckById(kitId);
        if (kitCheck == null)
        {
            throw new ServiceException("齐套检查记录不存在或已删除");
        }
        // 状态校验：只有待检查(0)可执行
        if (!"0".equals(kitCheck.getStatus()))
        {
            throw new ServiceException("齐套检查[" + kitCheck.getKitNo() + "]当前状态为" + statusName(kitCheck.getStatus()) + "，只有待检查状态可执行");
        }

        // 获取明细列表
        List<MmsKitCheckDetail> details = kitCheckMapper.selectKitCheckDetailByKitId(kitId);
        if (details == null || details.isEmpty())
        {
            throw new ServiceException("齐套检查明细为空，无法执行检查");
        }

        // 计算齐套情况
        int totalItems = details.size();
        int completeItems = 0;
        boolean allComplete = true;

        for (MmsKitCheckDetail detail : details)
        {
            // 计算缺口量 = 需求数量 - 可用数量
            BigDecimal required = detail.getRequiredQty() == null ? BigDecimal.ZERO : detail.getRequiredQty();
            BigDecimal available = detail.getAvailableQty() == null ? BigDecimal.ZERO : detail.getAvailableQty();

            BigDecimal shortage = required.subtract(available);
            if (shortage.compareTo(BigDecimal.ZERO) < 0)
            {
                shortage = BigDecimal.ZERO;
            }
            detail.setShortageQty(shortage);

            // 判断单项是否齐套
            if (shortage.compareTo(BigDecimal.ZERO) == 0)
            {
                detail.setIsComplete("1");
                completeItems++;
            }
            else
            {
                detail.setIsComplete("0");
                allComplete = false;
            }

            // 更新明细
            kitCheckMapper.insertKitCheckDetail(detail);
        }

        // 计算齐套率
        BigDecimal kitRate = BigDecimal.valueOf(completeItems)
                .divide(BigDecimal.valueOf(totalItems), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        // 更新主表
        kitCheck.setKitRate(kitRate);
        kitCheck.setIsComplete(allComplete ? "1" : "0");
        kitCheck.setStatus(allComplete ? "1" : "2");
        kitCheck.setCheckTime(new Date());
        kitCheck.setUpdateBy(SecurityUtils.getUsername());

        return kitCheckMapper.updateKitCheck(kitCheck);
    }

    // ========== 私有辅助方法 ==========

    private String statusName(String status)
    {
        switch (status)
        {
            case "0": return "待检查";
            case "1": return "已通过";
            case "2": return "缺料";
            default: return "未知(" + status + ")";
        }
    }

}

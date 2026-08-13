package com.ruoyi.safety.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.safety.domain.SafetyMaterial;
import com.ruoyi.safety.domain.SafetyMaterialRecord;
import com.ruoyi.safety.mapper.SafetyMaterialMapper;
import com.ruoyi.safety.mapper.SafetyMaterialRecordMapper;
import com.ruoyi.safety.service.ISafetyMaterialRecordService;
import com.ruoyi.mk.service.IMkNumberRuleService;

@Service
public class SafetyMaterialRecordServiceImpl implements ISafetyMaterialRecordService
{
    @Autowired
    private SafetyMaterialRecordMapper safetyMaterialRecordMapper;

    @Autowired
    private SafetyMaterialMapper safetyMaterialMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public SafetyMaterialRecord selectSafetyMaterialRecordById(Long recordId) {
        return safetyMaterialRecordMapper.selectSafetyMaterialRecordById(recordId);
    }

    @Override
    public List<SafetyMaterialRecord> selectSafetyMaterialRecordList(SafetyMaterialRecord record) {
        return safetyMaterialRecordMapper.selectSafetyMaterialRecordList(record);
    }

    @Override
    @Transactional
    public int insertSafetyMaterialRecord(SafetyMaterialRecord record)
    {
        // 生成单号
        if (StringUtils.isEmpty(record.getRecordCode()))
        {
            record.setRecordCode(mkNumberRuleService.generateNumber("safety_material_record"));
        }

        // 查询危化品信息
        SafetyMaterial material = safetyMaterialMapper.selectSafetyMaterialById(record.getMaterialId());
        if (material == null)
        {
            throw new ServiceException("危化品不存在");
        }

        // 填充冗余字段
        record.setMaterialCode(material.getMaterialCode());
        record.setMaterialName(material.getMaterialName());
        record.setUnit(material.getStorageUnit());

        // 计算操作前库存
        BigDecimal beforeStock = material.getCurrentStock() != null ? material.getCurrentStock() : BigDecimal.ZERO;
        record.setBeforeStock(beforeStock);

        // 计算操作后库存
        BigDecimal quantity = record.getQuantity();
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("数量必须大于0");
        }

        BigDecimal afterStock;
        if ("1".equals(record.getRecordType()))
        {
            // 入库：库存增加
            afterStock = beforeStock.add(quantity);
        }
        else if ("2".equals(record.getRecordType()))
        {
            // 出库：库存减少
            afterStock = beforeStock.subtract(quantity);
            if (afterStock.compareTo(BigDecimal.ZERO) < 0)
            {
                throw new ServiceException("出库数量不能超过当前库存（当前库存：" + beforeStock + "）");
            }
        }
        else
        {
            throw new ServiceException("记录类型无效（1=入库 2=出库）");
        }

        record.setAfterStock(afterStock);

        // 设置默认值
        if (record.getRecordTime() == null)
        {
            record.setRecordTime(new Date());
        }
        record.setStatus("0");

        // 插入记录
        int rows = safetyMaterialRecordMapper.insertSafetyMaterialRecord(record);

        // 动态更新危化品当前库存
        safetyMaterialService_updateCurrentStock(record.getMaterialId(), afterStock);

        return rows;
    }

    @Override
    @Transactional
    public int updateSafetyMaterialRecord(SafetyMaterialRecord record)
    {
        // 编辑出入库记录时不自动调整库存（避免库存计算混乱）
        // 如需调整库存，请通过新增冲正记录来处理
        return safetyMaterialRecordMapper.updateSafetyMaterialRecord(record);
    }

    @Override
    public int deleteSafetyMaterialRecordByIds(Long[] recordIds) {
        return safetyMaterialRecordMapper.deleteSafetyMaterialRecordByIds(recordIds);
    }

    @Override
    public int deleteSafetyMaterialRecordById(Long recordId) {
        return safetyMaterialRecordMapper.deleteSafetyMaterialRecordById(recordId);
    }

    /** 调用SafetyMaterialService更新库存 */
    private void safetyMaterialService_updateCurrentStock(Long materialId, BigDecimal afterStock)
    {
        SafetyMaterial update = new SafetyMaterial();
        update.setMaterialId(materialId);
        update.setCurrentStock(afterStock);
        update.setUpdateBy(SecurityUtils.getUsername());
        safetyMaterialMapper.updateSafetyMaterial(update);
    }
}

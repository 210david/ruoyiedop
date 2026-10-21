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
        SafetyMaterialRecord old = safetyMaterialRecordMapper.selectSafetyMaterialRecordById(record.getRecordId());
        if (old == null)
        {
            throw new ServiceException("出入库记录不存在");
        }
        BigDecimal oldQty = old.getQuantity() != null ? old.getQuantity() : BigDecimal.ZERO;
        String oldType = old.getRecordType();

        // 新值：未传时沿用原值
        BigDecimal newQty = record.getQuantity() != null ? record.getQuantity() : oldQty;
        if (newQty.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("数量必须大于0");
        }
        String newType = StringUtils.isNotEmpty(record.getRecordType()) ? record.getRecordType() : oldType;
        if (!"1".equals(newType) && !"2".equals(newType))
        {
            throw new ServiceException("记录类型无效（1=入库 2=出库）");
        }

        Long oldMaterialId = old.getMaterialId();
        Long newMaterialId = record.getMaterialId() != null ? record.getMaterialId() : oldMaterialId;

        if (!oldMaterialId.equals(newMaterialId) || !oldType.equals(newType) || oldQty.compareTo(newQty) != 0)
        {
            // 先冲销旧记录对旧物料的影响
            SafetyMaterial oldMaterial = safetyMaterialMapper.selectSafetyMaterialById(oldMaterialId);
            if (oldMaterial == null)
            {
                throw new ServiceException("危化品不存在");
            }
            BigDecimal oldStock = oldMaterial.getCurrentStock() != null ? oldMaterial.getCurrentStock() : BigDecimal.ZERO;
            BigDecimal oldEffect = "1".equals(oldType) ? oldQty : oldQty.negate();
            BigDecimal revertedStock = oldStock.subtract(oldEffect);
            if (revertedStock.compareTo(BigDecimal.ZERO) < 0)
            {
                throw new ServiceException("冲销后库存不能为负（当前库存：" + oldStock + "）");
            }

            BigDecimal baseStock;
            if (oldMaterialId.equals(newMaterialId))
            {
                baseStock = revertedStock;
            }
            else
            {
                // 换了物料：先回滚旧物料库存
                safetyMaterialService_updateCurrentStock(oldMaterialId, revertedStock);
                SafetyMaterial newMaterial = safetyMaterialMapper.selectSafetyMaterialById(newMaterialId);
                if (newMaterial == null)
                {
                    throw new ServiceException("危化品不存在");
                }
                baseStock = newMaterial.getCurrentStock() != null ? newMaterial.getCurrentStock() : BigDecimal.ZERO;
            }

            // 再应用新值
            BigDecimal afterStock;
            if ("1".equals(newType))
            {
                afterStock = baseStock.add(newQty);
            }
            else
            {
                afterStock = baseStock.subtract(newQty);
                if (afterStock.compareTo(BigDecimal.ZERO) < 0)
                {
                    throw new ServiceException("出库数量不能超过当前库存（当前库存：" + baseStock + "）");
                }
            }
            safetyMaterialService_updateCurrentStock(newMaterialId, afterStock);

            // 保证记录自身账实自洽
            record.setBeforeStock(baseStock);
            record.setAfterStock(afterStock);
            record.setMaterialId(newMaterialId);
        }

        return safetyMaterialRecordMapper.updateSafetyMaterialRecord(record);
    }

    @Override
    @Transactional
    public int deleteSafetyMaterialRecordByIds(Long[] recordIds) {
        for (Long recordId : recordIds)
        {
            revertRecordStock(recordId);
        }
        return safetyMaterialRecordMapper.deleteSafetyMaterialRecordByIds(recordIds);
    }

    @Override
    @Transactional
    public int deleteSafetyMaterialRecordById(Long recordId) {
        revertRecordStock(recordId);
        return safetyMaterialRecordMapper.deleteSafetyMaterialRecordById(recordId);
    }

    /** 删除出入库记录时冲正库存 */
    private void revertRecordStock(Long recordId)
    {
        SafetyMaterialRecord old = safetyMaterialRecordMapper.selectSafetyMaterialRecordById(recordId);
        if (old == null)
        {
            return;
        }
        SafetyMaterial material = safetyMaterialMapper.selectSafetyMaterialById(old.getMaterialId());
        if (material == null)
        {
            return;
        }
        BigDecimal qty = old.getQuantity() != null ? old.getQuantity() : BigDecimal.ZERO;
        BigDecimal stock = material.getCurrentStock() != null ? material.getCurrentStock() : BigDecimal.ZERO;
        // 入库删除则扣减，出库删除则回补
        BigDecimal reverted = "1".equals(old.getRecordType()) ? stock.subtract(qty) : stock.add(qty);
        if (reverted.compareTo(BigDecimal.ZERO) < 0)
        {
            throw new ServiceException("冲正后库存不能为负（当前库存：" + stock + "），请先核对库存");
        }
        safetyMaterialService_updateCurrentStock(old.getMaterialId(), reverted);
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

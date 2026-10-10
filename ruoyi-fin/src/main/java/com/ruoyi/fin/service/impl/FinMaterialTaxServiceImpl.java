package com.ruoyi.fin.service.impl;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.fin.domain.FinMaterialTax;
import com.ruoyi.fin.mapper.FinMaterialTaxMapper;
import com.ruoyi.fin.service.IFinMaterialTaxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 物料税率档案 Service 实现
 *
 * @author ruoyi
 */
@Service
public class FinMaterialTaxServiceImpl implements IFinMaterialTaxService
{
    private static final Logger log = LoggerFactory.getLogger(FinMaterialTaxServiceImpl.class);

    /** 税率上限（100%，防止误填大于 1 的值） */
    private static final BigDecimal MAX_TAX_RATE = BigDecimal.ONE;

    @Autowired
    private FinMaterialTaxMapper taxMapper;

    @Override
    public List<FinMaterialTax> selectTaxList(FinMaterialTax tax)
    {
        return taxMapper.selectTaxList(tax);
    }

    @Override
    public FinMaterialTax getEffectiveTax(String materialCode)
    {
        return taxMapper.selectEffectiveByMaterial(materialCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertTax(FinMaterialTax tax)
    {
        validate(tax, null);
        tax.setStatus(tax.getStatus() == null ? "0" : tax.getStatus());
        tax.setTaxIncluded(tax.getTaxIncluded() == null ? "Y" : tax.getTaxIncluded());
        tax.setCompanyId(tax.getCompanyId() == null ? 0L : tax.getCompanyId());
        if (tax.getEffectiveDate() == null)
        {
            tax.setEffectiveDate(new Date());
        }
        tax.setCreateBy(SecurityUtils.getUsername());
        return taxMapper.insertTax(tax);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateTax(FinMaterialTax tax)
    {
        if (tax.getMaterialTaxId() == null)
        {
            throw new IllegalArgumentException("档案ID不能为空");
        }
        validate(tax, tax.getMaterialTaxId());
        tax.setUpdateBy(SecurityUtils.getUsername());
        return taxMapper.updateTax(tax);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteTaxByIds(Long[] ids)
    {
        if (ids == null || ids.length == 0)
        {
            return 0;
        }
        return taxMapper.deleteTaxByIds(ids);
    }

    @Override
    public List<Map<String, Object>> untaxedMaterials(int limit)
    {
        return taxMapper.selectUntaxedMaterials(limit <= 0 ? 200 : limit);
    }

    @Override
    public int countEffective()
    {
        return taxMapper.countEffective();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInit(String taxRate)
    {
        BigDecimal rate = new BigDecimal(taxRate);
        validateRate(rate);
        List<Map<String, Object>> untaxed = taxMapper.selectUntaxedMaterials(500);
        String user = SecurityUtils.getUsername();
        Date today = new Date();
        int n = 0;

        for (Map<String, Object> row : untaxed)
        {
            Object code = row.get("materialCode");
            if (code == null)
            {
                continue;
            }
            FinMaterialTax t = new FinMaterialTax();
            t.setMaterialId(toLong(row.get("materialId")));
            t.setMaterialCode(code.toString());
            t.setTaxRate(rate);
            t.setTaxIncluded("Y");
            t.setEffectiveDate(today);
            t.setStatus("0");
            t.setCreateBy(user);
            t.setRemark("批量初始化");
            taxMapper.insertTax(t);
            n++;
        }
        log.info("批量初始化税率档案 {} 条，税率 {}", n, rate);
        return n;
    }

    // ==================== 私有方法 ====================

    /**
     * 校验税率档案。
     *
     * @param tax  档案
     * @param taxId 修改时传自身ID，校验重复时排除自己
     */
    private void validate(FinMaterialTax tax, Long taxId)
    {
        if (tax.getMaterialCode() == null || tax.getMaterialCode().trim().isEmpty())
        {
            throw new IllegalArgumentException("物料编码不能为空");
        }
        tax.setMaterialCode(tax.getMaterialCode().trim());

        if (tax.getTaxRate() == null)
        {
            throw new IllegalArgumentException("税率不能为空（零税率物料请显式填写 0）");
        }
        validateRate(tax.getTaxRate());

        if (tax.getEffectiveDate() == null)
        {
            throw new IllegalArgumentException("生效日期不能为空");
        }
        // 失效日期不得早于生效日期
        if (tax.getExpireDate() != null
                && tax.getExpireDate().before(tax.getEffectiveDate()))
        {
            throw new IllegalArgumentException("失效日期不能早于生效日期");
        }
        // 同一物料同一生效日不得重复
        if (taxMapper.checkDuplicate(tax.getMaterialCode(), tax.getEffectiveDate(), taxId) > 0)
        {
            throw new IllegalArgumentException("物料「" + tax.getMaterialCode()
                    + "」在该生效日期已有档案，同一物料同一生效日只能有一条");
        }
    }

    /**
     * 校验税率范围：0 ~ 1（即 0% ~ 100%）。
     */
    private void validateRate(BigDecimal rate)
    {
        if (rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(MAX_TAX_RATE) > 0)
        {
            throw new IllegalArgumentException(
                    "税率应在 0 到 1 之间（如13% 填0.13），当前值：" + rate.toPlainString());
        }
    }

    private static Long toLong(Object o)
    {
        if (o == null)
        {
            return null;
        }
        if (o instanceof Long)
        {
            return (Long) o;
        }
        if (o instanceof Number)
        {
            return ((Number) o).longValue();
        }
        return Long.valueOf(o.toString());
    }
}
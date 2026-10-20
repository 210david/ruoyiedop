package com.ruoyi.pdm.service.impl;

import com.ruoyi.common.annotation.DataScope;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.pdm.domain.PdmDesignBom;
import com.ruoyi.pdm.domain.PdmDesignBomItem;
import com.ruoyi.pdm.domain.PdmDocVersion;
import com.ruoyi.pdm.domain.PdmProduct;
import com.ruoyi.pdm.mapper.PdmDesignBomMapper;
import com.ruoyi.pdm.mapper.PdmProductMapper;
import com.ruoyi.pdm.service.IPdmDesignBomService;
import com.ruoyi.pdm.service.IPdmDocVersionService;

/**
 * 设计BOM Service 实现
 *
 * 版本机制挂接（pdm_doc_version biz_type=3）：
 * 新增自动创建 V1.0 版本 → 生效 makeEffective（依据=设计定稿）→ 旧版本转历史；
 * 已生效BOM修订自动小版本升版并回到"编制中"，原生效版本保持有效；
 * 下发转换 MBOM（mms receiveMbom）于第三批实现。
 *
 * @author ruoyi
 */
@Service
public class PdmDesignBomServiceImpl implements IPdmDesignBomService
{
    @Autowired
    private PdmDesignBomMapper bomMapper;

    @Autowired
    private PdmProductMapper productMapper;

    @Autowired
    private IPdmDocVersionService docVersionService;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    @DataScope(deptAlias = "pdm_design_bom")
    public List<PdmDesignBom> selectBomList(PdmDesignBom bom)
    {
        return bomMapper.selectBomList(bom);
    }

    @Override
    public PdmDesignBom selectBomById(Long bomId)
    {
        PdmDesignBom bom = bomMapper.selectBomById(bomId);
        if (bom != null)
        {
            bom.setItemList(bomMapper.selectItemsByBomId(bomId));
        }
        return bom;
    }

    @Override
    public List<PdmDesignBomItem> selectItemsByBomId(Long bomId)
    {
        return bomMapper.selectItemsByBomId(bomId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertBom(PdmDesignBom bom)
    {
        PdmProduct product = checkProduct(bom.getProductId());
        if (StringUtils.isEmpty(bom.getBomName()))
        {
            throw new ServiceException("请填写BOM名称");
        }
        if (bom.getBaseQty() == null || bom.getBaseQty().signum() <= 0)
        {
            bom.setBaseQty(java.math.BigDecimal.ONE);
        }
        // 保存即取号
        bom.setBomNo(mkNumberRuleService.generateNumber("pdm_design_bom"));
        bom.setBomStatus("0");
        bom.setProductCode(product.getProductCode());
        bom.setProductName(product.getMaterialName());
        if (StringUtils.isEmpty(bom.getBaseUnit()))
        {
            bom.setBaseUnit(product.getUnit());
        }
        // 记录创建人与创建时间
        bom.setCreateBy(SecurityUtils.getUsername());
        bom.setCreateTime(DateUtils.getNowDate());
        int rows = bomMapper.insertBom(bom);
        saveItems(bom);
        // 自动创建 V1.0 版本记录
        createBomVersion(bom.getBomId(), "初始版本", false);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateBom(PdmDesignBom bom)
    {
        PdmDesignBom exist = bomMapper.selectBomById(bom.getBomId());
        if (exist == null)
        {
            throw new ServiceException("设计BOM不存在或已删除");
        }
        // 已生效BOM修订：自动小版本升版并回到"编制中"，原生效版本保持有效
        if ("1".equals(exist.getBomStatus()))
        {
            createBomVersion(bom.getBomId(), "已生效BOM修订，自动升版", false);
            bom.setBomStatus("0");
        }
        // 编号与关联产品不允许修改
        bom.setBomNo(null);
        bom.setProductId(null);
        bom.setProductCode(null);
        bom.setProductName(null);
        int rows = bomMapper.updateBom(bom);
        if (bom.getItemList() != null)
        {
            saveItems(bom);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int effectiveBom(Long bomId, Long versionId, String effectRef)
    {
        PdmDesignBom exist = bomMapper.selectBomById(bomId);
        if (exist == null)
        {
            throw new ServiceException("设计BOM不存在或已删除");
        }
        if ("1".equals(exist.getBomStatus()))
        {
            throw new ServiceException("BOM【" + exist.getBomNo() + "】已是生效状态，重复生效请先升版");
        }
        int itemCount = bomMapper.selectItemsByBomId(bomId).size();
        if (itemCount == 0)
        {
            throw new ServiceException("BOM明细为空，请先录入物料明细再生效");
        }
        PdmDocVersion version;
        if (versionId != null)
        {
            version = docVersionService.selectVersionById(versionId);
            if (version == null || !"3".equals(version.getBizType()) || !bomId.equals(version.getBizId()))
            {
                throw new ServiceException("版本记录不属于该BOM，无法生效");
            }
        }
        else
        {
            version = docVersionService.selectLatestVersion("3", bomId);
            if (version == null)
            {
                throw new ServiceException("BOM版本记录缺失，无法生效");
            }
        }
        // 版本生效（依据=设计定稿，旧版本自动转历史）
        docVersionService.makeEffective(version.getVersionId(), "1",
                StringUtils.isNotEmpty(effectRef) ? effectRef : exist.getBomNo());
        PdmDesignBom up = new PdmDesignBom();
        up.setBomId(bomId);
        up.setBomStatus("1");
        up.setUpdateBy(SecurityUtils.getUsername());
        return bomMapper.updateBom(up);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int upgradeBom(Long bomId, String versionDesc, boolean majorUpgrade)
    {
        PdmDesignBom exist = bomMapper.selectBomById(bomId);
        if (exist == null)
        {
            throw new ServiceException("设计BOM不存在或已删除");
        }
        return createBomVersion(bomId, StringUtils.isNotEmpty(versionDesc) ? versionDesc : "手工升版", majorUpgrade);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBomByIds(Long[] bomIds)
    {
        for (Long bomId : bomIds)
        {
            PdmDesignBom exist = bomMapper.selectBomById(bomId);
            if (exist != null && "1".equals(exist.getBomStatus()))
            {
                throw new ServiceException("已生效BOM【" + exist.getBomNo() + "】不允许删除，如需调整请走技术变更流程");
            }
        }
        return bomMapper.deleteBomByIds(bomIds);
    }

    /** 按技术变更单升版生效（依据=技术变更单号；要求BOM已按变更单修订升版） */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int effectiveForChange(Long bomId, String changeNo)
    {
        PdmDesignBom exist = bomMapper.selectBomById(bomId);
        if (exist == null)
        {
            throw new ServiceException("设计BOM不存在或已删除");
        }
        if (bomMapper.selectItemsByBomId(bomId).isEmpty())
        {
            throw new ServiceException("BOM明细为空，无法按变更单生效");
        }
        PdmDocVersion latest = docVersionService.selectLatestVersion("3", bomId);
        PdmDocVersion effective = docVersionService.selectEffectiveVersion("3", bomId);
        if (latest == null)
        {
            throw new ServiceException("BOM版本记录缺失，无法按变更单生效");
        }
        if (effective != null && latest.getVersionNo().equals(effective.getVersionNo()))
        {
            throw new ServiceException("BOM【" + exist.getBomNo() + "】尚未按变更单修订升版，请先修改BOM内容");
        }
        // 版本生效（依据=技术变更单，旧版本自动转历史）+ 置为已生效
        docVersionService.makeEffective(latest.getVersionId(), "2", changeNo);
        PdmDesignBom up = new PdmDesignBom();
        up.setBomId(bomId);
        up.setBomStatus("1");
        up.setUpdateBy(SecurityUtils.getUsername());
        return bomMapper.updateBom(up);
    }

    /** 创建BOM版本记录 */
    private int createBomVersion(Long bomId, String versionDesc, boolean majorUpgrade)
    {
        PdmDocVersion version = new PdmDocVersion();
        version.setBizType("3");
        version.setBizId(bomId);
        version.setVersionDesc(versionDesc);
        version.setCreateBy(SecurityUtils.getUsername());
        return docVersionService.createVersion(version, majorUpgrade);
    }

    /**
     * 保存BOM明细（全量替换，parentIndex 临时引用解析为真实父子关系与层级）。
     * 前端行编辑以 parentIndex（-1=顶级，否则父行下标）表达层级，后端落库时解析。
     */
    private void saveItems(PdmDesignBom bom)
    {
        bomMapper.deleteItemsByBomId(bom.getBomId());
        List<PdmDesignBomItem> items = bom.getItemList();
        if (items == null || items.isEmpty())
        {
            throw new ServiceException("请至少录入一行BOM物料明细");
        }
        int n = items.size();
        Long[] ids = new Long[n];
        for (int i = 0; i < n; i++)
        {
            PdmDesignBomItem item = items.get(i);
            if (StringUtils.isEmpty(item.getMaterialName()))
            {
                throw new ServiceException("BOM明细存在未填写物料名称的行");
            }
            if (item.getUsageQty() == null || item.getUsageQty().signum() <= 0)
            {
                throw new ServiceException("BOM明细【" + item.getMaterialName() + "】用量必须大于0");
            }
            item.setBomId(bom.getBomId());
            item.setParentId(0L);
            item.setItemLevel(1);
            item.setOrderNum(i + 1);
            bomMapper.insertBomItem(item);
            ids[i] = item.getItemId();
        }
        for (int i = 0; i < n; i++)
        {
            Integer p = items.get(i).getParentIndex();
            if (p == null || p < 0)
            {
                continue;
            }
            if (p >= n || p.equals(i))
            {
                throw new ServiceException("BOM明细【" + items.get(i).getMaterialName() + "】的父项引用无效");
            }
            // 沿父链计算层级并检测循环引用
            int level = 2;
            Integer cur = items.get(p).getParentIndex();
            int guard = 0;
            while (cur != null && cur >= 0)
            {
                if (cur >= n || cur.equals(i) || ++guard > n)
                {
                    throw new ServiceException("BOM明细层级存在循环引用，请检查父项设置");
                }
                level++;
                cur = items.get(cur).getParentIndex();
            }
            PdmDesignBomItem up = new PdmDesignBomItem();
            up.setItemId(ids[i]);
            up.setParentId(ids[p]);
            up.setItemLevel(level);
            bomMapper.updateBomItem(up);
        }
    }

    /** 校验产品档案 */
    private PdmProduct checkProduct(Long productId)
    {
        if (productId == null)
        {
            throw new ServiceException("请选择关联产品档案");
        }
        PdmProduct product = productMapper.selectProductById(productId);
        if (product == null)
        {
            throw new ServiceException("产品档案不存在或已删除");
        }
        return product;
    }
}

package com.ruoyi.pdm.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mms.domain.MmsBom;
import com.ruoyi.mms.domain.MmsBomDetail;
import com.ruoyi.mms.service.IMmsBomService;
import com.ruoyi.pdm.domain.PdmDesignBom;
import com.ruoyi.pdm.domain.PdmDesignBomItem;
import com.ruoyi.pdm.domain.PdmDocVersion;
import com.ruoyi.pdm.domain.PdmMbomRelease;
import com.ruoyi.pdm.domain.PdmProduct;
import com.ruoyi.pdm.mapper.PdmDesignBomMapper;
import com.ruoyi.pdm.mapper.PdmMbomReleaseMapper;
import com.ruoyi.pdm.mapper.PdmProductMapper;
import com.ruoyi.pdm.service.IPdmDocVersionService;
import com.ruoyi.pdm.service.IPdmMbomReleaseService;
import com.ruoyi.system.utils.MessageHelper;

/**
 * BOM下发记录 Service 实现
 *
 * 下发链路（协调方案衔接点一 D1）：生效EBOM → 组装 MmsBom → mms.receiveMbom 落库
 * （bomType=0标准BOM、status=1已发布，同产品旧BOM置停用 D3）→ pdm_mbom_release 记录版本对应关系
 * → 通知生产管控岗。pdm 写记录与 mms 接收同事务，失败整体回滚。
 *
 * @author ruoyi
 */
@Service
public class PdmMbomReleaseServiceImpl implements IPdmMbomReleaseService
{
    @Autowired
    private PdmMbomReleaseMapper releaseMapper;

    @Autowired
    private PdmDesignBomMapper bomMapper;

    @Autowired
    private PdmProductMapper productMapper;

    @Autowired
    private IPdmDocVersionService docVersionService;

    @Autowired
    private IMmsBomService mmsBomService;

    @Autowired
    private MessageHelper messageHelper;

    @Override
    public List<PdmMbomRelease> selectReleaseList(PdmMbomRelease release)
    {
        return releaseMapper.selectReleaseList(release);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PdmMbomRelease releaseMbom(Long bomId)
    {
        PdmDesignBom bom = bomMapper.selectBomById(bomId);
        if (bom == null)
        {
            throw new ServiceException("设计BOM不存在或已删除");
        }
        if (!"1".equals(bom.getBomStatus()))
        {
            throw new ServiceException("仅已生效的设计BOM允许下发（先完成BOM生效）");
        }
        // 产品档案必须挂接 wms 物料（mms_bom.product_id 即物料ID）
        PdmProduct product = productMapper.selectProductById(bom.getProductId());
        if (product == null)
        {
            throw new ServiceException("产品档案不存在或已删除");
        }
        if (product.getMaterialId() == null)
        {
            throw new ServiceException("产品档案【" + product.getProductCode() + "】未挂接物料，无法下发");
        }
        // 下发前置校验（需求7.4）：明细非空、物料编码全部确认
        List<PdmDesignBomItem> items = bomMapper.selectItemsByBomId(bomId);
        if (items == null || items.isEmpty())
        {
            throw new ServiceException("BOM明细为空，无法下发");
        }
        for (PdmDesignBomItem item : items)
        {
            if (item.getMaterialId() == null)
            {
                throw new ServiceException("明细【" + item.getMaterialName() + "】未确认物料编码，禁止下发（请先在设计BOM中确认明细物料）");
            }
        }
        PdmDocVersion version = docVersionService.selectEffectiveVersion("3", bomId);
        if (version == null)
        {
            throw new ServiceException("BOM无生效版本记录，无法下发");
        }
        // 防重复下发：同产品当前生效版本已下发至制造端时禁止再次下发（需先升版生成新版本）
        PdmMbomRelease released = new PdmMbomRelease();
        released.setProductId(bom.getProductId());
        released.setReleaseStatus("0");
        List<PdmMbomRelease> releasedList = releaseMapper.selectReleaseList(released);
        if (!releasedList.isEmpty() && version.getVersionNo().equals(releasedList.get(0).getEbomVersion()))
        {
            throw new ServiceException("产品当前生效版本【" + version.getVersionNo()
                    + "】已下发至制造端（制造BOM：" + releasedList.get(0).getMmsBomNo()
                    + "），请勿重复下发；BOM内容变更请先升版再下发");
        }
        // 组装 MBOM（映射口径见协调方案 2.4：bomType=0、status=1、version=EBOM生效版本号）
        MmsBom mmsBom = new MmsBom();
        mmsBom.setBomName(bom.getBomName());
        mmsBom.setProductId(product.getMaterialId());
        mmsBom.setProductCode(product.getProductCode());
        mmsBom.setVersion(version.getVersionNo());
        mmsBom.setBaseQty(bom.getBaseQty());
        mmsBom.setBaseUnit(bom.getBaseUnit());
        mmsBom.setRemark("产品设计BOM下发 " + bom.getBomNo() + " " + version.getVersionNo());
        List<MmsBomDetail> detailList = new java.util.ArrayList<>();
        for (PdmDesignBomItem item : items)
        {
            MmsBomDetail d = new MmsBomDetail();
            d.setMaterialId(item.getMaterialId());
            d.setMaterialCode(item.getMaterialCode());
            d.setMaterialName(item.getMaterialName());
            d.setSpecModel(item.getSpecModel());
            d.setUnit(item.getUnit());
            d.setUsageQty(item.getUsageQty());
            d.setLossRate(item.getLossRate());
            d.setRemark(item.getRemark());
            detailList.add(d);
        }
        mmsBom.setDetailList(detailList);
        // mms 接收（内部停用旧发布BOM并落库），与下发记录同事务
        Long mmsBomId = mmsBomService.receiveMbom(mmsBom);
        // 下发记录：同产品旧记录置已替代（D3）
        PdmMbomRelease mark = new PdmMbomRelease();
        mark.setProductId(bom.getProductId());
        mark.setUpdateBy(SecurityUtils.getUsername());
        releaseMapper.markReplacedByProductId(mark);
        PdmMbomRelease release = new PdmMbomRelease();
        release.setBomId(bomId);
        release.setBomNo(bom.getBomNo());
        release.setProductId(bom.getProductId());
        release.setProductCode(product.getProductCode());
        release.setProductName(product.getMaterialName());
        release.setEbomVersion(version.getVersionNo());
        release.setMmsBomId(mmsBomId);
        release.setMmsBomNo(mmsBom.getBomNo());
        release.setMmsVersion(version.getVersionNo());
        release.setItemCount(detailList.size());
        release.setReleaseStatus("0");
        release.setReleaseBy(SecurityUtils.getUsername());
        release.setReleaseTime(new Date());
        release.setCreateBy(SecurityUtils.getUsername());
        release.setCreateTime(new Date());
        release.setRemark(mmsBom.getRemark());
        releaseMapper.insertRelease(release);
        // 通知生产管控岗（BOM下发完成）
        messageHelper.sendMessage(
                "制造BOM已下发：" + mmsBom.getBomNo(),
                "产品【" + product.getProductCode() + " " + product.getMaterialName() + "】设计BOM " + bom.getBomNo()
                        + " " + version.getVersionNo() + " 已下发转换为制造BOM，请在生产管控模块查看。",
                "1", "1", "pdm", release.getReleaseId(), "/pdm/designBom", "mms:bom:list", "已下发", "BOM管理");
        return release;
    }
}

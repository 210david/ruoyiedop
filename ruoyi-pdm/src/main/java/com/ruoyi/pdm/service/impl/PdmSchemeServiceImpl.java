package com.ruoyi.pdm.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.pdm.domain.PdmDocVersion;
import com.ruoyi.pdm.domain.PdmProduct;
import com.ruoyi.pdm.domain.PdmScheme;
import com.ruoyi.pdm.mapper.PdmProductMapper;
import com.ruoyi.pdm.mapper.PdmSchemeMapper;
import com.ruoyi.pdm.service.IPdmDocVersionService;
import com.ruoyi.pdm.service.IPdmSchemeService;

/**
 * 设计方案 Service 实现
 *
 * 版本机制挂接（pdm_doc_version biz_type=1）：
 * 新增自动创建 V1.0 版本（未生效）→ 定稿 makeEffective（依据=设计定稿）→ 旧版本转历史；
 * 已定稿方案修订自动小版本升版并回到"编制中"，原生效版本保持有效。
 *
 * @author ruoyi
 */
@Service
public class PdmSchemeServiceImpl implements IPdmSchemeService
{
    @Autowired
    private PdmSchemeMapper schemeMapper;

    @Autowired
    private PdmProductMapper productMapper;

    @Autowired
    private IPdmDocVersionService docVersionService;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    @DataScope(deptAlias = "pdm_scheme")
    public List<PdmScheme> selectSchemeList(PdmScheme scheme)
    {
        return schemeMapper.selectSchemeList(scheme);
    }

    @Override
    public PdmScheme selectSchemeById(Long schemeId)
    {
        PdmScheme scheme = schemeMapper.selectSchemeById(schemeId);
        if (scheme != null)
        {
            scheme.setFormulaList(schemeMapper.selectFormulaBySchemeId(schemeId));
        }
        return scheme;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertScheme(PdmScheme scheme)
    {
        if (StringUtils.isEmpty(scheme.getSchemeName()))
        {
            throw new ServiceException("请填写方案名称");
        }
        PdmProduct product = checkProduct(scheme.getProductId());
        // 保存即取号，编号生成后不可修改
        scheme.setSchemeNo(mkNumberRuleService.generateNumber("pdm_scheme"));
        scheme.setSchemeStatus("0");
        scheme.setProductCode(product.getProductCode());
        scheme.setProductName(product.getMaterialName());
        // 记录创建人与创建时间
        scheme.setCreateBy(SecurityUtils.getUsername());
        scheme.setCreateTime(DateUtils.getNowDate());
        int rows = schemeMapper.insertScheme(scheme);
        saveFormula(scheme);
        // 自动创建 V1.0 版本记录（未生效）
        createSchemeVersion(scheme.getSchemeId(), "初始版本", scheme.getFileUrl(), false);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateScheme(PdmScheme scheme)
    {
        PdmScheme exist = schemeMapper.selectSchemeById(scheme.getSchemeId());
        if (exist == null)
        {
            throw new ServiceException("设计方案不存在或已删除");
        }
        // 已停用方案为终态保留追溯，不允许修改
        if ("2".equals(exist.getSchemeStatus()))
        {
            throw new ServiceException("方案【" + exist.getSchemeNo() + "】已停用，不允许修改，如需调整请先恢复");
        }
        // 已定稿方案修订：自动小版本升版并回到"编制中"，原生效版本保持有效
        if ("1".equals(exist.getSchemeStatus()))
        {
            String fileUrl = scheme.getFileUrl() != null ? scheme.getFileUrl() : exist.getFileUrl();
            createSchemeVersion(scheme.getSchemeId(), "已定稿方案修订，自动升版", fileUrl, false);
            scheme.setSchemeStatus("0");
        }
        // 编号与关联产品不允许修改
        scheme.setSchemeNo(null);
        scheme.setProductId(null);
        scheme.setProductCode(null);
        scheme.setProductName(null);
        int rows = schemeMapper.updateScheme(scheme);
        if (scheme.getFormulaList() != null)
        {
            saveFormula(scheme);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int effectiveScheme(Long schemeId, Long versionId, String effectRef)
    {
        PdmScheme exist = schemeMapper.selectSchemeById(schemeId);
        if (exist == null)
        {
            throw new ServiceException("设计方案不存在或已删除");
        }
        if ("1".equals(exist.getSchemeStatus()))
        {
            throw new ServiceException("方案【" + exist.getSchemeNo() + "】已定稿，不允许重复定稿");
        }
        // 确定定稿版本（未指定则取最新版本）
        PdmDocVersion version;
        if (versionId != null)
        {
            version = docVersionService.selectVersionById(versionId);
            if (version == null || !"1".equals(version.getBizType()) || !schemeId.equals(version.getBizId()))
            {
                throw new ServiceException("版本记录不属于该方案，无法定稿");
            }
        }
        else
        {
            version = docVersionService.selectLatestVersion("1", schemeId);
            if (version == null)
            {
                throw new ServiceException("方案版本记录缺失，无法定稿");
            }
        }
        // 定稿时同步当前正文文件到生效版本记录
        PdmDocVersion sync = new PdmDocVersion();
        sync.setVersionId(version.getVersionId());
        sync.setFileUrl(exist.getFileUrl());
        docVersionService.updateVersion(sync);
        // 版本生效（依据=设计定稿，旧版本自动转历史）
        docVersionService.makeEffective(version.getVersionId(), "1",
                StringUtils.isNotEmpty(effectRef) ? effectRef : exist.getSchemeNo());
        // 方案状态置已定稿
        PdmScheme up = new PdmScheme();
        up.setSchemeId(schemeId);
        up.setSchemeStatus("1");
        up.setFinalTime(new Date());
        up.setFinalBy(SecurityUtils.getUsername());
        up.setUpdateBy(SecurityUtils.getUsername());
        int rows = schemeMapper.updateScheme(up);
        // 产品档案定型联动：设计中 → 已定型（只允许由定稿流程触发）
        PdmProduct product = productMapper.selectProductById(exist.getProductId());
        if (product != null && "0".equals(product.getProductStatus()))
        {
            PdmProduct pu = new PdmProduct();
            pu.setProductId(product.getProductId());
            pu.setProductStatus("1");
            pu.setFinalDate(new Date());
            pu.setUpdateBy(SecurityUtils.getUsername());
            productMapper.updateProduct(pu);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int upgradeScheme(Long schemeId, String versionDesc, boolean majorUpgrade)
    {
        PdmScheme exist = schemeMapper.selectSchemeById(schemeId);
        if (exist == null)
        {
            throw new ServiceException("设计方案不存在或已删除");
        }
        return createSchemeVersion(schemeId,
                StringUtils.isNotEmpty(versionDesc) ? versionDesc : "手工升版", exist.getFileUrl(), majorUpgrade);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteSchemeByIds(Long[] schemeIds)
    {
        for (Long schemeId : schemeIds)
        {
            PdmScheme exist = schemeMapper.selectSchemeById(schemeId);
            if (exist != null && ("1".equals(exist.getSchemeStatus()) || "2".equals(exist.getSchemeStatus())))
            {
                throw new ServiceException("方案【" + exist.getSchemeNo() + "】已定稿/已停用，不允许删除；已定稿调整请走技术变更流程，已停用保留用于历史追溯");
            }
        }
        return schemeMapper.deleteSchemeByIds(schemeIds);
    }

    /** 停用方案（已定稿 → 已停用；生命周期终止动作，不走变更单，版本记录保持不变） */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int disableScheme(Long schemeId, String remark)
    {
        PdmScheme exist = schemeMapper.selectSchemeById(schemeId);
        if (exist == null)
        {
            throw new ServiceException("设计方案不存在或已删除");
        }
        if (!"1".equals(exist.getSchemeStatus()))
        {
            throw new ServiceException("仅「已定稿」方案允许停用；编制中方案可直接删除");
        }
        PdmScheme up = new PdmScheme();
        up.setSchemeId(schemeId);
        up.setSchemeStatus("2");
        up.setRemark(remark);
        up.setUpdateBy(SecurityUtils.getUsername());
        return schemeMapper.updateScheme(up);
    }

    /** 恢复方案（已停用 → 已定稿，用于误停用或产品复产） */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int enableScheme(Long schemeId)
    {
        PdmScheme exist = schemeMapper.selectSchemeById(schemeId);
        if (exist == null)
        {
            throw new ServiceException("设计方案不存在或已删除");
        }
        if (!"2".equals(exist.getSchemeStatus()))
        {
            throw new ServiceException("仅「已停用」方案允许恢复");
        }
        PdmScheme up = new PdmScheme();
        up.setSchemeId(schemeId);
        up.setSchemeStatus("1");
        up.setUpdateBy(SecurityUtils.getUsername());
        return schemeMapper.updateScheme(up);
    }

    /** 按技术变更单升版生效（依据=技术变更单号；要求方案已按变更单修订升版） */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int effectiveForChange(Long schemeId, String changeNo)
    {
        PdmScheme exist = schemeMapper.selectSchemeById(schemeId);
        if (exist == null)
        {
            throw new ServiceException("设计方案不存在或已删除");
        }
        PdmDocVersion latest = docVersionService.selectLatestVersion("1", schemeId);
        PdmDocVersion effective = docVersionService.selectEffectiveVersion("1", schemeId);
        if (latest == null)
        {
            throw new ServiceException("方案版本记录缺失，无法按变更单生效");
        }
        if (effective != null && latest.getVersionNo().equals(effective.getVersionNo()))
        {
            throw new ServiceException("方案【" + exist.getSchemeNo() + "】尚未按变更单修订升版，请先修改方案内容");
        }
        // 同步当前正文文件到生效版本记录
        PdmDocVersion sync = new PdmDocVersion();
        sync.setVersionId(latest.getVersionId());
        sync.setFileUrl(exist.getFileUrl());
        docVersionService.updateVersion(sync);
        // 版本生效（依据=技术变更单，旧版本自动转历史）
        docVersionService.makeEffective(latest.getVersionId(), "2", changeNo);
        PdmScheme up = new PdmScheme();
        up.setSchemeId(schemeId);
        up.setSchemeStatus("1");
        up.setFinalTime(new Date());
        up.setFinalBy(SecurityUtils.getUsername());
        up.setUpdateBy(SecurityUtils.getUsername());
        return schemeMapper.updateScheme(up);
    }

    /** 创建方案版本记录 */
    private int createSchemeVersion(Long schemeId, String versionDesc, String fileUrl, boolean majorUpgrade)
    {
        PdmDocVersion version = new PdmDocVersion();
        version.setBizType("1");
        version.setBizId(schemeId);
        version.setVersionDesc(versionDesc);
        version.setFileUrl(fileUrl);
        version.setCreateBy(SecurityUtils.getUsername());
        return docVersionService.createVersion(version, majorUpgrade);
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

    /** 保存配方配比明细（全量替换） */
    private void saveFormula(PdmScheme scheme)
    {
        schemeMapper.deleteFormulaBySchemeId(scheme.getSchemeId());
        List<com.ruoyi.pdm.domain.PdmSchemeFormula> list = scheme.getFormulaList();
        if (list == null || list.isEmpty())
        {
            return;
        }
        int order = 1;
        for (com.ruoyi.pdm.domain.PdmSchemeFormula f : list)
        {
            if (StringUtils.isEmpty(f.getItemName()))
            {
                throw new ServiceException("配方明细存在未填写原料名称的行");
            }
            f.setSchemeId(scheme.getSchemeId());
            f.setOrderNum(order++);
        }
        schemeMapper.batchInsertFormula(list);
    }
}

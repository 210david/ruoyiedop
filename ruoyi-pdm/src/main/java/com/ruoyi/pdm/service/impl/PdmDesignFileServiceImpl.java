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
import com.ruoyi.pdm.domain.PdmDocVersion;
import com.ruoyi.pdm.domain.PdmDesignFile;
import com.ruoyi.pdm.domain.PdmProduct;
import com.ruoyi.pdm.domain.PdmScheme;
import com.ruoyi.pdm.mapper.PdmDesignFileMapper;
import com.ruoyi.pdm.mapper.PdmProductMapper;
import com.ruoyi.pdm.mapper.PdmSchemeMapper;
import com.ruoyi.pdm.service.IPdmDesignFileService;
import com.ruoyi.pdm.service.IPdmDocVersionService;

/**
 * 图纸技术文件 Service 实现
 *
 * 版本机制挂接（pdm_doc_version biz_type=2）：
 * 上传即纳入版本管理（新增自动创建 V1.0）→ 文件重新上传自动小版本升版 →
 * 生效 makeEffective（依据=设计定稿）置为受控，旧版本自动转历史。
 *
 * @author ruoyi
 */
@Service
public class PdmDesignFileServiceImpl implements IPdmDesignFileService
{
    @Autowired
    private PdmDesignFileMapper fileMapper;

    @Autowired
    private PdmProductMapper productMapper;

    @Autowired
    private PdmSchemeMapper schemeMapper;

    @Autowired
    private IPdmDocVersionService docVersionService;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    @DataScope(deptAlias = "pdm_design_file")
    public List<PdmDesignFile> selectFileList(PdmDesignFile file)
    {
        return fileMapper.selectFileList(file);
    }

    @Override
    public PdmDesignFile selectFileById(Long fileId)
    {
        return fileMapper.selectFileById(fileId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertFile(PdmDesignFile file)
    {
        if (StringUtils.isEmpty(file.getFileName()))
        {
            throw new ServiceException("请填写资料名称");
        }
        if (StringUtils.isEmpty(file.getFileUrl()))
        {
            throw new ServiceException("请上传资料文件");
        }
        PdmProduct product = checkProduct(file.getProductId());
        if (file.getSchemeId() != null)
        {
            PdmScheme scheme = schemeMapper.selectSchemeById(file.getSchemeId());
            file.setSchemeNo(scheme != null ? scheme.getSchemeNo() : null);
        }
        // 保存即取号
        file.setFileNo(mkNumberRuleService.generateNumber("pdm_design_file"));
        file.setFileStatus("0");
        file.setProductCode(product.getProductCode());
        file.setProductName(product.getMaterialName());
        // 记录创建人与创建时间
        file.setCreateBy(SecurityUtils.getUsername());
        file.setCreateTime(DateUtils.getNowDate());
        int rows = fileMapper.insertFile(file);
        // 上传即纳入版本管理：自动创建 V1.0 版本记录
        createFileVersion(file.getFileId(), "初始上传", file.getFileUrl(), false);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateFile(PdmDesignFile file)
    {
        PdmDesignFile exist = fileMapper.selectFileById(file.getFileId());
        if (exist == null)
        {
            throw new ServiceException("图纸技术文件不存在或已删除");
        }
        // 文件重新上传自动升版（同名资料重复上传生成新版本而非覆盖）
        String newUrl = file.getFileUrl();
        if (StringUtils.isNotEmpty(newUrl) && !newUrl.equals(exist.getFileUrl()))
        {
            createFileVersion(file.getFileId(), "文件重新上传，自动升版", newUrl, false);
        }
        // 编号与关联产品不允许修改
        file.setFileNo(null);
        file.setProductId(null);
        file.setProductCode(null);
        file.setProductName(null);
        file.setFileStatus(null);
        if (file.getSchemeId() != null)
        {
            PdmScheme scheme = schemeMapper.selectSchemeById(file.getSchemeId());
            file.setSchemeNo(scheme != null ? scheme.getSchemeNo() : null);
        }
        return fileMapper.updateFile(file);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int effectiveFile(Long fileId, Long versionId, String effectRef)
    {
        PdmDesignFile exist = fileMapper.selectFileById(fileId);
        if (exist == null)
        {
            throw new ServiceException("图纸技术文件不存在或已删除");
        }
        if ("1".equals(exist.getFileStatus()))
        {
            throw new ServiceException("文件【" + exist.getFileNo() + "】已是受控状态，重复生效请先升版");
        }
        PdmDocVersion version = resolveVersion(versionId, "2", fileId);
        // 版本生效（依据=设计定稿，旧版本自动转历史）
        docVersionService.makeEffective(version.getVersionId(), "1",
                StringUtils.isNotEmpty(effectRef) ? effectRef : exist.getFileNo());
        // 置为受控状态
        PdmDesignFile up = new PdmDesignFile();
        up.setFileId(fileId);
        up.setFileStatus("1");
        up.setUpdateBy(SecurityUtils.getUsername());
        return fileMapper.updateFile(up);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int upgradeFile(Long fileId, String versionDesc, boolean majorUpgrade)
    {
        PdmDesignFile exist = fileMapper.selectFileById(fileId);
        if (exist == null)
        {
            throw new ServiceException("图纸技术文件不存在或已删除");
        }
        return createFileVersion(fileId,
                StringUtils.isNotEmpty(versionDesc) ? versionDesc : "手工升版", exist.getFileUrl(), majorUpgrade);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteFileByIds(Long[] fileIds)
    {
        for (Long fileId : fileIds)
        {
            PdmDesignFile exist = fileMapper.selectFileById(fileId);
            if (exist != null && "1".equals(exist.getFileStatus()))
            {
                throw new ServiceException("受控文件【" + exist.getFileNo() + "】不允许删除，如需调整请走技术变更流程");
            }
        }
        return fileMapper.deleteFileByIds(fileIds);
    }

    /** 按技术变更单升版生效（依据=技术变更单号；要求文件已按变更单重新上传升版） */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int effectiveForChange(Long fileId, String changeNo)
    {
        PdmDesignFile exist = fileMapper.selectFileById(fileId);
        if (exist == null)
        {
            throw new ServiceException("图纸技术文件不存在或已删除");
        }
        PdmDocVersion latest = docVersionService.selectLatestVersion("2", fileId);
        PdmDocVersion effective = docVersionService.selectEffectiveVersion("2", fileId);
        if (latest == null)
        {
            throw new ServiceException("文件版本记录缺失，无法按变更单生效");
        }
        if (effective != null && latest.getVersionNo().equals(effective.getVersionNo()))
        {
            throw new ServiceException("文件【" + exist.getFileNo() + "】尚未按变更单升版，请先重新上传新版本");
        }
        // 同步当前文件到生效版本记录
        PdmDocVersion sync = new PdmDocVersion();
        sync.setVersionId(latest.getVersionId());
        sync.setFileUrl(exist.getFileUrl());
        docVersionService.updateVersion(sync);
        // 版本生效（依据=技术变更单，旧版本自动转历史）+ 置为受控
        docVersionService.makeEffective(latest.getVersionId(), "2", changeNo);
        PdmDesignFile up = new PdmDesignFile();
        up.setFileId(fileId);
        up.setFileStatus("1");
        up.setUpdateBy(SecurityUtils.getUsername());
        return fileMapper.updateFile(up);
    }

    /** 创建文件版本记录 */
    private int createFileVersion(Long fileId, String versionDesc, String fileUrl, boolean majorUpgrade)
    {
        PdmDocVersion version = new PdmDocVersion();
        version.setBizType("2");
        version.setBizId(fileId);
        version.setVersionDesc(versionDesc);
        version.setFileUrl(fileUrl);
        version.setCreateBy(SecurityUtils.getUsername());
        return docVersionService.createVersion(version, majorUpgrade);
    }

    /** 确定生效版本 */
    private PdmDocVersion resolveVersion(Long versionId, String bizType, Long bizId)
    {
        PdmDocVersion version;
        if (versionId != null)
        {
            version = docVersionService.selectVersionById(versionId);
            if (version == null || !bizType.equals(version.getBizType()) || !bizId.equals(version.getBizId()))
            {
                throw new ServiceException("版本记录不属于该资料，无法生效");
            }
        }
        else
        {
            version = docVersionService.selectLatestVersion(bizType, bizId);
            if (version == null)
            {
                throw new ServiceException("资料版本记录缺失，无法生效");
            }
        }
        return version;
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

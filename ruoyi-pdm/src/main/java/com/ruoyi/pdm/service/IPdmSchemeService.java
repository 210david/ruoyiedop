package com.ruoyi.pdm.service;

import java.util.List;
import com.ruoyi.pdm.domain.PdmScheme;

/**
 * 设计方案 Service 接口
 *
 * 业务规则（设计方案 7.2）：
 * 1. 任一时刻同一产品最多只有一份"已定稿"的生效方案（生效版本机制保证）；
 * 2. 定稿动作必须关联审批依据（设计定稿），触发产品档案"已定型"状态联动；
 * 3. 已定稿方案修订自动小版本升版，状态回到"编制中"，原生效版本保持有效。
 *
 * @author ruoyi
 */
public interface IPdmSchemeService
{
    /** 查询设计方案列表（数据权限） */
    public List<PdmScheme> selectSchemeList(PdmScheme scheme);

    /** 查询方案详情（含配方明细） */
    public PdmScheme selectSchemeById(Long schemeId);

    /** 新增方案（保存即取号 + 自动创建V1.0版本记录） */
    public int insertScheme(PdmScheme scheme);

    /** 修改方案（已定稿方案修订自动升版并回到编制中） */
    public int updateScheme(PdmScheme scheme);

    /** 方案定稿（版本生效 + 产品档案定型联动） */
    public int effectiveScheme(Long schemeId, Long versionId, String effectRef);

    /** 方案升版（手工生成新版本记录） */
    public int upgradeScheme(Long schemeId, String versionDesc, boolean majorUpgrade);

    /** 批量删除（仅限编制中方案） */
    public int deleteSchemeByIds(Long[] schemeIds);

    /** 按技术变更单升版生效（依据=技术变更单号，第三批变更联动） */
    public int effectiveForChange(Long schemeId, String changeNo);

    /**
     * 停用方案（已定稿 → 已停用；生命周期终止动作，不走变更单）
     */
    public int disableScheme(Long schemeId, String remark);

    /**
     * 恢复方案（已停用 → 已定稿）
     */
    public int enableScheme(Long schemeId);
}

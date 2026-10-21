package com.ruoyi.pdm.service;

import java.util.List;
import com.ruoyi.pdm.domain.PdmDesignFile;

/**
 * 图纸技术文件 Service 接口
 *
 * 业务规则（设计方案 7.3）：
 * 1. 资料上传即纳入版本管理，同名资料重复上传生成新版本而非覆盖；
 * 2. 必须为受控状态（经审批生效）后方可对外分发，未生效文件仅设计团队内部可见；
 * 3. 已受控文件修订自动小版本升版，原受控版本保持有效直至新版本生效。
 *
 * @author ruoyi
 */
public interface IPdmDesignFileService
{
    /** 查询图纸技术文件列表 */
    public List<PdmDesignFile> selectFileList(PdmDesignFile file);

    /** 查询详情 */
    public PdmDesignFile selectFileById(Long fileId);

    /** 上传资料（保存即取号 + 自动创建V1.0版本记录） */
    public int insertFile(PdmDesignFile file);

    /** 修改资料（文件替换自动升版） */
    public int updateFile(PdmDesignFile file);

    /** 文件生效（版本生效 + 置为受控状态） */
    public int effectiveFile(Long fileId, Long versionId, String effectRef);

    /** 手工升版（生成新版本记录） */
    public int upgradeFile(Long fileId, String versionDesc, boolean majorUpgrade);

    /** 批量删除（受控文件不允许删除） */
    public int deleteFileByIds(Long[] fileIds);

    /** 按技术变更单升版生效（依据=技术变更单号，第三批变更联动） */
    public int effectiveForChange(Long fileId, String changeNo);
}

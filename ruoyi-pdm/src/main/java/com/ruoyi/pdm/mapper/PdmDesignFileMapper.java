package com.ruoyi.pdm.mapper;

import java.util.List;
import com.ruoyi.pdm.domain.PdmDesignFile;

/**
 * 图纸技术文件 Mapper 接口
 *
 * @author ruoyi
 */
public interface PdmDesignFileMapper
{
    /** 查询图纸技术文件列表 */
    public List<PdmDesignFile> selectFileList(PdmDesignFile file);

    /** 根据ID查询 */
    public PdmDesignFile selectFileById(Long fileId);

    /** 新增 */
    public int insertFile(PdmDesignFile file);

    /** 修改 */
    public int updateFile(PdmDesignFile file);

    /** 批量删除 */
    public int deleteFileByIds(Long[] fileIds);
}

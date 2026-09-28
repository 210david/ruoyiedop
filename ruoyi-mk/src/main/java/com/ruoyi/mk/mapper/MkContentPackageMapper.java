package com.ruoyi.mk.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mk.domain.MkContentPackage;

/**
 * 内容包 数据层
 *
 * @author ruoyi
 */
public interface MkContentPackageMapper
{
    public MkContentPackage selectContentPackageById(Long packageId);

    public List<MkContentPackage> selectContentPackageList(MkContentPackage contentPackage);

    public int insertContentPackage(MkContentPackage contentPackage);

    public int updateContentPackage(MkContentPackage contentPackage);

    public int deleteContentPackageByIds(Long[] packageIds);

    /** 按内容ID物理删除内容包 */
    public int deleteByContentId(@Param("contentId") Long contentId);

    /** 按内容ID查询内容包列表 */
    public List<MkContentPackage> selectByContentId(@Param("contentId") Long contentId);

    /** 按标题精确查询（推广数据导入按标题匹配内容包） */
    public List<MkContentPackage> selectByTitle(@Param("packageTitle") String packageTitle);
}

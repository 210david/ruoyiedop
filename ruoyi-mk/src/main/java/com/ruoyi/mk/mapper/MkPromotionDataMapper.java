package com.ruoyi.mk.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mk.domain.MkPromotionData;

/**
 * 推广效果数据 数据层
 *
 * @author ruoyi
 */
public interface MkPromotionDataMapper
{
    public MkPromotionData selectPromotionDataById(Long dataId);

    public List<MkPromotionData> selectPromotionDataList(MkPromotionData promotionData);

    public int insertPromotionData(MkPromotionData promotionData);

    public int updatePromotionData(MkPromotionData promotionData);

    public int deletePromotionDataByIds(Long[] dataIds);

    /** 按内容包+日期查询（每日录入去重/累加） */
    public MkPromotionData selectByPackageAndDate(@Param("packageId") Long packageId, @Param("dataDate") String dataDate);
}

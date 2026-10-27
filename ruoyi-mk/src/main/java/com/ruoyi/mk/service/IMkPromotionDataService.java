package com.ruoyi.mk.service;

import java.util.List;
import com.ruoyi.mk.domain.MkPromotionData;

/**
 * 推广数据 Service接口
 *
 * @author ruoyi
 */
public interface IMkPromotionDataService
{
    public List<MkPromotionData> selectDataList(MkPromotionData data);

    public MkPromotionData selectDataById(Long dataId);

    /** 新增/修改：按 内容包+日期 唯一键 upsert */
    public int saveData(MkPromotionData data);

    /** 批量导入：结构化结果（成功/失败明细，供 ExcelImportDialog 展示） */
    public com.ruoyi.common.core.domain.AjaxResult importData(List<MkPromotionData> dataList, boolean updateSupport, String operName);

    public int deleteDataByIds(Long[] dataIds);
}

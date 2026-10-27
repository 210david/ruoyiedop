package com.ruoyi.mk.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.domain.MkPromotionData;
import com.ruoyi.mk.domain.MkContentPackage;
import com.ruoyi.mk.mapper.MkContentPackageMapper;
import com.ruoyi.mk.mapper.MkPromotionDataMapper;
import com.ruoyi.mk.service.IMkPromotionDataService;

import java.util.List;
import java.util.Map;

/**
 * 推广数据 Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class MkPromotionDataServiceImpl implements IMkPromotionDataService
{
    @Autowired
    private MkPromotionDataMapper mkPromotionDataMapper;

    @Autowired
    private MkContentPackageMapper mkContentPackageMapper;

    @Override
    public List<MkPromotionData> selectDataList(MkPromotionData data)
    {
        return mkPromotionDataMapper.selectPromotionDataList(data);
    }

    @Override
    public MkPromotionData selectDataById(Long dataId)
    {
        return mkPromotionDataMapper.selectPromotionDataById(dataId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveData(MkPromotionData data)
    {
        if (data.getPackageId() == null || data.getDataDate() == null)
        {
            throw new ServiceException("内容包和日期不能为空");
        }
        MkPromotionData existing = mkPromotionDataMapper.selectByPackageAndDate(data.getPackageId(), DateUtils.parseDateToStr("yyyy-MM-dd", data.getDataDate()));
        if (existing != null)
        {
            data.setDataId(existing.getDataId());
            data.setUpdateBy(SecurityUtils.getUsername());
            return mkPromotionDataMapper.updatePromotionData(data);
        }
        data.setCreateBy(SecurityUtils.getUsername());
        data.setCreateTime(DateUtils.getNowDate());
        return mkPromotionDataMapper.insertPromotionData(data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public com.ruoyi.common.core.domain.AjaxResult importData(List<MkPromotionData> dataList, boolean updateSupport, String operName)
    {
        int successNum = 0;
        int failureNum = 0;
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
        List<Map<String, Object>> successList = new java.util.ArrayList<>();
        List<Map<String, Object>> failureList = new java.util.ArrayList<>();

        for (MkPromotionData row : dataList)
        {
            String errorReason = null;
            MkContentPackage pkg = null;
            try
            {
                // 内容包解析：ID 优先，其次按标题精确匹配（多条同名判定不唯一）
                if (row.getPackageId() != null)
                {
                    pkg = mkContentPackageMapper.selectContentPackageById(row.getPackageId());
                    if (pkg == null)
                    {
                        errorReason = "内容包ID " + row.getPackageId() + " 不存在";
                    }
                }
                else if (StringUtils.isNotEmpty(row.getImportPackageTitle()))
                {
                    List<MkContentPackage> matched = mkContentPackageMapper.selectByTitle(row.getImportPackageTitle().trim());
                    if (matched == null || matched.isEmpty())
                    {
                        errorReason = "内容包标题「" + row.getImportPackageTitle() + "」不存在";
                    }
                    else if (matched.size() > 1)
                    {
                        errorReason = "内容包标题「" + row.getImportPackageTitle() + "」存在多条同名记录，请改用内容包ID";
                    }
                    else
                    {
                        pkg = matched.get(0);
                    }
                }
                else
                {
                    errorReason = "内容包ID与内容包标题至少填写一项";
                }

                if (errorReason == null && row.getDataDate() == null)
                {
                    errorReason = "数据日期不能为空";
                }

                if (errorReason == null)
                {
                    String dataDateStr = dateFormat.format(row.getDataDate());
                    MkPromotionData existing = mkPromotionDataMapper.selectByPackageAndDate(pkg.getPackageId(), dataDateStr);
                    if (existing != null)
                    {
                        if (!updateSupport)
                        {
                            errorReason = "该内容包在 " + dataDateStr + " 的数据已存在（未勾选更新已存在数据）";
                        }
                        else
                        {
                            row.setDataId(existing.getDataId());
                            row.setPackageId(pkg.getPackageId());
                            row.setUpdateBy(operName);
                            mkPromotionDataMapper.updatePromotionData(row);
                            successNum++;
                            successList.add(buildSuccessItem(pkg.getPackageId(), row, dataDateStr, "更新成功"));
                        }
                    }
                    else
                    {
                        row.setPackageId(pkg.getPackageId());
                        row.setCreateBy(operName);
                        mkPromotionDataMapper.insertPromotionData(row);
                        successNum++;
                        successList.add(buildSuccessItem(pkg.getPackageId(), row, dataDateStr, "导入成功"));
                    }
                }
            }
            catch (Exception e)
            {
                errorReason = "处理异常：" + e.getMessage();
            }

            if (errorReason != null)
            {
                failureNum++;
                failureList.add(buildFailureItem(row, errorReason));
            }
        }

        com.ruoyi.common.core.domain.AjaxResult ajax = com.ruoyi.common.core.domain.AjaxResult.success();
        ajax.put("successNum", successNum);
        ajax.put("failureNum", failureNum);
        ajax.put("successList", successList);
        ajax.put("failureList", failureList);
        if (failureNum > 0 && successNum == 0)
        {
            ajax.put("code", 500);
            ajax.put("msg", "导入失败，共 " + failureNum + " 条数据异常");
        }
        else if (failureNum > 0)
        {
            ajax.put("msg", "部分导入成功：成功 " + successNum + " 条，失败 " + failureNum + " 条");
        }
        else
        {
            ajax.put("msg", "导入成功，共 " + successNum + " 条");
        }
        return ajax;
    }

    /** 成功明细项（列与导入模板对齐） */
    private Map<String, Object> buildSuccessItem(Long packageId, MkPromotionData row, String dataDateStr, String result)
    {
        Map<String, Object> item = new java.util.LinkedHashMap<>();
        item.put("packageId", packageId != null ? packageId.toString() : "");
        item.put("importPackageTitle", StringUtils.isNotEmpty(row.getImportPackageTitle()) ? row.getImportPackageTitle() : "");
        item.put("dataDate", dataDateStr);
        item.put("exposureCount", row.getExposureCount() != null ? row.getExposureCount().toString() : "0");
        item.put("readCount", row.getReadCount() != null ? row.getReadCount().toString() : "0");
        item.put("likeCount", row.getLikeCount() != null ? row.getLikeCount().toString() : "0");
        item.put("commentCount", row.getCommentCount() != null ? row.getCommentCount().toString() : "0");
        item.put("leadCount", row.getLeadCount() != null ? row.getLeadCount().toString() : "0");
        item.put("result", result);
        return item;
    }

    /** 失败明细项（列与导入模板对齐） */
    private Map<String, Object> buildFailureItem(MkPromotionData row, String errorReason)
    {
        Map<String, Object> item = new java.util.LinkedHashMap<>();
        item.put("packageId", row.getPackageId() != null ? row.getPackageId().toString() : "");
        item.put("importPackageTitle", StringUtils.isNotEmpty(row.getImportPackageTitle()) ? row.getImportPackageTitle() : "");
        item.put("dataDate", row.getDataDate() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(row.getDataDate()) : "");
        item.put("exposureCount", row.getExposureCount() != null ? row.getExposureCount().toString() : "");
        item.put("readCount", row.getReadCount() != null ? row.getReadCount().toString() : "");
        item.put("likeCount", row.getLikeCount() != null ? row.getLikeCount().toString() : "");
        item.put("commentCount", row.getCommentCount() != null ? row.getCommentCount().toString() : "");
        item.put("leadCount", row.getLeadCount() != null ? row.getLeadCount().toString() : "");
        item.put("errorReason", errorReason);
        return item;
    }

    @Override
    public int deleteDataByIds(Long[] dataIds)
    {
        return mkPromotionDataMapper.deletePromotionDataByIds(dataIds);
    }
}

package com.ruoyi.oa.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.DictUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.oa.domain.OaSeal;
import com.ruoyi.oa.mapper.OaSealMapper;
import com.ruoyi.oa.service.IOaSealService;

@Service
public class OaSealServiceImpl implements IOaSealService
{
    @Autowired
    private OaSealMapper oaSealMapper;

    @Override
    public OaSeal selectOaSealById(Long sealId) { return oaSealMapper.selectOaSealById(sealId); }

    @Override
    public List<OaSeal> selectOaSealList(OaSeal oaSeal) { return oaSealMapper.selectOaSealList(oaSeal); }

    @Override
    public AjaxResult importSeal(List<OaSeal> sealList, boolean updateSupport, String operName)
    {
        if (StringUtils.isEmpty(sealList))
        {
            throw new ServiceException("导入印章数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        List<Map<String, Object>> successList = new ArrayList<>();
        List<Map<String, Object>> failureList = new ArrayList<>();
        for (OaSeal seal : sealList)
        {
            try
            {
                if (StringUtils.isEmpty(seal.getSealName()))
                {
                    throw new ServiceException("印章名称不能为空");
                }
                if (StringUtils.isNotEmpty(seal.getSealType()) && StringUtils.isEmpty(DictUtils.getDictLabel("oa_seal_type", seal.getSealType())))
                {
                    throw new ServiceException("印章类型【" + seal.getSealType() + "】不存在");
                }
                if (StringUtils.isEmpty(seal.getStatus()))
                {
                    seal.setStatus("0");
                }
                else if (StringUtils.isEmpty(DictUtils.getDictLabel("oa_seal_status", seal.getStatus())))
                {
                    throw new ServiceException("印章状态【" + seal.getStatus() + "】不存在");
                }
                OaSeal exist = StringUtils.isNotEmpty(seal.getSealCode()) ? oaSealMapper.selectOaSealByCode(seal.getSealCode()) : null;
                if (exist != null)
                {
                    if (!updateSupport)
                    {
                        throw new ServiceException("印章编号【" + seal.getSealCode() + "】已存在");
                    }
                    seal.setSealId(exist.getSealId());
                    seal.setUpdateBy(operName);
                    oaSealMapper.updateOaSeal(seal);
                    successNum++;
                    successList.add(buildItem(seal, "更新成功"));
                }
                else
                {
                    seal.setCreateBy(operName);
                    oaSealMapper.insertOaSeal(seal);
                    successNum++;
                    successList.add(buildItem(seal, "新增成功"));
                }
            }
            catch (Exception e)
            {
                failureNum++;
                failureList.add(buildFailureItem(seal, e.getMessage() != null ? e.getMessage() : "数据格式错误"));
            }
        }
        return buildResult(successNum, failureNum, successList, failureList);
    }

    private Map<String, Object> buildItem(OaSeal seal, String result)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("sealName", seal.getSealName() != null ? seal.getSealName() : "");
        item.put("sealCode", seal.getSealCode() != null ? seal.getSealCode() : "");
        item.put("sealType", dictText("oa_seal_type", seal.getSealType()));
        item.put("keeperName", seal.getKeeperName() != null ? seal.getKeeperName() : "");
        item.put("keeperDept", seal.getKeeperDept() != null ? seal.getKeeperDept() : "");
        item.put("keeperPhone", seal.getKeeperPhone() != null ? seal.getKeeperPhone() : "");
        item.put("keepPlace", seal.getKeepPlace() != null ? seal.getKeepPlace() : "");
        item.put("enableDate", seal.getEnableDate() != null ? DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, seal.getEnableDate()) : "");
        item.put("status", dictText("oa_seal_status", seal.getStatus()));
        item.put("result", result);
        return item;
    }

    private Map<String, Object> buildFailureItem(OaSeal seal, String errorReason)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("sealName", seal.getSealName() != null ? seal.getSealName() : "");
        item.put("sealCode", seal.getSealCode() != null ? seal.getSealCode() : "");
        item.put("sealType", dictText("oa_seal_type", seal.getSealType()));
        item.put("keeperName", seal.getKeeperName() != null ? seal.getKeeperName() : "");
        item.put("keeperDept", seal.getKeeperDept() != null ? seal.getKeeperDept() : "");
        item.put("keeperPhone", seal.getKeeperPhone() != null ? seal.getKeeperPhone() : "");
        item.put("keepPlace", seal.getKeepPlace() != null ? seal.getKeepPlace() : "");
        item.put("enableDate", seal.getEnableDate() != null ? DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, seal.getEnableDate()) : "");
        item.put("status", dictText("oa_seal_status", seal.getStatus()));
        item.put("errorReason", errorReason);
        return item;
    }

    private String dictText(String dictType, String value)
    {
        if (StringUtils.isEmpty(value))
        {
            return "";
        }
        String label = DictUtils.getDictLabel(dictType, value);
        return StringUtils.isNotEmpty(label) ? label : value;
    }

    private AjaxResult buildResult(int successNum, int failureNum, List<Map<String, Object>> successList, List<Map<String, Object>> failureList)
    {
        AjaxResult ajax = AjaxResult.success();
        ajax.put("successNum", successNum);
        ajax.put("failureNum", failureNum);
        ajax.put("successList", successList);
        ajax.put("failureList", failureList);
        if (failureNum > 0)
        {
            ajax.put("msg", successNum > 0 ? "部分数据导入失败，成功 " + successNum + " 条，失败 " + failureNum + " 条" : "导入失败，共 " + failureNum + " 条数据存在错误");
        }
        else
        {
            ajax.put("msg", "导入成功，共 " + successNum + " 条数据");
        }
        return ajax;
    }

    @Override
    public int insertOaSeal(OaSeal oaSeal) { return oaSealMapper.insertOaSeal(oaSeal); }

    @Override
    public int updateOaSeal(OaSeal oaSeal) { return oaSealMapper.updateOaSeal(oaSeal); }

    @Override
    public int deleteOaSealByIds(Long[] sealIds) { return oaSealMapper.deleteOaSealByIds(sealIds); }

    @Override
    public int deleteOaSealById(Long sealId) { return oaSealMapper.deleteOaSealById(sealId); }
}

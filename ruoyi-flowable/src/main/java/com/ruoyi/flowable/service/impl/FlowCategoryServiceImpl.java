package com.ruoyi.flowable.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ModelQuery;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.domain.entity.SysDictData;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.flowable.domain.FlowCategory;
import com.ruoyi.flowable.service.IFlowCategoryService;
import com.ruoyi.system.service.ISysDictDataService;

/**
 * 流程分类 Service 实现（基于字典 flowable_category，含引用占用校验）
 *
 * @author ruoyi
 */
@Service
public class FlowCategoryServiceImpl implements IFlowCategoryService
{
    /** 流程分类字典类型 */
    public static final String DICT_TYPE = "flowable_category";

    @Autowired
    private ISysDictDataService dictDataService;

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public List<FlowCategory> selectFlowCategoryList(FlowCategory query)
    {
        SysDictData cond = new SysDictData();
        cond.setDictType(DICT_TYPE);
        if (query != null)
        {
            if (StringUtils.isNotEmpty(query.getDictLabel()))
            {
                cond.setDictLabel(query.getDictLabel());
            }
            if (StringUtils.isNotEmpty(query.getRemark()))
            {
                cond.setRemark(query.getRemark());
            }
        }
        List<SysDictData> dictList = dictDataService.selectDictDataList(cond);
        // 引用统计：流程模型 + 已部署流程定义
        Map<String, Long> modelCount = countModelByCategory();
        Map<String, Long> defCount = countDefinitionByCategory();
        List<FlowCategory> result = new ArrayList<>();
        for (SysDictData d : dictList)
        {
            FlowCategory c = new FlowCategory();
            c.setDictCode(d.getDictCode());
            c.setDictSort(d.getDictSort());
            c.setDictLabel(d.getDictLabel());
            c.setDictValue(d.getDictValue());
            c.setDictType(DICT_TYPE);
            c.setIcon(d.getCssClass());
            c.setListClass(d.getListClass());
            c.setIsDefault(d.getIsDefault());
            c.setStatus(d.getStatus());
            c.setCreateBy(d.getCreateBy());
            c.setCreateTime(d.getCreateTime());
            c.setUpdateBy(d.getUpdateBy());
            c.setUpdateTime(d.getUpdateTime());
            c.setRemark(d.getRemark());
            c.setModelCount(modelCount.getOrDefault(d.getDictValue(), 0L));
            c.setDefinitionCount(defCount.getOrDefault(d.getDictValue(), 0L));
            result.add(c);
        }
        return result;
    }

    private Map<String, Long> countModelByCategory()
    {
        Map<String, Long> counts = new HashMap<>();
        for (Model m : repositoryService.createModelQuery().list())
        {
            String category = m.getCategory();
            if (StringUtils.isEmpty(category))
            {
                continue;
            }
            counts.merge(category, 1L, Long::sum);
        }
        return counts;
    }

    private Map<String, Long> countDefinitionByCategory()
    {
        Map<String, Long> counts = new HashMap<>();
        for (ProcessDefinition d : repositoryService.createProcessDefinitionQuery().list())
        {
            String category = d.getCategory();
            if (StringUtils.isEmpty(category))
            {
                continue;
            }
            counts.merge(category, 1L, Long::sum);
        }
        return counts;
    }

    @Override
    public FlowCategory selectFlowCategoryByCode(Long dictCode)
    {
        SysDictData d = dictDataService.selectDictDataById(dictCode);
        if (d == null || !DICT_TYPE.equals(d.getDictType()))
        {
            throw new ServiceException("流程分类不存在");
        }
        FlowCategory c = new FlowCategory();
        c.setDictCode(d.getDictCode());
        c.setDictSort(d.getDictSort());
        c.setDictLabel(d.getDictLabel());
        c.setDictValue(d.getDictValue());
        c.setDictType(DICT_TYPE);
        c.setIcon(d.getCssClass());
        c.setListClass(d.getListClass());
        c.setIsDefault(d.getIsDefault());
        c.setStatus(d.getStatus());
        c.setCreateBy(d.getCreateBy());
        c.setCreateTime(d.getCreateTime());
        c.setUpdateBy(d.getUpdateBy());
        c.setUpdateTime(d.getUpdateTime());
        c.setRemark(d.getRemark());
        return c;
    }

    @Override
    public int insertFlowCategory(FlowCategory category)
    {
        validate(category, true);
        SysDictData d = toDictData(category);
        d.setDictType(DICT_TYPE);
        // 分类Key对用户隐藏，按 cat + 自增序号 自动生成，保证唯一且不影响模型引用
        d.setDictValue(nextCategoryValue());
        int rows = dictDataService.insertDictData(d);
        category.setDictCode(d.getDictCode());
        return rows;
    }

    @Override
    public int updateFlowCategory(FlowCategory category)
    {
        if (category.getDictCode() == null)
        {
            throw new ServiceException("分类编号不能为空");
        }
        SysDictData old = dictDataService.selectDictDataById(category.getDictCode());
        if (old == null || !DICT_TYPE.equals(old.getDictType()))
        {
            throw new ServiceException("流程分类不存在");
        }
        validate(category, false);
        // dictValue 变更时同步改写引用（流程模型 / 已部署定义的 category）
        if (StringUtils.isNotEmpty(category.getDictValue())
                && !category.getDictValue().equals(old.getDictValue()))
        {
            String oldVal = old.getDictValue();
            String newVal = category.getDictValue();
            for (Model m : repositoryService.createModelQuery().list())
            {
                if (oldVal.equals(m.getCategory()))
                {
                    m.setCategory(newVal);
                    repositoryService.saveModel(m);
                }
            }
            for (ProcessDefinition d : repositoryService.createProcessDefinitionQuery().list())
            {
                if (oldVal.equals(d.getCategory()))
                {
                    // Flowable 定义 category 部署后不可改，仅提示
                    throw new ServiceException("分类Key[" + oldVal + "]已被已部署流程定义引用，不能修改Key，仅可修改名称");
                }
            }
        }
        SysDictData d = toDictData(category);
        d.setDictCode(category.getDictCode());
        d.setDictType(DICT_TYPE);
        return dictDataService.updateDictData(d);
    }

    @Override
    public int deleteFlowCategoryByCodes(Long[] dictCodes)
    {
        for (Long code : dictCodes)
        {
            SysDictData d = dictDataService.selectDictDataById(code);
            if (d == null || !DICT_TYPE.equals(d.getDictType()))
            {
                continue;
            }
            String value = d.getDictValue();
            long modelRefs = repositoryService.createModelQuery().list().stream()
                    .filter(m -> value.equals(m.getCategory())).count();
            long defRefs = repositoryService.createProcessDefinitionQuery().list().stream()
                    .filter(p -> value.equals(p.getCategory())).count();
            if (modelRefs > 0 || defRefs > 0)
            {
                throw new ServiceException("分类[" + d.getDictLabel() + "]已被 "
                        + (modelRefs + defRefs) + " 个流程引用，不能删除");
            }
        }
        dictDataService.deleteDictDataByIds(dictCodes);
        return dictCodes.length;
    }

    private void validate(FlowCategory category, boolean isInsert)
    {
        if (StringUtils.isEmpty(category.getDictLabel()))
        {
            throw new ServiceException("分类名称不能为空");
        }
        if (isInsert && !dictDataService.checkDictLabelUnique(toDictData(category)))
        {
            throw new ServiceException("分类名称已存在：" + category.getDictLabel());
        }
    }

    /** 生成下一个分类Key：cat{n}，取现有 cat 序号最大值 +1 */
    private String nextCategoryValue()
    {
        int max = 0;
        for (SysDictData d : dictDataService.selectDictDataList(newSysDictDataCond()))
        {
            String v = d.getDictValue();
            if (v != null && v.startsWith("cat"))
            {
                try
                {
                    max = Math.max(max, Integer.parseInt(v.substring(3)));
                }
                catch (NumberFormatException ignored)
                {
                }
            }
        }
        return "cat" + (max + 1);
    }

    private SysDictData newSysDictDataCond()
    {
        SysDictData cond = new SysDictData();
        cond.setDictType(DICT_TYPE);
        return cond;
    }

    private SysDictData toDictData(FlowCategory category)
    {
        SysDictData d = new SysDictData();
        d.setDictCode(category.getDictCode());
        d.setDictSort(category.getDictSort());
        d.setDictLabel(category.getDictLabel());
        d.setDictValue(category.getDictValue());
        d.setDictType(DICT_TYPE);
        d.setCssClass(category.getIcon());
        d.setListClass(category.getListClass());
        d.setIsDefault(category.getIsDefault());
        d.setStatus(category.getStatus());
        d.setRemark(category.getRemark());
        return d;
    }
}

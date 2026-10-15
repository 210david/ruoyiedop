package com.ruoyi.food.mapper;

import java.util.List;
import com.ruoyi.food.domain.FoodCcpTemplate;

/**
 * HACCP CCP控制点模板 Mapper接口
 *
 * @author ruoyi
 */
public interface FoodCcpTemplateMapper
{
    public List<FoodCcpTemplate> selectCcpTemplateList(FoodCcpTemplate template);

    public FoodCcpTemplate selectCcpTemplateById(Long templateId);

    public int insertCcpTemplate(FoodCcpTemplate template);

    public int updateCcpTemplate(FoodCcpTemplate template);

    public int deleteCcpTemplateByIds(Long[] templateIds);
}

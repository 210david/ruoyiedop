package com.ruoyi.qms.mapper;

import java.util.List;
import com.ruoyi.qms.domain.QmsIndustryTpl;

/**
 * 行业模板配置 Mapper
 *
 * @author ruoyi
 */
public interface QmsIndustryTplMapper
{
    public List<QmsIndustryTpl> selectIndustryTplList(QmsIndustryTpl industryTpl);
    public QmsIndustryTpl selectIndustryTplById(Long tplId);
    public QmsIndustryTpl selectIndustryTplByCode(String tplCode);
    public int insertIndustryTpl(QmsIndustryTpl industryTpl);
    public int updateIndustryTpl(QmsIndustryTpl industryTpl);
    public int deleteIndustryTplByIds(Long[] tplIds);
}

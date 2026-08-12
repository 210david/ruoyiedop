package com.ruoyi.qms.service;

import java.util.List;
import com.ruoyi.qms.domain.QmsIndustryTpl;

/**
 * 行业模板配置 Service接口
 *
 * @author ruoyi
 */
public interface IQmsIndustryTplService
{
    public List<QmsIndustryTpl> selectIndustryTplList(QmsIndustryTpl industryTpl);
    public QmsIndustryTpl selectIndustryTplById(Long tplId);
    public int insertIndustryTpl(QmsIndustryTpl industryTpl);
    public int updateIndustryTpl(QmsIndustryTpl industryTpl);
    public int deleteIndustryTplByIds(Long[] tplIds);
}

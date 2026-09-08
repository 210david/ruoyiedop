package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.hr.domain.HrScheduleTemplate;
import com.ruoyi.hr.mapper.HrScheduleTemplateMapper;
import com.ruoyi.hr.service.IHrScheduleTemplateService;
import com.ruoyi.mk.service.IMkNumberRuleService;

@Service
public class HrScheduleTemplateServiceImpl implements IHrScheduleTemplateService
{
    @Autowired
    private HrScheduleTemplateMapper hrScheduleTemplateMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public HrScheduleTemplate selectHrScheduleTemplateById(Long templateId) { return hrScheduleTemplateMapper.selectHrScheduleTemplateById(templateId); }

    @Override
    public List<HrScheduleTemplate> selectHrScheduleTemplateList(HrScheduleTemplate template) { return hrScheduleTemplateMapper.selectHrScheduleTemplateList(template); }

    @Override
    public int insertHrScheduleTemplate(HrScheduleTemplate template)
    {
        if (StringUtils.isEmpty(template.getTemplateCode()))
        {
            template.setTemplateCode(mkNumberRuleService.generateNumber("hr_schedule_template"));
        }
        if (StringUtils.isEmpty(template.getStatus()))
        {
            template.setStatus("0");
        }
        return hrScheduleTemplateMapper.insertHrScheduleTemplate(template);
    }

    @Override
    public int updateHrScheduleTemplate(HrScheduleTemplate template) { return hrScheduleTemplateMapper.updateHrScheduleTemplate(template); }

    @Override
    public int deleteHrScheduleTemplateByIds(Long[] templateIds) { return hrScheduleTemplateMapper.deleteHrScheduleTemplateByIds(templateIds); }

    @Override
    public int deleteHrScheduleTemplateById(Long templateId) { return hrScheduleTemplateMapper.deleteHrScheduleTemplateById(templateId); }
}

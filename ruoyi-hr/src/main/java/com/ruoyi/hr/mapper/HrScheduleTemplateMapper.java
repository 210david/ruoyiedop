package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrScheduleTemplate;

public interface HrScheduleTemplateMapper {
    public HrScheduleTemplate selectHrScheduleTemplateById(Long templateId);
    public List<HrScheduleTemplate> selectHrScheduleTemplateList(HrScheduleTemplate template);
    public int insertHrScheduleTemplate(HrScheduleTemplate template);
    public int updateHrScheduleTemplate(HrScheduleTemplate template);
    public int deleteHrScheduleTemplateByIds(Long[] templateIds);
    public int deleteHrScheduleTemplateById(Long templateId);
}

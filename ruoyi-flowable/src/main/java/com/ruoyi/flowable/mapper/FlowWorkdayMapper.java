package com.ruoyi.flowable.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 企业工作日历查询 Mapper（表单规则引擎 workDays 函数专用）
 *
 * 复用 HR 模块的企业运营日历表 biz_hr_workday_calendar（0=工作日 1=休息日 2=法定节假日），
 * 流程模块不依赖 ruoyi-hr，这里只做只读区间查询
 */
public interface FlowWorkdayMapper
{
    /** 查询日期区间内的日历标记 [{dt: 'yyyy-MM-dd', dateType: '0|1|2'}] */
    public List<Map<String, Object>> selectDateTypeRange(@Param("startDate") String startDate, @Param("endDate") String endDate);
}

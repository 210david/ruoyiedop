package com.ruoyi.oa.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 资源日历查询单元测试
 *
 * 重点回归：日期区间参数必须按「区间结束时间、区间开始时间」传入 SQL 的
 * `depart_time <= ? AND return_time >= ?` 两个占位符，防止再次出现参数顺序颠倒导致查询结果为空。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("资源日历查询 — 日期参数顺序与结果组装")
class OaResourceCalendarServiceTest
{
    private static final String START_DATE = "2026-11-01";
    private static final String END_DATE = "2026-11-30";
    private static final String SQL_START = "2026-11-01 00:00:00";
    private static final String SQL_END = "2026-11-30 23:59:59";

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private OaResourceCalendarService service;

    @Test
    @DisplayName("车辆日历：SQL参数为「结束时间, 开始时间」，防止区间条件颠倒")
    void vehiclePassesEndBeforeStart()
    {
        service.selectCalendar("vehicle", START_DATE, END_DATE);

        verify(jdbcTemplate).queryForList(
                contains("FROM biz_oa_vehicle"),
                eq(SQL_END),
                eq(SQL_START));
    }

    @Test
    @DisplayName("司机日历：SQL参数为「结束时间, 开始时间」")
    void driverPassesEndBeforeStart()
    {
        service.selectCalendar("driver", START_DATE, END_DATE);

        verify(jdbcTemplate).queryForList(
                contains("FROM biz_oa_driver"),
                eq(SQL_END),
                eq(SQL_START));
    }

    @Test
    @DisplayName("印章日历：SQL参数为「结束时间, 开始时间」")
    void sealPassesEndBeforeStart()
    {
        service.selectCalendar("seal", START_DATE, END_DATE);

        verify(jdbcTemplate).queryForList(
                contains("FROM biz_oa_seal"),
                eq(SQL_END),
                eq(SQL_START));
    }

    @Test
    @DisplayName("结果组装：同一资源多条占用只保留一行资源，占用记录全部返回")
    void assemblesResourcesAndUsages()
    {
        Map<String, Object> usage1 = usageRow(3, "京A12345", "inst-1", "客户拜访用车");
        Map<String, Object> usage2 = usageRow(3, "京A12345", "inst-2", "机场接送用车");
        Map<String, Object> idleResource = resourceRow(4, "京B10001");
        when(jdbcTemplate.queryForList(anyString(), anyString(), anyString()))
                .thenReturn(Arrays.asList(usage1, usage2, idleResource));

        Map<String, Object> result = service.selectCalendar("vehicle", START_DATE, END_DATE);

        List<?> resources = (List<?>) result.get("resources");
        List<?> usages = (List<?>) result.get("usages");
        assertEquals(2, resources.size(), "同一资源应去重为一行");
        assertEquals(2, usages.size(), "两条占用记录均应返回");
    }

    @Test
    @DisplayName("未知资源类型：不查询数据库，返回空集合")
    void unknownTypeDoesNotQueryDatabase()
    {
        Map<String, Object> result = service.selectCalendar("meetingRoom", START_DATE, END_DATE);

        verifyNoInteractions(jdbcTemplate);
        assertTrue(((List<?>) result.get("resources")).isEmpty());
        assertTrue(((List<?>) result.get("usages")).isEmpty());
    }

    @Test
    @DisplayName("数据库异常：捕获后返回空集合，不向上抛出")
    void databaseExceptionReturnsEmptyResult()
    {
        when(jdbcTemplate.queryForList(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("db down"));

        Map<String, Object> result = service.selectCalendar("vehicle", START_DATE, END_DATE);

        assertTrue(((List<?>) result.get("resources")).isEmpty());
        assertTrue(((List<?>) result.get("usages")).isEmpty());
    }

    /** 构造一条占用记录 */
    private Map<String, Object> usageRow(Integer resourceId, String resourceName, String instanceId, String title)
    {
        Map<String, Object> row = resourceRow(resourceId, resourceName);
        row.put("flowInstanceId", instanceId);
        row.put("flowTitle", title);
        row.put("applicant", "测试用户");
        row.put("startTime", "2026-11-10 09:00:00");
        row.put("endTime", "2026-11-12 18:00:00");
        row.put("procStatus", "1");
        return row;
    }

    /** 构造一条无占用（LEFT JOIN 空值）的资源记录 */
    private Map<String, Object> resourceRow(Integer resourceId, String resourceName)
    {
        Map<String, Object> row = new HashMap<>();
        row.put("resourceId", resourceId);
        row.put("resourceName", resourceName);
        row.put("resourceStatus", "0");
        row.put("resourceType", "1");
        row.put("flowInstanceId", null);
        row.put("flowTitle", null);
        row.put("applicant", null);
        row.put("startTime", null);
        row.put("endTime", null);
        row.put("procStatus", null);
        return row;
    }
}

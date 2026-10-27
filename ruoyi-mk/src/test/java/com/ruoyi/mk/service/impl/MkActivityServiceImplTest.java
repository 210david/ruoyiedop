package com.ruoyi.mk.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mk.domain.MkActivity;
import com.ruoyi.mk.mapper.MkActivityMapper;
import com.ruoyi.mk.service.IMkNumberRuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 市场活动时间范围校验单元测试
 * 规则：结束时间必须晚于开始时间（严格大于，相等亦拦截）
 */
@DisplayName("市场活动 — 结束时间必须晚于开始时间")
class MkActivityServiceImplTest
{
    private static final String MSG = "结束时间必须晚于开始时间";

    private MkActivityServiceImpl service;
    private MkActivityMapper activityMapper;
    private IMkNumberRuleService numberRuleService;

    @BeforeEach
    void setUp() throws Exception
    {
        service = new MkActivityServiceImpl();
        activityMapper = Mockito.mock(MkActivityMapper.class);
        numberRuleService = Mockito.mock(IMkNumberRuleService.class);
        setField("mkActivityMapper", activityMapper);
        setField("mkNumberRuleService", numberRuleService);
    }

    private void setField(String name, Object value) throws Exception
    {
        Field f = MkActivityServiceImpl.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(service, value);
    }

    private Date ts(String datetime) throws Exception
    {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(datetime);
    }

    private MkActivity build(String start, String end) throws Exception
    {
        MkActivity activity = new MkActivity();
        activity.setActivityName("渠道交流会");
        activity.setActivityNo("ACT001");
        activity.setStartTime(start == null ? null : ts(start));
        activity.setEndTime(end == null ? null : ts(end));
        return activity;
    }

    @Test
    @DisplayName("新增：结束晚于开始 → 保存成功")
    void insertValidRange() throws Exception
    {
        when(activityMapper.insertActivity(any(MkActivity.class))).thenReturn(1);
        assertEquals(1, service.insertActivity(build("2026-09-12 09:00:00", "2026-09-12 18:00:00")));
        verify(activityMapper).insertActivity(any(MkActivity.class));
    }

    @Test
    @DisplayName("新增：结束等于开始 → 拦截")
    void insertEqualRange() throws Exception
    {
        ServiceException ex = assertThrows(ServiceException.class, () -> service.insertActivity(build("2026-09-12 09:00:00", "2026-09-12 09:00:00")));
        assertEquals(MSG, ex.getMessage());
        verify(activityMapper, never()).insertActivity(any(MkActivity.class));
    }

    @Test
    @DisplayName("新增：结束早于开始 → 拦截")
    void insertReversedRange() throws Exception
    {
        ServiceException ex = assertThrows(ServiceException.class, () -> service.insertActivity(build("2026-09-12 18:00:00", "2026-09-12 09:00:00")));
        assertEquals(MSG, ex.getMessage());
        verify(activityMapper, never()).insertActivity(any(MkActivity.class));
    }

    @Test
    @DisplayName("新增：起止为空 → 不触发时间校验")
    void insertMissingTimes() throws Exception
    {
        when(activityMapper.insertActivity(any(MkActivity.class))).thenReturn(1);
        assertEquals(1, service.insertActivity(build(null, null)));
        verify(activityMapper).insertActivity(any(MkActivity.class));
    }

    @Test
    @DisplayName("修改：结束早于开始 → 拦截")
    void updateReversedRange() throws Exception
    {
        MkActivity activity = build("2026-09-12 18:00:00", "2026-09-12 09:00:00");
        activity.setActivityId(1L);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.updateActivity(activity));
        assertEquals(MSG, ex.getMessage());
        verify(activityMapper, never()).updateActivity(any(MkActivity.class));
    }

    @Test
    @DisplayName("修改：合法范围 → 保存成功")
    void updateValidRange() throws Exception
    {
        MkActivity activity = build("2026-09-12 09:00:00", "2026-09-12 18:00:00");
        activity.setActivityId(1L);
        when(activityMapper.updateActivity(any(MkActivity.class))).thenReturn(1);
        assertEquals(1, service.updateActivity(activity));
        verify(activityMapper).updateActivity(any(MkActivity.class));
    }
}

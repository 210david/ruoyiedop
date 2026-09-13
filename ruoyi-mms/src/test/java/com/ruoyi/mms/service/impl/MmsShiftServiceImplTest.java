package com.ruoyi.mms.service.impl;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.mms.domain.MmsShift;
import com.ruoyi.mms.mapper.MmsShiftMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 班次管理时间范围校验单元测试
 * 规则：结束时间必须晚于开始时间（严格大于，相等亦拦截）
 */
@DisplayName("班次管理 — 结束时间必须晚于开始时间")
class MmsShiftServiceImplTest
{
    private MmsShiftServiceImpl service;
    private MmsShiftMapper shiftMapper;
    private IMkNumberRuleService numberRuleService;

    @BeforeEach
    void setUp() throws Exception
    {
        service = new MmsShiftServiceImpl();
        shiftMapper = mock(MmsShiftMapper.class);
        numberRuleService = mock(IMkNumberRuleService.class);
        setField("shiftMapper", shiftMapper);
        setField("mkNumberRuleService", numberRuleService);
        // 模拟登录用户（SecurityUtils.getUsername 依赖）
        SysUser user = new SysUser();
        user.setUserName("admin");
        LoginUser loginUser = new LoginUser();
        loginUser.setUser(user);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(loginUser, null));
    }

    @AfterEach
    void tearDown()
    {
        SecurityContextHolder.clearContext();
    }

    private void setField(String name, Object value) throws Exception
    {
        Field f = MmsShiftServiceImpl.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(service, value);
    }

    private MmsShift build(String name, String start, String end)
    {
        MmsShift s = new MmsShift();
        s.setShiftName(name);
        s.setShiftCode("BC001");
        s.setStartTime(start);
        s.setEndTime(end);
        return s;
    }

    @Test
    @DisplayName("新增：结束时间晚于开始时间 → 保存成功")
    void insertValidRange()
    {
        when(shiftMapper.selectShiftList(any(MmsShift.class))).thenReturn(Collections.emptyList());
        when(shiftMapper.insertShift(any(MmsShift.class))).thenReturn(1);
        assertEquals(1, service.insertShift(build("白班", "08:00", "17:00")));
        verify(shiftMapper).insertShift(any(MmsShift.class));
    }

    @Test
    @DisplayName("新增：结束时间等于开始时间 → 拦截")
    void insertEqualRange()
    {
        ServiceException ex = assertThrows(ServiceException.class, () -> service.insertShift(build("白班", "08:00", "08:00")));
        assertEquals("结束时间必须晚于开始时间", ex.getMessage());
        verify(shiftMapper, never()).insertShift(any(MmsShift.class));
    }

    @Test
    @DisplayName("新增：结束时间早于开始时间 → 拦截")
    void insertReversedRange()
    {
        ServiceException ex = assertThrows(ServiceException.class, () -> service.insertShift(build("夜班", "22:00", "06:00")));
        assertEquals("结束时间必须晚于开始时间", ex.getMessage());
        verify(shiftMapper, never()).insertShift(any(MmsShift.class));
    }

    @Test
    @DisplayName("新增：起止时间为空 → 不触发时间校验")
    void insertMissingTimes()
    {
        when(shiftMapper.selectShiftList(any(MmsShift.class))).thenReturn(Collections.emptyList());
        when(shiftMapper.insertShift(any(MmsShift.class))).thenReturn(1);
        assertEquals(1, service.insertShift(build("机动班", null, null)));
        verify(shiftMapper).insertShift(any(MmsShift.class));
    }

    @Test
    @DisplayName("修改：结束时间早于开始时间 → 拦截")
    void updateReversedRange()
    {
        MmsShift s = build("白班", "10:00", "09:00");
        s.setShiftId(1L);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.updateShift(s));
        assertEquals("结束时间必须晚于开始时间", ex.getMessage());
        verify(shiftMapper, never()).updateShift(any(MmsShift.class));
    }

    @Test
    @DisplayName("修改：合法范围 → 保存成功")
    void updateValidRange()
    {
        when(shiftMapper.selectShiftList(any(MmsShift.class))).thenReturn(Collections.emptyList());
        when(shiftMapper.updateShift(any(MmsShift.class))).thenReturn(1);
        MmsShift s = build("白班", "08:00", "17:00");
        s.setShiftId(1L);
        assertEquals(1, service.updateShift(s));
        verify(shiftMapper).updateShift(any(MmsShift.class));
    }

    @Test
    @DisplayName("自动计算：未填可用工时，08:00-17:00 休息45分钟 → 8.25")
    void computeAvailHoursWithRest()
    {
        when(shiftMapper.selectShiftList(any(MmsShift.class))).thenReturn(Collections.emptyList());
        when(shiftMapper.insertShift(any(MmsShift.class))).thenReturn(1);
        MmsShift s = build("白班", "08:00", "17:00");
        s.setRestMinutes(45);
        service.insertShift(s);
        assertEquals(0, s.getAvailHours().compareTo(new java.math.BigDecimal("8.25")));
    }

    @Test
    @DisplayName("自动计算：未填可用工时且无休息 → 按全天时长9小时")
    void computeAvailHoursNoRest()
    {
        when(shiftMapper.selectShiftList(any(MmsShift.class))).thenReturn(Collections.emptyList());
        when(shiftMapper.insertShift(any(MmsShift.class))).thenReturn(1);
        MmsShift s = build("白班", "08:00", "17:00");
        service.insertShift(s);
        assertEquals(0, s.getAvailHours().compareTo(new java.math.BigDecimal("9")));
    }

    @Test
    @DisplayName("自动计算：手动填写可用工时 → 保留不覆盖")
    void keepManualAvailHours()
    {
        when(shiftMapper.selectShiftList(any(MmsShift.class))).thenReturn(Collections.emptyList());
        when(shiftMapper.insertShift(any(MmsShift.class))).thenReturn(1);
        MmsShift s = build("白班", "08:00", "17:00");
        s.setAvailHours(new java.math.BigDecimal("7.5"));
        s.setRestMinutes(45);
        service.insertShift(s);
        assertEquals(0, s.getAvailHours().compareTo(new java.math.BigDecimal("7.5")));
    }

    @Test
    @DisplayName("自动计算：休息超过班次时长 → 不产生负值，保持为空")
    void noNegativeAvailHours()
    {
        when(shiftMapper.selectShiftList(any(MmsShift.class))).thenReturn(Collections.emptyList());
        when(shiftMapper.insertShift(any(MmsShift.class))).thenReturn(1);
        MmsShift s = build("白班", "08:00", "12:00");
        s.setRestMinutes(300);
        service.insertShift(s);
        assertNull(s.getAvailHours());
    }
}

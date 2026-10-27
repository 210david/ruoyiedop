package com.ruoyi.hr.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.hr.domain.HrShift;
import com.ruoyi.hr.mapper.HrShiftMapper;
import com.ruoyi.mk.service.IMkNumberRuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * HR班次时间范围校验单元测试
 * 规则：跨零点班次（结束时间早于开始时间）视为次日，合法；
 * 仅禁止结束时间与开始时间相同（时长为0）
 * 校验与前端 computeShiftFields 的 00:00→1440 归一化规则保持一致
 */
@DisplayName("HR班次 — 跨零点合法，禁止结束时间等于开始时间")
class HrShiftServiceImplTest
{
    private static final String MSG = "结束时间不能等于开始时间（跨零点班次结束时间不可与开始时间相同）";

    private HrShiftServiceImpl service;
    private HrShiftMapper hrShiftMapper;
    private IMkNumberRuleService numberRuleService;

    @BeforeEach
    void setUp() throws Exception
    {
        service = new HrShiftServiceImpl();
        hrShiftMapper = mock(HrShiftMapper.class);
        numberRuleService = mock(IMkNumberRuleService.class);
        setField("hrShiftMapper", hrShiftMapper);
        setField("mkNumberRuleService", numberRuleService);
    }

    private void setField(String name, Object value) throws Exception
    {
        Field f = HrShiftServiceImpl.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(service, value);
    }

    private HrShift build(String name, String start, String end)
    {
        HrShift shift = new HrShift();
        shift.setShiftName(name);
        shift.setShiftCode("SC001");
        shift.setStartTime(start);
        shift.setEndTime(end);
        return shift;
    }

    @Test
    @DisplayName("新增：白天班次 08:00-17:00 → 保存成功，时长9小时，不跨零点")
    void insertValidDayShift()
    {
        when(hrShiftMapper.insertHrShift(any(HrShift.class))).thenReturn(1);
        HrShift shift = build("白班", "08:00", "17:00");
        assertEquals(1, service.insertHrShift(shift));
        verify(hrShiftMapper).insertHrShift(any(HrShift.class));
        assertEquals(Integer.valueOf(0), shift.getCrossMidnight());
        assertEquals(0, shift.getWorkHours().compareTo(new BigDecimal("9")));
    }

    @Test
    @DisplayName("新增：结束等于开始 08:00-08:00 → 拦截")
    void insertEqualTimes()
    {
        ServiceException ex = assertThrows(ServiceException.class, () -> service.insertHrShift(build("异常班", "08:00", "08:00")));
        assertEquals(MSG, ex.getMessage());
        verify(hrShiftMapper, never()).insertHrShift(any(HrShift.class));
    }

    @Test
    @DisplayName("新增：跨零点班次 22:00-06:00 → 合法，时长8小时")
    void insertCrossMidnightShift()
    {
        when(hrShiftMapper.insertHrShift(any(HrShift.class))).thenReturn(1);
        HrShift shift = build("夜班", "22:00", "06:00");
        assertEquals(1, service.insertHrShift(shift));
        verify(hrShiftMapper).insertHrShift(any(HrShift.class));
        assertEquals(Integer.valueOf(1), shift.getCrossMidnight());
        assertEquals(0, shift.getWorkHours().compareTo(new BigDecimal("8")));
    }

    @Test
    @DisplayName("新增：结束为00:00（22:00-00:00）→ 合法，按1440归一化，时长2小时")
    void insertEndAtMidnight()
    {
        when(hrShiftMapper.insertHrShift(any(HrShift.class))).thenReturn(1);
        HrShift shift = build("大夜班", "22:00", "00:00");
        assertEquals(1, service.insertHrShift(shift));
        verify(hrShiftMapper).insertHrShift(any(HrShift.class));
        assertEquals(0, shift.getWorkHours().compareTo(new BigDecimal("2")));
    }

    @Test
    @DisplayName("新增：起止为空 → 不触发时间校验，不计算时长")
    void insertMissingTimes()
    {
        when(hrShiftMapper.insertHrShift(any(HrShift.class))).thenReturn(1);
        HrShift shift = build("机动班", null, null);
        assertEquals(1, service.insertHrShift(shift));
        verify(hrShiftMapper).insertHrShift(any(HrShift.class));
        assertNull(shift.getWorkHours());
    }

    @Test
    @DisplayName("修改：结束等于开始 → 拦截")
    void updateEqualTimes()
    {
        HrShift shift = build("异常班", "12:00", "12:00");
        shift.setShiftId(1L);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.updateHrShift(shift));
        assertEquals(MSG, ex.getMessage());
        verify(hrShiftMapper, never()).updateHrShift(any(HrShift.class));
    }

    @Test
    @DisplayName("修改：跨零点班次 20:00-04:00 → 保存成功，时长8小时")
    void updateCrossMidnightShift()
    {
        when(hrShiftMapper.updateHrShift(any(HrShift.class))).thenReturn(1);
        HrShift shift = build("夜班", "20:00", "04:00");
        shift.setShiftId(1L);
        assertEquals(1, service.updateHrShift(shift));
        verify(hrShiftMapper).updateHrShift(any(HrShift.class));
        assertEquals(Integer.valueOf(1), shift.getCrossMidnight());
        assertEquals(0, shift.getWorkHours().compareTo(new BigDecimal("8")));
    }
}

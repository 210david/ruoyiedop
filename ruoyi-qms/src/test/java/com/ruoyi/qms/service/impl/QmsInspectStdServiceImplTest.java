package com.ruoyi.qms.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.qms.domain.QmsInspectStd;
import com.ruoyi.qms.mapper.QmsInspectStdMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 检验标准库校验单元测试
 * 覆盖：关键业务字段必填（检验项目/检验方法/计量单位/判定规则）；
 * 判定规则驱动的条件必填（区间判定上下限、单值判定目标值）；
 * 标准上限不能小于标准下限（相等允许）
 */
@DisplayName("检验标准库 — 必填校验与上下限关系校验")
class QmsInspectStdServiceImplTest
{
    private QmsInspectStdServiceImpl service;
    private QmsInspectStdMapper stdMapper;
    private IMkNumberRuleService numberRuleService;

    @BeforeEach
    void setUp() throws Exception
    {
        service = new QmsInspectStdServiceImpl();
        stdMapper = mock(QmsInspectStdMapper.class);
        numberRuleService = mock(IMkNumberRuleService.class);
        setField("qmsInspectStdMapper", stdMapper);
        setField("mkNumberRuleService", numberRuleService);
    }

    private void setField(String name, Object value) throws Exception
    {
        Field f = QmsInspectStdServiceImpl.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(service, value);
    }

    private QmsInspectStd build()
    {
        QmsInspectStd std = new QmsInspectStd();
        std.setStdName("外径");
        std.setInspectMethod("卡尺测量");
        std.setUnit("mm");
        std.setJudgeRule("1");
        std.setValMin(new BigDecimal("9.90"));
        std.setValMax(new BigDecimal("10.10"));
        return std;
    }

    @Test
    @DisplayName("新增：合法区间判定 → 保存成功")
    void insertValidRangeStd()
    {
        when(stdMapper.insertInspectStd(any(QmsInspectStd.class))).thenReturn(1);
        assertEquals(1, service.insertInspectStd(build()));
        verify(stdMapper).insertInspectStd(any(QmsInspectStd.class));
    }

    @Test
    @DisplayName("新增：缺少检验方法 → 拦截")
    void insertMissingInspectMethod()
    {
        QmsInspectStd std = build();
        std.setInspectMethod(null);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.insertInspectStd(std));
        assertEquals("检验方法不能为空", ex.getMessage());
        verify(stdMapper, never()).insertInspectStd(any(QmsInspectStd.class));
    }

    @Test
    @DisplayName("新增：缺少计量单位 → 拦截")
    void insertMissingUnit()
    {
        QmsInspectStd std = build();
        std.setUnit(null);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.insertInspectStd(std));
        assertEquals("计量单位不能为空", ex.getMessage());
        verify(stdMapper, never()).insertInspectStd(any(QmsInspectStd.class));
    }

    @Test
    @DisplayName("新增：区间判定缺少标准上限 → 拦截")
    void insertRangeMissingValMax()
    {
        QmsInspectStd std = build();
        std.setValMax(null);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.insertInspectStd(std));
        assertEquals("区间判定必须填写标准下限和标准上限", ex.getMessage());
        verify(stdMapper, never()).insertInspectStd(any(QmsInspectStd.class));
    }

    @Test
    @DisplayName("新增：标准上限小于标准下限 → 拦截")
    void insertValMaxLessThanValMin()
    {
        QmsInspectStd std = build();
        std.setValMin(new BigDecimal("10.10"));
        std.setValMax(new BigDecimal("9.90"));
        ServiceException ex = assertThrows(ServiceException.class, () -> service.insertInspectStd(std));
        assertEquals("标准上限不能小于标准下限", ex.getMessage());
        verify(stdMapper, never()).insertInspectStd(any(QmsInspectStd.class));
    }

    @Test
    @DisplayName("新增：标准上限等于标准下限 → 允许")
    void insertValMaxEqualsValMin()
    {
        when(stdMapper.insertInspectStd(any(QmsInspectStd.class))).thenReturn(1);
        QmsInspectStd std = build();
        std.setValMin(new BigDecimal("10.00"));
        std.setValMax(new BigDecimal("10.00"));
        assertEquals(1, service.insertInspectStd(std));
        verify(stdMapper).insertInspectStd(any(QmsInspectStd.class));
    }

    @Test
    @DisplayName("新增：定性判定无上下限 → 允许")
    void insertQualitativeStd()
    {
        when(stdMapper.insertInspectStd(any(QmsInspectStd.class))).thenReturn(1);
        QmsInspectStd std = build();
        std.setJudgeRule("3");
        std.setValMin(null);
        std.setValMax(null);
        assertEquals(1, service.insertInspectStd(std));
        verify(stdMapper).insertInspectStd(any(QmsInspectStd.class));
    }

    @Test
    @DisplayName("新增：单值判定缺少目标值 → 拦截")
    void insertSingleValueMissingTarget()
    {
        QmsInspectStd std = build();
        std.setJudgeRule("2");
        std.setValMin(null);
        std.setValMax(null);
        std.setTargetVal(null);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.insertInspectStd(std));
        assertEquals("单值判定必须填写目标值", ex.getMessage());
        verify(stdMapper, never()).insertInspectStd(any(QmsInspectStd.class));
    }

    @Test
    @DisplayName("修改：标准上限小于标准下限 → 拦截")
    void updateValMaxLessThanValMin()
    {
        QmsInspectStd std = build();
        std.setStdId(1L);
        std.setValMin(new BigDecimal("10.10"));
        std.setValMax(new BigDecimal("9.90"));
        ServiceException ex = assertThrows(ServiceException.class, () -> service.updateInspectStd(std));
        assertEquals("标准上限不能小于标准下限", ex.getMessage());
        verify(stdMapper, never()).updateInspectStd(any(QmsInspectStd.class));
    }

    @Test
    @DisplayName("修改：合法区间 → 保存成功")
    void updateValidRangeStd()
    {
        when(stdMapper.updateInspectStd(any(QmsInspectStd.class))).thenReturn(1);
        QmsInspectStd std = build();
        std.setStdId(1L);
        assertEquals(1, service.updateInspectStd(std));
        verify(stdMapper).updateInspectStd(any(QmsInspectStd.class));
    }
}

package com.ruoyi.pms.service.impl;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.pms.domain.PmsPurchasePlan;
import com.ruoyi.pms.mapper.PmsPurchasePlanMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 采购计划时间范围校验单元测试
 * 规则：结束日期不能早于或等于开始日期（严格大于，相等亦拦截）
 */
@DisplayName("采购计划 — 结束日期不能早于或等于开始日期")
class PmsPurchasePlanServiceImplTest
{
    private static final String MSG = "结束日期不能早于或等于开始日期";

    private PmsPurchasePlanServiceImpl service;
    private PmsPurchasePlanMapper planMapper;
    private IMkNumberRuleService numberRuleService;

    @BeforeEach
    void setUp() throws Exception
    {
        service = new PmsPurchasePlanServiceImpl();
        planMapper = mock(PmsPurchasePlanMapper.class);
        numberRuleService = mock(IMkNumberRuleService.class);
        setField("pmsPurchasePlanMapper", planMapper);
        setField("mkNumberRuleService", numberRuleService);
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
        Field f = PmsPurchasePlanServiceImpl.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(service, value);
    }

    private Date day(String date) throws Exception
    {
        return new SimpleDateFormat("yyyy-MM-dd").parse(date);
    }

    private PmsPurchasePlan build(String start, String end) throws Exception
    {
        PmsPurchasePlan plan = new PmsPurchasePlan();
        plan.setTitle("9月原材料采购");
        plan.setPlanNo("PP001");
        plan.setStartDate(start == null ? null : day(start));
        plan.setEndDate(end == null ? null : day(end));
        return plan;
    }

    @Test
    @DisplayName("新增：结束日期晚于开始日期 → 保存成功")
    void insertValidRange() throws Exception
    {
        when(planMapper.insertPmsPurchasePlan(any(PmsPurchasePlan.class))).thenReturn(1);
        assertEquals(1, service.insertPmsPurchasePlan(build("2026-09-01", "2026-09-30")));
        verify(planMapper).insertPmsPurchasePlan(any(PmsPurchasePlan.class));
    }

    @Test
    @DisplayName("新增：结束日期等于开始日期 → 拦截")
    void insertEqualRange() throws Exception
    {
        ServiceException ex = assertThrows(ServiceException.class, () -> service.insertPmsPurchasePlan(build("2026-09-01", "2026-09-01")));
        assertEquals(MSG, ex.getMessage());
        verify(planMapper, never()).insertPmsPurchasePlan(any(PmsPurchasePlan.class));
    }

    @Test
    @DisplayName("新增：结束日期早于开始日期 → 拦截")
    void insertReversedRange() throws Exception
    {
        ServiceException ex = assertThrows(ServiceException.class, () -> service.insertPmsPurchasePlan(build("2026-09-30", "2026-09-01")));
        assertEquals(MSG, ex.getMessage());
        verify(planMapper, never()).insertPmsPurchasePlan(any(PmsPurchasePlan.class));
    }

    @Test
    @DisplayName("新增：起止为空 → 不触发时间校验")
    void insertMissingTimes() throws Exception
    {
        when(planMapper.insertPmsPurchasePlan(any(PmsPurchasePlan.class))).thenReturn(1);
        assertEquals(1, service.insertPmsPurchasePlan(build(null, null)));
        verify(planMapper).insertPmsPurchasePlan(any(PmsPurchasePlan.class));
    }

    @Test
    @DisplayName("修改：结束日期早于开始日期 → 拦截")
    void updateReversedRange() throws Exception
    {
        PmsPurchasePlan plan = build("2026-09-30", "2026-09-01");
        plan.setPlanId(1L);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.updatePmsPurchasePlan(plan));
        assertEquals(MSG, ex.getMessage());
        verify(planMapper, never()).updatePmsPurchasePlan(any(PmsPurchasePlan.class));
    }

    @Test
    @DisplayName("修改：合法范围 → 保存成功")
    void updateValidRange() throws Exception
    {
        PmsPurchasePlan existing = new PmsPurchasePlan();
        existing.setStatus("0");
        when(planMapper.selectPmsPurchasePlanById(1L)).thenReturn(existing);
        when(planMapper.updatePmsPurchasePlan(any(PmsPurchasePlan.class))).thenReturn(1);
        PmsPurchasePlan plan = build("2026-09-01", "2026-09-30");
        plan.setPlanId(1L);
        assertEquals(1, service.updatePmsPurchasePlan(plan));
        verify(planMapper).updatePmsPurchasePlan(any(PmsPurchasePlan.class));
    }
}

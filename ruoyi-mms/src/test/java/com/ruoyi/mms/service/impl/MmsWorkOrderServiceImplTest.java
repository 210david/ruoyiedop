package com.ruoyi.mms.service.impl;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.mms.domain.MmsWorkOrder;
import com.ruoyi.mms.mapper.MmsWorkOrderMapper;
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
 * 生产工单时间范围校验单元测试
 * 规则：计划完工时间必须晚于计划开工时间（严格大于，相等亦拦截）
 */
@DisplayName("生产工单 — 计划完工时间必须晚于计划开工时间")
class MmsWorkOrderServiceImplTest
{
    private static final String MSG = "计划完工时间必须晚于计划开工时间";

    private MmsWorkOrderServiceImpl service;
    private MmsWorkOrderMapper workOrderMapper;
    private IMkNumberRuleService numberRuleService;

    @BeforeEach
    void setUp() throws Exception
    {
        service = new MmsWorkOrderServiceImpl();
        workOrderMapper = mock(MmsWorkOrderMapper.class);
        numberRuleService = mock(IMkNumberRuleService.class);
        setField("workOrderMapper", workOrderMapper);
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
        Field f = MmsWorkOrderServiceImpl.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(service, value);
    }

    private Date ts(String datetime) throws Exception
    {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(datetime);
    }

    @Test
    @DisplayName("新增：计划完工晚于计划开工 → 保存成功")
    void insertValidRange() throws Exception
    {
        MmsWorkOrder wo = new MmsWorkOrder();
        wo.setWorkOrderNo("WO001");
        wo.setPlanStart(ts("2026-09-12 08:00:00"));
        wo.setPlanFinish(ts("2026-09-12 17:00:00"));
        when(workOrderMapper.insertWorkOrder(any(MmsWorkOrder.class))).thenReturn(1);
        assertEquals(1, service.insertWorkOrder(wo));
        verify(workOrderMapper).insertWorkOrder(any(MmsWorkOrder.class));
    }

    @Test
    @DisplayName("新增：计划完工等于计划开工 → 拦截")
    void insertEqualRange() throws Exception
    {
        MmsWorkOrder wo = new MmsWorkOrder();
        wo.setWorkOrderNo("WO001");
        wo.setPlanStart(ts("2026-09-12 08:00:00"));
        wo.setPlanFinish(ts("2026-09-12 08:00:00"));
        ServiceException ex = assertThrows(ServiceException.class, () -> service.insertWorkOrder(wo));
        assertEquals(MSG, ex.getMessage());
        verify(workOrderMapper, never()).insertWorkOrder(any(MmsWorkOrder.class));
    }

    @Test
    @DisplayName("新增：计划完工早于计划开工 → 拦截")
    void insertReversedRange() throws Exception
    {
        MmsWorkOrder wo = new MmsWorkOrder();
        wo.setWorkOrderNo("WO001");
        wo.setPlanStart(ts("2026-09-12 18:00:00"));
        wo.setPlanFinish(ts("2026-09-12 08:00:00"));
        ServiceException ex = assertThrows(ServiceException.class, () -> service.insertWorkOrder(wo));
        assertEquals(MSG, ex.getMessage());
        verify(workOrderMapper, never()).insertWorkOrder(any(MmsWorkOrder.class));
    }

    @Test
    @DisplayName("新增：起止时间为空 → 不触发时间校验")
    void insertMissingTimes()
    {
        MmsWorkOrder wo = new MmsWorkOrder();
        wo.setWorkOrderNo("WO001");
        when(workOrderMapper.insertWorkOrder(any(MmsWorkOrder.class))).thenReturn(1);
        assertEquals(1, service.insertWorkOrder(wo));
        verify(workOrderMapper).insertWorkOrder(any(MmsWorkOrder.class));
    }

    @Test
    @DisplayName("修改：计划完工早于计划开工 → 拦截")
    void updateReversedRange() throws Exception
    {
        MmsWorkOrder wo = new MmsWorkOrder();
        wo.setWorkOrderId(1L);
        wo.setWorkOrderNo("WO001");
        wo.setPlanStart(ts("2026-09-12 18:00:00"));
        wo.setPlanFinish(ts("2026-09-12 08:00:00"));
        ServiceException ex = assertThrows(ServiceException.class, () -> service.updateWorkOrder(wo));
        assertEquals(MSG, ex.getMessage());
        verify(workOrderMapper, never()).updateWorkOrder(any(MmsWorkOrder.class));
    }

    @Test
    @DisplayName("修改：合法范围 → 保存成功")
    void updateValidRange() throws Exception
    {
        MmsWorkOrder existing = new MmsWorkOrder();
        existing.setStatus("0");
        when(workOrderMapper.selectWorkOrderById(1L)).thenReturn(existing);
        when(workOrderMapper.updateWorkOrder(any(MmsWorkOrder.class))).thenReturn(1);
        MmsWorkOrder wo = new MmsWorkOrder();
        wo.setWorkOrderId(1L);
        wo.setWorkOrderNo("WO001");
        wo.setPlanStart(ts("2026-09-12 08:00:00"));
        wo.setPlanFinish(ts("2026-09-12 17:00:00"));
        assertEquals(1, service.updateWorkOrder(wo));
        verify(workOrderMapper).updateWorkOrder(any(MmsWorkOrder.class));
    }
}

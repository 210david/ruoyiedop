package com.ruoyi.mms.service.impl;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.mms.domain.MmsDowntime;
import com.ruoyi.mms.mapper.MmsDowntimeMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.math.BigDecimal;
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
 * 停机管理时间范围校验单元测试
 * 规则：结束时间必须晚于开始时间（严格大于，相等亦拦截）；
 * 原逻辑对负时长静默钳制为0，现改为直接拦截
 */
@DisplayName("停机管理 — 结束时间必须晚于开始时间")
class MmsDowntimeServiceImplTest
{
    private static final String MSG = "结束时间必须晚于开始时间";

    private MmsDowntimeServiceImpl service;
    private MmsDowntimeMapper downtimeMapper;
    private IMkNumberRuleService numberRuleService;

    @BeforeEach
    void setUp() throws Exception
    {
        service = new MmsDowntimeServiceImpl();
        downtimeMapper = mock(MmsDowntimeMapper.class);
        numberRuleService = mock(IMkNumberRuleService.class);
        setField("downtimeMapper", downtimeMapper);
        setField("mkNumberRuleService", numberRuleService);
        SysUser user = new SysUser();
        user.setUserName("admin");
        user.setNickName("管理员");
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
        Field f = MmsDowntimeServiceImpl.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(service, value);
    }

    private Date ts(String datetime) throws Exception
    {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(datetime);
    }

    @Test
    @DisplayName("新增：结束晚于开始 → 保存成功且自动计算停机时长")
    void insertValidRange() throws Exception
    {
        MmsDowntime d = new MmsDowntime();
        d.setDowntimeNo("DT001");
        d.setStartTime(ts("2026-09-12 08:00:00"));
        d.setEndTime(ts("2026-09-12 10:00:00"));
        when(downtimeMapper.insertDowntime(any(MmsDowntime.class))).thenReturn(1);
        assertEquals(1, service.insertDowntime(d));
        verify(downtimeMapper).insertDowntime(any(MmsDowntime.class));
        assertEquals(0, d.getHours().compareTo(new BigDecimal("2")), "停机2小时应为2.00");
    }

    @Test
    @DisplayName("新增：结束等于开始 → 拦截（不再钳制为0时长）")
    void insertEqualRange() throws Exception
    {
        MmsDowntime d = new MmsDowntime();
        d.setDowntimeNo("DT001");
        d.setStartTime(ts("2026-09-12 08:00:00"));
        d.setEndTime(ts("2026-09-12 08:00:00"));
        ServiceException ex = assertThrows(ServiceException.class, () -> service.insertDowntime(d));
        assertEquals(MSG, ex.getMessage());
        verify(downtimeMapper, never()).insertDowntime(any(MmsDowntime.class));
    }

    @Test
    @DisplayName("新增：结束早于开始 → 拦截")
    void insertReversedRange() throws Exception
    {
        MmsDowntime d = new MmsDowntime();
        d.setDowntimeNo("DT001");
        d.setStartTime(ts("2026-09-12 10:00:00"));
        d.setEndTime(ts("2026-09-12 08:00:00"));
        ServiceException ex = assertThrows(ServiceException.class, () -> service.insertDowntime(d));
        assertEquals(MSG, ex.getMessage());
        verify(downtimeMapper, never()).insertDowntime(any(MmsDowntime.class));
    }

    @Test
    @DisplayName("新增：起止时间为空 → 不触发时间校验")
    void insertMissingTimes()
    {
        MmsDowntime d = new MmsDowntime();
        d.setDowntimeNo("DT001");
        when(downtimeMapper.insertDowntime(any(MmsDowntime.class))).thenReturn(1);
        assertEquals(1, service.insertDowntime(d));
        verify(downtimeMapper).insertDowntime(any(MmsDowntime.class));
    }

    @Test
    @DisplayName("修改：结束早于开始 → 拦截")
    void updateReversedRange() throws Exception
    {
        MmsDowntime d = new MmsDowntime();
        d.setDowntimeId(1L);
        d.setStartTime(ts("2026-09-12 10:00:00"));
        d.setEndTime(ts("2026-09-12 08:00:00"));
        ServiceException ex = assertThrows(ServiceException.class, () -> service.updateDowntime(d));
        assertEquals(MSG, ex.getMessage());
        verify(downtimeMapper, never()).updateDowntime(any(MmsDowntime.class));
    }

    @Test
    @DisplayName("修改：合法范围 → 保存成功")
    void updateValidRange() throws Exception
    {
        MmsDowntime d = new MmsDowntime();
        d.setDowntimeId(1L);
        d.setStartTime(ts("2026-09-12 08:00:00"));
        d.setEndTime(ts("2026-09-12 09:30:00"));
        when(downtimeMapper.updateDowntime(any(MmsDowntime.class))).thenReturn(1);
        assertEquals(1, service.updateDowntime(d));
        verify(downtimeMapper).updateDowntime(any(MmsDowntime.class));
        assertEquals(0, d.getHours().compareTo(new BigDecimal("1.5")), "停机1.5小时应为1.50");
    }
}

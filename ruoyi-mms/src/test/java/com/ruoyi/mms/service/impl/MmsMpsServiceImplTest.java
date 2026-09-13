package com.ruoyi.mms.service.impl;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.mms.domain.MmsMps;
import com.ruoyi.mms.mapper.MmsMpsMapper;
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
 * 生产计划（MPS）时间范围校验单元测试
 * 规则：计划期结束不能早于或等于计划期开始（严格大于，相等亦拦截）
 */
@DisplayName("生产计划MPS — 计划期结束不能早于或等于计划期开始")
class MmsMpsServiceImplTest
{
    private static final String MSG = "计划期结束不能早于或等于计划期开始";

    private MmsMpsServiceImpl service;
    private MmsMpsMapper mpsMapper;
    private IMkNumberRuleService numberRuleService;

    @BeforeEach
    void setUp() throws Exception
    {
        service = new MmsMpsServiceImpl();
        mpsMapper = mock(MmsMpsMapper.class);
        numberRuleService = mock(IMkNumberRuleService.class);
        setField("mpsMapper", mpsMapper);
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
        Field f = MmsMpsServiceImpl.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(service, value);
    }

    private Date day(String date) throws Exception
    {
        return new SimpleDateFormat("yyyy-MM-dd").parse(date);
    }

    private MmsMps build(String start, String end) throws Exception
    {
        MmsMps mps = new MmsMps();
        mps.setMpsNo("MPS001");
        mps.setPeriodStart(start == null ? null : day(start));
        mps.setPeriodEnd(end == null ? null : day(end));
        return mps;
    }

    @Test
    @DisplayName("新增：计划期结束晚于开始 → 保存成功")
    void insertValidRange() throws Exception
    {
        when(mpsMapper.insertMps(any(MmsMps.class))).thenReturn(1);
        assertEquals(1, service.insertMps(build("2026-09-01", "2026-09-30")));
        verify(mpsMapper).insertMps(any(MmsMps.class));
    }

    @Test
    @DisplayName("新增：计划期结束等于开始 → 拦截")
    void insertEqualRange() throws Exception
    {
        ServiceException ex = assertThrows(ServiceException.class, () -> service.insertMps(build("2026-09-01", "2026-09-01")));
        assertEquals(MSG, ex.getMessage());
        verify(mpsMapper, never()).insertMps(any(MmsMps.class));
    }

    @Test
    @DisplayName("新增：计划期结束早于开始 → 拦截")
    void insertReversedRange() throws Exception
    {
        ServiceException ex = assertThrows(ServiceException.class, () -> service.insertMps(build("2026-09-30", "2026-09-01")));
        assertEquals(MSG, ex.getMessage());
        verify(mpsMapper, never()).insertMps(any(MmsMps.class));
    }

    @Test
    @DisplayName("新增：起止为空 → 不触发时间校验")
    void insertMissingTimes() throws Exception
    {
        when(mpsMapper.insertMps(any(MmsMps.class))).thenReturn(1);
        assertEquals(1, service.insertMps(build(null, null)));
        verify(mpsMapper).insertMps(any(MmsMps.class));
    }

    @Test
    @DisplayName("修改：计划期结束早于开始 → 拦截且不触发状态检查")
    void updateReversedRange() throws Exception
    {
        MmsMps mps = build("2026-09-30", "2026-09-01");
        mps.setMpsId(1L);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.updateMps(mps));
        assertEquals(MSG, ex.getMessage());
        verify(mpsMapper, never()).selectMpsById(1L);
        verify(mpsMapper, never()).updateMps(any(MmsMps.class));
    }

    @Test
    @DisplayName("修改：合法范围（草稿状态）→ 保存成功")
    void updateValidRange() throws Exception
    {
        MmsMps existing = new MmsMps();
        existing.setMpsNo("MPS001");
        existing.setStatus("0");
        when(mpsMapper.selectMpsById(1L)).thenReturn(existing);
        when(mpsMapper.updateMps(any(MmsMps.class))).thenReturn(1);
        MmsMps mps = build("2026-09-01", "2026-09-30");
        mps.setMpsId(1L);
        assertEquals(1, service.updateMps(mps));
        verify(mpsMapper).updateMps(any(MmsMps.class));
    }
}

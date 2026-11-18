package com.ruoyi.oa.domain;

import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 印章台账逾期判断单元测试
 * 逾期仅对外带使用中（status=3）且预计归还时间已过的印章生效。
 */
@DisplayName("印章台账 — 外带逾期判断")
class OaSealTest
{
    private static final long DAY = 24L * 60 * 60 * 1000;

    @Test
    @DisplayName("外带使用中 + 预计归还时间已过 → 逾期")
    void overdueWhenExternalAndPast()
    {
        OaSeal seal = seal("3", new Date(System.currentTimeMillis() - DAY));
        assertTrue(seal.isOverdue());
    }

    @Test
    @DisplayName("外带使用中 + 未到预计归还时间 → 不逾期")
    void notOverdueWhenExternalAndFuture()
    {
        OaSeal seal = seal("3", new Date(System.currentTimeMillis() + DAY));
        assertFalse(seal.isOverdue());
    }

    @Test
    @DisplayName("非外带状态 + 预计归还时间已过 → 不逾期")
    void notOverdueWhenNormal()
    {
        OaSeal seal = seal("0", new Date(System.currentTimeMillis() - DAY));
        assertFalse(seal.isOverdue());
    }

    @Test
    @DisplayName("外带使用中但没有预计归还时间 → 不逾期")
    void notOverdueWhenNoReturnTime()
    {
        OaSeal seal = seal("3", null);
        assertFalse(seal.isOverdue());
    }

    private OaSeal seal(String status, Date useReturnTime)
    {
        OaSeal seal = new OaSeal();
        seal.setStatus(status);
        seal.setUseReturnTime(useReturnTime);
        return seal;
    }
}

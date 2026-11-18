package com.ruoyi.oa.listener;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * OA资源台账流程归档处理器单元测试
 * 验证流程办结/终止后的占用释放、复活后的重新占用及实例重绑。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OA资源台账归档 — 占用释放/复活")
class OaResourceArchiveHandlerTest
{
    private static final String INSTANCE_ID = "inst-0002";

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private OaResourceArchiveHandler handler;

    @Test
    @DisplayName("归档：车辆/司机/印章占用状态释放为空闲")
    void archiveReleasesOccupation()
    {
        handler.archive("busUseApply", INSTANCE_ID, "100");

        verify(jdbcTemplate).update("UPDATE biz_oa_vehicle SET status = '0' WHERE flow_instance_id = ? AND status = '1'", INSTANCE_ID);
        verify(jdbcTemplate).update("UPDATE biz_oa_driver SET status = '0' WHERE flow_instance_id = ? AND status = '3'", INSTANCE_ID);
        verify(jdbcTemplate).update("UPDATE biz_oa_seal SET status = '0' WHERE flow_instance_id = ? AND status = '3'", INSTANCE_ID);
    }

    @Test
    @DisplayName("终止：同样释放车辆/司机/印章占用")
    void cancelledReleasesOccupation()
    {
        handler.cancelled("sealApply", INSTANCE_ID, "100");

        verify(jdbcTemplate).update("UPDATE biz_oa_vehicle SET status = '0' WHERE flow_instance_id = ? AND status = '1'", INSTANCE_ID);
        verify(jdbcTemplate).update("UPDATE biz_oa_driver SET status = '0' WHERE flow_instance_id = ? AND status = '3'", INSTANCE_ID);
        verify(jdbcTemplate).update("UPDATE biz_oa_seal SET status = '0' WHERE flow_instance_id = ? AND status = '3'", INSTANCE_ID);
    }

    @Test
    @DisplayName("复活：车辆/司机/印章重新置为占用")
    void rollbackOccupiesAgain()
    {
        handler.rollback("busUseApply", INSTANCE_ID, "100");

        verify(jdbcTemplate).update("UPDATE biz_oa_vehicle SET status = '1' WHERE flow_instance_id = ? AND status = '0'", INSTANCE_ID);
        verify(jdbcTemplate).update("UPDATE biz_oa_driver SET status = '3' WHERE flow_instance_id = ? AND status = '0'", INSTANCE_ID);
        verify(jdbcTemplate).update("UPDATE biz_oa_seal SET status = '3' WHERE flow_instance_id = ? AND status = '0'", INSTANCE_ID);
    }

    @Test
    @DisplayName("复活重绑：三张台账的流程实例ID统一切换为新实例")
    void rebindUpdatesInstanceId()
    {
        handler.rebind("busUseApply", "old-inst", "new-inst");

        verify(jdbcTemplate).update("UPDATE biz_oa_vehicle SET flow_instance_id = ? WHERE flow_instance_id = ?", "new-inst", "old-inst");
        verify(jdbcTemplate).update("UPDATE biz_oa_driver SET flow_instance_id = ? WHERE flow_instance_id = ?", "new-inst", "old-inst");
        verify(jdbcTemplate).update("UPDATE biz_oa_seal SET flow_instance_id = ? WHERE flow_instance_id = ?", "new-inst", "old-inst");
    }

    @Test
    @DisplayName("处理的流程Key包含公车使用申请与用印申请")
    void processKeysContainsOaFlows()
    {
        assertTrue(handler.processKeys().contains("busUseApply"));
        assertTrue(handler.processKeys().contains("sealApply"));
    }

    @Test
    @DisplayName("数据库异常：归档/释放不向流程主流程抛异常")
    void databaseExceptionDoesNotPropagate()
    {
        doThrow(new RuntimeException("db down")).when(jdbcTemplate).update(anyString(), anyString());

        assertDoesNotThrow(() -> handler.archive("busUseApply", INSTANCE_ID, "100"));
        assertDoesNotThrow(() -> handler.cancelled("busUseApply", INSTANCE_ID, "100"));
    }
}

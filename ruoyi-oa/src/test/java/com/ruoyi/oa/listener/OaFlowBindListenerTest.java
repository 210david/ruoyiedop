package com.ruoyi.oa.listener;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import com.ruoyi.common.event.FlowFormSubmitEvent;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * OA台账流程关联监听器单元测试
 * 验证「公车使用申请」「用印申请」流程表单提交后，车辆/司机/印章台账的
 * flow_instance_id 写回逻辑（按名称匹配、空值跳过、异常不阻断主流程）。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OA台账流程关联监听器 — flow_instance_id 写回")
class OaFlowBindListenerTest
{
    private static final String INSTANCE_ID = "inst-0001";

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private OaFlowBindListener listener;

    /** 构造事件 */
    private FlowFormSubmitEvent event(String phase, String processKey, Map<String, Object> vars)
    {
        return event(phase, processKey, null, null, vars);
    }

    /** 构造事件（携带办理节点） */
    private FlowFormSubmitEvent event(String phase, String processKey, String nodeKey, String nodeName,
            Map<String, Object> vars)
    {
        return new FlowFormSubmitEvent(this, phase, processKey, 19L, INSTANCE_ID, "100",
                "flow_data_bus_use_apply", vars, nodeKey, nodeName);
    }

    private Map<String, Object> vars(String k1, Object v1, String k2, Object v2)
    {
        Map<String, Object> m = new HashMap<>();
        if (k1 != null)
        {
            m.put(k1, v1);
        }
        if (k2 != null)
        {
            m.put(k2, v2);
        }
        return m;
    }

    /** 用印申请变量：印章名称 + 使用方式 */
    private Map<String, Object> sealVars(String sealName, String useMode)
    {
        Map<String, Object> m = new HashMap<>();
        m.put("seal_name", sealName);
        if (useMode != null)
        {
            m.put("use_mode", useMode);
        }
        return m;
    }

    // ==================== 公车使用申请（busUseApply） ====================

    @Test
    @DisplayName("公车使用：指派车辆+指派驾驶员 → 车辆/司机台账先后写回流程实例ID")
    void busUseApply_bindsVehicleAndDriver()
    {
        listener.onApplicationEvent(event(FlowFormSubmitEvent.PHASE_START, "busUseApply",
                vars("assigned_vehicle", "京B10001", "assigned_driver", "王建国")));

        InOrder inOrder = inOrder(jdbcTemplate);
        inOrder.verify(jdbcTemplate).update(
                "UPDATE biz_oa_vehicle SET flow_instance_id = ?, status = CASE WHEN status = '0' THEN '1' ELSE status END WHERE plate_no = ? AND del_flag = '0'",
                INSTANCE_ID, "京B10001");
        inOrder.verify(jdbcTemplate).update(
                "UPDATE biz_oa_driver SET flow_instance_id = ?, status = CASE WHEN status = '0' THEN '3' ELSE status END WHERE driver_name = ? AND del_flag = '0'",
                INSTANCE_ID, "王建国");
    }

    @Test
    @DisplayName("公车使用：调度节点办理（complete阶段）指派/改派同样触发写回")
    void busUseApply_completePhaseAlsoBinds()
    {
        listener.onApplicationEvent(event(FlowFormSubmitEvent.PHASE_COMPLETE, "busUseApply",
                vars("assigned_vehicle", "京B20002", "assigned_driver", "刘洋")));

        verify(jdbcTemplate).update(anyString(), org.mockito.ArgumentMatchers.eq(INSTANCE_ID),
                org.mockito.ArgumentMatchers.eq("京B20002"));
        verify(jdbcTemplate).update(anyString(), org.mockito.ArgumentMatchers.eq(INSTANCE_ID),
                org.mockito.ArgumentMatchers.eq("刘洋"));
    }

    @Test
    @DisplayName("公车使用：仅指派车辆（调度未定司机）→ 只写车辆，不写司机")
    void busUseApply_onlyVehicleBound()
    {
        listener.onApplicationEvent(event(FlowFormSubmitEvent.PHASE_START, "busUseApply",
                vars("assigned_vehicle", "京B30003", "assigned_driver", null)));

        verify(jdbcTemplate).update(
                "UPDATE biz_oa_vehicle SET flow_instance_id = ?, status = CASE WHEN status = '0' THEN '1' ELSE status END WHERE plate_no = ? AND del_flag = '0'",
                INSTANCE_ID, "京B30003");
        verify(jdbcTemplate, never()).update(
                org.mockito.ArgumentMatchers.contains("biz_oa_driver"), anyString(), anyString());
    }

    @Test
    @DisplayName("公车使用：车辆匹配不到（返回0行）→ 不抛异常，司机继续正常绑定")
    void busUseApply_vehicleNotMatchedStillBindsDriver()
    {
        when(jdbcTemplate.update(org.mockito.ArgumentMatchers.contains("biz_oa_vehicle"),
                anyString(), anyString())).thenReturn(0);

        assertDoesNotThrow(() -> listener.onApplicationEvent(event(FlowFormSubmitEvent.PHASE_START,
                "busUseApply", vars("assigned_vehicle", "手填车牌不存在", "assigned_driver", "陈晓东"))));

        verify(jdbcTemplate).update(
                "UPDATE biz_oa_driver SET flow_instance_id = ?, status = CASE WHEN status = '0' THEN '3' ELSE status END WHERE driver_name = ? AND del_flag = '0'",
                INSTANCE_ID, "陈晓东");
    }

    // ==================== 值清洗 ====================

    @Test
    @DisplayName("值清洗：null/空白/'null'字符串均视为空值，跳过写回")
    void blankAndNullStringValuesAreSkipped()
    {
        listener.onApplicationEvent(event(FlowFormSubmitEvent.PHASE_START, "busUseApply",
                vars("assigned_vehicle", "  ", "assigned_driver", "null")));

        listener.onApplicationEvent(event(FlowFormSubmitEvent.PHASE_START, "sealApply",
                vars("seal_name", null, null, null)));

        verifyNoInteractions(jdbcTemplate);
    }

    // ==================== 用印申请（sealApply） ====================

    @Test
    @DisplayName("用印申请：携出盖章 + 综合管理部审核并盖章办理完成 → 置为外带使用中")
    void sealApply_bindsSeal()
    {
        listener.onApplicationEvent(event(FlowFormSubmitEvent.PHASE_COMPLETE, "sealApply",
                "Activity_seal", "综合管理部审核并盖章", sealVars("公司公章", "携出盖章")));

        verify(jdbcTemplate).update(
                "UPDATE biz_oa_seal SET flow_instance_id = ?, status = CASE WHEN status = '0' THEN '3' ELSE status END WHERE seal_name = ? AND del_flag = '0'",
                INSTANCE_ID, "公司公章");
        verify(jdbcTemplate, never()).update(
                org.mockito.ArgumentMatchers.contains("biz_oa_vehicle"), anyString(), anyString());
        verify(jdbcTemplate, never()).update(
                org.mockito.ArgumentMatchers.contains("biz_oa_driver"), anyString(), anyString());
    }

    @Test
    @DisplayName("用印申请：发起阶段即使携出盖章 → 只关联流程，不占用")
    void sealApply_startOnlyBinds()
    {
        listener.onApplicationEvent(event(FlowFormSubmitEvent.PHASE_START, "sealApply",
                sealVars("公司公章", "携出盖章")));

        verify(jdbcTemplate).update(
                "UPDATE biz_oa_seal SET flow_instance_id = ? WHERE seal_name = ? AND del_flag = '0'",
                INSTANCE_ID, "公司公章");
        verify(jdbcTemplate, never()).update(
                org.mockito.ArgumentMatchers.contains("status = CASE"), anyString(), anyString());
    }

    @Test
    @DisplayName("用印申请：其他节点办理完成（如部门负责人）→ 只关联流程，不占用")
    void sealApply_otherNodeCompleteOnlyBinds()
    {
        listener.onApplicationEvent(event(FlowFormSubmitEvent.PHASE_COMPLETE, "sealApply",
                "Activity_dept", "部门负责人审批", sealVars("公司公章", "携出盖章")));

        verify(jdbcTemplate).update(
                "UPDATE biz_oa_seal SET flow_instance_id = ? WHERE seal_name = ? AND del_flag = '0'",
                INSTANCE_ID, "公司公章");
        verify(jdbcTemplate, never()).update(
                org.mockito.ArgumentMatchers.contains("status = CASE"), anyString(), anyString());
    }

    @Test
    @DisplayName("用印申请：节点Key变化但名称为综合管理部 → 仍按综合管理部节点占用")
    void sealApply_issueNodeMatchedByName()
    {
        listener.onApplicationEvent(event(FlowFormSubmitEvent.PHASE_COMPLETE, "sealApply",
                "custom_node", "综合管理部审核并盖章", sealVars("公司公章", "携出盖章")));

        verify(jdbcTemplate).update(
                "UPDATE biz_oa_seal SET flow_instance_id = ?, status = CASE WHEN status = '0' THEN '3' ELSE status END WHERE seal_name = ? AND del_flag = '0'",
                INSTANCE_ID, "公司公章");
    }

    @Test
    @DisplayName("用印申请：现场盖章 + 综合管理部办理完成 → 不占用")
    void sealApply_onSiteDoesNotOccupy()
    {
        listener.onApplicationEvent(event(FlowFormSubmitEvent.PHASE_COMPLETE, "sealApply",
                "Activity_seal", "综合管理部审核并盖章", sealVars("公司公章", "现场盖章")));

        verify(jdbcTemplate).update(
                "UPDATE biz_oa_seal SET flow_instance_id = ? WHERE seal_name = ? AND del_flag = '0'",
                INSTANCE_ID, "公司公章");
        verify(jdbcTemplate, never()).update(
                org.mockito.ArgumentMatchers.contains("status = CASE"), anyString(), anyString());
    }

    @Test
    @DisplayName("用印申请：未填使用方式 + 综合管理部办理完成 → 不占用")
    void sealApply_missingUseModeDoesNotOccupy()
    {
        listener.onApplicationEvent(event(FlowFormSubmitEvent.PHASE_COMPLETE, "sealApply",
                "Activity_seal", "综合管理部审核并盖章", sealVars("公司公章", null)));

        verify(jdbcTemplate).update(
                "UPDATE biz_oa_seal SET flow_instance_id = ? WHERE seal_name = ? AND del_flag = '0'",
                INSTANCE_ID, "公司公章");
    }

    @Test
    @DisplayName("用印申请：印章匹配不到（返回0行）→ 不抛异常仅告警")
    void sealApply_sealNotMatchedNoThrow()
    {
        when(jdbcTemplate.update(org.mockito.ArgumentMatchers.contains("biz_oa_seal"),
                anyString(), anyString())).thenReturn(0);

        assertDoesNotThrow(() -> listener.onApplicationEvent(event(FlowFormSubmitEvent.PHASE_SAVE,
                "sealApply", sealVars("不存在的印章", "携出盖章"))));
    }

    // ==================== 隔离与容错 ====================

    @Test
    @DisplayName("其他流程（如请假）不触发任何OA台账写回")
    void otherProcessKeysAreIgnored()
    {
        listener.onApplicationEvent(event(FlowFormSubmitEvent.PHASE_START, "leaveApply",
                vars("assigned_vehicle", "京B10001", "assigned_driver", "王建国")));

        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    @DisplayName("variables为null（无表单数据的流程）→ 不处理不报错")
    void nullVariablesIgnored()
    {
        assertDoesNotThrow(() -> listener.onApplicationEvent(event(FlowFormSubmitEvent.PHASE_START,
                "busUseApply", null)));
        assertDoesNotThrow(() -> listener.onApplicationEvent(event(FlowFormSubmitEvent.PHASE_START,
                "sealApply", null)));
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    @DisplayName("JdbcTemplate写回抛异常 → 监听器整体捕获，不向流程主流程传播")
    void jdbcExceptionDoesNotPropagate()
    {
        doThrow(new RuntimeException("db down")).when(jdbcTemplate)
                .update(anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> listener.onApplicationEvent(event(FlowFormSubmitEvent.PHASE_START,
                "busUseApply", vars("assigned_vehicle", "京B10001", "assigned_driver", "王建国"))));
        assertDoesNotThrow(() -> listener.onApplicationEvent(event(FlowFormSubmitEvent.PHASE_COMPLETE,
                "sealApply", "Activity_seal", "综合管理部审核并盖章", sealVars("公司公章", "携出盖章"))));
    }
}

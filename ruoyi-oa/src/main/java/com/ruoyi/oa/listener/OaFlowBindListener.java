package com.ruoyi.oa.listener;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import com.ruoyi.common.event.FlowFormSubmitEvent;
import com.ruoyi.common.utils.StringUtils;

/**
 * OA 台账流程关联监听器
 *
 * 消费流程表单提交事件（FlowFormSubmitEvent），把「公车使用申请」「用印申请」流程与
 * OA 综合办公台账建立关联（写回 flow_instance_id）：
 *
 * 1. busUseApply（公车使用申请）：表单「指派车辆」（车牌号）写回 biz_oa_vehicle.flow_instance_id，
 *    「指派驾驶员」（司机姓名）写回 biz_oa_driver.flow_instance_id；
 *    调度节点指派/改派、暂存时同样触发，台账始终记录最近一次关联的流程。
 * 2. sealApply（用印申请）：表单「用印印章」（印章名称）写回 biz_oa_seal.flow_instance_id；
 *    仅「携出盖章」且「综合管理部审核并盖章」节点办理完成时，把印章状态置为「外带使用中」（3）；
 *    发起、暂存及其他节点办理只留痕不占用，流程办结/终止时由归档处理器释放。
 *
 * 隔离约定：按名称匹配不到台账记录（如手填车牌）时跳过，写回失败只记日志，不阻断流程主流程。
 *
 * @author ruoyi
 */
@Component
public class OaFlowBindListener implements ApplicationListener<FlowFormSubmitEvent>
{
    private static final Logger log = LoggerFactory.getLogger(OaFlowBindListener.class);

    /** 公车使用申请流程Key */
    private static final String KEY_BUS_USE_APPLY = "busUseApply";
    /** 用印申请流程Key */
    private static final String KEY_SEAL_APPLY = "sealApply";
    /** 用印流程「综合管理部审核并盖章」节点Key */
    private static final String SEAL_ISSUE_NODE_KEY = "Activity_seal";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void onApplicationEvent(FlowFormSubmitEvent event)
    {
        try
        {
            String key = event.getProcessKey();
            if (KEY_BUS_USE_APPLY.equals(key))
            {
                bindBusUse(event);
            }
            else if (KEY_SEAL_APPLY.equals(key))
            {
                bindSeal(event);
            }
        }
        catch (Exception e)
        {
            log.error("OA台账流程关联失败（不阻断主流程）：processKey {} 实例 {}，原因：{}",
                    event.getProcessKey(), event.getProcessInstanceId(), e.getMessage(), e);
        }
    }

    /**
     * 公车使用申请 → 指派车辆（车牌号）、指派驾驶员（姓名）写回流程实例ID
     */
    private void bindBusUse(FlowFormSubmitEvent event)
    {
        Map<String, Object> vars = event.getVariables();
        if (vars == null)
        {
            return;
        }
        String instanceId = event.getProcessInstanceId();
        String plate = str(vars.get("assigned_vehicle"));
        String driverName = str(vars.get("assigned_driver"));
        if (StringUtils.isNotEmpty(plate))
        {
            int n = jdbcTemplate.update(
                    "UPDATE biz_oa_vehicle SET flow_instance_id = ?, "
                            + "status = CASE WHEN status = '0' THEN '1' ELSE status END "
                            + "WHERE plate_no = ? AND del_flag = '0'",
                    instanceId, plate);
            if (n == 0)
            {
                log.warn("公车使用关联：未找到车牌号 [{}] 对应车辆台账记录，实例 {}", plate, instanceId);
            }
            else
            {
                log.info("公车使用关联：车辆 [{}] 已关联流程实例 {}，状态置为使用中", plate, instanceId);
            }
        }
        if (StringUtils.isNotEmpty(driverName))
        {
            jdbcTemplate.update(
                    "UPDATE biz_oa_driver SET flow_instance_id = ?, "
                            + "status = CASE WHEN status = '0' THEN '3' ELSE status END "
                            + "WHERE driver_name = ? AND del_flag = '0'",
                    instanceId, driverName);
        }
    }

    /**
     * 用印申请 → 用印印章（印章名称）写回流程实例ID；
     * 仅携出盖章且综合管理部审核并盖章节点办理完成时置为外带使用中，其余场景只留痕不占用。
     */
    private void bindSeal(FlowFormSubmitEvent event)
    {
        Map<String, Object> vars = event.getVariables();
        if (vars == null)
        {
            return;
        }
        String sealName = str(vars.get("seal_name"));
        if (StringUtils.isEmpty(sealName))
        {
            return;
        }
        String instanceId = event.getProcessInstanceId();
        boolean external = "携出盖章".equals(str(vars.get("use_mode")));
        boolean issueNode = SEAL_ISSUE_NODE_KEY.equals(event.getTaskDefinitionKey())
                || (event.getTaskName() != null && event.getTaskName().contains("综合管理部"));
        boolean occupy = external && FlowFormSubmitEvent.PHASE_COMPLETE.equals(event.getPhase()) && issueNode;
        String sql = occupy
                ? "UPDATE biz_oa_seal SET flow_instance_id = ?, "
                        + "status = CASE WHEN status = '0' THEN '3' ELSE status END "
                        + "WHERE seal_name = ? AND del_flag = '0'"
                : "UPDATE biz_oa_seal SET flow_instance_id = ? "
                        + "WHERE seal_name = ? AND del_flag = '0'";
        int n = jdbcTemplate.update(sql, instanceId, sealName);
        if (n == 0)
        {
            log.warn("用印关联：未找到印章 [{}] 对应印章台账记录，实例 {}", sealName, instanceId);
        }
        else
        {
            log.info("用印关联：印章 [{}] 已关联流程实例 {}，{}", sealName, instanceId,
                    occupy ? "综合管理部办理完成，置为外带使用中" : "仅关联流程，不改变状态");
        }
    }

    /** 变量值转字符串（数字/文本统一处理） */
    private String str(Object v)
    {
        if (v == null)
        {
            return "";
        }
        String s = String.valueOf(v).trim();
        return "null".equals(s) ? "" : s;
    }
}

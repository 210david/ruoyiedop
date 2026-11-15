package com.ruoyi.flowable.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;

/**
 * 流程节点「已查看人员」记录。
 *
 * <p>对标泛微 E9 运行期流程图：点开当前审批节点时，除「未审批操作人」「已操作人员」外，
 * 还要能看到「已查看人员」——谁打开过这个任务、但还没办理。</p>
 *
 * <p>Flowable 自身没有「查看」的概念（{@code act_hi_identitylink} 只有 candidate/participant 等），
 * 因此用一张轻量表 {@code flow_node_viewer} 记录：办理页打开时前端上报 taskId，
 * 后端按 taskId 反查实例/节点/当前登录人后写入，同一「实例+节点+用户」只保留首次查看时间。</p>
 *
 * <p>该表只用于展示，流程流转完全不依赖；表缺失或写入失败时静默降级，不影响办理。</p>
 *
 * @author ruoyi
 */
@Service
public class FlowNodeViewService
{
    private static final Logger log = LoggerFactory.getLogger(FlowNodeViewService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 记录一次节点查看。
     *
     * @param instanceId 流程实例ID
     * @param nodeId     节点ID（BPMN activityId / taskDefinitionKey）
     * @param taskId     任务ID（同一节点多次办理时用于区分，可为空）
     * @param userId     查看人用户ID
     * @param userName   查看人姓名（冗余存储）
     */
    public void recordView(String instanceId, String nodeId, String taskId, Long userId, String userName)
    {
        if (StringUtils.isEmpty(instanceId) || StringUtils.isEmpty(nodeId) || userId == null)
        {
            return;
        }
        try
        {
            // 重复查看只刷新姓名与最近任务，保留首次查看时间（「已查看人员」看的是谁什么时候看过）
            jdbcTemplate.update("INSERT INTO flow_node_viewer (instance_id, node_id, task_id, user_id, user_name, view_time) "
                    + "VALUES (?, ?, ?, ?, ?, NOW()) "
                    + "ON DUPLICATE KEY UPDATE task_id = VALUES(task_id), user_name = VALUES(user_name)",
                    instanceId, nodeId, taskId, userId, userName == null ? "" : userName);
        }
        catch (Exception e)
        {
            log.debug("记录流程节点查看失败（不影响办理）：{}", e.getMessage());
        }
    }

    /**
     * 查询某流程实例下各节点的已查看人。
     *
     * @param instanceId 流程实例ID
     * @return nodeId → [{userId, userName, time}]（按查看时间正序，同一人只一条）
     */
    public Map<String, List<Map<String, Object>>> selectViewersByNode(String instanceId)
    {
        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();
        if (StringUtils.isEmpty(instanceId))
        {
            return result;
        }
        try
        {
            jdbcTemplate.query("SELECT node_id, user_id, user_name, view_time FROM flow_node_viewer "
                    + "WHERE instance_id = ? ORDER BY view_time ASC, viewer_id ASC", rs -> {
                        String nodeId = rs.getString("node_id");
                        if (StringUtils.isEmpty(nodeId))
                        {
                            return;
                        }
                        Map<String, Object> person = new LinkedHashMap<>();
                        person.put("userId", rs.getString("user_id"));
                        person.put("userName", rs.getString("user_name"));
                        person.put("time", rs.getTimestamp("view_time"));
                        result.computeIfAbsent(nodeId, k -> new ArrayList<>()).add(person);
                    }, instanceId);
        }
        catch (Exception e)
        {
            // 未执行建表脚本时降级为「没有查看记录」
            log.debug("查询流程节点查看记录失败（忽略）：{}", e.getMessage());
        }
        return result;
    }
}

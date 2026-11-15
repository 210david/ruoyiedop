package com.ruoyi.flowable.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * 流程请求登记（对标泛微 workflow_requestbase.requestid）
 *
 * Flowable 的流程实例ID是 36 位 UUID，不便业务引用与人工沟通。
 * 本服务为每个流程实例分配一个自增的短数字编号（flow_request.request_id），
 * 作为对外展示与检索的「流程编号」，引擎内部仍使用 UUID 实例ID。
 *
 * @author ruoyi
 */
@Service
public class FlowRequestService
{
    private static final Logger log = LoggerFactory.getLogger(FlowRequestService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /** 按流程实例ID取流程编号，未登记返回 null */
    public Long requestIdOf(String instanceId)
    {
        if (instanceId == null || instanceId.isEmpty())
        {
            return null;
        }
        try
        {
            List<Long> ids = jdbcTemplate.queryForList(
                    "SELECT request_id FROM flow_request WHERE instance_id = ?", Long.class, instanceId);
            return ids.isEmpty() ? null : ids.get(0);
        }
        catch (Exception e)
        {
            log.warn("读取流程编号失败（实例 {}）：{}", instanceId, e.getMessage());
            return null;
        }
    }

    /** 按流程编号取流程实例ID，未登记返回 null */
    public String instanceIdOf(Long requestId)
    {
        if (requestId == null)
        {
            return null;
        }
        try
        {
            List<String> ids = jdbcTemplate.queryForList(
                    "SELECT instance_id FROM flow_request WHERE request_id = ?", String.class, requestId);
            return ids.isEmpty() ? null : ids.get(0);
        }
        catch (Exception e)
        {
            log.warn("读取流程实例失败（流程编号 {}）：{}", requestId, e.getMessage());
            return null;
        }
    }

    /**
     * 登记流程实例并返回流程编号（幂等）
     *
     * 引擎创建流程实例时由监听器调用；历史实例由 SQL 脚本回填。
     */
    public Long register(String instanceId, String processDefinitionKey, String businessKey)
    {
        if (instanceId == null || instanceId.isEmpty())
        {
            return null;
        }
        try
        {
            jdbcTemplate.update(
                    "INSERT IGNORE INTO flow_request (instance_id, process_definition_key, business_key, create_time) "
                            + "VALUES (?, ?, ?, NOW())",
                    instanceId, processDefinitionKey, businessKey);
            return requestIdOf(instanceId);
        }
        catch (Exception e)
        {
            log.error("流程请求登记失败（实例 {}）：{}", instanceId, e.getMessage());
            return null;
        }
    }
}

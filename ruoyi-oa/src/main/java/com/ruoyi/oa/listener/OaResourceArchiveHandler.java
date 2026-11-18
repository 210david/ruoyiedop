package com.ruoyi.oa.listener;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.ruoyi.flowable.service.FlowArchiveHandler;

/**
 * OA 资源台账流程归档处理器
 *
 * 流程办结/终止后释放台账占用状态：
 * - busUseApply（公车使用申请）：车辆「使用中」→「可用」，司机「出车中」→「在岗」
 * - sealApply（用印申请）：印章「使用中」→「正常」
 *
 * 只释放仍指向当前流程实例且处于占用状态的数据，避免误改手工维护的维修/停用/注销状态。
 *
 * @author ruoyi
 */
@Component
public class OaResourceArchiveHandler implements FlowArchiveHandler
{
    private static final Logger log = LoggerFactory.getLogger(OaResourceArchiveHandler.class);

    private static final String KEY_BUS_USE_APPLY = "busUseApply";
    private static final String KEY_SEAL_APPLY = "sealApply";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Set<String> processKeys()
    {
        return new HashSet<>(Arrays.asList(KEY_BUS_USE_APPLY, KEY_SEAL_APPLY));
    }

    @Override
    public void archive(String processKey, String instanceId, String businessKey)
    {
        release(instanceId);
    }

    @Override
    public void cancelled(String processKey, String instanceId, String businessKey)
    {
        release(instanceId);
    }

    @Override
    public void rollback(String processKey, String instanceId, String businessKey)
    {
        occupy(instanceId);
    }

    @Override
    public void rebind(String processKey, String oldInstanceId, String newInstanceId)
    {
        try
        {
            jdbcTemplate.update("UPDATE biz_oa_vehicle SET flow_instance_id = ? WHERE flow_instance_id = ?", newInstanceId, oldInstanceId);
            jdbcTemplate.update("UPDATE biz_oa_driver SET flow_instance_id = ? WHERE flow_instance_id = ?", newInstanceId, oldInstanceId);
            jdbcTemplate.update("UPDATE biz_oa_seal SET flow_instance_id = ? WHERE flow_instance_id = ?", newInstanceId, oldInstanceId);
        }
        catch (Exception e)
        {
            log.error("OA资源台账流程复活重绑失败：实例 {} -> {}，原因：{}", oldInstanceId, newInstanceId, e.getMessage(), e);
        }
    }

    /** 释放占用：仅当台账仍关联当前流程且处于占用状态时恢复为空闲 */
    private void release(String instanceId)
    {
        try
        {
            int vehicle = jdbcTemplate.update(
                    "UPDATE biz_oa_vehicle SET status = '0' WHERE flow_instance_id = ? AND status = '1'", instanceId);
            int driver = jdbcTemplate.update(
                    "UPDATE biz_oa_driver SET status = '0' WHERE flow_instance_id = ? AND status = '3'", instanceId);
            int seal = jdbcTemplate.update(
                    "UPDATE biz_oa_seal SET status = '0' WHERE flow_instance_id = ? AND status = '3'", instanceId);
            log.info("OA资源台账释放占用：实例 {}，车辆 {} 条，司机 {} 条，印章 {} 条", instanceId, vehicle, driver, seal);
        }
        catch (Exception e)
        {
            log.error("OA资源台账释放占用失败：实例 {}，原因：{}", instanceId, e.getMessage(), e);
        }
    }

    /** 复活时重新占用：仅把空闲状态置为占用 */
    private void occupy(String instanceId)
    {
        try
        {
            jdbcTemplate.update("UPDATE biz_oa_vehicle SET status = '1' WHERE flow_instance_id = ? AND status = '0'", instanceId);
            jdbcTemplate.update("UPDATE biz_oa_driver SET status = '3' WHERE flow_instance_id = ? AND status = '0'", instanceId);
            jdbcTemplate.update("UPDATE biz_oa_seal SET status = '3' WHERE flow_instance_id = ? AND status = '0'", instanceId);
        }
        catch (Exception e)
        {
            log.error("OA资源台账重新占用失败：实例 {}，原因：{}", instanceId, e.getMessage(), e);
        }
    }
}

package com.ruoyi.flowable.config;

import java.util.Arrays;
import org.flowable.common.engine.impl.AbstractEngineConfiguration;
import org.flowable.common.engine.impl.history.HistoryLevel;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.ruoyi.flowable.listener.FlowProcessEndListener;

/**
 * Flowable 流程引擎配置
 *
 * 仅对引擎行为做增强配置，不改动任何现有模块。
 * 引擎数据源默认复用主数据源（druid master），由 Flowable 自动装配完成。
 *
 * @author ruoyi
 */
@Configuration
public class FlowableEngineConfig
{
    /**
     * 流程引擎参数配置
     */
    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> processEngineConfigurer()
    {
        return (SpringProcessEngineConfiguration configuration) ->
        {
            // 数据库结构自动更新：引擎启动时自动创建/升级 act_* 表
            configuration.setDatabaseSchemaUpdate(AbstractEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
            // 历史记录级别：full，保留完整审批轨迹（含表单变量、身份信息）
            configuration.setHistoryLevel(HistoryLevel.FULL);
            // 异步执行器：启用，用于定时边界事件、异步任务
            configuration.setAsyncExecutorActivate(true);
            // 字体（流程图生成中文不乱码）
            configuration.setActivityFontName("宋体");
            configuration.setLabelFontName("宋体");
            configuration.setAnnotationFontName("宋体");
            // 操作组任务监听器：任务创建时按节点 flowable:operatorGroups 解析候选用户（部门/分部/岗位/创建人）
            // 参与者派发监听器：任务创建时按节点 flowable:participantConfig 解析并指派办理人（活动/组织/表单字段关系）
            // 参与者预置监听器已移除：会签/串签的集合改由表达式
            // ${participantCollection.collectionFor('节点ID', execution)} 在引擎求值时回调解析器，
            // 不再依赖 ACTIVITY/SEQUENCEFLOW 事件的执行时序
            // 抄送监听器：任务到达 / 办理完成时按节点 flowable:ccConfig 生成「待阅」记录
            configuration.setEventListeners(Arrays.asList(
                    new com.ruoyi.flowable.listener.OperatorGroupTaskListener(),
                    new com.ruoyi.flowable.listener.ParticipantResolveListener(),
                    new com.ruoyi.flowable.listener.FlowCcListener()));
        };
    }
}

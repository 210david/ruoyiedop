package com.ruoyi.flowable.service;

import java.util.List;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.flowable.domain.FlowProcdefExt;
import com.ruoyi.flowable.mapper.FlowProcdefExtMapper;

/**
 * 流程定义版本解析器
 *
 * <p>统一「发起流程到底用哪个版本」这一口径，避免各处各写一套 Query：</p>
 * <ul>
 *   <li>优先使用被「设为活动版本」的版本（flow_procdef_ext.active_flag = '1'）；</li>
 *   <li>没有活动版本时回退到最高版本（Flowable 的 latestVersion）；</li>
 *   <li>同 Key 出现多个活动标志时取版本号最大者（防止历史脏数据造成歧义）。</li>
 * </ul>
 *
 * <p>分成两个入口：{@link #resolve(String)} 用于流程图/XML 预览等只读场景，宽松回退；
 * {@link #requireLaunchable(String)} 用于真正发起流程，活动版本被挂起时抛明确异常，
 * 而不是静默换一个版本把人绕进去。</p>
 *
 * @author ruoyi
 */
@Component
public class FlowDefinitionResolver
{
    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private FlowProcdefExtMapper flowProcdefExtMapper;

    /**
     * 解析用于展示的版本：活动版本（未挂起）优先，否则最高版本
     *
     * @param processKey 流程定义 Key
     * @return 流程定义；该 Key 尚未部署时返回 null
     */
    public ProcessDefinition resolve(String processKey)
    {
        List<ProcessDefinition> defs = versions(processKey);
        if (defs.isEmpty())
        {
            return null;
        }
        ProcessDefinition active = activeFlagged(defs);
        if (active != null && !active.isSuspended())
        {
            return active;
        }
        ProcessDefinition usable = highestUsable(defs);
        return usable != null ? usable : defs.get(0);
    }

    /**
     * 解析用于发起流程的版本：活动版本优先
     *
     * <p>活动版本被挂起时直接抛异常，因为此时"活动版本"本身不可发起，
     * 静默回退到最高版本会让用户以为自己在用旧版本，实际却发起了一个新版本。</p>
     *
     * @param processKey 流程定义 Key
     * @return 流程定义；该 Key 尚未部署时返回 null（由调用方给出各自的提示）
     */
    public ProcessDefinition requireLaunchable(String processKey)
    {
        List<ProcessDefinition> defs = versions(processKey);
        if (defs.isEmpty())
        {
            return null;
        }
        ProcessDefinition target;
        ProcessDefinition active = activeFlagged(defs);
        if (active != null)
        {
            if (active.isSuspended())
            {
                throw new ServiceException("当前活动版本 V" + active.getVersion()
                        + " 已挂起，无法发起：请先激活该版本，或在版本管理中把其他版本设为活动版本");
            }
            target = active;
        }
        else
        {
            target = highestUsable(defs);
            if (target == null)
            {
                throw new ServiceException("流程[" + processKey + "]所有版本均已挂起，无法发起：请先在版本管理中激活其中一个版本");
            }
        }
        // 高级设置「流程锁定」：锁定即视为停止对外受理，遍历所有发起入口（通用表单/请假/预演）统一拦截
        if (isModelLocked(processKey))
        {
            throw new ServiceException("流程「" + target.getName() + "」已在高级设置中锁定，前台不可发起");
        }
        return target;
    }

    /**
     * 流程模型是否开启「高级设置 → 流程锁定」
     *
     * <p>锁定标志存在 act_re_model.META_INFO 的 locked 字段上（按流程 Key 作用于该流程的全部版本），
     * 未部署或模型已删除时视为未锁定。</p>
     *
     * @param processKey 流程定义 Key
     * @return true=已锁定，不可发起新流程
     */
    public boolean isModelLocked(String processKey)
    {
        List<Model> models = repositoryService.createModelQuery().modelKey(processKey).list();
        for (Model model : models)
        {
            try
            {
                JSONObject meta = JSONObject.parseObject(model.getMetaInfo() == null ? "{}" : model.getMetaInfo());
                if (Boolean.TRUE.equals(meta.getBoolean("locked")))
                {
                    return true;
                }
            }
            catch (Exception ignored)
            {
                // 历史脏数据解析失败按未锁定处理，不影响发起
            }
        }
        return false;
    }

    /** 版本倒序列表中第一个未挂起的版本；全部挂起时返回 null */
    private ProcessDefinition highestUsable(List<ProcessDefinition> defs)
    {
        for (ProcessDefinition def : defs)
        {
            if (!def.isSuspended())
            {
                return def;
            }
        }
        return null;
    }

    /** 同 Key 全部已部署版本，按版本号倒序 */
    private List<ProcessDefinition> versions(String processKey)
    {
        return repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processKey)
                .orderByProcessDefinitionVersion().desc()
                .list();
    }

    /** 取第一个被打上活动标志的版本（入参已按版本倒序，多活动时自然取最高版本） */
    private ProcessDefinition activeFlagged(List<ProcessDefinition> defs)
    {
        for (ProcessDefinition def : defs)
        {
            FlowProcdefExt ext = flowProcdefExtMapper.selectFlowProcdefExtByProcdefId(def.getId());
            if (ext != null && "1".equals(ext.getActiveFlag()))
            {
                return def;
            }
        }
        return null;
    }
}

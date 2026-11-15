package com.ruoyi.flowable.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.flowable.domain.FlowUrgencyLevel;
import com.ruoyi.flowable.mapper.FlowUrgencyLevelMapper;
import com.ruoyi.flowable.service.IFlowUrgencyLevelService;

/**
 * 紧急程度配置 Service 实现
 *
 * 替代原先 FlowSystemFields 中硬编码的紧急程度档位，支持自定义名称/排序/封存。
 *
 * @author ruoyi
 */
@Service
public class FlowUrgencyLevelServiceImpl implements IFlowUrgencyLevelService
{
    @Autowired
    private FlowUrgencyLevelMapper urgencyLevelMapper;

    @Override
    public List<FlowUrgencyLevel> selectFlowUrgencyLevelList(FlowUrgencyLevel query)
    {
        return urgencyLevelMapper.selectFlowUrgencyLevelList(query);
    }

    @Override
    public FlowUrgencyLevel selectFlowUrgencyLevelById(Long levelId)
    {
        return urgencyLevelMapper.selectFlowUrgencyLevelById(levelId);
    }

    @Override
    public List<FlowUrgencyLevel> selectSelectableList()
    {
        return urgencyLevelMapper.selectSelectableList();
    }

    @Override
    public int insertFlowUrgencyLevel(FlowUrgencyLevel level)
    {
        if (StringUtils.isEmpty(level.getLevelValue()))
        {
            throw new ServiceException("档位值不能为空");
        }
        if (StringUtils.isEmpty(level.getDefaultName()))
        {
            throw new ServiceException("默认名称不能为空");
        }
        // 档位值唯一校验
        if (urgencyLevelMapper.selectByLevelValue(level.getLevelValue()) != null)
        {
            throw new ServiceException("档位值已存在：" + level.getLevelValue());
        }
        if (level.getShowOrder() == null)
        {
            level.setShowOrder(0);
        }
        if (StringUtils.isEmpty(level.getIsSealed()))
        {
            level.setIsSealed("0");
        }
        if (StringUtils.isEmpty(level.getIsRemind()))
        {
            level.setIsRemind("0");
        }
        if (StringUtils.isEmpty(level.getStatus()))
        {
            level.setStatus("0");
        }
        level.setCreateBy(SecurityUtils.getUsername());
        return urgencyLevelMapper.insertFlowUrgencyLevel(level);
    }

    @Override
    public int updateFlowUrgencyLevel(FlowUrgencyLevel level)
    {
        if (level.getLevelId() == null)
        {
            throw new ServiceException("主键不能为空");
        }
        FlowUrgencyLevel exist = urgencyLevelMapper.selectFlowUrgencyLevelById(level.getLevelId());
        if (exist == null)
        {
            throw new ServiceException("紧急程度档位不存在");
        }
        level.setUpdateBy(SecurityUtils.getUsername());
        return urgencyLevelMapper.updateFlowUrgencyLevel(level);
    }

    @Override
    public int deleteFlowUrgencyLevelById(Long levelId)
    {
        return urgencyLevelMapper.deleteFlowUrgencyLevelById(levelId);
    }

    @Override
    public int deleteFlowUrgencyLevelByIds(Long[] levelIds)
    {
        return urgencyLevelMapper.deleteFlowUrgencyLevelByIds(levelIds);
    }

    @Override
    public boolean isValidSelectable(String levelValue)
    {
        if (StringUtils.isEmpty(levelValue))
        {
            return false;
        }
        FlowUrgencyLevel level = urgencyLevelMapper.selectByLevelValue(levelValue);
        if (level == null)
        {
            return false;
        }
        return "0".equals(level.getStatus()) && !"1".equals(level.getIsSealed());
    }
}

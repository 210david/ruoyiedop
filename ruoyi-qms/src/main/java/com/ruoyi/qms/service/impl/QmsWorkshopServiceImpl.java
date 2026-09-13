package com.ruoyi.qms.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.qms.domain.QmsWorkshop;
import com.ruoyi.qms.mapper.QmsWorkshopMapper;
import com.ruoyi.qms.service.IQmsWorkshopService;

/**
 * 车间/产线/工位 Service实现
 *
 * @author ruoyi
 */
@Service
public class QmsWorkshopServiceImpl implements IQmsWorkshopService
{
    @Autowired
    private QmsWorkshopMapper qmsWorkshopMapper;

    @Override
    public List<QmsWorkshop> selectWorkshopList(QmsWorkshop workshop)
    {
        return qmsWorkshopMapper.selectWorkshopList(workshop);
    }

    @Override
    public List<QmsWorkshop> buildWorkshopTree(List<QmsWorkshop> list)
    {
        List<QmsWorkshop> tree = new ArrayList<>();
        for (QmsWorkshop node : list)
        {
            if (node.getParentId() == null || node.getParentId() == 0L)
            {
                tree.add(node);
            }
        }
        for (QmsWorkshop root : tree)
        {
            recursiveBuildChildren(root, list);
        }
        return tree;
    }

    private void recursiveBuildChildren(QmsWorkshop parent, List<QmsWorkshop> all)
    {
        List<QmsWorkshop> children = all.stream()
                .filter(n -> parent.getWorkshopId().equals(n.getParentId()))
                .collect(Collectors.toList());
        parent.setChildren(children);
        for (QmsWorkshop child : children)
        {
            recursiveBuildChildren(child, all);
        }
    }

    @Override
    public QmsWorkshop selectWorkshopById(Long workshopId)
    {
        return qmsWorkshopMapper.selectWorkshopById(workshopId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertWorkshop(QmsWorkshop workshop)
    {
        // 编码唯一性校验，避免重复编码触发数据库异常
        QmsWorkshop exist = qmsWorkshopMapper.checkWorkshopCodeUnique(workshop.getWorkshopCode());
        if (exist != null)
        {
            throw new ServiceException("节点编码[" + workshop.getWorkshopCode() + "]已存在，请修改后重试");
        }
        workshop.setDelFlag("0");
        if (workshop.getStatus() == null)
        {
            workshop.setStatus("0");
        }
        if (workshop.getOrderNum() == null)
        {
            workshop.setOrderNum(0);
        }
        if (workshop.getParentId() == null)
        {
            workshop.setParentId(0L);
        }
        return qmsWorkshopMapper.insertWorkshop(workshop);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateWorkshop(QmsWorkshop workshop)
    {
        // 编码唯一性校验（排除自身），避免重复编码触发数据库异常
        if (workshop.getWorkshopCode() != null && !workshop.getWorkshopCode().isEmpty())
        {
            QmsWorkshop exist = qmsWorkshopMapper.checkWorkshopCodeUnique(workshop.getWorkshopCode());
            if (exist != null && !exist.getWorkshopId().equals(workshop.getWorkshopId()))
            {
                throw new ServiceException("节点编码[" + workshop.getWorkshopCode() + "]已存在，请修改后重试");
            }
        }
        return qmsWorkshopMapper.updateWorkshop(workshop);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteWorkshopByIds(Long[] workshopIds)
    {
        for (Long workshopId : workshopIds)
        {
            if (qmsWorkshopMapper.hasChildByWorkshopId(workshopId) > 0)
            {
                throw new ServiceException("存在子节点，不允许删除");
            }
        }
        return qmsWorkshopMapper.deleteWorkshopByIds(workshopIds);
    }
}

package com.ruoyi.mk.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.domain.MkActivity;
import com.ruoyi.mk.domain.MkActivityStatusLog;
import com.ruoyi.mk.mapper.MkActivityMapper;
import com.ruoyi.mk.mapper.MkActivityStatusLogMapper;
import com.ruoyi.mk.service.IMkActivityService;
import com.ruoyi.mk.service.IMkNumberRuleService;

/**
 * 营销活动 Service实现
 *
 * @author ruoyi
 */
@Service
public class MkActivityServiceImpl implements IMkActivityService
{
    @Autowired
    private MkActivityMapper mkActivityMapper;

    @Autowired
    private MkActivityStatusLogMapper mkActivityStatusLogMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<MkActivity> selectActivityList(MkActivity activity)
    {
        return mkActivityMapper.selectActivityList(activity);
    }

    @Override
    public MkActivity selectActivityById(Long activityId)
    {
        return mkActivityMapper.selectActivityById(activityId);
    }

    @Override
    public int insertActivity(MkActivity activity)
    {
        activity.setDelFlag("0");
        if (activity.getActivityStatus() == null)
        {
            activity.setActivityStatus("0");
        }
        if (activity.getActivityForm() == null)
        {
            activity.setActivityForm("0");
        }
        if (activity.getCost() == null)
        {
            activity.setCost(BigDecimal.ZERO);
        }
        // 自动生成活动编号
        if (StringUtils.isEmpty(activity.getActivityNo()))
        {
            activity.setActivityNo(mkNumberRuleService.generateNumber("activity"));
        }
        return mkActivityMapper.insertActivity(activity);
    }

    @Override
    public int updateActivity(MkActivity activity)
    {
        return mkActivityMapper.updateActivity(activity);
    }

    @Override
    public int deleteActivityByIds(Long[] activityIds)
    {
        return mkActivityMapper.deleteActivityByIds(activityIds);
    }

    /** P0-1: 活动状态流转 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changeStatus(Long activityId, String toStatus, String remark)
    {
        MkActivity dbActivity = mkActivityMapper.selectActivityById(activityId);
        if (dbActivity == null)
        {
            throw new ServiceException("活动不存在");
        }
        String fromStatus = dbActivity.getActivityStatus();
        // 状态流转校验
        validateStatusTransition(fromStatus, toStatus);
        // 发布时校验必填项
        if ("1".equals(toStatus))
        {
            if (dbActivity.getStartTime() == null || dbActivity.getEndTime() == null)
            {
                throw new ServiceException("发布活动前请填写开始时间和结束时间");
            }
            if (dbActivity.getUserId() == null)
            {
                throw new ServiceException("发布活动前请选择负责人");
            }
        }
        // 取消时需要备注
        if ("5".equals(toStatus) && StringUtils.isEmpty(remark))
        {
            throw new ServiceException("取消活动需要填写原因");
        }
        String username = SecurityUtils.getUsername();
        mkActivityMapper.updateActivityStatus(activityId, toStatus, username);
        // 记录流转日志
        MkActivityStatusLog log = new MkActivityStatusLog();
        log.setActivityId(activityId);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setOperatorId(SecurityUtils.getUserId());
        log.setOperatorName(username);
        log.setRemark(remark);
        mkActivityStatusLogMapper.insertStatusLog(log);
        return 1;
    }

    /** 状态流转校验：0草稿→1已发布→2进行中→3已结束→4已归档；1/2→5已取消 */
    private void validateStatusTransition(String from, String to)
    {
        // 允许的流转关系
        boolean valid = false;
        if ("0".equals(from) && "1".equals(to)) valid = true;       // 草稿→发布
        else if ("1".equals(from) && "2".equals(to)) valid = true;  // 发布→开始
        else if ("1".equals(from) && "5".equals(to)) valid = true;  // 发布→取消
        else if ("2".equals(from) && "3".equals(to)) valid = true;  // 进行中→结束
        else if ("2".equals(from) && "5".equals(to)) valid = true;  // 进行中→取消
        else if ("3".equals(from) && "4".equals(to)) valid = true;  // 已结束→归档
        else if ("3".equals(from) && "3".equals(to)) valid = true;  // 已结束→复盘(不变状态)
        if (!valid)
        {
            throw new ServiceException("不允许从状态[" + from + "]变更为[" + to + "]");
        }
    }

    @Override
    public List<MkActivityStatusLog> selectStatusLogs(Long activityId)
    {
        return mkActivityStatusLogMapper.selectStatusLogList(activityId);
    }

    /** P1-4: 活动复盘回填 - 自动统计 */
    @Override
    public Map<String, Object> reviewActivity(Long activityId)
    {
        MkActivity dbActivity = mkActivityMapper.selectActivityById(activityId);
        if (dbActivity == null)
        {
            throw new ServiceException("活动不存在");
        }
        Map<String, Object> stats = mkActivityMapper.selectActivityStats(activityId);
        // 计算ROI
        BigDecimal budget = dbActivity.getBudget() != null ? dbActivity.getBudget() : BigDecimal.ZERO;
        BigDecimal dealAmount = new BigDecimal(stats.get("dealAmount").toString());
        if (budget.compareTo(BigDecimal.ZERO) > 0)
        {
            BigDecimal roi = dealAmount.subtract(budget).divide(budget, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
            stats.put("actualRoi", roi);
        }
        else
        {
            stats.put("actualRoi", BigDecimal.ZERO);
        }
        stats.put("budget", budget);
        stats.put("activityId", activityId);
        return stats;
    }

    /** P1-4: 保存复盘结果 */
    @Override
    public int saveReview(MkActivity activity)
    {
        activity.setUpdateBy(SecurityUtils.getUsername());
        return mkActivityMapper.updateActivity(activity);
    }

    /** P2-11: 活动复制 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int copyActivity(Long activityId)
    {
        MkActivity source = mkActivityMapper.selectActivityById(activityId);
        if (source == null)
        {
            throw new ServiceException("源活动不存在");
        }
        MkActivity target = new MkActivity();
        target.setActivityName(source.getActivityName() + "_副本");
        target.setActivityType(source.getActivityType());
        target.setActivityForm(source.getActivityForm());
        target.setActivityStatus("0"); // 草稿
        target.setLocation(source.getLocation());
        target.setBudget(source.getBudget());
        target.setTargetCount(source.getTargetCount());
        target.setTargetLeadCount(source.getTargetLeadCount());
        target.setUserId(source.getUserId());
        target.setDeptId(source.getDeptId());
        target.setSummary(source.getSummary());
        target.setContent(source.getContent());
        target.setActivityNo(mkNumberRuleService.generateNumber("activity"));
        target.setCreateBy(SecurityUtils.getUsername());
        return mkActivityMapper.insertActivity(target);
    }

    /** P3-12: 批量更新状态 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchChangeStatus(Long[] activityIds, String status)
    {
        String username = SecurityUtils.getUsername();
        for (Long id : activityIds)
        {
            MkActivity db = mkActivityMapper.selectActivityById(id);
            if (db != null)
            {
                validateStatusTransition(db.getActivityStatus(), status);
                MkActivityStatusLog log = new MkActivityStatusLog();
                log.setActivityId(id);
                log.setFromStatus(db.getActivityStatus());
                log.setToStatus(status);
                log.setOperatorId(SecurityUtils.getUserId());
                log.setOperatorName(username);
                mkActivityStatusLogMapper.insertStatusLog(log);
            }
        }
        return mkActivityMapper.batchUpdateStatus(activityIds, status, username);
    }

    /** P3-12: 批量更新负责人 */
    @Override
    public int batchUpdateOwner(Long[] activityIds, Long userId, String userName, Long deptId)
    {
        return mkActivityMapper.batchUpdateOwner(activityIds, userId, userName, deptId, SecurityUtils.getUsername());
    }
}

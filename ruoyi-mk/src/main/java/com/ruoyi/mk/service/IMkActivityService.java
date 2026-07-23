package com.ruoyi.mk.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.mk.domain.MkActivity;
import com.ruoyi.mk.domain.MkActivityStatusLog;

/**
 * 营销活动 Service接口
 *
 * @author ruoyi
 */
public interface IMkActivityService
{
    public List<MkActivity> selectActivityList(MkActivity activity);
    public MkActivity selectActivityById(Long activityId);
    public int insertActivity(MkActivity activity);
    public int updateActivity(MkActivity activity);
    public int deleteActivityByIds(Long[] activityIds);

    /** P0-1: 活动状态流转 */
    public int changeStatus(Long activityId, String toStatus, String remark);

    /** P0-1: 查询状态流转日志 */
    public List<MkActivityStatusLog> selectStatusLogs(Long activityId);

    /** P1-4: 活动复盘回填 */
    public Map<String, Object> reviewActivity(Long activityId);

    /** P1-4: 保存复盘结果 */
    public int saveReview(MkActivity activity);

    /** P2-11: 活动复制 */
    public int copyActivity(Long activityId);

    /** P3-12: 批量更新状态 */
    public int batchChangeStatus(Long[] activityIds, String status);

    /** P3-12: 批量更新负责人 */
    public int batchUpdateOwner(Long[] activityIds, Long userId, String userName, Long deptId);
}

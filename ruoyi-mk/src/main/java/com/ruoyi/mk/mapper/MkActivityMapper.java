package com.ruoyi.mk.mapper;

import java.util.List;
import java.util.Map;
import com.ruoyi.mk.domain.MkActivity;
import org.apache.ibatis.annotations.Param;

public interface MkActivityMapper
{
    public List<MkActivity> selectActivityList(MkActivity activity);
    public MkActivity selectActivityById(Long activityId);
    public int insertActivity(MkActivity activity);
    public int updateActivity(MkActivity activity);
    public int deleteActivityByIds(Long[] activityIds);

    /** 更新活动状态 (P0-1) */
    public int updateActivityStatus(@Param("activityId") Long activityId, @Param("status") String status, @Param("updateBy") String updateBy);

    /** 批量更新活动状态 (P3-12) */
    public int batchUpdateStatus(@Param("activityIds") Long[] activityIds, @Param("status") String status, @Param("updateBy") String updateBy);

    /** 批量更新活动负责人 (P3-12) */
    public int batchUpdateOwner(@Param("activityIds") Long[] activityIds, @Param("userId") Long userId, @Param("userName") String userName, @Param("deptId") Long deptId, @Param("updateBy") String updateBy);

    /** 统计活动的报名/签到/线索数 (P2-9) */
    public Map<String, Object> selectActivityStats(@Param("activityId") Long activityId);

    /** P3-15: 根据活动ID和安全令牌查询活动（公开接口专用） */
    public MkActivity selectActivityByToken(@Param("activityId") Long activityId, @Param("registerToken") String registerToken);

    /** P3-15: 更新活动报名令牌 */
    public int updateRegisterToken(@Param("activityId") Long activityId, @Param("registerToken") String registerToken);
}

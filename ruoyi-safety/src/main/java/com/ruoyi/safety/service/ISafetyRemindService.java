package com.ruoyi.safety.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.safety.domain.SafetyRemind;

public interface ISafetyRemindService {
    public SafetyRemind selectSafetyRemindById(Long remindId);
    public List<SafetyRemind> selectSafetyRemindList(SafetyRemind safetyRemind);
    public int insertSafetyRemind(SafetyRemind safetyRemind);
    public int updateSafetyRemind(SafetyRemind safetyRemind);
    public int deleteSafetyRemindByIds(Long[] remindIds);
    public int deleteSafetyRemindById(Long remindId);
    public int handleRemind(SafetyRemind safetyRemind);
    public int countByStatus(String remindStatus);

    /**
     * 到期提醒按类型统计
     */
    public List<Map<String, Object>> getRemindCountByType();

    /**
     * 即将到期提醒列表（未来30天）
     */
    public List<Map<String, Object>> getUpcomingReminds();
}
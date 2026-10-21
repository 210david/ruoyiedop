package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.SysMessageSetting;

/**
 * 消息通知设置Mapper
 *
 * @author ruoyi
 */
public interface SysMessageSettingMapper
{
    /** 查询用户通知设置（无记录返回null，业务层兜底全开） */
    public SysMessageSetting selectSettingByUserId(Long userId);

    /** 新增用户通知设置 */
    public int insertSetting(SysMessageSetting setting);

    /** 更新用户通知设置 */
    public int updateSetting(SysMessageSetting setting);
}

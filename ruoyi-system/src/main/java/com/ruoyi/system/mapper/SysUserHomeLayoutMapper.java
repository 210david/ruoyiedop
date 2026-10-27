package com.ruoyi.system.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户门户首页布局配置 Mapper
 *
 * @author ruoyi
 */
public interface SysUserHomeLayoutMapper
{
    /**
     * 查询用户首页模块显示配置
     *
     * @param userId 用户ID
     * @return 模块编码（逗号分隔）
     */
    @Select("SELECT module_codes FROM sys_user_home_layout WHERE user_id = #{userId}")
    public String selectModuleCodesByUserId(@Param("userId") Long userId);

    /**
     * 保存用户首页模块显示配置（存在则更新）
     *
     * @param userId 用户ID
     * @param moduleCodes 模块编码（逗号分隔）
     * @param updateBy 更新者
     * @return 结果
     */
    @Insert("INSERT INTO sys_user_home_layout(user_id, module_codes, update_by, update_time) "
            + "VALUES(#{userId}, #{moduleCodes}, #{updateBy}, NOW()) "
            + "ON DUPLICATE KEY UPDATE module_codes = #{moduleCodes}, update_by = #{updateBy}, update_time = NOW()")
    public int saveHomeLayout(@Param("userId") Long userId, @Param("moduleCodes") String moduleCodes, @Param("updateBy") String updateBy);
}

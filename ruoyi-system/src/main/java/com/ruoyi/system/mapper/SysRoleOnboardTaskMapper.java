package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.ruoyi.system.domain.SysRoleOnboardTask;

/**
 * 岗位新人任务模板 Mapper
 *
 * @author ruoyi
 */
public interface SysRoleOnboardTaskMapper
{
    /**
     * 按角色（岗位）查询启用的任务模板
     *
     * @param roleIds 角色ID集合
     * @return 任务模板列表
     */
    @Select("<script>"
            + "SELECT t.id, t.role_id AS roleId, t.seq, t.title, t.description, t.menu_path AS menuPath, "
            + "t.check_type AS checkType, t.check_param AS checkParam, t.enabled, r.role_name AS roleName "
            + "FROM sys_role_onboard_task t LEFT JOIN sys_role r ON r.role_id = t.role_id "
            + "WHERE t.enabled = '0' AND t.role_id IN "
            + "<foreach collection='roleIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> "
            + "ORDER BY t.role_id, t.seq"
            + "</script>")
    public List<SysRoleOnboardTask> selectByRoleIds(@Param("roleIds") List<Long> roleIds);

    /**
     * 查询用户手动确认完成的任务ID集合
     */
    @Select("SELECT task_id FROM sys_user_onboard_progress WHERE user_id = #{userId}")
    public List<Long> selectManualDoneTaskIds(@Param("userId") Long userId);

    /**
     * 记录一次任务手工完成（幂等）
     */
    @Insert("INSERT INTO sys_user_onboard_progress(user_id, task_id, done_time) VALUES(#{userId}, #{taskId}, NOW()) "
            + "ON DUPLICATE KEY UPDATE done_time = NOW()")
    public int insertProgress(@Param("userId") Long userId, @Param("taskId") Long taskId);

    /**
     * 查询用户级新手引导开关（0开启 1关闭；无记录视为开启）
     */
    @Select("SELECT enabled FROM sys_user_onboard_setting WHERE user_id = #{userId}")
    public String selectUserEnabled(@Param("userId") Long userId);

    /**
     * 保存用户级新手引导开关
     *
     * @param enabled 0开启 1关闭
     */
    @Insert("INSERT INTO sys_user_onboard_setting(user_id, enabled, update_time) VALUES(#{userId}, #{enabled}, NOW()) "
            + "ON DUPLICATE KEY UPDATE enabled = #{enabled}, update_time = NOW()")
    public int saveUserEnabled(@Param("userId") Long userId, @Param("enabled") String enabled);
}

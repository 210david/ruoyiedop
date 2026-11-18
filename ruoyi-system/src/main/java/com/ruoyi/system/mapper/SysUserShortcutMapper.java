package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.ruoyi.system.domain.SysUserShortcut;

/**
 * 用户常用功能 Mapper
 *
 * @author ruoyi
 */
public interface SysUserShortcutMapper
{
    /**
     * 查询用户常用功能（手动固定的排在前面，其余按访问频次降序）
     *
     * 防御说明：历史脏数据（menu_path 为 '//xxx' 或 menu_title 为空）会导致前端渲染出
     * "只有图标、点击 404"的按钮，这里直接排除，避免脏数据被摆到常用功能第一位。
     *
     * @param userId 用户ID
     * @param limit 返回条数
     * @return 常用功能列表
     */
    @Select("SELECT id, user_id AS userId, menu_path AS menuPath, menu_title AS menuTitle, "
            + "menu_icon AS menuIcon, module_code AS moduleCode, source, visit_count AS visitCount "
            + "FROM sys_user_shortcut WHERE user_id = #{userId} "
            + "AND menu_path LIKE '/%' AND menu_path NOT LIKE '//%' "
            + "AND menu_title IS NOT NULL AND menu_title <> '' "
            + "ORDER BY (source = 'M') DESC, visit_count DESC, last_visit_time DESC LIMIT #{limit}")
    public List<SysUserShortcut> selectByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 记录一次菜单访问（不存在则新建，存在则累加次数）
     *
     * 60 秒内重复访问同一菜单不重复计数，避免刷新页面刷高频次；last_visit_time 始终更新。
     */
    @Insert("INSERT INTO sys_user_shortcut(user_id, menu_path, menu_title, menu_icon, module_code, source, visit_count, last_visit_time, create_time, update_time) "
            + "VALUES(#{userId}, #{menuPath}, #{menuTitle}, #{menuIcon}, #{moduleCode}, 'A', 1, NOW(), NOW(), NOW()) "
            + "ON DUPLICATE KEY UPDATE "
            + "visit_count = visit_count + IF(last_visit_time IS NULL OR last_visit_time < DATE_SUB(NOW(), INTERVAL 60 SECOND), 1, 0), "
            + "last_visit_time = NOW(), "
            + "menu_title = VALUES(menu_title), menu_icon = VALUES(menu_icon), "
            + "module_code = VALUES(module_code), update_time = NOW()")
    public int recordVisit(@Param("userId") Long userId, @Param("menuPath") String menuPath,
            @Param("menuTitle") String menuTitle, @Param("menuIcon") String menuIcon,
            @Param("moduleCode") String moduleCode);

    /**
     * 清空用户手动固定的常用功能
     */
    @Delete("DELETE FROM sys_user_shortcut WHERE user_id = #{userId} AND source = 'M'")
    public int deleteManualByUserId(@Param("userId") Long userId);

    /**
     * 新增一条手动固定的常用功能
     */
    @Insert("INSERT INTO sys_user_shortcut(user_id, menu_path, menu_title, menu_icon, module_code, source, visit_count, last_visit_time, create_time, update_time) "
            + "VALUES(#{userId}, #{menuPath}, #{menuTitle}, #{menuIcon}, #{moduleCode}, 'M', 1, NOW(), NOW(), NOW()) "
            + "ON DUPLICATE KEY UPDATE source = 'M', menu_title = VALUES(menu_title), "
            + "menu_icon = VALUES(menu_icon), module_code = VALUES(module_code), update_time = NOW()")
    public int insertManual(@Param("userId") Long userId, @Param("menuPath") String menuPath,
            @Param("menuTitle") String menuTitle, @Param("menuIcon") String menuIcon,
            @Param("moduleCode") String moduleCode);

    /**
     * 查询用户访问过的菜单路由集合（用于新人任务 MENU_VISITED 判定）
     */
    @Select("SELECT menu_path FROM sys_user_shortcut WHERE user_id = #{userId} AND visit_count > 0")
    public List<String> selectVisitedPaths(@Param("userId") Long userId);
}

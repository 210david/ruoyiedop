package com.ruoyi.flowable.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;

/**
 * 流程人员选择用的「组织全量」数据源。
 *
 * <p>流程办理页的转办 / 加签 / 抄送，本质是「把流程交给组织内的人」，
 * 与业务数据权限无关：普通员工点抄送时也应该能选到全公司任何一个人。
 * 而 {@code /system/user/list}、{@code /system/user/deptTree} 走的是
 * {@code @DataScope} 数据范围过滤（普通员工 data_scope=5 时只能看到自己、部门树为空），
 * 直接用会把选人范围压成「只有自己」。</p>
 *
 * <p>因此这里只做两件事：按组织机构读取全部在职用户、读取全部正常部门树，
 * 不做数据范围过滤；查询字段刻意与前端 {@code UserPicker} 期望的结构保持一致，
 * 以免影响其它调用方。</p>
 *
 * @author ruoyi
 */
@Service
public class FlowOrgPickerService
{
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 组织全量用户（不受数据权限限制）。
     *
     * @param keyword 姓名/账号关键字，可为空
     * @return 用户列表，字段与 SysUser 常用结构对齐：userId/nickName/userName/deptId/dept/deptName/postName/phonenumber
     */
    public List<Map<String, Object>> selectOrgUsers(String keyword)
    {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT u.user_id, u.nick_name, u.user_name, u.dept_id, u.phonenumber, d.dept_name, ")
           .append("(SELECT GROUP_CONCAT(p.post_name) FROM sys_user_post up ")
           .append(" LEFT JOIN sys_post p ON p.post_id = up.post_id WHERE up.user_id = u.user_id) AS post_name ")
           .append("FROM sys_user u LEFT JOIN sys_dept d ON d.dept_id = u.dept_id ")
           .append("WHERE u.del_flag = '0' AND u.status = '0' ");
        List<Object> args = new ArrayList<>();
        if (StringUtils.isNotEmpty(keyword))
        {
            sql.append("AND (u.nick_name LIKE ? OR u.user_name LIKE ?) ");
            args.add("%" + keyword.trim() + "%");
            args.add("%" + keyword.trim() + "%");
        }
        sql.append("ORDER BY COALESCE(d.order_num, 9999), u.dept_id, u.user_id");

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), args.toArray());
        List<Map<String, Object>> result = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows)
        {
            Long userId = toLong(row.get("user_id"));
            Long deptId = toLong(row.get("dept_id"));
            String deptName = toStr(row.get("dept_name"));

            Map<String, Object> user = new LinkedHashMap<>();
            user.put("userId", userId);
            user.put("nickName", toStr(row.get("nick_name")));
            user.put("userName", toStr(row.get("user_name")));
            user.put("deptId", deptId);
            user.put("deptName", deptName);
            user.put("postName", toStr(row.get("post_name")));
            user.put("phonenumber", toStr(row.get("phonenumber")));
            if (deptId != null)
            {
                Map<String, Object> dept = new LinkedHashMap<>();
                dept.put("deptId", deptId);
                dept.put("deptName", deptName);
                user.put("dept", dept);
            }
            result.add(user);
        }
        return result;
    }

    /**
     * 组织全量部门树（不受数据权限限制），节点结构为前端 el-tree 需要的 { id, label, children }。
     *
     * @return 部门树（顶级为根部门）
     */
    public List<Map<String, Object>> selectOrgDeptTree()
    {
        String sql = "SELECT dept_id, parent_id, dept_name FROM sys_dept "
                + "WHERE del_flag = '0' AND status = '0' ORDER BY parent_id, order_num, dept_id";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

        Map<Long, Map<String, Object>> nodes = new LinkedHashMap<>();
        List<Map<String, Object>> roots = new ArrayList<>();
        for (Map<String, Object> row : rows)
        {
            Long deptId = toLong(row.get("dept_id"));
            if (deptId == null)
            {
                continue;
            }
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", deptId);
            node.put("label", toStr(row.get("dept_name")));
            node.put("children", new ArrayList<Map<String, Object>>());
            nodes.put(deptId, node);
        }
        for (Map<String, Object> row : rows)
        {
            Long deptId = toLong(row.get("dept_id"));
            Long parentId = toLong(row.get("parent_id"));
            if (deptId == null)
            {
                continue;
            }
            Map<String, Object> node = nodes.get(deptId);
            Map<String, Object> parent = parentId == null || parentId.equals(deptId) ? null : nodes.get(parentId);
            if (parent == null)
            {
                roots.add(node);
            }
            else
            {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> children = (List<Map<String, Object>>) parent.get("children");
                children.add(node);
            }
        }
        // 去掉空 children，前端据此把叶子节点渲染成「人员/末级部门」图标
        stripEmptyChildren(roots);
        return roots;
    }

    private void stripEmptyChildren(List<Map<String, Object>> list)
    {
        for (Map<String, Object> node : list)
        {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
            if (children == null || children.isEmpty())
            {
                node.remove("children");
            }
            else
            {
                stripEmptyChildren(children);
            }
        }
    }

    private static Long toLong(Object value)
    {
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        if (value == null)
        {
            return null;
        }
        try
        {
            return Long.valueOf(String.valueOf(value));
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private static String toStr(Object value)
    {
        return value == null ? null : String.valueOf(value);
    }
}

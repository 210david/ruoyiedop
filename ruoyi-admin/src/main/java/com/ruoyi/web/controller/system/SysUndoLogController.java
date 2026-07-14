package com.ruoyi.web.controller.system;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 操作撤销控制器
 * 提供撤销日志查询和数据回滚功能
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/undo")
public class SysUndoLogController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(SysUndoLogController.class);

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    /**
     * 查询当前用户可撤销的操作列表
     */
    @GetMapping("/list")
    public AjaxResult list()
    {
        try
        {
            String currentUser = getCurrentUserName();
            List<Map<String, Object>> resultList = new ArrayList<>();

            String querySql = "SELECT id, entity_type, entity_id, operate_type, create_time, expire_time "
                    + "FROM sys_undo_log WHERE create_by = ? AND expire_time > NOW() ORDER BY create_time DESC";

            try (SqlSession sqlSession = sqlSessionFactory.openSession())
            {
                Connection conn = sqlSession.getConnection();
                try (PreparedStatement ps = conn.prepareStatement(querySql))
                {
                    ps.setString(1, currentUser);
                    try (ResultSet rs = ps.executeQuery())
                    {
                        ResultSetMetaData metaData = rs.getMetaData();
                        while (rs.next())
                        {
                            Map<String, Object> row = new LinkedHashMap<>();
                            for (int i = 1; i <= metaData.getColumnCount(); i++)
                            {
                                row.put(metaData.getColumnLabel(i), rs.getObject(i));
                            }
                            resultList.add(row);
                        }
                    }
                }
            }
            return AjaxResult.success(resultList);
        }
        catch (Exception e)
        {
            log.error("查询撤销日志异常", e);
            return AjaxResult.error("查询撤销日志失败，请稍后重试");
        }
    }

    /**
     * 执行撤销回滚操作
     * @param id 撤销日志ID
     */
    @PostMapping("/rollback/{id}")
    public AjaxResult rollback(@PathVariable Long id)
    {
        try
        {
            // 查询撤销日志
            String querySql = "SELECT entity_type, entity_id, data_snapshot, operate_type "
                    + "FROM sys_undo_log WHERE id = ? AND expire_time > NOW()";

            String entityType = null;
            String entityId = null;
            String dataSnapshot = null;

            try (SqlSession sqlSession = sqlSessionFactory.openSession())
            {
                Connection conn = sqlSession.getConnection();
                try (PreparedStatement ps = conn.prepareStatement(querySql))
                {
                    ps.setLong(1, id);
                    try (ResultSet rs = ps.executeQuery())
                    {
                        if (rs.next())
                        {
                            entityType = rs.getString("entity_type");
                            entityId = rs.getString("entity_id");
                            dataSnapshot = rs.getString("data_snapshot");
                        }
                    }
                }
            }

            if (entityType == null || dataSnapshot == null)
            {
                return AjaxResult.error("撤销记录不存在或已过期");
            }

            // 解析快照JSON，构建UPDATE语句回滚数据
            JSONObject snapshot = JSON.parseObject(dataSnapshot);
            Map<String, Object> snapshotMap = new HashMap<>(snapshot);

            // 先查询该记录当前是否存在
            String checkSql = "SELECT COUNT(1) FROM " + entityType + " WHERE " + getKeyColumn(entityType) + " = ?";
            boolean exists = false;
            try (SqlSession sqlSession = sqlSessionFactory.openSession())
            {
                Connection conn = sqlSession.getConnection();
                try (PreparedStatement ps = conn.prepareStatement(checkSql))
                {
                    ps.setObject(1, entityId);
                    try (ResultSet rs = ps.executeQuery())
                    {
                        if (rs.next() && rs.getInt(1) > 0)
                        {
                            exists = true;
                        }
                    }
                }
            }

            if (!exists)
            {
                return AjaxResult.error("原记录已不存在，无法回滚");
            }

            // 构建动态UPDATE语句
            String keyColumn = getKeyColumn(entityType);
            StringBuilder updateSql = new StringBuilder("UPDATE ").append(entityType).append(" SET ");
            List<Object> params = new ArrayList<>();

            boolean first = true;
            for (Map.Entry<String, Object> entry : snapshotMap.entrySet())
            {
                String column = entry.getKey();
                // 跳过主键列
                if (column.equalsIgnoreCase(keyColumn))
                {
                    continue;
                }
                if (!first)
                {
                    updateSql.append(", ");
                }
                updateSql.append(column).append(" = ?");
                params.add(entry.getValue());
                first = false;
            }
            updateSql.append(" WHERE ").append(keyColumn).append(" = ?");
            params.add(entityId);

            // 执行回滚
            try (SqlSession sqlSession = sqlSessionFactory.openSession(true))
            {
                Connection conn = sqlSession.getConnection();
                try (PreparedStatement ps = conn.prepareStatement(updateSql.toString()))
                {
                    for (int i = 0; i < params.size(); i++)
                    {
                        ps.setObject(i + 1, params.get(i));
                    }
                    int affected = ps.executeUpdate();
                    if (affected > 0)
                    {
                        log.info("撤销回滚成功，实体[{}]ID[{}]，操作ID[{}]", entityType, entityId, id);
                    }
                }
            }

            // 删除已使用的撤销日志
            deleteUndoLog(id);

            return AjaxResult.success("数据已回滚成功");
        }
        catch (Exception e)
        {
            log.error("执行撤销回滚异常", e);
            return AjaxResult.error("回滚操作失败，请稍后重试");
        }
    }

    /**
     * 删除撤销日志记录
     */
    @DeleteMapping("/{id}")
    public AjaxResult deleteLog(@PathVariable Long id)
    {
        deleteUndoLog(id);
        return AjaxResult.success();
    }

    private void deleteUndoLog(Long id)
    {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(true))
        {
            Connection conn = sqlSession.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM sys_undo_log WHERE id = ?"))
            {
                ps.setLong(1, id);
                ps.executeUpdate();
            }
        }
        catch (Exception e)
        {
            log.error("删除撤销日志异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 推断主键列名（常见命名：id、xx_id）
     */
    private String getKeyColumn(String tableName)
    {
        // 从表名推断主键列，如 sys_user → user_id
        if (tableName.startsWith("sys_") || tableName.startsWith("gen_"))
        {
            String suffix = tableName.substring(tableName.indexOf('_') + 1);
            return suffix + "_id";
        }
        return "id";
    }

    /**
     * 获取当前登录用户名
     */
    private String getCurrentUserName()
    {
        try
        {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            if (loginUser != null)
            {
                return loginUser.getUsername();
            }
        }
        catch (Exception e)
        {
            // ignore
        }
        return "anonymous";
    }
}

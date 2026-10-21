package com.ruoyi.common.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 导出数据勾选过滤工具
 * 前端导出时若勾选了数据，通过 params 传递：
 *   exportIds     - 勾选记录的主键集合（逗号分隔字符串）
 *   exportIdField - 主键字段名（如 equipmentId）
 * 未勾选时 exportIds 为空，按筛选条件导出全部数据（不做过滤）
 */
public class ExportIdFilter
{
    public static <T> List<T> filter(List<T> list, BaseEntity query)
    {
        if (list == null || list.isEmpty() || query == null
                || query.getParams() == null)
        {
            return list;
        }
        Object idsObj = query.getParams().get("exportIds");
        if (idsObj == null || idsObj.toString().trim().isEmpty())
        {
            return list;
        }
        Set<Long> selectedIds = new HashSet<>();
        for (String s : idsObj.toString().split(","))
        {
            if (!s.trim().isEmpty())
            {
                try
                {
                    selectedIds.add(Long.valueOf(s.trim()));
                }
                catch (NumberFormatException ignored)
                {
                }
            }
        }
        if (selectedIds.isEmpty())
        {
            return list;
        }
        Object fieldObj = query.getParams().get("exportIdField");
        String idField = fieldObj != null ? fieldObj.toString().trim() : null;
        Method getter = findIdGetter(list.get(0).getClass(), idField);
        if (getter == null)
        {
            return list;
        }
        List<T> result = new ArrayList<>();
        for (T item : list)
        {
            try
            {
                Object id = getter.invoke(item);
                if (id instanceof Long && selectedIds.contains(id))
                {
                    result.add(item);
                }
            }
            catch (Exception ignored)
            {
            }
        }
        return result;
    }

    /**
     * 查找主键 getter：优先精确匹配 exportIdField 对应的 getXxx()；
     * 未指定字段名时取第一个返回 Long 且以 Id 结尾的属性（排除 parentId 等层级字段）
     */
    private static Method findIdGetter(Class<?> clazz, String idField)
    {
        Method fallback = null;
        for (Method m : clazz.getMethods())
        {
            String name = m.getName();
            if (!name.startsWith("get") || m.getParameterCount() != 0)
            {
                continue;
            }
            String prop = Character.toLowerCase(name.charAt(3)) + name.substring(4);
            if (!prop.endsWith("Id") || prop.equals("parentId") || prop.equals("userId"))
            {
                continue;
            }
            Class<?> rt = m.getReturnType();
            if (rt != Long.class && rt != long.class)
            {
                continue;
            }
            if (idField != null && !idField.isEmpty())
            {
                if (prop.equals(idField))
                {
                    return m;
                }
            }
            else if (fallback == null)
            {
                fallback = m;
            }
        }
        return fallback;
    }
}

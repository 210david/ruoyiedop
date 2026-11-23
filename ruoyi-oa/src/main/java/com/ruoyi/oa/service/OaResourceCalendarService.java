package com.ruoyi.oa.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * 综合办公资源使用日历
 *
 * 以台账资源（车辆/司机/印章）为行、日期为列，返回指定日期区间内被流程占用的时段。
 * 数据来源：台账表 + 流程数据表（flow_data_bus_use_apply / flow_data_17）。
 * 仅做查询，不写库。
 *
 * @author ruoyi
 */
@Service
public class OaResourceCalendarService
{
    private static final Logger log = LoggerFactory.getLogger(OaResourceCalendarService.class);

    private static final String TYPE_VEHICLE = "vehicle";
    private static final String TYPE_DRIVER = "driver";
    private static final String TYPE_SEAL = "seal";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 查询资源使用日历
     *
     * @param type      资源类型：vehicle / driver / seal
     * @param startDate 开始日期 yyyy-MM-dd
     * @param endDate   结束日期 yyyy-MM-dd
     */
    public Map<String, Object> selectCalendar(String type, String startDate, String endDate)
    {
        String start = startDate + " 00:00:00";
        String end = endDate + " 23:59:59";
        List<Map<String, Object>> rows;
        if (TYPE_VEHICLE.equals(type))
        {
            rows = queryVehicle(start, end);
        }
        else if (TYPE_DRIVER.equals(type))
        {
            rows = queryDriver(start, end);
        }
        else if (TYPE_SEAL.equals(type))
        {
            rows = querySeal(start, end);
        }
        else
        {
            rows = new ArrayList<>();
        }

        Map<Object, Map<String, Object>> resourceMap = new LinkedHashMap<>();
        List<Map<String, Object>> usages = new ArrayList<>();
        for (Map<String, Object> row : rows)
        {
            Object resourceId = row.get("resourceId");
            if (resourceId != null && !resourceMap.containsKey(resourceId))
            {
                Map<String, Object> resource = new LinkedHashMap<>();
                resource.put("resourceId", resourceId);
                resource.put("resourceName", row.get("resourceName"));
                resource.put("resourceStatus", row.get("resourceStatus"));
                resource.put("resourceType", row.get("resourceType"));
                resourceMap.put(resourceId, resource);
            }
            if (row.get("flowInstanceId") != null)
            {
                usages.add(row);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("resources", new ArrayList<>(resourceMap.values()));
        result.put("usages", usages);
        return result;
    }

    private List<Map<String, Object>> queryVehicle(String start, String end)
    {
        String sql = "SELECT v.vehicle_id AS resourceId, v.plate_no AS resourceName, v.status AS resourceStatus, "
                + "v.vehicle_type AS resourceType, f.process_instance_id AS flowInstanceId, f.title AS flowTitle, "
                + "f.applicant AS applicant, "
                + "DATE_FORMAT(f.depart_time, '%Y-%m-%d %H:%i:%s') AS startTime, "
                + "DATE_FORMAT(f.return_time, '%Y-%m-%d %H:%i:%s') AS endTime, f.procStatus AS procStatus "
                + "FROM biz_oa_vehicle v "
                + "LEFT JOIN flow_data_bus_use_apply f ON f.assigned_vehicle = v.plate_no "
                + "AND f.depart_time IS NOT NULL AND f.return_time IS NOT NULL "
                + "AND f.depart_time <= ? AND f.return_time >= ? "
                + "WHERE v.del_flag = '0' ORDER BY v.plate_no, f.depart_time";
        return safeQuery(sql, end, start);
    }

    private List<Map<String, Object>> queryDriver(String start, String end)
    {
        String sql = "SELECT d.driver_id AS resourceId, d.driver_name AS resourceName, d.status AS resourceStatus, "
                + "NULL AS resourceType, f.process_instance_id AS flowInstanceId, f.title AS flowTitle, "
                + "f.applicant AS applicant, "
                + "DATE_FORMAT(f.depart_time, '%Y-%m-%d %H:%i:%s') AS startTime, "
                + "DATE_FORMAT(f.return_time, '%Y-%m-%d %H:%i:%s') AS endTime, f.procStatus AS procStatus "
                + "FROM biz_oa_driver d "
                + "LEFT JOIN flow_data_bus_use_apply f ON f.assigned_driver = d.driver_name "
                + "AND f.depart_time IS NOT NULL AND f.return_time IS NOT NULL "
                + "AND f.depart_time <= ? AND f.return_time >= ? "
                + "WHERE d.del_flag = '0' ORDER BY d.driver_name, f.depart_time";
        return safeQuery(sql, end, start);
    }

    private List<Map<String, Object>> querySeal(String start, String end)
    {
        String sql = "SELECT s.seal_id AS resourceId, s.seal_name AS resourceName, s.status AS resourceStatus, "
                + "s.seal_type AS resourceType, f.process_instance_id AS flowInstanceId, f.title AS flowTitle, "
                + "f.applicant AS applicant, "
                + "DATE_FORMAT(f.apply_date, '%Y-%m-%d %H:%i:%s') AS startTime, "
                + "DATE_FORMAT(COALESCE(f.return_time, f.apply_date), '%Y-%m-%d %H:%i:%s') AS endTime, "
                + "f.procStatus AS procStatus "
                + "FROM biz_oa_seal s "
                + "LEFT JOIN flow_data_17 f ON f.seal_name = s.seal_name "
                + "AND f.apply_date IS NOT NULL "
                + "AND f.apply_date <= ? AND COALESCE(f.return_time, f.apply_date) >= ? "
                + "WHERE s.del_flag = '0' ORDER BY s.seal_name, f.apply_date";
        return safeQuery(sql, end, start);
    }

    private List<Map<String, Object>> safeQuery(String sql, Object... args)
    {
        try
        {
            return jdbcTemplate.queryForList(sql, args);
        }
        catch (Exception e)
        {
            log.error("资源日历查询失败：{}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}

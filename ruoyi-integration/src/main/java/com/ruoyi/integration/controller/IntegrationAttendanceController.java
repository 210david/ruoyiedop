package com.ruoyi.integration.controller;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.hr.domain.HrEmployee;
import com.ruoyi.hr.mapper.HrEmployeeMapper;
import com.ruoyi.integration.domain.IntegrationUserBinding;
import com.ruoyi.integration.mapper.IntegrationUserBindingMapper;
import com.ruoyi.integration.service.IDingtalkSyncService;
import com.ruoyi.integration.service.IWecomSyncService;

/**
 * 第三方考勤集成管理接口（钉钉 / 企业微信）
 *
 * 同一套接口通过 connectorType 参数区分连接器：
 *   connectorType=dingtalk（默认，兼容历史调用）
 *   connectorType=wecom
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/integration/attendance")
public class IntegrationAttendanceController extends BaseController
{
    /** 连接器类型：钉钉 */
    private static final String CONNECTOR_DINGTALK = "dingtalk";

    /** 连接器类型：企业微信 */
    private static final String CONNECTOR_WECOM = "wecom";

    @Autowired
    private IDingtalkSyncService dingtalkSyncService;

    @Autowired
    private IWecomSyncService wecomSyncService;

    @Autowired
    private IntegrationUserBindingMapper bindingMapper;

    @Autowired
    private HrEmployeeMapper hrEmployeeMapper;

    /**
     * 归一化连接器类型（未传或非法值一律回退钉钉，保证历史前端调用行为不变）
     */
    private String normalizeConnector(String connectorType)
    {
        return CONNECTOR_WECOM.equalsIgnoreCase(connectorType) ? CONNECTOR_WECOM : CONNECTOR_DINGTALK;
    }

    /**
     * 连接器中文名（用于提示文案）
     */
    private String connectorLabel(String connectorType)
    {
        return CONNECTOR_WECOM.equals(connectorType) ? "企业微信" : "钉钉";
    }

    /**
     * 绑定关系列表（含未匹配，用于人工核对）
     *
     * @param connectorType 连接器类型（dingtalk 默认 / wecom）
     */
    @PreAuthorize("@ss.hasPermi('integration:attendance:list')")
    @GetMapping("/bindings")
    public TableDataInfo bindings(IntegrationUserBinding query, String connectorType)
    {
        String connector = normalizeConnector(StringUtils.isNotEmpty(connectorType) ? connectorType : query.getConnectorType());
        query.setConnectorType(connector);
        startPage();
        List<IntegrationUserBinding> list = bindingMapper.selectIntegrationUserBindingList(query);
        return getDataTable(list);
    }

    /**
     * 绑定状态统计（列表页状态页签计数：全部 / 已匹配 / 未匹配）
     */
    @PreAuthorize("@ss.hasPermi('integration:attendance:list')")
    @GetMapping("/bindingStats")
    public AjaxResult bindingStats(String connectorType)
    {
        String connector = normalizeConnector(connectorType);
        Long all = bindingMapper.countBinding(connector, null);
        Long matched = bindingMapper.countBinding(connector, "yes");
        long total = all == null ? 0L : all;
        long matchedCount = matched == null ? 0L : matched;
        AjaxResult ajax = success();
        ajax.put("all", total);
        ajax.put("matched", matchedCount);
        ajax.put("unmatched", total - matchedCount);
        return ajax;
    }

    /**
     * 同步通讯录并自动匹配员工
     */
    @PreAuthorize("@ss.hasPermi('integration:attendance:sync')")
    @PostMapping("/syncContacts")
    public AjaxResult syncContacts(String connectorType)
    {
        String connector = normalizeConnector(connectorType);
        return CONNECTOR_WECOM.equals(connector)
                ? wecomSyncService.syncContacts()
                : dingtalkSyncService.syncContacts();
    }

    /**
     * 手动同步打卡数据（按日期范围）
     *
     * @param beginDate yyyy-MM-dd
     * @param endDate   yyyy-MM-dd
     */
    @PreAuthorize("@ss.hasPermi('integration:attendance:sync')")
    @PostMapping("/sync/{beginDate}/{endDate}")
    public AjaxResult sync(@PathVariable("beginDate") String beginDate, @PathVariable("endDate") String endDate,
            String connectorType)
    {
        Date begin = DateUtils.parseDate(beginDate);
        Date end = DateUtils.parseDate(endDate);
        if (begin == null || end == null)
        {
            return error("日期格式应为 yyyy-MM-dd");
        }
        String connector = normalizeConnector(connectorType);
        return CONNECTOR_WECOM.equals(connector)
                ? wecomSyncService.syncAttendance(begin, end)
                : dingtalkSyncService.syncAttendance(begin, end);
    }

    /**
     * 人工绑定：将绑定关系指定到员工工号（自动匹配失败时使用）
     */
    @PreAuthorize("@ss.hasPermi('integration:attendance:sync')")
    @PutMapping("/binding/bind")
    public AjaxResult bind(@RequestBody IntegrationUserBinding body)
    {
        if (body.getBindingId() == null || StringUtils.isEmpty(body.getEmployeeNo()))
        {
            return error("参数不完整：需要 bindingId 与 employeeNo");
        }
        IntegrationUserBinding existing = bindingMapper.selectIntegrationUserBindingById(body.getBindingId());
        if (existing == null)
        {
            return error("绑定关系不存在");
        }
        HrEmployee employee = hrEmployeeMapper.selectHrEmployeeByEmployeeNo(body.getEmployeeNo());
        if (employee == null)
        {
            return error("工号 " + body.getEmployeeNo() + " 未找到对应员工档案");
        }
        // 一一绑定约束：一个员工在同一连接器下只能绑定一个外部用户，否则打卡数据会互相覆盖
        String connector = normalizeConnector(existing.getConnectorType());
        IntegrationUserBinding occupied = bindingMapper.selectOccupiedBinding(connector,
                employee.getEmployeeId(), body.getBindingId());
        if (occupied != null)
        {
            return error("该员工已被" + connectorLabel(connector) + "用户「" + occupied.getExternalName()
                    + "」绑定，一个员工只能绑定一个" + connectorLabel(connector) + "用户，请先解绑后再绑定");
        }
        IntegrationUserBinding update = new IntegrationUserBinding();
        update.setBindingId(body.getBindingId());
        update.setEmployeeNo(employee.getEmployeeNo());
        update.setEmployeeId(employee.getEmployeeId());
        update.setEmployeeName(employee.getEmployeeName());
        update.setMatchType("manual");
        update.setSyncTime(new Date());
        update.setUpdateBy(getUsername());
        bindingMapper.updateIntegrationUserBinding(update);
        return success("绑定成功：" + existing.getExternalName() + " → " + employee.getEmployeeName());
    }

    /**
     * 检查员工是否已被其他外部用户绑定（一一绑定约束，供前端选人后即时提示）
     *
     * @param employeeNo    员工工号
     * @param bindingId     当前操作的绑定关系ID（变更绑定时排除自身），可为 null
     * @param connectorType 连接器类型（dingtalk 默认 / wecom）
     */
    @PreAuthorize("@ss.hasPermi('integration:attendance:sync')")
    @GetMapping("/binding/occupant")
    public AjaxResult occupant(String employeeNo, Long bindingId, String connectorType)
    {
        AjaxResult ajax = success();
        if (StringUtils.isEmpty(employeeNo))
        {
            ajax.put("occupied", false);
            return ajax;
        }
        HrEmployee employee = hrEmployeeMapper.selectHrEmployeeByEmployeeNo(employeeNo);
        if (employee == null)
        {
            ajax.put("occupied", false);
            return ajax;
        }
        IntegrationUserBinding occupied = bindingMapper.selectOccupiedBinding(normalizeConnector(connectorType),
                employee.getEmployeeId(), bindingId);
        ajax.put("occupied", occupied != null);
        if (occupied != null)
        {
            ajax.put("externalName", occupied.getExternalName());
            ajax.put("mobile", occupied.getMobile());
            ajax.put("bindingId", occupied.getBindingId());
            ajax.put("employeeName", occupied.getEmployeeName());
        }
        return ajax;
    }

    /**
     * 解绑：清空该钉钉用户与平台员工的关联，钉钉用户信息保留以便重新绑定或等待下次自动匹配
     * <p>仅解除对应关系，不会删除已同步到 HR 考勤的历史记录</p>
     */
    @PreAuthorize("@ss.hasPermi('integration:attendance:sync')")
    @PutMapping("/binding/unbind/{bindingId}")
    public AjaxResult unbind(@PathVariable("bindingId") Long bindingId)
    {
        IntegrationUserBinding existing = bindingMapper.selectIntegrationUserBindingById(bindingId);
        if (existing == null)
        {
            return error("绑定关系不存在");
        }
        if (existing.getEmployeeId() == null)
        {
            return error("该" + connectorLabel(normalizeConnector(existing.getConnectorType())) + "用户当前未绑定员工，无需解绑");
        }
        bindingMapper.clearBinding(bindingId, getUsername());
        return success("已解绑：" + existing.getExternalName() + " ✕ " + existing.getEmployeeName());
    }
}

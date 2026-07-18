package com.ruoyi.dms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.dms.domain.DmsEquipment;
import com.ruoyi.dms.domain.DmsEquipmentLog;
import com.ruoyi.dms.domain.DmsWorkOrder;
import com.ruoyi.dms.mapper.DmsWorkOrderMapper;
import com.ruoyi.dms.service.IDmsEquipmentLogService;
import com.ruoyi.dms.service.IDmsEquipmentService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/dms/equipment")
public class DmsEquipmentController extends BaseController
{
    @Autowired
    private IDmsEquipmentService dmsEquipmentService;

    @Autowired
    private IDmsEquipmentLogService dmsEquipmentLogService;

    @Autowired
    private DmsWorkOrderMapper dmsWorkOrderMapper;

    @PreAuthorize("@ss.hasPermi('dms:equipment:list')")
    @GetMapping("/list")
    public TableDataInfo list(DmsEquipment equipment)
    {
        startPage();
        List<DmsEquipment> list = dmsEquipmentService.selectEquipmentList(equipment);
        return getDataTable(list);
    }

    @Log(title = "设备台账管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('dms:equipment:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, DmsEquipment equipment)
    {
        List<DmsEquipment> list = dmsEquipmentService.selectEquipmentList(equipment);
        ExcelUtil<DmsEquipment> util = new ExcelUtil<>(DmsEquipment.class);
        util.exportExcel(response, list, "设备台账数据");
    }

    @PreAuthorize("@ss.hasPermi('dms:equipment:query')")
    @GetMapping(value = "/{equipmentId}")
    public AjaxResult getInfo(@PathVariable("equipmentId") Long equipmentId)
    {
        return AjaxResult.success(dmsEquipmentService.selectEquipmentById(equipmentId));
    }

    @Log(title = "设备台账管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('dms:equipment:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody DmsEquipment equipment)
    {
        return toAjax(dmsEquipmentService.insertEquipment(equipment));
    }

    @Log(title = "设备台账管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('dms:equipment:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody DmsEquipment equipment)
    {
        // 记录状态变更日志
        if (equipment.getEquipmentId() != null && equipment.getEquipmentStatus() != null)
        {
            DmsEquipment old = dmsEquipmentService.selectEquipmentById(equipment.getEquipmentId());
            if (old != null && !old.getEquipmentStatus().equals(equipment.getEquipmentStatus()))
            {
                DmsEquipmentLog log = new DmsEquipmentLog();
                log.setEquipmentId(equipment.getEquipmentId());
                log.setEquipmentCode(old.getEquipmentCode());
                log.setEquipmentName(old.getEquipmentName());
                log.setChangeType("0"); // 状态变更
                log.setOldValue(old.getEquipmentStatus());
                log.setNewValue(equipment.getEquipmentStatus());
                log.setChangeReason(equipment.getRemark());
                log.setOperatorName(getUsername());
                dmsEquipmentLogService.insertLog(log);
            }
        }
        return toAjax(dmsEquipmentService.updateEquipment(equipment));
    }

    @Log(title = "设备台账管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('dms:equipment:remove')")
    @DeleteMapping("/{equipmentIds}")
    public AjaxResult remove(@PathVariable Long[] equipmentIds)
    {
        return toAjax(dmsEquipmentService.deleteEquipmentByIds(equipmentIds));
    }

    /** 设备变更履历列表 */
    @PreAuthorize("@ss.hasPermi('dms:equipment:query')")
    @GetMapping("/log/list")
    public TableDataInfo logList(DmsEquipmentLog log)
    {
        startPage();
        List<DmsEquipmentLog> list = dmsEquipmentLogService.selectLogList(log);
        return getDataTable(list);
    }

    /** 设备维修履历（关联工单） */
    @PreAuthorize("@ss.hasPermi('dms:equipment:query')")
    @GetMapping("/history/{equipmentId}")
    public AjaxResult history(@PathVariable("equipmentId") Long equipmentId)
    {
        DmsWorkOrder query = new DmsWorkOrder();
        query.setEquipmentId(equipmentId);
        List<DmsWorkOrder> orders = dmsWorkOrderMapper.selectWorkOrderList(query);
        return AjaxResult.success(orders);
    }
}

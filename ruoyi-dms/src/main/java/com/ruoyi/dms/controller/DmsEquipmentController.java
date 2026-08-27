package com.ruoyi.dms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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

    @Log(title = "设备台账管理", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('dms:equipment:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport,
            @RequestParam(value = "updateKey", required = false, defaultValue = "equipmentName") String updateKey) throws Exception
    {
        ExcelUtil<DmsEquipment> util = new ExcelUtil<>(DmsEquipment.class);
        List<DmsEquipment> list = util.importExcel(file.getInputStream());
        String operName = getUsername();
        return dmsEquipmentService.importEquipment(list, updateSupport, updateKey, operName);
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<DmsEquipment> util = new ExcelUtil<>(DmsEquipment.class);
        // 排除自动生成的设备编号字段，使导入模板与新建表单一致
        util.excludeFields = new String[]{"equipmentCode"};
        util.importTemplateExcel(response, "设备台账数据");
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
        if (equipment.getEquipmentId() != null)
        {
            DmsEquipment old = dmsEquipmentService.selectEquipmentById(equipment.getEquipmentId());
            if (old != null)
            {
                String operator = getUsername();
                // 逐字段对比，记录所有变更
                recordChange(old, equipment, "equipmentStatus", "设备状态", "0", operator);
                recordChange(old, equipment, "installLocation", "安装位置", "1", operator);
                recordChange(old, equipment, "deptName", "使用部门", "2", operator);
                recordChange(old, equipment, "responsibleName", "责任人", "3", operator);
                recordChange(old, equipment, "assetCode", "资产编号", "5", operator);
                recordChange(old, equipment, "equipmentName", "设备名称", "5", operator);
                recordChange(old, equipment, "model", "型号", "5", operator);
                recordChange(old, equipment, "serialNumber", "序列号", "5", operator);
                recordChange(old, equipment, "manufacturer", "制造商", "5", operator);
                recordChange(old, equipment, "supplier", "供应商", "5", operator);
                recordChange(old, equipment, "originalValue", "原值", "5", operator);
                recordChange(old, equipment, "equipmentLevel", "设备等级", "5", operator);
                recordChange(old, equipment, "purchaseDate", "购置日期", "5", operator);
                recordChange(old, equipment, "installDate", "启用日期", "5", operator);
                recordChange(old, equipment, "retireDate", "报废处置日期", "5", operator);
                recordChange(old, equipment, "warrantyDate", "质保期限", "5", operator);
                recordChange(old, equipment, "remark", "备注", "5", operator);
            }
        }
        return toAjax(dmsEquipmentService.updateEquipment(equipment));
    }

    /**
     * 对比字段新旧值，如果有变化则记录变更日志
     * @param oldData 旧数据
     * @param newData 新数据
     * @param fieldName 字段名（DmsEquipment属性名）
     * @param fieldLabel 字段中文名（用于日志显示）
     * @param changeType 变更类型（0状态 1位置 2部门 3责任人 5信息变更）
     * @param operator 操作人
     */
    private void recordChange(DmsEquipment oldData, DmsEquipment newData, String fieldName, String fieldLabel, String changeType, String operator)
    {
        String oldVal = getFieldValue(oldData, fieldName);
        String newVal = getFieldValue(newData, fieldName);
        // 两个值都为空不算变更
        if (isBlank(oldVal) && isBlank(newVal)) return;
        // 值相同不算变更
        if (eq(oldVal, newVal)) return;
        DmsEquipmentLog log = new DmsEquipmentLog();
        log.setEquipmentId(oldData.getEquipmentId());
        log.setEquipmentCode(oldData.getEquipmentCode());
        log.setEquipmentName(oldData.getEquipmentName());
        log.setChangeType(changeType);
        log.setFieldLabel(fieldLabel);
        log.setOldValue(oldVal);
        log.setNewValue(newVal);
        log.setChangeReason(fieldLabel + "变更");
        log.setOperatorName(operator);
        dmsEquipmentLogService.insertLog(log);
    }

    /** 通过反射获取 DmsEquipment 字段值的字符串表示 */
    private String getFieldValue(DmsEquipment obj, String fieldName)
    {
        try
        {
            java.lang.reflect.Field f = DmsEquipment.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            Object val = f.get(obj);
            return val == null ? "" : val.toString();
        }
        catch (Exception e)
        {
            return "";
        }
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    private boolean eq(String a, String b) {
        if (a == null) a = "";
        if (b == null) b = "";
        return a.equals(b);
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

    /** 设备维修履历（关联工单）- 分页查询 */
    @PreAuthorize("@ss.hasPermi('dms:equipment:query')")
    @GetMapping("/history/{equipmentId}")
    public TableDataInfo history(@PathVariable("equipmentId") Long equipmentId)
    {
        startPage();
        DmsWorkOrder query = new DmsWorkOrder();
        query.setEquipmentId(equipmentId);
        List<DmsWorkOrder> orders = dmsWorkOrderMapper.selectWorkOrderList(query);
        return getDataTable(orders);
    }
}

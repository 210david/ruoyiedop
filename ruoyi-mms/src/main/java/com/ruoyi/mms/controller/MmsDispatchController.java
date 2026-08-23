package com.ruoyi.mms.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
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
import com.ruoyi.mms.domain.MmsDispatch;
import com.ruoyi.mms.service.IMmsDispatchService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 派工管理 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mms/dispatch")
public class MmsDispatchController extends BaseController
{
    @Autowired
    private IMmsDispatchService mmsDispatchService;

    // ========== 标准 CRUD ==========

    @PreAuthorize("@ss.hasPermi('mms:dispatch:list')")
    @GetMapping("/list")
    public TableDataInfo list(MmsDispatch dispatch)
    {
        startPage();
        List<MmsDispatch> list = mmsDispatchService.selectDispatchList(dispatch);
        return getDataTable(list);
    }

    @Log(title = "派工管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('mms:dispatch:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MmsDispatch dispatch)
    {
        List<MmsDispatch> list = mmsDispatchService.selectDispatchList(dispatch);
        ExcelUtil<MmsDispatch> util = new ExcelUtil<>(MmsDispatch.class);
        util.exportExcel(response, list, "派工单数据");
    }

    @PreAuthorize("@ss.hasPermi('mms:dispatch:query')")
    @GetMapping(value = "/{dispatchId}")
    public AjaxResult getInfo(@PathVariable("dispatchId") Long dispatchId)
    {
        return AjaxResult.success(mmsDispatchService.selectDispatchById(dispatchId));
    }

    @Log(title = "派工管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:dispatch:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MmsDispatch dispatch)
    {
        return toAjax(mmsDispatchService.insertDispatch(dispatch));
    }

    @Log(title = "派工管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:dispatch:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MmsDispatch dispatch)
    {
        return toAjax(mmsDispatchService.updateDispatch(dispatch));
    }

    @Log(title = "派工管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('mms:dispatch:remove')")
    @DeleteMapping("/{dispatchIds}")
    public AjaxResult remove(@PathVariable Long[] dispatchIds)
    {
        return toAjax(mmsDispatchService.deleteDispatchByIds(dispatchIds));
    }

    // ========== 业务流程操作 ==========

    /**
     * 派工开工
     * 状态：0(待开工) → 1(进行中)
     * 需填写：产能单元（工序/工单未指定时可修改）、班组、操作人员（默认当前登录用户）、操作时间（默认当前时间）
     */
    @Log(title = "派工管理-开工", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:dispatch:start')")
    @PutMapping("/start/{dispatchId}")
    public AjaxResult start(@PathVariable("dispatchId") Long dispatchId, @RequestBody(required = false) Map<String, Object> body)
    {
        String operatorName = body != null && body.get("operatorName") != null ? body.get("operatorName").toString() : null;
        String operateTime = body != null && body.get("operateTime") != null ? body.get("operateTime").toString() : null;
        Long teamId = null;
        String teamName = null;
        Long resourceId = null;
        String resourceName = null;
        if (body != null)
        {
            if (body.get("teamId") != null)
            {
                try { teamId = Long.valueOf(body.get("teamId").toString()); } catch (NumberFormatException e) {}
            }
            if (body.get("teamName") != null)
            {
                teamName = body.get("teamName").toString();
            }
            if (body.get("resourceId") != null)
            {
                try { resourceId = Long.valueOf(body.get("resourceId").toString()); } catch (NumberFormatException e) {}
            }
            if (body.get("resourceName") != null)
            {
                resourceName = body.get("resourceName").toString();
            }
        }
        return toAjax(mmsDispatchService.startDispatch(dispatchId, operatorName, operateTime, teamId, teamName, resourceId, resourceName));
    }

    /**
     * 派工完工
     * 状态：1(进行中) → 2(已完成)
     * 需填写：合格数量、不良数量、实际开始时间、实际结束时间、完工备注（不良原因等）
     * 完工后自动：回写派工单数量和时间 → 生成报工记录 → 更新工单进度
     */
    @Log(title = "派工管理-完工", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:dispatch:finish')")
    @PutMapping("/finish/{dispatchId}")
    public AjaxResult finish(@PathVariable("dispatchId") Long dispatchId, @RequestBody(required = false) Map<String, Object> body)
    {
        body = body != null ? body : Map.of();
        BigDecimal goodQty = body.get("goodQty") != null ? new BigDecimal(body.get("goodQty").toString()) : BigDecimal.ZERO;
        BigDecimal defectQty = body.get("defectQty") != null ? new BigDecimal(body.get("defectQty").toString()) : BigDecimal.ZERO;
        String remark = body.get("remark") != null ? body.get("remark").toString() : null;
        // 实际开始/结束时间（用户可在完工时修改，为空则后端自动处理）
        java.util.Date actualStart = null;
        java.util.Date actualEnd = null;
        if (body.get("actualStart") != null && !body.get("actualStart").toString().isEmpty())
        {
            try { actualStart = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(body.get("actualStart").toString()); }
            catch (Exception e) {}
        }
        if (body.get("actualEnd") != null && !body.get("actualEnd").toString().isEmpty())
        {
            try { actualEnd = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(body.get("actualEnd").toString()); }
            catch (Exception e) {}
        }
        return toAjax(mmsDispatchService.finishDispatch(dispatchId, goodQty, defectQty, remark, actualStart, actualEnd));
    }

    /**
     * 派工取消
     * 状态：0(待开工)/1(进行中) → 3(已取消)
     */
    @Log(title = "派工管理-取消", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:dispatch:cancel')")
    @PutMapping("/cancel/{dispatchId}")
    public AjaxResult cancel(@PathVariable("dispatchId") Long dispatchId)
    {
        return toAjax(mmsDispatchService.cancelDispatch(dispatchId));
    }
}

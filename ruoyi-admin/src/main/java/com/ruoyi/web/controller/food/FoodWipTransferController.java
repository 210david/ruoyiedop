package com.ruoyi.web.controller.food;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.food.domain.FoodWipTransfer;
import com.ruoyi.food.service.IFoodWipTransferService;

/**
 * 工序转移单 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/food/wip/transfer")
public class FoodWipTransferController extends BaseController
{
    @Autowired
    private IFoodWipTransferService transferService;

    @PreAuthorize("@ss.hasPermi('food:wipTransfer:list')")
    @GetMapping("/list")
    public TableDataInfo list(FoodWipTransfer transfer)
    {
        startPage();
        List<FoodWipTransfer> list = transferService.selectTransferList(transfer);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('food:wipTransfer:export')")
    @Log(title = "工序转移单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, FoodWipTransfer transfer)
    {
        List<FoodWipTransfer> list = transferService.selectTransferList(transfer);
        ExcelUtil<FoodWipTransfer> util = new ExcelUtil<FoodWipTransfer>(FoodWipTransfer.class);
        util.exportExcel(response, list, "工序转移单");
    }

    @PreAuthorize("@ss.hasPermi('food:wipTransfer:query')")
    @GetMapping("/{transferId}")
    public AjaxResult getInfo(@PathVariable Long transferId)
    {
        return success(transferService.selectTransferById(transferId));
    }

    @PreAuthorize("@ss.hasPermi('food:wipTransfer:add')")
    @Log(title = "工序转移单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody FoodWipTransfer transfer)
    {
        return toAjax(transferService.insertTransfer(transfer));
    }
}

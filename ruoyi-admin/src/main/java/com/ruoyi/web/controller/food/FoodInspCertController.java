package com.ruoyi.web.controller.food;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.food.domain.FoodInspCert;
import com.ruoyi.food.service.IFoodInspCertService;

/**
 * 检验索证索票 Controller
 *
 * 放行前置：来料检验(IQC)判定放行前必须上传必传证件附件。
 * 权限复用检验任务权限（qms:task:query/edit），检验人员在检验录入页直接操作。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/food/inspcert")
public class FoodInspCertController extends BaseController
{
    @Autowired
    private IFoodInspCertService certService;

    /** 检验任务的证件附件列表 */
    @PreAuthorize("@ss.hasPermi('qms:task:query')")
    @GetMapping("/list/{taskId}")
    public AjaxResult list(@PathVariable Long taskId)
    {
        return success(certService.selectCertListByTaskId(taskId));
    }

    /** 必传证件配置（开关+类型列表），取系统参数 food.insp.cert.required* */
    @PreAuthorize("@ss.hasPermi('qms:task:query')")
    @GetMapping("/requiredTypes")
    public AjaxResult requiredTypes()
    {
        return success(certService.requiredTypes());
    }

    /** 上传证件附件登记 */
    @PreAuthorize("@ss.hasPermi('qms:task:edit')")
    @Log(title = "检验索证索票", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody FoodInspCert cert)
    {
        return toAjax(certService.insertInspCert(cert));
    }

    /** 删除证件附件（登记错误时） */
    @PreAuthorize("@ss.hasPermi('qms:task:edit')")
    @Log(title = "检验索证索票", businessType = BusinessType.DELETE)
    @DeleteMapping("/{certIds}")
    public AjaxResult remove(@PathVariable Long[] certIds)
    {
        return toAjax(certService.deleteCertByIds(certIds));
    }
}

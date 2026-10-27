package com.ruoyi.qms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.qms.domain.QmsEsigRecord;
import com.ruoyi.qms.service.IQmsEsigRecordService;

/**
 * 电子签名记录 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/qms/esig")
public class QmsEsigRecordController extends BaseController
{
    @Autowired
    private IQmsEsigRecordService qmsEsigRecordService;

    /**
     * 列表查询
     */
    @PreAuthorize("@ss.hasPermi('qms:esig:list')")
    @GetMapping("/list")
    public TableDataInfo list(QmsEsigRecord esigRecord)
    {
        startPage();
        List<QmsEsigRecord> list = qmsEsigRecordService.selectEsigRecordList(esigRecord);
        return getDataTable(list);
    }

    /**
     * 查询业务关联的签名记录
     */
    @PreAuthorize("@ss.hasPermi('qms:esig:list')")
    @GetMapping("/byBiz/{bizType}/{bizId}")
    public AjaxResult listByBiz(@PathVariable String bizType, @PathVariable Long bizId)
    {
        List<QmsEsigRecord> list = qmsEsigRecordService.selectEsigRecordByBiz(bizType, bizId);
        return AjaxResult.success(list);
    }

    /**
     * 详情
     */
    @PreAuthorize("@ss.hasPermi('qms:esig:query')")
    @GetMapping("/{esigId}")
    public AjaxResult getInfo(@PathVariable Long esigId)
    {
        return AjaxResult.success(qmsEsigRecordService.selectEsigRecordById(esigId));
    }

    /**
     * 新增（一般不直接调用，由业务操作自动生成）
     */
    @PreAuthorize("@ss.hasPermi('qms:esig:add')")
    @Log(title = "电子签名记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody QmsEsigRecord esigRecord)
    {
        return toAjax(qmsEsigRecordService.saveEsigRecord(esigRecord));
    }

    /**
     * 删除
     */
    @PreAuthorize("@ss.hasPermi('qms:esig:remove')")
    @Log(title = "电子签名记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{esigIds}")
    public AjaxResult remove(@PathVariable Long[] esigIds)
    {
        return toAjax(qmsEsigRecordService.deleteEsigRecordByIds(esigIds));
    }
}
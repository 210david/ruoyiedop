package com.ruoyi.pjm.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.pjm.domain.PjmPmsLedger;
import com.ruoyi.pjm.service.IPjmPmsLedgerService;

/**
 * 项目采购台账（聚合采购管理模块中关联项目的单据）
 */
@RestController
@RequestMapping("/pjm/pms")
public class PjmPmsLedgerController extends BaseController
{
    @Autowired
    private IPjmPmsLedgerService ledgerService;

    @PreAuthorize("@ss.hasPermi('pjm:pms:list')")
    @GetMapping("/ledger")
    public TableDataInfo ledger(PjmPmsLedger query)
    {
        startPage();
        List<PjmPmsLedger> list = ledgerService.selectLedgerList(query);
        return getDataTable(list);
    }
}

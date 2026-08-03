package com.ruoyi.pms.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.pms.domain.vo.PmsPriceCompareVo;
import com.ruoyi.pms.mapper.PmsPriceCompareMapper;

/**
 * 价格比价Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/pms/priceCompare")
public class PmsPriceCompareController extends BaseController
{
    @Autowired
    private PmsPriceCompareMapper pmsPriceCompareMapper;

    @PreAuthorize("@ss.hasPermi('pms:priceCompare:list')")
    @GetMapping("/list")
    public TableDataInfo list(PmsPriceCompareVo priceCompare)
    {
        startPage();
        List<PmsPriceCompareVo> list = pmsPriceCompareMapper.selectPriceCompareList(priceCompare);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('pms:priceCompare:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, PmsPriceCompareVo priceCompare)
    {
        List<PmsPriceCompareVo> list = pmsPriceCompareMapper.selectPriceCompareList(priceCompare);
        ExcelUtil<PmsPriceCompareVo> util = new ExcelUtil<PmsPriceCompareVo>(PmsPriceCompareVo.class);
        util.exportExcel(response, list, "价格比价数据");
    }
}

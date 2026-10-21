package com.ruoyi.pms.mapper;

import java.util.List;
import com.ruoyi.pms.domain.vo.PmsPriceCompareVo;

/**
 * 价格比价Mapper接口
 *
 * @author ruoyi
 */
public interface PmsPriceCompareMapper
{
    /**
     * 查询价格比价列表
     *
     * @param priceCompare 价格比价查询条件
     * @return 价格比价列表
     */
    public List<PmsPriceCompareVo> selectPriceCompareList(PmsPriceCompareVo priceCompare);
}

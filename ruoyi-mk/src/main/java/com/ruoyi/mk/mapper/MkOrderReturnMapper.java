package com.ruoyi.mk.mapper;

import java.util.List;
import com.ruoyi.mk.domain.MkOrderReturn;

/**
 * 订单退货 Mapper
 *
 * @author ruoyi
 */
public interface MkOrderReturnMapper
{
    List<MkOrderReturn> selectReturnList(MkOrderReturn orderReturn);
    MkOrderReturn selectReturnById(Long returnId);
    int insertReturn(MkOrderReturn orderReturn);
    int updateReturn(MkOrderReturn orderReturn);
    int deleteReturnByIds(Long[] returnIds);
}

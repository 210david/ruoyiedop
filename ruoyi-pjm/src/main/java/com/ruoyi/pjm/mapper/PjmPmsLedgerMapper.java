package com.ruoyi.pjm.mapper;

import java.util.List;
import com.ruoyi.pjm.domain.PjmPmsLedger;

/**
 * 项目采购台账Mapper（同库直连 pms 模块表）
 */
public interface PjmPmsLedgerMapper
{
    List<PjmPmsLedger> selectLedgerList(PjmPmsLedger query);
}

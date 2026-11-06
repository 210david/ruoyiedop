package com.ruoyi.pjm.service;

import java.util.List;
import com.ruoyi.pjm.domain.PjmPmsLedger;

/**
 * 项目采购台账Service
 */
public interface IPjmPmsLedgerService
{
    List<PjmPmsLedger> selectLedgerList(PjmPmsLedger query);
}

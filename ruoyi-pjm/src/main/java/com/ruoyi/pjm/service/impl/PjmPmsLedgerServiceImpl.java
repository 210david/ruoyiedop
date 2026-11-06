package com.ruoyi.pjm.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.pjm.domain.PjmPmsLedger;
import com.ruoyi.pjm.mapper.PjmPmsLedgerMapper;
import com.ruoyi.pjm.service.IPjmPmsLedgerService;

/**
 * 项目采购台账Service实现
 */
@Service
public class PjmPmsLedgerServiceImpl implements IPjmPmsLedgerService
{
    @Autowired
    private PjmPmsLedgerMapper ledgerMapper;

    @Override
    public List<PjmPmsLedger> selectLedgerList(PjmPmsLedger query)
    {
        return ledgerMapper.selectLedgerList(query);
    }
}

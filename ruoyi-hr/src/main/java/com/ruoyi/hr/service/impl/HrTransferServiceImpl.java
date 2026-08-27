package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hr.domain.HrTransfer;
import com.ruoyi.hr.mapper.HrTransferMapper;
import com.ruoyi.hr.service.IHrTransferService;

@Service
public class HrTransferServiceImpl implements IHrTransferService
{
    @Autowired
    private HrTransferMapper hrTransferMapper;

    @Override
    public HrTransfer selectHrTransferById(Long transferId) { return hrTransferMapper.selectHrTransferById(transferId); }

    @Override
    public List<HrTransfer> selectHrTransferList(HrTransfer hrTransfer) { return hrTransferMapper.selectHrTransferList(hrTransfer); }

    @Override
    public int insertHrTransfer(HrTransfer hrTransfer) { return hrTransferMapper.insertHrTransfer(hrTransfer); }

    @Override
    public int updateHrTransfer(HrTransfer hrTransfer) { return hrTransferMapper.updateHrTransfer(hrTransfer); }

    @Override
    public int deleteHrTransferByIds(Long[] transferIds) { return hrTransferMapper.deleteHrTransferByIds(transferIds); }

    @Override
    public int deleteHrTransferById(Long transferId) { return hrTransferMapper.deleteHrTransferById(transferId); }
}

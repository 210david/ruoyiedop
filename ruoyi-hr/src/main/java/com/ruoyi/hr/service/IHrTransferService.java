package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrTransfer;

public interface IHrTransferService {
    public HrTransfer selectHrTransferById(Long transferId);
    public List<HrTransfer> selectHrTransferList(HrTransfer hrTransfer);
    public int insertHrTransfer(HrTransfer hrTransfer);
    public int updateHrTransfer(HrTransfer hrTransfer);
    public int deleteHrTransferByIds(Long[] transferIds);
    public int deleteHrTransferById(Long transferId);
}

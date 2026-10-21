package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrTransfer;

public interface HrTransferMapper {
    public HrTransfer selectHrTransferById(Long transferId);
    public List<HrTransfer> selectHrTransferList(HrTransfer hrTransfer);
    public int insertHrTransfer(HrTransfer hrTransfer);
    public int updateHrTransfer(HrTransfer hrTransfer);
    public int deleteHrTransferByIds(Long[] transferIds);
    public int deleteHrTransferById(Long transferId);
}

package com.ruoyi.qms.mapper;

import java.util.List;
import com.ruoyi.qms.domain.QmsEsigRecord;

/**
 * 电子签名记录 Mapper接口
 *
 * @author ruoyi
 */
public interface QmsEsigRecordMapper
{
    public List<QmsEsigRecord> selectEsigRecordList(QmsEsigRecord esigRecord);

    public QmsEsigRecord selectEsigRecordById(Long esigId);

    public List<QmsEsigRecord> selectEsigRecordByBiz(String bizType, Long bizId);

    public int insertEsigRecord(QmsEsigRecord esigRecord);

    public int deleteEsigRecordByIds(Long[] esigIds);
}

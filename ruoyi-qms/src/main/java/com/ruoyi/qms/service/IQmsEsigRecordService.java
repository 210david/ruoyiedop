package com.ruoyi.qms.service;

import java.util.List;
import com.ruoyi.qms.domain.QmsEsigRecord;

/**
 * 电子签名记录 Service接口
 *
 * @author ruoyi
 */
public interface IQmsEsigRecordService
{
    public List<QmsEsigRecord> selectEsigRecordList(QmsEsigRecord esigRecord);

    public QmsEsigRecord selectEsigRecordById(Long esigId);

    public List<QmsEsigRecord> selectEsigRecordByBiz(String bizType, Long bizId);

    /**
     * 保存电子签名记录（自动填充用户信息、IP、时间）
     */
    public int saveEsigRecord(QmsEsigRecord esigRecord);

    public int deleteEsigRecordByIds(Long[] esigIds);
}
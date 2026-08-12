package com.ruoyi.qms.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.qms.domain.QmsEsigRecord;
import com.ruoyi.qms.mapper.QmsEsigRecordMapper;
import com.ruoyi.qms.service.IQmsEsigRecordService;

/**
 * 电子签名记录 Service实现
 *
 * @author ruoyi
 */
@Service
public class QmsEsigRecordServiceImpl implements IQmsEsigRecordService
{
    @Autowired
    private QmsEsigRecordMapper qmsEsigRecordMapper;

    @Override
    public List<QmsEsigRecord> selectEsigRecordList(QmsEsigRecord esigRecord)
    {
        return qmsEsigRecordMapper.selectEsigRecordList(esigRecord);
    }

    @Override
    public QmsEsigRecord selectEsigRecordById(Long esigId)
    {
        return qmsEsigRecordMapper.selectEsigRecordById(esigId);
    }

    @Override
    public List<QmsEsigRecord> selectEsigRecordByBiz(String bizType, Long bizId)
    {
        return qmsEsigRecordMapper.selectEsigRecordByBiz(bizType, bizId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveEsigRecord(QmsEsigRecord esigRecord)
    {
        if (StringUtils.isEmpty(esigRecord.getBizType()) || esigRecord.getBizId() == null)
        {
            throw new RuntimeException("业务类型和业务ID不能为空");
        }
        // 自动填充用户信息
        if (esigRecord.getUserId() == null)
        {
            esigRecord.setUserId(SecurityUtils.getUserId());
        }
        if (StringUtils.isEmpty(esigRecord.getUserName()))
        {
            esigRecord.setUserName(SecurityUtils.getUsername());
        }
        if (StringUtils.isEmpty(esigRecord.getNickName()))
        {
            esigRecord.setNickName(SecurityUtils.getLoginUser().getUser().getNickName());
        }
        // 自动填充IP
        if (StringUtils.isEmpty(esigRecord.getSigIp()))
        {
            esigRecord.setSigIp(IpUtils.getIpAddr());
        }
        // 自动填充时间
        if (esigRecord.getSigTime() == null)
        {
            esigRecord.setSigTime(new Date());
        }
        return qmsEsigRecordMapper.insertEsigRecord(esigRecord);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteEsigRecordByIds(Long[] esigIds)
    {
        return qmsEsigRecordMapper.deleteEsigRecordByIds(esigIds);
    }
}
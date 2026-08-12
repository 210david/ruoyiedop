package com.ruoyi.qms.service;

import java.util.List;
import com.ruoyi.qms.domain.QmsGauge;

/**
 * 量检具台账 Service接口
 *
 * @author ruoyi
 */
public interface IQmsGaugeService
{
    public List<QmsGauge> selectGaugeList(QmsGauge gauge);
    public QmsGauge selectGaugeById(Long gaugeId);
    public int insertGauge(QmsGauge gauge);
    public int updateGauge(QmsGauge gauge);
    public int deleteGaugeByIds(Long[] gaugeIds);
}

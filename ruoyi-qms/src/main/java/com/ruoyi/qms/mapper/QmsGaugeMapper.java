package com.ruoyi.qms.mapper;

import java.util.List;
import com.ruoyi.qms.domain.QmsGauge;

/**
 * 量检具台账 Mapper
 *
 * @author ruoyi
 */
public interface QmsGaugeMapper
{
    public List<QmsGauge> selectGaugeList(QmsGauge gauge);
    public QmsGauge selectGaugeById(Long gaugeId);
    public QmsGauge selectGaugeByNo(String gaugeNo);
    public int insertGauge(QmsGauge gauge);
    public int updateGauge(QmsGauge gauge);
    public int deleteGaugeByIds(Long[] gaugeIds);
}

package com.ruoyi.qms.mapper;

import java.util.List;
import com.ruoyi.qms.domain.QmsNcr;

/**
 * 不合格品报告 Mapper
 *
 * @author ruoyi
 */
public interface QmsNcrMapper
{
    public List<QmsNcr> selectNcrList(QmsNcr ncr);
    public QmsNcr selectNcrById(Long ncrId);
    public QmsNcr selectNcrByNo(String ncrNo);
    public int insertNcr(QmsNcr ncr);
    public int updateNcr(QmsNcr ncr);
    public int deleteNcrByIds(Long[] ncrIds);
}

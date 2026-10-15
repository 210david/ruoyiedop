package com.ruoyi.food.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.food.domain.FoodCcpRecord;

/**
 * HACCP CCP监控记录 Mapper接口
 *
 * @author ruoyi
 */
public interface FoodCcpRecordMapper
{
    public List<FoodCcpRecord> selectCcpRecordList(FoodCcpRecord record);

    public FoodCcpRecord selectCcpRecordById(Long recordId);

    public int insertCcpRecord(FoodCcpRecord record);

    public int updateCcpRecord(FoodCcpRecord record);

    /**
     * 超限联动回写：NCR编号写入监控记录
     */
    public int updateOverLink(@Param("recordId") Long recordId, @Param("ncrId") Long ncrId, @Param("ncrNo") String ncrNo);

    /**
     * 记录取号：CCP+yyyyMMdd+4位
     */
    public String selectNextRecordNo();
}

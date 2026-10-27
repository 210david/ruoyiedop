package com.ruoyi.food.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.food.domain.FoodInspCert;

/**
 * 检验索证索票附件 Mapper接口
 *
 * @author ruoyi
 */
public interface FoodInspCertMapper
{
    public List<FoodInspCert> selectCertListByTaskId(Long taskId);

    public int insertInspCert(FoodInspCert cert);

    public int deleteCertByIds(Long[] certIds);

    /**
     * 统计任务下某证件类型的有效附件数（索证放行前置校验用）
     */
    public int countByTaskAndType(@Param("taskId") Long taskId, @Param("certType") String certType);

    /**
     * 复检任务建立时复制原任务证件附件
     */
    public int copyFromTask(@Param("originTaskId") Long originTaskId, @Param("newTaskId") Long newTaskId,
            @Param("newTaskNo") String newTaskNo, @Param("operator") String operator);
}

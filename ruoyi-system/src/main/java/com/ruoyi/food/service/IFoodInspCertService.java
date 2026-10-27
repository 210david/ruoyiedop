package com.ruoyi.food.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.food.domain.FoodInspCert;

/**
 * 检验索证索票 Service接口
 *
 * @author ruoyi
 */
public interface IFoodInspCertService
{
    public List<FoodInspCert> selectCertListByTaskId(Long taskId);

    public int insertInspCert(FoodInspCert cert);

    public int deleteCertByIds(Long[] certIds);

    /**
     * 索证放行前置校验：统计任务下某证件类型的有效附件数
     */
    public int countByTaskAndType(Long taskId, String certType);

    /**
     * 复检任务建立时复制原任务证件附件
     */
    public int copyFromTask(Long originTaskId, Long newTaskId, String newTaskNo, String operator);

    /**
     * 必传证件配置：开关 + 类型列表（certType/label），取系统参数 food.insp.cert.required*
     */
    public Map<String, Object> requiredTypes();
}

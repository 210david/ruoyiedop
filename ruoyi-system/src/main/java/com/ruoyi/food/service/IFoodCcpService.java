package com.ruoyi.food.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.food.domain.FoodCcpRecord;
import com.ruoyi.food.domain.FoodCcpTemplate;

/**
 * HACCP CCP电子台账 Service接口
 *
 * @author ruoyi
 */
public interface IFoodCcpService
{
    // ================= 模板（配置驱动） =================

    public List<FoodCcpTemplate> selectTemplateList(FoodCcpTemplate template);

    public FoodCcpTemplate selectTemplateById(Long templateId);

    public int insertTemplate(FoodCcpTemplate template);

    public int updateTemplate(FoodCcpTemplate template);

    public int deleteTemplateByIds(Long[] templateIds);

    // ================= 监控记录（不可删除，验厂留痕） =================

    public List<FoodCcpRecord> selectRecordList(FoodCcpRecord record);

    public FoodCcpRecord selectRecordById(Long recordId);

    /**
     * 录入CCP监控记录：模板快照 + 关键限值校验 + 超限自动联动NCR/批次冻结（Spring事件）
     */
    public int insertCcpRecord(FoodCcpRecord record);

    /**
     * 补充记录：仅允许补充偏差说明/纠正措施（核心数据不可改）
     */
    public int updateRecord(FoodCcpRecord record);

    /**
     * 超限联动回写NCR编号（qms监听器调用）
     */
    public int updateOverLink(Long recordId, Long ncrId, String ncrNo);

    /**
     * 按时段统计（导出封页用）：记录数/合格/超限
     */
    public Map<String, Object> statsSummary(FoodCcpRecord record);
}

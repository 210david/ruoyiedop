package com.ruoyi.qms.domain;

/**
 * 检验单项实测表 qms_insp_item
 *
 * @author ruoyi
 */
public class QmsInspItem
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long itemId;

    /** 检验任务ID */
    private Long taskId;

    /** 检验标准ID */
    private Long stdId;

    /** 检验项目名称 */
    private String stdName;

    /** 抽样序号 */
    private Integer sampleSeq;

    /** 实测值 */
    private String measuredVal;

    /** 单项判定（1合格 2不合格） */
    private String itemResult;

    /** 不良代码 */
    private String defectCode;

    /** 不良名称 */
    private String defectName;

    /** 缺陷等级（1致命 2严重 3一般 4轻微） */
    private String defectLevel;

    /** 创建者 */
    private String createBy;

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Long getStdId() { return stdId; }
    public void setStdId(Long stdId) { this.stdId = stdId; }
    public String getStdName() { return stdName; }
    public void setStdName(String stdName) { this.stdName = stdName; }
    public Integer getSampleSeq() { return sampleSeq; }
    public void setSampleSeq(Integer sampleSeq) { this.sampleSeq = sampleSeq; }
    public String getMeasuredVal() { return measuredVal; }
    public void setMeasuredVal(String measuredVal) { this.measuredVal = measuredVal; }
    public String getItemResult() { return itemResult; }
    public void setItemResult(String itemResult) { this.itemResult = itemResult; }
    public String getDefectCode() { return defectCode; }
    public void setDefectCode(String defectCode) { this.defectCode = defectCode; }
    public String getDefectName() { return defectName; }
    public void setDefectName(String defectName) { this.defectName = defectName; }
    public String getDefectLevel() { return defectLevel; }
    public void setDefectLevel(String defectLevel) { this.defectLevel = defectLevel; }
    public String getCreateBy() { return createBy; }
    public void setCreateBy(String createBy) { this.createBy = createBy; }
}

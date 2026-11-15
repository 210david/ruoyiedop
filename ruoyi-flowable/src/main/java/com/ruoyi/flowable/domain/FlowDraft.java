package com.ruoyi.flowable.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 流程草稿对象 flow_draft
 *
 * 对标泛微 workflow_requestbase 的「草稿」：保存表单内容但不进入流程流转，
 * 在「待办流程-草稿」可见，继续填写后提交才真正发起流程。
 *
 * @author ruoyi
 */
public class FlowDraft extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 草稿ID */
    private Long draftId;

    /** 流程定义Key（模型Key） */
    private String processKey;

    /** 流程名称 */
    private String processName;

    /** 关联表单ID */
    private Long formId;

    /** 流程标题 */
    private String title;

    /** 表单数据JSON（dynData + detailData） */
    private String formData;

    /** 发起人用户ID（数据归属，服务端强制取当前登录人） */
    private Long startUserId;

    /** 发起人登录名 */
    private String startUserName;

    /** 发起人姓名 */
    private String startNickName;

    /** 发起人部门ID */
    private Long deptId;

    public Long getDraftId() { return draftId; }
    public void setDraftId(Long draftId) { this.draftId = draftId; }
    public String getProcessKey() { return processKey; }
    public void setProcessKey(String processKey) { this.processKey = processKey; }
    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = processName; }
    public Long getFormId() { return formId; }
    public void setFormId(Long formId) { this.formId = formId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getFormData() { return formData; }
    public void setFormData(String formData) { this.formData = formData; }
    public Long getStartUserId() { return startUserId; }
    public void setStartUserId(Long startUserId) { this.startUserId = startUserId; }
    public String getStartUserName() { return startUserName; }
    public void setStartUserName(String startUserName) { this.startUserName = startUserName; }
    public String getStartNickName() { return startNickName; }
    public void setStartNickName(String startNickName) { this.startNickName = startNickName; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
}

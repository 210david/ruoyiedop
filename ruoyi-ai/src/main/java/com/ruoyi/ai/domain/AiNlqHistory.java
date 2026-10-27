package com.ruoyi.ai.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * AI数据查询历史记录 ai_nlq_history
 * 用户个人数据类：按user_id隔离，仅本人可查
 *
 * @author ruoyi
 */
public class AiNlqHistory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long id;

    /** 提问用户ID */
    private Long userId;

    /** 提问用户名 */
    private String userName;

    /** 问题 */
    private String question;

    /** 回答（AI结论） */
    private String answer;

    /** 查询结果JSON（columns/rows/sql/emptyReason） */
    private String resultJson;

    /** 状态（0成功 1失败） */
    private String status;

    /** 分页参数（仅查询用，非表字段） */
    private transient Integer pageSize = 20;
    private transient Integer offset = 0;

    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
    public Integer getOffset() { return offset; }
    public void setOffset(Integer offset) { this.offset = offset; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public String getResultJson() { return resultJson; }
    public void setResultJson(String resultJson) { this.resultJson = resultJson; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

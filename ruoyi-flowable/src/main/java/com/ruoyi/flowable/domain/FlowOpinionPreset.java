package com.ruoyi.flowable.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 签字意见-个人常用意见对象 flow_opinion_preset
 *
 * 对标泛微「签字意见常用语」：每位办理人可维护自己的快捷短语，办理时一键追加到签字意见。
 * 个人未配置时，前端回退到系统默认短语（字典 flow_opinion_preset，管理员统一维护）。
 *
 * 数据边界：接口一律以当前登录人为准（userId 由后端注入，不接受前端传值）。
 *
 * @author ruoyi
 */
public class FlowOpinionPreset extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long presetId;

    /** 所属用户ID */
    private Long userId;

    /** 常用意见内容（纯文本） */
    private String content;

    /** 排序（越小越靠前） */
    private Integer showOrder;

    public Long getPresetId() { return presetId; }

    public void setPresetId(Long presetId) { this.presetId = presetId; }

    public Long getUserId() { return userId; }

    public void setUserId(Long userId) { this.userId = userId; }

    public String getContent() { return content; }

    public void setContent(String content) { this.content = content; }

    public Integer getShowOrder() { return showOrder; }

    public void setShowOrder(Integer showOrder) { this.showOrder = showOrder; }
}

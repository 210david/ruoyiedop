package com.ruoyi.flowable.domain.dto;

import java.util.List;

/**
 * 个人常用意见保存入参
 *
 * 整体覆盖语义：提交的列表即为该用户最终的全部常用意见（顺序即展示顺序）。
 *
 * @author ruoyi
 */
public class FlowOpinionPresetDTO
{
    /** 常用意见内容列表（按展示顺序） */
    private List<String> presets;

    public List<String> getPresets() { return presets; }

    public void setPresets(List<String> presets) { this.presets = presets; }
}

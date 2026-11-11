package com.ruoyi.flowable.service;

import java.util.List;
import java.util.Map;

/**
 * 签字意见-个人常用意见 Service 接口
 *
 * 两级配置：个人自定义优先，未配置时回退系统默认（字典 flow_opinion_preset）。
 *
 * @author ruoyi
 */
public interface IFlowOpinionPresetService
{
    /**
     * 当前登录人的常用意见
     *
     * @return current   当前生效的短语列表（未自定义时为系统默认）
     *         defaults  系统默认短语（字典配置，未配置时为内置兜底）
     *         personalized 本人是否做过自定义
     */
    public Map<String, Object> selectMyPresets();

    /**
     * 整体覆盖保存当前登录人的常用意见
     *
     * @param presets 短语列表（按展示顺序；自动去空行、去重、限长）
     * @return 保存后的短语列表
     */
    public List<String> saveMyPresets(List<String> presets);

    /**
     * 清空个人配置，恢复系统默认短语
     *
     * @return 系统默认短语列表
     */
    public List<String> resetMyPresets();
}

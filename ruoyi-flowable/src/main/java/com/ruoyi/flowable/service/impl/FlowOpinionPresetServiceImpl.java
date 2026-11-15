package com.ruoyi.flowable.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.domain.entity.SysDictData;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DictUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.flowable.domain.FlowOpinionPreset;
import com.ruoyi.flowable.mapper.FlowOpinionPresetMapper;
import com.ruoyi.flowable.service.IFlowOpinionPresetService;

/**
 * 签字意见-个人常用意见 Service 实现
 *
 * 数据边界一律取当前登录人；保存采用「先删后插」整体覆盖，保证顺序与内容与前端一致。
 *
 * @author ruoyi
 */
@Service
public class FlowOpinionPresetServiceImpl implements IFlowOpinionPresetService
{
    private static final Logger log = LoggerFactory.getLogger(FlowOpinionPresetServiceImpl.class);

    /** 系统默认短语字典类型（管理员在字典管理维护，全员生效） */
    public static final String DICT_TYPE = "flow_opinion_preset";

    /** 字典未配置时的兜底短语（与字典初始数据一致） */
    private static final List<String> BUILTIN_DEFAULTS = Arrays.asList(
            "同意", "情况属实，同意办理", "同意，请领导审批", "请补充相关材料后再提交", "不同意，请修改后重新提交");

    /** 单条短语最大长度（与表字段 content varchar(200) 对齐） */
    private static final int MAX_LENGTH = 200;

    /** 每人最多短语条数（防止误粘贴大量文本把界面撑爆） */
    private static final int MAX_COUNT = 30;

    @Autowired
    private FlowOpinionPresetMapper flowOpinionPresetMapper;

    @Override
    public Map<String, Object> selectMyPresets()
    {
        List<String> mine = contentsOf(flowOpinionPresetMapper.selectByUser(currentUserId()));
        List<String> defaults = systemDefaults();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("personalized", !mine.isEmpty());
        data.put("current", mine.isEmpty() ? defaults : mine);
        data.put("defaults", defaults);
        return data;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> saveMyPresets(List<String> presets)
    {
        List<String> cleaned = clean(presets);
        if (cleaned.isEmpty())
        {
            throw new ServiceException("请至少保留一条常用意见");
        }
        Long userId = currentUserId();
        String operator = SecurityUtils.getUsername();
        flowOpinionPresetMapper.deleteByUser(userId);
        List<FlowOpinionPreset> rows = new ArrayList<>(cleaned.size());
        int order = 1;
        for (String content : cleaned)
        {
            FlowOpinionPreset row = new FlowOpinionPreset();
            row.setUserId(userId);
            row.setContent(content);
            row.setShowOrder(order++);
            row.setCreateBy(operator);
            rows.add(row);
        }
        flowOpinionPresetMapper.batchInsert(rows);
        return cleaned;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> resetMyPresets()
    {
        flowOpinionPresetMapper.deleteByUser(currentUserId());
        return systemDefaults();
    }

    /** 系统默认短语：优先取字典 flow_opinion_preset，未配置时用内置兜底 */
    private List<String> systemDefaults()
    {
        List<String> list = new ArrayList<>();
        try
        {
            List<SysDictData> dictDatas = DictUtils.getDictCache(DICT_TYPE);
            if (dictDatas != null)
            {
                for (SysDictData data : dictDatas)
                {
                    String label = data == null ? null : data.getDictLabel();
                    if (StringUtils.isNotEmpty(label) && !list.contains(label.trim()))
                    {
                        list.add(label.trim());
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.warn("读取字典 {} 失败，回退内置常用意见：{}", DICT_TYPE, e.getMessage());
        }
        return list.isEmpty() ? new ArrayList<>(BUILTIN_DEFAULTS) : list;
    }

    /** 清洗：去 HTML 标签与首尾空白、去空、去重、限长、限条数 */
    private List<String> clean(List<String> presets)
    {
        Set<String> set = new LinkedHashSet<>();
        if (presets != null)
        {
            for (String raw : presets)
            {
                if (raw == null)
                {
                    continue;
                }
                // 前端传纯文本；这里再兜一层，避免把富文本标签存进库
                String text = raw.replaceAll("<[^>]*>", "").replace("&nbsp;", " ").trim();
                if (text.isEmpty())
                {
                    continue;
                }
                if (text.length() > MAX_LENGTH)
                {
                    throw new ServiceException("单条常用意见不能超过 " + MAX_LENGTH + " 字：" + text.substring(0, 20) + "…");
                }
                set.add(text);
            }
        }
        List<String> list = new ArrayList<>(set);
        if (list.size() > MAX_COUNT)
        {
            throw new ServiceException("常用意见最多保留 " + MAX_COUNT + " 条，请精简后再保存");
        }
        return list;
    }

    private List<String> contentsOf(List<FlowOpinionPreset> rows)
    {
        List<String> list = new ArrayList<>();
        if (rows != null)
        {
            for (FlowOpinionPreset row : rows)
            {
                if (row != null && StringUtils.isNotEmpty(row.getContent()))
                {
                    list.add(row.getContent());
                }
            }
        }
        return list;
    }

    private Long currentUserId()
    {
        Long userId = SecurityUtils.getUserId();
        if (userId == null)
        {
            throw new ServiceException("登录状态失效，请重新登录");
        }
        return userId;
    }
}

package com.ruoyi.flowable.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 系统流程注册表。
 *
 * <p>考勤（出差/外出/补卡/加班/请假）、车辆（公车使用）、印章（用印）等流程已与对应业务模块联动，
 * 属于系统流程：其表单、表单字段与流程模型均不允许删除，只允许新增/修改字段或调整流程模型。</p>
 *
 * @author ruoyi
 */
public final class SystemFlowRegistry
{
    /** 系统流程 Key（与 act_re_model.KEY_ 一致） */
    private static final Set<String> PROCESS_KEYS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "travelApply",        // 出差
            "outingApply",        // 外出
            "makeupApply",        // 打卡补卡
            "overtimeApply",      // 加班
            "wf_94663220701",     // 请假流程
            "busUseApply",        // 公车使用
            "sealApply"           // 用印
    )));

    /** 系统流程表单 Key（含未回写 process_key 的表单） */
    private static final Set<String> FORM_KEYS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "travel_apply",
            "outing_apply",
            "makeup_apply",
            "overtime_apply",
            "leave_form",
            "bus_use_apply",
            "17"
    )));

    private SystemFlowRegistry()
    {
    }

    /** 是否系统流程 */
    public static boolean isSystemProcess(String processKey)
    {
        return processKey != null && PROCESS_KEYS.contains(processKey);
    }

    /** 是否系统流程表单 */
    public static boolean isSystemFormKey(String formKey)
    {
        return formKey != null && FORM_KEYS.contains(formKey);
    }

    /** 删除模型时的统一提示 */
    public static String modelDeleteTip(String processName)
    {
        return "流程《" + processName + "》属于系统流程，已与业务模块（考勤/车辆/印章等）关联，不允许删除。"
                + "如需调整，请修改流程模型或表单字段（支持新增、修改字段）；不再启用时请挂起对应流程版本。";
    }

    /** 删除表单时的统一提示 */
    public static String formDeleteTip(String formName)
    {
        return "流程表单《" + formName + "》属于系统流程，已与业务模块（考勤/车辆/印章等）关联，不允许删除。"
                + "如需调整，请新增或修改字段。";
    }

    /** 删除表单字段时的统一提示 */
    public static String fieldDeleteTip(String formName)
    {
        return "流程表单《" + formName + "》属于系统流程，不允许删除已有字段，只能新增或修改字段。";
    }
}

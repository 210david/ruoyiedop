package com.ruoyi.flowable.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.flowable.domain.FlowForm;

/**
 * 流程表单 Mapper 接口
 *
 * @author ruoyi
 */
public interface FlowFormMapper
{
    public FlowForm selectFlowFormByFormId(Long formId);

    public FlowForm selectFlowFormByFormKey(String formKey);

    public FlowForm selectFlowFormByProcessKey(String processKey);

    public List<FlowForm> selectFlowFormList(FlowForm form);

    public int insertFlowForm(FlowForm form);

    public int updateFlowForm(FlowForm form);

    public int deleteFlowFormByFormIds(Long[] formIds);
}

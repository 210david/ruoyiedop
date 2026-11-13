package com.ruoyi.integration.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.integration.domain.IntegrationUserBinding;

/**
 * 外部平台用户绑定 Mapper
 *
 * @author ruoyi
 */
public interface IntegrationUserBindingMapper
{
    public IntegrationUserBinding selectIntegrationUserBindingById(Long bindingId);

    public IntegrationUserBinding selectByConnectorAndExternalUserid(@Param("connectorType") String connectorType, @Param("externalUserid") String externalUserid);

    public List<IntegrationUserBinding> selectIntegrationUserBindingList(IntegrationUserBinding query);

    /**
     * 统计绑定关系数量（用于列表页状态页签计数）
     *
     * @param connectorType 连接器类型
     * @param matched       匹配状态：null=全部，yes=已匹配，no=未匹配
     * @return 数量
     */
    public Long countBinding(@Param("connectorType") String connectorType, @Param("matched") String matched);

    public List<IntegrationUserBinding> selectMatchedBindings(String connectorType);

    public int insertIntegrationUserBinding(IntegrationUserBinding binding);

    public int updateIntegrationUserBinding(IntegrationUserBinding binding);

    /**
     * 清空绑定关系（解绑）：将员工关联置空，钉钉用户信息保留以便重新绑定
     *
     * @param bindingId 绑定关系ID
     * @param updateBy  操作人
     * @return 影响行数
     */
    public int clearBinding(@Param("bindingId") Long bindingId, @Param("updateBy") String updateBy);

    /**
     * 查询某员工当前被哪个外部平台用户绑定（一一绑定校验）
     *
     * @param connectorType     连接器类型
     * @param employeeId        员工ID
     * @param excludeBindingId  排除的绑定关系ID（变更绑定时排除自身），可为 null
     * @return 占用该员工的绑定关系，未被占用返回 null
     */
    public IntegrationUserBinding selectOccupiedBinding(@Param("connectorType") String connectorType,
            @Param("employeeId") Long employeeId, @Param("excludeBindingId") Long excludeBindingId);

    /**
     * 查询重复绑定（同一员工被多个外部用户绑定）用于数据自检
     *
     * @param connectorType 连接器类型
     * @return 重复绑定的员工ID列表
     */
    public List<Long> selectDuplicatedEmployeeIds(@Param("connectorType") String connectorType);
}

package com.ruoyi.fin.mapper;

import com.ruoyi.fin.domain.FinPartner;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 财务往来单位 Mapper
 *
 * @author ruoyi
 */
public interface FinPartnerMapper
{
    /**
     * 查询往来单位列表
     *
     * @param partner 查询条件
     * @return 列表
     */
    List<FinPartner> selectPartnerList(FinPartner partner);

    /**
     * 按ID查询
     *
     * @param partnerId 往来单位ID
     * @return 往来单位
     */
    FinPartner selectPartnerById(Long partnerId);

    /**
     * 按映射键查询（source_module + source_id），用于同步既有主数据
     *
     * @param sourceModule 来源模块
     * @param sourceId     来源ID
     * @return 往来单位，不存在返回 null
     */
    FinPartner selectBySource(@Param("sourceModule") String sourceModule,
                              @Param("sourceId") Long sourceId);

    /**
     * 统计单位下是否存在财务单据（有则禁止删除）
     *
     * @param partnerId 往来单位ID
     * @return 单据数
     */
    int countVoucherUsage(@Param("partnerId") Long partnerId);

    /**
     * 新增
     *
     * @param partner 往来单位
     * @return 影响行数
     */
    int insertPartner(FinPartner partner);

    /**
     * 修改
     *
     * @param partner 往来单位
     * @return 影响行数
     */
    int updatePartner(FinPartner partner);

    /**
     * 逻辑删除
     *
     * @param partnerIds ID数组
     * @return 影响行数
     */
    int deletePartnerByIds(Long[] partnerIds);

    /**
     * 校验单位名称是否唯一（同类型下）
     *
     * @param partnerName 单位名称
     * @param partnerType 单位类型
     * @param partnerId   修改时传自身ID
     * @return 同名记录数，0=唯一
     */
    int checkNameUnique(@Param("partnerName") String partnerName,
                        @Param("partnerType") String partnerType,
                        @Param("partnerId") Long partnerId);
}
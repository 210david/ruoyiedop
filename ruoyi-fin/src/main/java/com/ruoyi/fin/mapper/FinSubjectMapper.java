package com.ruoyi.fin.mapper;

import com.ruoyi.fin.domain.FinSubject;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会计科目 Mapper
 *
 * @author ruoyi
 */
public interface FinSubjectMapper
{
    /**
     * 查询科目列表（支持按名称/类别/状态过滤，不分页——树形展示需全量）
     *
     * @param subject 查询条件
     * @return 科目平铺列表
     */
    List<FinSubject> selectSubjectList(FinSubject subject);

    /**
     * 查询全部启用科目（供下拉选择与凭证录入用）
     *
     * @return 科目列表
     */
    List<FinSubject> selectAllEnabled();

    /**
     * 按科目ID查询
     *
     * @param subjectId 科目ID
     * @return 科目
     */
    FinSubject selectSubjectById(Long subjectId);

    /**
     * 按科目编码查询（唯一约束校验用）
     *
     * @param subjectCode 科目编码
     * @return 科目，不存在返回 null
     */
    FinSubject selectSubjectByCode(@Param("subjectCode") String subjectCode);

    /**
     * 统计子科目数量（有子节点时禁止删除）
     *
     * @param subjectId 科目ID
     * @return 子科目数
     */
    int countChildren(@Param("subjectId") Long subjectId);

    /**
     * 统计科目下是否已有凭证分录（有则禁止删除）
     *
     * @param subjectCode 科目编码
     * @return 分录数
     */
    int countVoucherEntry(@Param("subjectCode") String subjectCode);

    /**
     * 新增科目
     *
     * @param subject 科目
     * @return 影响行数
     */
    int insertSubject(FinSubject subject);

    /**
     * 修改科目
     *
     * @param subject 科目
     * @return 影响行数
     */
    int updateSubject(FinSubject subject);

    /**
     * 逻辑删除科目
     *
     * @param subjectIds 科目ID数组
     * @return 影响行数
     */
    int deleteSubjectByIds(Long[] subjectIds);

    /**
     * 统计同编码科目数，用于唯一性校验。
     *
     * @param subjectCode 科目编码
     * @param subjectId   修改时传入自身ID以排除自身，新增传 null
     * @return 同编码记录数，0=唯一
     */
    int checkCodeUnique(@Param("subjectCode") String subjectCode,
                        @Param("subjectId") Long subjectId);
}
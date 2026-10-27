package com.ruoyi.fin.service;

import com.ruoyi.fin.domain.FinSubject;

import java.util.List;

/**
 * 会计科目 Service 接口
 *
 * @author ruoyi
 */
public interface IFinSubjectService
{
    /**
     * 查询科目列表（平铺）
     *
     * @param subject 查询条件
     * @return 科目列表
     */
    List<FinSubject> selectSubjectList(FinSubject subject);

    /**
     * 查询全部启用科目
     *
     * @return 科目列表
     */
    List<FinSubject> selectAllEnabled();

    /**
     * 按ID查询科目
     *
     * @param subjectId 科目ID
     * @return 科目
     */
    FinSubject selectSubjectById(Long subjectId);

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
     * 删除科目（有子科目或已有凭证分录时拒绝）
     *
     * @param subjectIds 科目ID数组
     * @return 影响行数
     */
    int deleteSubjectByIds(Long[] subjectIds);

    /**
     * 校验科目编码唯一性
     *
     * @param subjectCode 科目编码
     * @param subjectId   修改时传自身ID
     * @return true=唯一
     */
    boolean checkCodeUnique(String subjectCode, Long subjectId);
    /**
     * Excel 批量导入科目（D-01 修复）。
     *
     * <p>全部成功或全部失败（事务回滚）。校验：编码必填且唯一、名称必填、
     * 类别/方向合法、父科目存在（父自动转非明细）。</p>
     *
     * @param subjects Excel 解析出的科目列表
     * @param operName 操作人
     * @return 导入条数
     */
    int importSubjects(java.util.List<FinSubject> subjects, String operName);
}
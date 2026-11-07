package com.ruoyi.safety.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.safety.domain.SafetyExamUser;

/**
 * 考试手动添加人员 数据层
 *
 * @author ruoyi
 */
public interface SafetyExamUserMapper
{
    /** 按场次删除名单 */
    public int deleteByExamId(Long examId);

    /** 批量插入名单 */
    public int batchInsert(@Param("list") List<SafetyExamUser> list);

    /** 查询场次手动名单 */
    public List<SafetyExamUser> selectByExamId(Long examId);

    /** 依据用户ID批量查询有效用户信息（校验+回填显示名/部门） */
    public List<SafetyExamUser> selectUsersByIds(@Param("ids") List<Long> ids);

    /** 可选用户（sys_user 正常账号，keyword 模糊匹配登录名/昵称） */
    public List<SafetyExamUser> selectUserOptions(@Param("keyword") String keyword);
}

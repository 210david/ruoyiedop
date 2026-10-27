package com.ruoyi.ai.mapper;

import java.util.List;
import com.ruoyi.ai.domain.AiTool;

/**
 * AI工具注册 Mapper接口
 *
 * @author ruoyi
 */
public interface AiToolMapper
{
    /** 查询启用中的工具列表 */
    public List<AiTool> selectEnabledToolList();

    /** 按工具名查询（含停用，管理用） */
    public AiTool selectToolByName(String toolName);

    /** 查询工具列表（管理） */
    public List<AiTool> selectToolList(AiTool tool);

    /** 新增 */
    public int insertTool(AiTool tool);

    /** 修改 */
    public int updateTool(AiTool tool);

    /** 删除 */
    public int deleteToolById(Long toolId);
}

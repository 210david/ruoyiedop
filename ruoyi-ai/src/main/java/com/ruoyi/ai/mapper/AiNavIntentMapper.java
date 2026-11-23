package com.ruoyi.ai.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.ruoyi.ai.domain.AiNavIntent;

/**
 * AI导航意图 Mapper（注解式，无需 XML）
 *
 * @author ruoyi
 */
public interface AiNavIntentMapper
{
    String COLS = "intent_id AS intentId, intent_key AS intentKey, menu_id AS menuId, "
            + "module_code AS moduleCode, title, aliases, examples, nav_query AS navQuery, "
            + "doc_id AS docId, sort_no AS sortNo, status, "
            + "create_by AS createBy, create_time AS createTime, update_by AS updateBy, update_time AS updateTime";

    /** 全部启用意图（权限过滤前的原始集合） */
    @Select("SELECT " + COLS + " FROM ai_nav_intent WHERE status = '0' ORDER BY sort_no DESC, intent_id")
    List<AiNavIntent> selectEnabledList();

    /** 按绑定的知识文档查（知识问答确定性直连用） */
    @Select("SELECT " + COLS + " FROM ai_nav_intent WHERE status = '0' AND doc_id = #{docId}")
    List<AiNavIntent> selectByDocId(@Param("docId") Long docId);

    /** 按菜单ID查（判断某菜单是否已被注册为意图，管理界面提示用） */
    @Select("SELECT " + COLS + " FROM ai_nav_intent WHERE menu_id = #{menuId}")
    List<AiNavIntent> selectByMenuId(@Param("menuId") Long menuId);

    @Select("SELECT " + COLS + " FROM ai_nav_intent WHERE intent_id = #{intentId}")
    AiNavIntent selectById(@Param("intentId") Long intentId);

    @Select("SELECT " + COLS + " FROM ai_nav_intent ORDER BY module_code, sort_no DESC, intent_id")
    List<AiNavIntent> selectAll();

    @Insert("INSERT INTO ai_nav_intent(intent_key, menu_id, module_code, title, aliases, examples, "
            + "nav_query, doc_id, sort_no, status, create_by, create_time) VALUES "
            + "(#{intentKey}, #{menuId}, #{moduleCode}, #{title}, #{aliases}, #{examples}, "
            + "#{navQuery}, #{docId}, #{sortNo}, #{status}, #{createBy}, NOW())")
    int insert(AiNavIntent intent);

    @Update("UPDATE ai_nav_intent SET intent_key = #{intentKey}, menu_id = #{menuId}, "
            + "module_code = #{moduleCode}, title = #{title}, aliases = #{aliases}, examples = #{examples}, "
            + "nav_query = #{navQuery}, doc_id = #{docId}, sort_no = #{sortNo}, status = #{status}, "
            + "update_by = #{updateBy}, update_time = NOW() WHERE intent_id = #{intentId}")
    int update(AiNavIntent intent);

    @Delete("DELETE FROM ai_nav_intent WHERE intent_id = #{intentId}")
    int deleteById(@Param("intentId") Long intentId);
}

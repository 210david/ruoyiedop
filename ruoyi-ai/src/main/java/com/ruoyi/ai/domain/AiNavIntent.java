package com.ruoyi.ai.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * AI导航意图 ai_nav_intent
 *
 * 核心设计：只存 menuId，不存 path。
 * 路径由服务端按登录人权限菜单树实时计算，菜单改名/改路径/改权限均无需维护本表，
 * 从根本上避免「AI 推荐的路径 404」和手工填写错配。
 *
 * @author ruoyi
 */
public class AiNavIntent extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long intentId;

    /** 意图标识（LLM 选择用，唯一） */
    private String intentKey;

    /** 目标菜单ID（sys_menu.menu_id） */
    private Long menuId;

    /** 模块编码（wms/dms/qms/...），两级路由用 */
    private String moduleCode;

    /** 展示名称 */
    private String title;

    /** 别名/同义词，英文逗号分隔 */
    private String aliases;

    /** 示例问句，英文逗号分隔 */
    private String examples;

    /** 跳转附加参数JSON，如 {"status":"0"} */
    private String navQuery;

    /** 绑定的知识文档ID（P2 文档直连用） */
    private Long docId;

    /** 排序权重 */
    private Integer sortNo;

    /** 状态（0正常 1停用） */
    private String status;

    public Long getIntentId() { return intentId; }
    public void setIntentId(Long intentId) { this.intentId = intentId; }
    public String getIntentKey() { return intentKey; }
    public void setIntentKey(String intentKey) { this.intentKey = intentKey; }
    public Long getMenuId() { return menuId; }
    public void setMenuId(Long menuId) { this.menuId = menuId; }
    public String getModuleCode() { return moduleCode; }
    public void setModuleCode(String moduleCode) { this.moduleCode = moduleCode; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAliases() { return aliases; }
    public void setAliases(String aliases) { this.aliases = aliases; }
    public String getExamples() { return examples; }
    public void setExamples(String examples) { this.examples = examples; }
    public String getNavQuery() { return navQuery; }
    public void setNavQuery(String navQuery) { this.navQuery = navQuery; }
    public Long getDocId() { return docId; }
    public void setDocId(Long docId) { this.docId = docId; }
    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

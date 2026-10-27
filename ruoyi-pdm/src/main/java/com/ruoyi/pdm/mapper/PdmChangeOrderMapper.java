package com.ruoyi.pdm.mapper;

import java.util.List;
import com.ruoyi.pdm.domain.PdmChangeOrder;
import com.ruoyi.pdm.domain.PdmChangeOrderItem;

/**
 * 技术变更单 Mapper 接口
 *
 * @author ruoyi
 */
public interface PdmChangeOrderMapper
{
    /** 查询变更单列表（数据权限） */
    public List<PdmChangeOrder> selectChangeList(PdmChangeOrder change);

    /** 根据ID查询变更单 */
    public PdmChangeOrder selectChangeById(Long changeId);

    /** 查询变更单涉及资料明细 */
    public List<PdmChangeOrderItem> selectItemsByChangeId(Long changeId);

    /** 根据ID查询明细 */
    public PdmChangeOrderItem selectItemById(Long itemId);

    /** 新增变更单 */
    public int insertChange(PdmChangeOrder change);

    /** 修改变更单 */
    public int updateChange(PdmChangeOrder change);

    /** 批量删除（软删除，仅草稿） */
    public int deleteChangeByIds(Long[] changeIds);

    /** 新增明细 */
    public int insertItem(PdmChangeOrderItem item);

    /** 修改明细 */
    public int updateItem(PdmChangeOrderItem item);

    /** 删除变更单全部明细 */
    public int deleteItemsByChangeId(Long changeId);
}

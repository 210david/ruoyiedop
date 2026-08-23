package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsReturnMaterial;
import com.ruoyi.mms.domain.MmsReturnDetail;

public interface MmsReturnMaterialMapper {
    public List<MmsReturnMaterial> selectReturnMaterialList(MmsReturnMaterial m);
    public MmsReturnMaterial selectReturnMaterialById(Long returnId);
    public int insertReturnMaterial(MmsReturnMaterial m);
    public int updateReturnMaterial(MmsReturnMaterial m);
    public int deleteReturnMaterialByIds(Long[] returnIds);

    /** 查询退料明细列表 */
    public List<MmsReturnDetail> selectReturnDetailByReturnId(Long returnId);
    /** 新增退料明细 */
    public int insertReturnDetail(MmsReturnDetail detail);
    /** 删除退料明细（按退料单ID） */
    public int deleteReturnDetailByReturnId(Long returnId);

    /** 查询某个领料单已退料的明细（只查已退料状态=1的退料单） */
    public List<MmsReturnDetail> selectReturnedDetailByIssueId(Long issueId);
}

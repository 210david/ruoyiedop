package com.ruoyi.pms.mapper;

import java.util.List;
import com.ruoyi.pms.domain.PmsReturn;
import com.ruoyi.pms.domain.PmsReturnDetail;

/**
 * 退货 Mapper
 *
 * @author ruoyi
 */
public interface PmsReturnMapper
{
    public List<PmsReturn> selectReturnList(PmsReturn pmsReturn);
    public PmsReturn selectReturnById(Long returnId);
    public int insertReturn(PmsReturn pmsReturn);
    public int updateReturn(PmsReturn pmsReturn);
    public int deleteReturnByIds(Long[] returnIds);
    public List<PmsReturnDetail> selectReturnDetailByReturnId(Long returnId);
    public int insertReturnDetail(PmsReturnDetail detail);
    public int deleteReturnDetailByReturnId(Long returnId);
}

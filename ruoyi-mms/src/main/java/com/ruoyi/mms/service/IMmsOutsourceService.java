package com.ruoyi.mms.service;

import java.util.List;
import com.ruoyi.mms.domain.MmsOutsource;

public interface IMmsOutsourceService
{
    public List<MmsOutsource> selectOutsourceList(MmsOutsource outsource);
    public MmsOutsource selectOutsourceById(Long outsourceId);
    public int insertOutsource(MmsOutsource outsource);
    public int updateOutsource(MmsOutsource outsource);
    public int deleteOutsourceByIds(Long[] outsourceIds);
    /** 发料外协：状态0→1，记录外协日期 */
    public int sendOut(Long outsourceId, MmsOutsource outsource);
    /** 回厂验收：状态1→2，录入回厂数量/实际回厂日期，自动计算损耗 */
    public int backAccept(Long outsourceId, MmsOutsource outsource);
}

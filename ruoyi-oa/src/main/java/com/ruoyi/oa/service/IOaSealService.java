package com.ruoyi.oa.service;

import java.util.List;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.oa.domain.OaSeal;

public interface IOaSealService
{
    public OaSeal selectOaSealById(Long sealId);

    public List<OaSeal> selectOaSealList(OaSeal oaSeal);

    public AjaxResult importSeal(List<OaSeal> sealList, boolean updateSupport, String operName);

    public int insertOaSeal(OaSeal oaSeal);

    public int updateOaSeal(OaSeal oaSeal);

    public int deleteOaSealByIds(Long[] sealIds);

    public int deleteOaSealById(Long sealId);
}

package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsKitCheck;
import com.ruoyi.mms.domain.MmsKitCheckDetail;

public interface MmsKitCheckMapper {
    public List<MmsKitCheck> selectKitCheckList(MmsKitCheck kitCheck);
    public MmsKitCheck selectKitCheckById(Long kitId);
    public int insertKitCheck(MmsKitCheck kitCheck);
    public int updateKitCheck(MmsKitCheck kitCheck);
    public int deleteKitCheckByIds(Long[] kitIds);
    public List<MmsKitCheckDetail> selectKitCheckDetailByKitId(Long kitId);
    public int insertKitCheckDetail(MmsKitCheckDetail detail);
    public int deleteKitCheckDetailByKitId(Long kitId);
}
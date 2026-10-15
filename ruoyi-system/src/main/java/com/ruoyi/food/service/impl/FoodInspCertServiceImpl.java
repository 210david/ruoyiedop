package com.ruoyi.food.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.DictUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.food.domain.FoodInspCert;
import com.ruoyi.food.mapper.FoodInspCertMapper;
import com.ruoyi.food.service.IFoodInspCertService;
import com.ruoyi.system.service.ISysConfigService;

/**
 * 检验索证索票 Service实现
 *
 * 放行前置：系统参数 food.insp.cert.required 开启后，来料检验(IQC)判定放行前
 * 必须按 food.insp.cert.required.types 配置的证件类型上传附件
 *
 * @author ruoyi
 */
@Service
public class FoodInspCertServiceImpl implements IFoodInspCertService
{
    /** 索证放行前置开关（系统参数） */
    public static final String CONFIG_REQUIRED = "food.insp.cert.required";

    /** 必传证件类型，逗号分隔（系统参数） */
    public static final String CONFIG_REQUIRED_TYPES = "food.insp.cert.required.types";

    @Autowired
    private FoodInspCertMapper certMapper;

    @Autowired
    private ISysConfigService configService;

    @Override
    public List<FoodInspCert> selectCertListByTaskId(Long taskId)
    {
        return certMapper.selectCertListByTaskId(taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertInspCert(FoodInspCert cert)
    {
        if (cert.getTaskId() == null)
        {
            throw new ServiceException("检验任务ID不能为空");
        }
        String username = SecurityUtils.getUsername();
        cert.setUploadBy(username);
        cert.setUploadTime(DateUtils.getNowDate());
        cert.setCreateBy(username);
        cert.setCreateTime(DateUtils.getNowDate());
        cert.setDelFlag("0");
        return certMapper.insertInspCert(cert);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteCertByIds(Long[] certIds)
    {
        return certMapper.deleteCertByIds(certIds);
    }

    @Override
    public int countByTaskAndType(Long taskId, String certType)
    {
        return certMapper.countByTaskAndType(taskId, certType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int copyFromTask(Long originTaskId, Long newTaskId, String newTaskNo, String operator)
    {
        return certMapper.copyFromTask(originTaskId, newTaskId, newTaskNo, operator);
    }

    @Override
    public Map<String, Object> requiredTypes()
    {
        Map<String, Object> result = new HashMap<>();
        String required = configService.selectConfigByKey(CONFIG_REQUIRED);
        result.put("required", "true".equals(required));
        List<Map<String, String>> types = new ArrayList<>();
        String typeStr = configService.selectConfigByKey(CONFIG_REQUIRED_TYPES);
        if (StringUtils.isNotEmpty(typeStr))
        {
            for (String type : typeStr.split(","))
            {
                String t = type.trim();
                if (StringUtils.isEmpty(t))
                {
                    continue;
                }
                Map<String, String> item = new HashMap<>();
                item.put("certType", t);
                String label = DictUtils.getDictLabel("food_cert_type", t);
                item.put("label", StringUtils.isNotEmpty(label) ? label : t);
                types.add(item);
            }
        }
        result.put("types", types);
        return result;
    }
}

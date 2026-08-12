package com.ruoyi.safety.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.safety.domain.SafetyTrainingAttendee;
import com.ruoyi.safety.domain.SafetyTrainingCert;
import com.ruoyi.safety.domain.SafetyTrainingRecord;
import com.ruoyi.safety.mapper.SafetyTrainingAttendeeMapper;
import com.ruoyi.safety.mapper.SafetyTrainingCertMapper;
import com.ruoyi.safety.mapper.SafetyTrainingRecordMapper;
import com.ruoyi.safety.service.ISafetyTrainingAttendeeService;

@Service
public class SafetyTrainingAttendeeServiceImpl implements ISafetyTrainingAttendeeService
{
    @Autowired
    private SafetyTrainingAttendeeMapper safetyTrainingAttendeeMapper;

    @Autowired
    private SafetyTrainingCertMapper safetyTrainingCertMapper;

    @Autowired
    private SafetyTrainingRecordMapper safetyTrainingRecordMapper;

    @Override
    public SafetyTrainingAttendee selectSafetyTrainingAttendeeById(Long attendeeId) { return safetyTrainingAttendeeMapper.selectSafetyTrainingAttendeeById(attendeeId); }

    @Override
    public List<SafetyTrainingAttendee> selectSafetyTrainingAttendeeList(SafetyTrainingAttendee safetyTrainingAttendee) { return safetyTrainingAttendeeMapper.selectSafetyTrainingAttendeeList(safetyTrainingAttendee); }

    @Override
    public int insertSafetyTrainingAttendee(SafetyTrainingAttendee safetyTrainingAttendee)
    {
        checkDuplicate(safetyTrainingAttendee.getRecordId(), safetyTrainingAttendee.getUserId());
        int rows = safetyTrainingAttendeeMapper.insertSafetyTrainingAttendee(safetyTrainingAttendee);
        return rows;
    }

    @Override
    public int batchInsertAttendee(List<SafetyTrainingAttendee> attendeeList)
    {
        if (attendeeList == null || attendeeList.isEmpty())
        {
            throw new ServiceException("参训人员列表不能为空");
        }
        // 取第一条的 recordId 查询已存在的用户
        Long recordId = attendeeList.get(0).getRecordId();
        List<Long> existingUserIds = safetyTrainingAttendeeMapper.selectExistingUserIdsByRecordId(recordId);
        // 过滤掉已存在的用户
        List<SafetyTrainingAttendee> toInsert = attendeeList.stream()
                .filter(a -> a.getUserId() != null && !existingUserIds.contains(a.getUserId()))
                .collect(Collectors.toList());
        if (toInsert.isEmpty())
        {
            throw new ServiceException("所选人员均已存在于此培训记录中");
        }
        int rows = safetyTrainingAttendeeMapper.batchInsertAttendee(toInsert);
        if (toInsert.size() < attendeeList.size())
        {
            throw new ServiceException("部分人员已存在，已跳过 " + (attendeeList.size() - toInsert.size()) + " 人，成功添加 " + rows + " 人");
        }
        return rows;
    }

    @Override
    public int updateSafetyTrainingAttendee(SafetyTrainingAttendee safetyTrainingAttendee)
    {
        int rows = safetyTrainingAttendeeMapper.updateSafetyTrainingAttendee(safetyTrainingAttendee);
        // 考试合格时自动生成证书
        if ("1".equals(safetyTrainingAttendee.getIsPass()) && safetyTrainingAttendee.getUserId() != null && safetyTrainingAttendee.getRecordId() != null)
        {
            autoGenerateCert(safetyTrainingAttendee);
        }
        return rows;
    }

    @Override
    public int deleteSafetyTrainingAttendeeByIds(Long[] attendeeIds) { return safetyTrainingAttendeeMapper.deleteSafetyTrainingAttendeeByIds(attendeeIds); }

    @Override
    public int deleteSafetyTrainingAttendeeById(Long attendeeId) { return safetyTrainingAttendeeMapper.deleteSafetyTrainingAttendeeById(attendeeId); }

    @Override
    public List<SafetyTrainingAttendee> selectHoursStatistics(SafetyTrainingAttendee safetyTrainingAttendee) { return safetyTrainingAttendeeMapper.selectHoursStatistics(safetyTrainingAttendee); }

    @Override
    public String importAttendee(List<SafetyTrainingAttendee> list, boolean updateSupport, String operName)
    {
        if (list == null || list.isEmpty())
        {
            throw new ServiceException("导入数据不能为空");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (SafetyTrainingAttendee attendee : list)
        {
            try
            {
                // 检查重复
                if (attendee.getRecordId() != null && attendee.getUserId() != null)
                {
                    SafetyTrainingAttendee existing = safetyTrainingAttendeeMapper.checkDuplicate(attendee.getRecordId(), attendee.getUserId());
                    if (existing != null)
                    {
                        if (updateSupport)
                        {
                            attendee.setAttendeeId(existing.getAttendeeId());
                            safetyTrainingAttendeeMapper.updateSafetyTrainingAttendee(attendee);
                            successNum++;
                            successMsg.append("<br/>").append(successNum).append("、账号 ").append(attendee.getUserName()).append(" 更新成功");
                            continue;
                        }
                        else
                        {
                            failureNum++;
                            failureMsg.append("<br/>").append(failureNum).append("、账号 ").append(attendee.getUserName()).append(" 已存在");
                            continue;
                        }
                    }
                }
                attendee.setCreateBy(operName);
                safetyTrainingAttendeeMapper.insertSafetyTrainingAttendee(attendee);
                successNum++;
                successMsg.append("<br/>").append(successNum).append("、账号 ").append(attendee.getUserName()).append(" 导入成功");
            }
            catch (Exception e)
            {
                failureNum++;
                failureMsg.append("<br/>").append(failureNum).append("、账号 ").append(attendee.getUserName()).append(" 导入失败：").append(e.getMessage());
            }
        }
        if (failureNum > 0)
        {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new ServiceException(failureMsg.toString());
        }
        else
        {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }

    /** 检查同一培训记录下是否已存在该用户 */
    private void checkDuplicate(Long recordId, Long userId)
    {
        if (recordId != null && userId != null)
        {
            SafetyTrainingAttendee existing = safetyTrainingAttendeeMapper.checkDuplicate(recordId, userId);
            if (existing != null)
            {
                throw new ServiceException("该用户已存在于此培训记录的参训人员中，请勿重复添加");
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public int batchUpdateExamScore(Map<String, Object> payload)
    {
        List<?> rawIds = (List<?>) payload.get("attendeeIds");
        if (rawIds == null || rawIds.isEmpty())
        {
            throw new ServiceException("请选择需要维护成绩的参训人员");
        }
        List<Long> attendeeIds = new ArrayList<>();
        for (Object id : rawIds)
        {
            if (id instanceof Number)
            {
                attendeeIds.add(((Number) id).longValue());
            }
            else
            {
                attendeeIds.add(Long.valueOf(id.toString()));
            }
        }
        String isPass = (String) payload.get("isPass");
        if (isPass == null || isPass.isEmpty())
        {
            isPass = "1"; // 默认合格
        }
        Object examScoreObj = payload.get("examScore");
        java.math.BigDecimal examScore = null;
        if (examScoreObj != null)
        {
            try
            {
                examScore = new java.math.BigDecimal(examScoreObj.toString());
            }
            catch (NumberFormatException e)
            {
                throw new ServiceException("考试分数格式不正确");
            }
        }
        String examTimeStr = (String) payload.get("examTime");
        Date examTime = null;
        if (examTimeStr != null && !examTimeStr.isEmpty())
        {
            try
            {
                examTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(examTimeStr);
            }
            catch (Exception e)
            {
                throw new ServiceException("考试时间格式不正确");
            }
        }
        int rows = safetyTrainingAttendeeMapper.batchUpdateExamScore(
                attendeeIds.toArray(new Long[0]), isPass, examScore, examTime);
        // 考试合格时自动生成证书
        if ("1".equals(isPass))
        {
            for (Long attendeeId : attendeeIds)
            {
                SafetyTrainingAttendee attendee = safetyTrainingAttendeeMapper.selectSafetyTrainingAttendeeById(attendeeId);
                if (attendee != null && attendee.getUserId() != null && attendee.getRecordId() != null)
                {
                    attendee.setIsPass(isPass);
                    attendee.setExamScore(examScore);
                    autoGenerateCert(attendee);
                }
            }
        }
        return rows;
    }

    /** 考试合格后自动生成证书 */
    private void autoGenerateCert(SafetyTrainingAttendee attendee)
    {
        // 检查是否已存在相同 recordId + userId 的证书
        SafetyTrainingCert existingCert = safetyTrainingCertMapper.selectByRecordIdAndUserId(attendee.getRecordId(), attendee.getUserId());
        if (existingCert != null)
        {
            return; // 已存在证书，不重复生成
        }
        // 查询培训记录获取课程信息
        SafetyTrainingRecord record = safetyTrainingRecordMapper.selectSafetyTrainingRecordById(attendee.getRecordId());
        // 构建证书对象
        SafetyTrainingCert cert = new SafetyTrainingCert();
        cert.setCertNo("CERT-" + System.currentTimeMillis());
        cert.setCertName(record != null ? record.getCourseName() + "合格证书" : "培训合格证书");
        cert.setCertType("1"); // 默认类型：培训合格证
        cert.setUserId(attendee.getUserId());
        cert.setUserName(attendee.getUserName());
        cert.setDeptId(attendee.getDeptId());
        cert.setDeptName(attendee.getDeptName());
        cert.setRecordId(attendee.getRecordId());
        cert.setCourseId(record != null ? record.getCourseId() : null);
        cert.setIssueDate(new Date());
        cert.setIssueOrg("安全生产管理部");
        cert.setStatus("0"); // 正常
        cert.setCreateBy(attendee.getUpdateBy() != null ? attendee.getUpdateBy() : attendee.getCreateBy());
        cert.setRemark("考试合格自动生成，分数：" + attendee.getExamScore());
        safetyTrainingCertMapper.insertSafetyTrainingCert(cert);
    }
}

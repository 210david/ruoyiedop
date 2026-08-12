package com.ruoyi.safety.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.safety.domain.SafetyRectify;
import com.ruoyi.safety.mapper.SafetyRectifyMapper;
import com.ruoyi.safety.service.ISafetyRectifyService;

@Service
public class SafetyRectifyServiceImpl implements ISafetyRectifyService
{
    @Autowired
    private SafetyRectifyMapper safetyRectifyMapper;

    @Override
    public SafetyRectify selectSafetyRectifyById(Long rectifyId) { return safetyRectifyMapper.selectSafetyRectifyById(rectifyId); }

    @Override
    public List<SafetyRectify> selectSafetyRectifyList(SafetyRectify safetyRectify) { return safetyRectifyMapper.selectSafetyRectifyList(safetyRectify); }

    @Override
    public int insertSafetyRectify(SafetyRectify safetyRectify) { return safetyRectifyMapper.insertSafetyRectify(safetyRectify); }

    @Override
    public int updateSafetyRectify(SafetyRectify safetyRectify) { return safetyRectifyMapper.updateSafetyRectify(safetyRectify); }

    @Override
    public int deleteSafetyRectifyByIds(Long[] rectifyIds) { return safetyRectifyMapper.deleteSafetyRectifyByIds(rectifyIds); }

    @Override
    public int deleteSafetyRectifyById(Long rectifyId) { return safetyRectifyMapper.deleteSafetyRectifyById(rectifyId); }
}
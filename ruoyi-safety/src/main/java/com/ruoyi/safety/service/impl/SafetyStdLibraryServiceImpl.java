package com.ruoyi.safety.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.safety.domain.SafetyStdLibrary;
import com.ruoyi.safety.mapper.SafetyStdLibraryMapper;
import com.ruoyi.safety.service.ISafetyStdLibraryService;

@Service
public class SafetyStdLibraryServiceImpl implements ISafetyStdLibraryService
{
    @Autowired
    private SafetyStdLibraryMapper safetyStdLibraryMapper;

    @Override
    public SafetyStdLibrary selectSafetyStdLibraryById(Long libraryId) { return safetyStdLibraryMapper.selectSafetyStdLibraryById(libraryId); }

    @Override
    public List<SafetyStdLibrary> selectSafetyStdLibraryList(SafetyStdLibrary safetyStdLibrary) { return safetyStdLibraryMapper.selectSafetyStdLibraryList(safetyStdLibrary); }

    @Override
    public int insertSafetyStdLibrary(SafetyStdLibrary safetyStdLibrary) { return safetyStdLibraryMapper.insertSafetyStdLibrary(safetyStdLibrary); }

    @Override
    public int updateSafetyStdLibrary(SafetyStdLibrary safetyStdLibrary) { return safetyStdLibraryMapper.updateSafetyStdLibrary(safetyStdLibrary); }

    @Override
    public int deleteSafetyStdLibraryByIds(Long[] libraryIds) { return safetyStdLibraryMapper.deleteSafetyStdLibraryByIds(libraryIds); }

    @Override
    public int deleteSafetyStdLibraryById(Long libraryId) { return safetyStdLibraryMapper.deleteSafetyStdLibraryById(libraryId); }
}
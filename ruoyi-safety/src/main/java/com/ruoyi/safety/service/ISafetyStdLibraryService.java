package com.ruoyi.safety.service;

import java.util.List;
import com.ruoyi.safety.domain.SafetyStdLibrary;

public interface ISafetyStdLibraryService {
    public SafetyStdLibrary selectSafetyStdLibraryById(Long libraryId);
    public List<SafetyStdLibrary> selectSafetyStdLibraryList(SafetyStdLibrary safetyStdLibrary);
    public int insertSafetyStdLibrary(SafetyStdLibrary safetyStdLibrary);
    public int updateSafetyStdLibrary(SafetyStdLibrary safetyStdLibrary);
    public int deleteSafetyStdLibraryByIds(Long[] libraryIds);
    public int deleteSafetyStdLibraryById(Long libraryId);
}
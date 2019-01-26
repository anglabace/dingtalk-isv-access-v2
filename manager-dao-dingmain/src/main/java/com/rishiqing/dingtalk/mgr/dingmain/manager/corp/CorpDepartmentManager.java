package com.rishiqing.dingtalk.mgr.dingmain.manager.corp;

import com.rishiqing.dingtalk.api.model.vo.corp.CorpDepartmentVO;

import java.util.List;

/**
 * @author Wallace Mao
 * Date: 2018-11-03 15:23
 */
public interface CorpDepartmentManager {
    CorpDepartmentVO getCorpDepartmentByCorpIdAndDeptId(String corpId, Long deptId);

    List<CorpDepartmentVO> getCorpDepartmentListByCorpId(String corpId);

    void saveOrUpdateCorpDepartment(CorpDepartmentVO dept);

    void updateRsqInfo(CorpDepartmentVO departmentVO);

    void deleteCorpDepartmentByCorpIdAndScopeVersionLessThan(String corpId, Long scopeVersion);

    List<CorpDepartmentVO> getCorpDepartmentListByCorpIdAndParentId(String corpId, Long deptId);

    List<Long> listCorpDepartmentDeptIdByCorpIdAndParentId(String corpId, Long deptId);

    CorpDepartmentVO getCorpDepartmentByCorpIdAndDeptIdAndScopeVersion(
            String corpId, Long deptId, Long scopeVersion);

    List<CorpDepartmentVO> getCorpDepartmentListByCorpIdAndScopeVersion(
            String corpId, Long scopeVersion);

    List<CorpDepartmentVO> getCorpDepartmentListByCorpIdAndScopeVersionLessThan(
            String corpId, Long scopeVersion);

    List<CorpDepartmentVO> getCorpDepartmentListByCorpIdAndParentIdAndScopeVersion(
            String corpId, Long deptId, Long scopeVersion);

    List<CorpDepartmentVO> listTopCorpDepartmentByScopeVersion(String corpId, Long scopeVersion);

    void deleteCorpDepartmentByCorpIdAndDeptId(String corpId, Long deptId);
}

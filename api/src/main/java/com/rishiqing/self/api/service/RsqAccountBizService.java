package com.rishiqing.self.api.service;

import com.rishiqing.dingtalk.isv.api.model.corp.CorpDepartmentVO;
import com.rishiqing.dingtalk.isv.api.model.corp.CorpStaffVO;

/**
 * @author Wallace Mao
 * Date: 2018-11-07 21:10
 */
public interface RsqAccountBizService {
    void pushCreateAll(String corpId);

    void updateRsqDepartment(CorpDepartmentVO departmentVO);

    void deleteRsqDepartment(CorpDepartmentVO departmentVO);

    void createRsqTeamStaff(CorpStaffVO staffVO);

    void createRsqDepartment(CorpDepartmentVO departmentVO);

    void updateRsqTeamStaff(CorpStaffVO corpStaffVO);

    void removeRsqTeamStaff(CorpStaffVO corpStaffVO);

    void updateRsqTeamStaffSetAdmin(CorpStaffVO corpStaffVO);
}
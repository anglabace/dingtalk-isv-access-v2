package com.rishiqing.dingtalk.svc.dingpush.handler.impl;

import com.alibaba.fastjson.JSONObject;
import com.rishiqing.dingtalk.svc.converter.suite.SuiteDbCheckConverter;
import com.rishiqing.dingtalk.svc.service.biz.impl.StaffService;
import com.rishiqing.dingtalk.svc.dingpush.handler.SyncActionHandler;
import com.rishiqing.dingtalk.api.model.vo.corp.CorpStaffVO;
import com.rishiqing.dingtalk.api.model.vo.dingpush.OpenSyncBizDataVO;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * @author Wallace Mao
 * Date: 2018-11-10 14:33
 */
public class UserAddOrgSyncActionHandler implements SyncActionHandler {
    @Autowired
    private StaffService staffService;

    /**
     * @link https://open-doc.dingtalk.com/microapp/ln6dmh/troq7i
     subscribe_id  ： 套件suiteid加下划线0
     copp_id : 开通套件微应用的企业corpid
     biz_id             ： 员工userId
     biz_data         ：数据为如下两种 Json格式：
     企业增加或修改员工，字段值来自于开放平台接口user/get 。
     "syncAction": "user_add_org"，表示企业增加员工事件之后的员工信息
     "syncAction": "user_modify_org" ,表示企业修改员事件之后的员工信息
     "syncAction": "user_dept_change" ，表示企业修改员工所在部门事件之后的员工信息
     "syncAction": "user_role_change"，表示企业修改员工所在角色(包括管理员变更)事件之后的员工信息
     * @param data
     */
    @Override
    public void handleSyncAction(OpenSyncBizDataVO data) {
        JSONObject json = JSONObject.parseObject(data.getBizData());
        // 如果errcode不为0，例如50002，说明请求的员工userid不在授权范围内，那么不需要操作直接返回
        if (json.containsKey("errcode") && json.getLong("errcode") > 0) {
            return;
        }
        String corpId = data.getCorpId();
        CorpStaffVO corpStaffVO = SuiteDbCheckConverter.json2CorpStaff(json);
        corpStaffVO.setCorpId(corpId);
        staffService.saveCorpStaffAndAddCount(corpStaffVO, new Date().getTime());
    }
}

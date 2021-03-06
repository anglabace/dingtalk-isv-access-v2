package com.rishiqing.dingtalk.biz.service.biz.impl;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.rishiqing.dingtalk.auth.http.SuiteRequestHelper;
import com.rishiqing.dingtalk.biz.converter.corp.CorpConverter;
import com.rishiqing.dingtalk.biz.converter.suite.CorpAppConverter;
import com.rishiqing.dingtalk.biz.converter.suite.CorpSuiteAuthConverter;
import com.rishiqing.dingtalk.isv.api.event.CorpOrgChangedEvent;
import com.rishiqing.dingtalk.isv.api.event.CorpOrgCreatedEvent;
import com.rishiqing.dingtalk.isv.api.event.OrderChargeEvent;
import com.rishiqing.dingtalk.isv.api.exception.BizRuntimeException;
import com.rishiqing.dingtalk.isv.api.model.corp.CorpAppVO;
import com.rishiqing.dingtalk.isv.api.model.corp.CorpAuthInfoVO;
import com.rishiqing.dingtalk.isv.api.model.corp.CorpAuthScopeInfoVO;
import com.rishiqing.dingtalk.isv.api.model.corp.CorpTokenVO;
import com.rishiqing.dingtalk.isv.api.model.order.OrderEventVO;
import com.rishiqing.dingtalk.isv.api.model.suite.*;
import com.rishiqing.dingtalk.manager.corp.CorpManager;
import com.rishiqing.dingtalk.manager.suite.AppManager;
import com.rishiqing.dingtalk.manager.suite.CorpAppManager;
import com.rishiqing.dingtalk.manager.suite.CorpSuiteAuthManager;
import com.rishiqing.dingtalk.manager.suite.SuiteManager;
import com.rishiqing.dingtalk.isv.api.service.biz.CorpBizService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 跟公司相关的上层业务服务，用来处理开通应用等业务
 *
 * @author Wallace Mao
 * Date: 2018-11-03 17:43
 */
public class CorpBizServiceImpl implements CorpBizService {
    @Autowired
    private SuiteManager suiteManager;
    @Autowired
    private AppManager appManager;
    @Autowired
    private CorpManager corpManager;
    @Autowired
    private CorpAppManager corpAppManager;
    @Autowired
    private CorpSuiteAuthManager corpSuiteAuthManager;
    @Autowired
    private DeptService deptService;
    @Autowired
    private StaffService staffService;
    @Autowired
    private SuiteRequestHelper suiteRequestHelper;
    @Autowired
    private EventBus asyncCorpOrgCreatedEventBus;
    @Autowired
    private EventBus asyncCorpOrgChangedEventBus;
    @Autowired
    private AsyncEventBus asyncOrderChargeEventBus;

    /**
     * 激活应用。
     * 激活应用兼容两种方式：
     * （1）传统callback的方式。只有corpId，那么在本方法中会获取授权公司信息、授权用户信息、授权信息、授权范围等。
     * （2）钉钉云推送的方式。会获取到包含授权公司信息、授权用户信息、授权信息的json字段
     * 本方法只处理将企业部门以及人员信息从钉钉抓取到auth server。
     * 分为如下几步：
     * 1. 获取企业基本信息（实际上是授权信息），如果没有通讯录可见范围的话要获取通讯录可见范围
     * 2. 保存企业基本信息
     * 3. 根据通讯录可见范围保存部门
     * 4. 获取用户，保存用户
     * 5. eventBus抛出事件，进行后续处理（推送到日事清等）
     *
     * @param corpAuthInfo
     * @param timestamp
     */
    @Override
    public void activateCorpApp(CorpAuthInfoVO corpAuthInfo, Long timestamp) {
        fetchAndSaveCorpAuth(corpAuthInfo, timestamp);
        //  8.  异步，更新钉钉的组织机构以及用户信息到本地，然后与ISV更新组织机构和人员信息
        postCorpOrgCreatedEvent(corpAuthInfo, timestamp);
    }

    /**
     * 变更授权
     授权改变了之后逻辑如下：部门、成员、开通微应用的userId
     1. 开通微应用的userId做更新
     2. saveOrUpdate过程：与开通类似，将可见范围内的所有部门和用户均同步下来，并标记批次版本，并在功倍后台创建
     3. delete过程：将dept和user中小于标记版本的记录，删除掉，并推送到功倍后台
     3.2  成员：成员的删除在推送到功倍后台之后，成员会退出团队，注意要删除user_dept和user_dept_ascription等与部门的关联
     3.1  部门：如果在功倍后台删除部门，那么要推送到功倍后台，功倍后台将单用户的成员踢出团队。注意，如果一个成员有多个部门，那么不踢出团队
     * @param corpAuthInfo
     * @param timestamp 作为同步的版本
     */
    @Override
    public void changeCorpApp(CorpAuthInfoVO corpAuthInfo, Long timestamp) {
        fetchAndSaveCorpAuth(corpAuthInfo, timestamp);
        //  8.  异步，更新钉钉的组织机构以及用户信息到本地，然后与ISV更新组织机构和人员信息
        postCorpOrgChangedEvent(corpAuthInfo, timestamp);
    }

    /**
     * 解除授权
     * @param corpId
     */
    @Override
    public void relieveCorpApp(String corpId) {
        //  删除团队后，暂时不会做任何处理
        AppVO app = appManager.getDefaultAppVO();
        corpManager.deleteCorpSuiteAuthByCorpId(corpId);
        corpManager.deleteCorpAppByCorpId(corpId, app.getAppId());
        corpManager.deleteCorpTokenByCorpId(corpId);
        corpManager.deleteCorpJSAPITicketByCorpId(corpId);
        //  fail暂不处理
//        failBizService.deleteCorpSuiteAuthFailById();
    }

    /**
     * 收到充值事件，做充值前的准备工作
     *
     * @param orderEvent
     */
    @Override
    public void prepareChargeCorpApp(OrderEventVO orderEvent) {
    }

    /**
     * 进行充值
     *
     * @param orderEvent
     */
    @Override
    public void chargeCorpApp(OrderEventVO orderEvent) {
        //  使用eventBus异步调用
        OrderChargeEvent event = new OrderChargeEvent();
        event.setSuiteKey(orderEvent.getSuiteKey());
        event.setOrderId(orderEvent.getOrderId());
        event.setCorpId(orderEvent.getBuyCorpId());
        asyncOrderChargeEventBus.post(event);
    }

    private void fetchAndSaveCorpAuth(CorpAuthInfoVO corpAuthInfo, Long timestamp) {
        SuiteVO suiteVO = suiteManager.getSuite();
        String suiteKey = suiteVO.getSuiteKey();

        String corpId = corpAuthInfo.getAuthCorpInfo().getCorpId();
        if (corpId == null) {
            throw new BizRuntimeException("corpId is null, corpAuthInfo is: " + corpAuthInfo);
        }

        // 首先，获取一次corpToken和corpJSAPIToken
        corpManager.getCorpTokenByCorpId(corpId);
        corpManager.getCorpJSAPITicketByCorpId(corpId);

        //  1. 如果corpAuthInfo中authInfo的授权信息不存在，那么重新获取授权信息
        if (corpAuthInfo.getAuthInfo() == null) {
            // 根据corpId获取authInfo，走钉钉的获取授权信息的接口
            SuiteTicketVO suiteTicketVO = suiteManager.getSuiteTicket();
            corpAuthInfo = suiteRequestHelper.getCorpAuthInfo(suiteVO, suiteTicketVO, corpId);
        }

        //  2. 如果corpAuthInfo中的authScope不存在，那么重新获取可见范围信息
        if (corpAuthInfo.getAuthScope() == null) {
            // 获取企业的可见范围
            CorpTokenVO corpTokenVO = corpManager.getCorpTokenByCorpId(corpId);
            CorpAuthScopeInfoVO scopeInfoVO = suiteRequestHelper.getCorpAuthScopeInfo(suiteVO, corpTokenVO);
            corpAuthInfo.setAuthScope(scopeInfoVO);
        }

        //  在这里corpAuthInfo是完整的包含可见范围和授权的企业信息的对象

        //  3.  保存公司信息
        corpManager.saveOrUpdateCorp(
                CorpConverter.corpAuthInfoVO2CorpVO(corpAuthInfo, timestamp)
        );

        //  4.  保存corpApp的关联信息
        List<CorpAppVO> corpAppVOList = CorpAppConverter.corpAuthInfoVO2AppVOList(corpAuthInfo);
        for (CorpAppVO corpAppVO : corpAppVOList) {
            corpAppManager.saveOrUpdateCorpApp(corpAppVO);
        }

        //  5.  根据可见范围获取部门
        CorpAuthScopeInfoVO scopeInfo = corpAuthInfo.getAuthScope();
        CorpAuthScopeInfoVO.AuthOrgScopes scopes = scopeInfo.getAuthOrgScopes();
        deptService.fetchAndSaveCorpDepartmentList(corpId, scopes.getAuthedDept(), timestamp);

        //  6.  根据可见范围获取成员信息。获取的成员信息有两部分：
        // （1）第一部分是可见范围内的部门所包含的成员信息
        // （2）第二部分是可见范围内的成员列表所包含的成员信息
        long userCount = staffService.fetchAndSaveAllCorpDepartmentStaff(corpId, timestamp);
        List<String> authUserList = scopes.getAuthedUser();
        if (authUserList != null) {
            userCount = userCount + authUserList.size();
        }
        staffService.fetchAndSaveCorpStaffList(corpId, authUserList, timestamp);
        corpManager.saveOrUpdateCorpStatisticUserCount(corpId, userCount);

        //  7.  保存授权信息。处理授权方管理员，谁开通的日事清，就以谁作为创建者。
        CorpSuiteAuthVO corpSuiteAuthVO = CorpSuiteAuthConverter.corpAuthInfoVO2CorpSuiteAuthVO(suiteKey, corpAuthInfo);
        corpSuiteAuthManager.saveOrUpdateCorpSuiteAuth(corpSuiteAuthVO);
    }

    private void postCorpOrgCreatedEvent(CorpAuthInfoVO corpAuthInfo, Long timestamp) {
        SuiteVO suiteVO = suiteManager.getSuite();
        String suiteKey = suiteVO.getSuiteKey();
        String corpId = corpAuthInfo.getAuthCorpInfo().getCorpId();
        CorpOrgCreatedEvent corpOrgCreatedEvent = new CorpOrgCreatedEvent();
        corpOrgCreatedEvent.setSuiteKey(suiteKey);
        corpOrgCreatedEvent.setCorpId(corpId);
        corpOrgCreatedEvent.setScopeVersion(timestamp);
        asyncCorpOrgCreatedEventBus.post(corpOrgCreatedEvent);
    }

    private void postCorpOrgChangedEvent(CorpAuthInfoVO corpAuthInfo, Long scopeVersion) {
        SuiteVO suiteVO = suiteManager.getSuite();
        String suiteKey = suiteVO.getSuiteKey();
        String corpId = corpAuthInfo.getAuthCorpInfo().getCorpId();
        CorpOrgChangedEvent corpOrgChangedEvent = new CorpOrgChangedEvent();
        corpOrgChangedEvent.setSuiteKey(suiteKey);
        corpOrgChangedEvent.setCorpId(corpId);
        corpOrgChangedEvent.setScopeVersion(scopeVersion);
        asyncCorpOrgChangedEventBus.post(corpOrgChangedEvent);
    }
}

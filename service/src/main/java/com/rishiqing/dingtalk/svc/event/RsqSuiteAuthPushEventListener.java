package com.rishiqing.dingtalk.svc.event;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.rishiqing.common.log.LogFormatter;
import com.rishiqing.dingtalk.api.event.CorpOrgCreatedEvent;
import com.rishiqing.dingtalk.api.event.EventListener;
import com.rishiqing.dingtalk.mgr.dingmain.manager.corp.CorpDepartmentManager;
import com.rishiqing.dingtalk.mgr.dingmain.manager.corp.CorpStaffManager;
import com.rishiqing.dingtalk.api.service.biz.FailBizService;
import com.rishiqing.dingtalk.api.service.rsq.RsqAccountBizService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Wallace Mao
 * Date: 2018-11-07 2:14
 */
public class RsqSuiteAuthPushEventListener implements EventListener {
    private static final Logger bizLogger = LoggerFactory.getLogger(RsqSuiteAuthPushEventListener.class);

    @Autowired
    private RsqAccountBizService rsqAccountBizService;
    @Autowired
    private FailBizService failBizService;
    @Autowired
    private CorpDepartmentManager corpDepartmentManager;
    @Autowired
    private CorpStaffManager corpStaffManager;

    @Subscribe
    @AllowConcurrentEvents //  event并行执行
    public void listenCorpOrgSyncEvent(CorpOrgCreatedEvent corpOrgCreatedEvent) {
        try{
            bizLogger.info("-------------------corpOrgCreatedEvent: " + corpOrgCreatedEvent);
            String corpId = corpOrgCreatedEvent.getCorpId();
            Long scopeVersion = corpOrgCreatedEvent.getScopeVersion();
            rsqAccountBizService.syncAllCreated(corpId);
            //  再将本地删除
            corpDepartmentManager.deleteCorpDepartmentByCorpIdAndScopeVersionLessThan(corpId, scopeVersion);
            corpStaffManager.deleteCorpStaffByCorpIdAndScopeVersionLessThan(corpId, scopeVersion);
            // 最后，同步现有用户的管理员信息
            rsqAccountBizService.updateAllCorpAdmin(corpId, scopeVersion);
            bizLogger.info("-------------------corpOrgCreatedEvent----end: " + corpOrgCreatedEvent);
        }catch (Exception e){
            bizLogger.error("-------------------exception----end: " + corpOrgCreatedEvent);
            //  加入失败job,失败任务会重试
            failBizService.saveCorpOrgSyncFail(corpOrgCreatedEvent);
            bizLogger.error(LogFormatter.format(
                    LogFormatter.LogEvent.EXCEPTION,
                    "RsqSuiteAuthPushEventListener",
                    new LogFormatter.KeyValue("corpOrgSyncEvent", corpOrgCreatedEvent)
            ), e);
        }
    }
}

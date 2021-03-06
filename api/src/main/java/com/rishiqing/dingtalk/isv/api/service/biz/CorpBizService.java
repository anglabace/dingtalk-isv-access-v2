package com.rishiqing.dingtalk.isv.api.service.biz;

import com.rishiqing.dingtalk.isv.api.model.corp.CorpAuthInfoVO;
import com.rishiqing.dingtalk.isv.api.model.order.OrderEventVO;

/**
 * @author Wallace Mao
 * Date: 2018-11-03 17:42
 */
public interface CorpBizService {
    void activateCorpApp(CorpAuthInfoVO corpAuthInfo, Long timestamp);

    void changeCorpApp(CorpAuthInfoVO corpAuthInfo, Long timestamp);

    void relieveCorpApp(String corpId);

    void prepareChargeCorpApp(OrderEventVO orderEvent);


    void chargeCorpApp(OrderEventVO orderEvent);
}

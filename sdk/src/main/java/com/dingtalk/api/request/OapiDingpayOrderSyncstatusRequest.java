package com.dingtalk.api.request;

import com.taobao.api.internal.util.RequestCheckUtils;
import java.util.Map;
import java.util.List;

import com.taobao.api.ApiRuleException;
import com.taobao.api.BaseTaobaoRequest;
import com.dingtalk.api.DingTalkConstants;
import com.taobao.api.Constants;
import com.taobao.api.internal.util.TaobaoHashMap;
import com.taobao.api.internal.util.TaobaoUtils;

import com.dingtalk.api.response.OapiDingpayOrderSyncstatusResponse;

/**
 * TOP DingTalk-API: dingtalk.oapi.dingpay.order.syncstatus request
 * 
 * @author top auto create
 * @since 1.0, 2018.10.26
 */
public class OapiDingpayOrderSyncstatusRequest extends BaseTaobaoRequest<OapiDingpayOrderSyncstatusResponse> {
	
	

	/** 
	* 钉支付订单号
	 */
	private String orderNos;

	public void setOrderNos(String orderNos) {
		this.orderNos = orderNos;
	}

	public String getOrderNos() {
		return this.orderNos;
	}

	public String getApiMethodName() {
		return "dingtalk.oapi.dingpay.order.syncstatus";
	}

	private String topResponseType = Constants.RESPONSE_TYPE_DINGTALK_OAPI;

     public String getTopResponseType() {
        return this.topResponseType;
     }

     public void setTopResponseType(String topResponseType) {
        this.topResponseType = topResponseType;
     }

     public String getTopApiCallType() {
        return DingTalkConstants.CALL_TYPE_OAPI;
     }

     private String topHttpMethod = DingTalkConstants.HTTP_METHOD_POST;

     public String getTopHttpMethod() {
     	return this.topHttpMethod;
     }

     public void setTopHttpMethod(String topHttpMethod) {
        this.topHttpMethod = topHttpMethod;
     }

     public void setHttpMethod(String httpMethod) {
         this.setTopHttpMethod(httpMethod);
     }

	public Map<String, String> getTextParams() {		
		TaobaoHashMap txtParams = new TaobaoHashMap();
		txtParams.put("order_nos", this.orderNos);
		if(this.udfParams != null) {
			txtParams.putAll(this.udfParams);
		}
		return txtParams;
	}

	public Class<OapiDingpayOrderSyncstatusResponse> getResponseClass() {
		return OapiDingpayOrderSyncstatusResponse.class;
	}

	public void check() throws ApiRuleException {
		RequestCheckUtils.checkNotEmpty(orderNos, "orderNos");
		RequestCheckUtils.checkMaxListSize(orderNos, 20, "orderNos");
	}
	

}
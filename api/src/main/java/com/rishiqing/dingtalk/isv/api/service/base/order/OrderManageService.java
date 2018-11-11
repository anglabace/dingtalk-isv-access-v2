package com.rishiqing.dingtalk.isv.api.service.base.order;

import com.rishiqing.dingtalk.isv.api.model.order.OrderEventVO;
import com.rishiqing.dingtalk.isv.api.model.order.OrderSpecItemVO;
import com.rishiqing.dingtalk.isv.api.model.order.OrderStatusVO;

/**
 * @author Wallace Mao
 * Date: 2018-11-08 0:02
 */
public interface OrderManageService {
    OrderEventVO getOrderEventByCorpIdAndLatest(String corpId);

    OrderStatusVO getOrderStatusByOrderId(Long orderId);

    OrderSpecItemVO getOrderSpecItemByGoodsCodeAndItemCode(String goodsCode, String itemCode);
}
package com.xianguoji.server.module.order.service;

import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.module.order.dto.*;
import com.xianguoji.server.module.order.vo.OrderPreviewVO;
import com.xianguoji.server.module.order.vo.OrderVO;
import com.xianguoji.server.module.order.vo.RefundVO;

public interface OrderService {

    OrderPreviewVO preview(Long uid, OrderPreviewDto dto);

    String submit(Long uid, OrderSubmitDto dto);

    Object pay(Long uid, String orderNo);

    PageVO<OrderVO> userOrderPage(Long uid, OrderQry qry);

    OrderVO getOrderDetail(Long uid, String orderNo);

    void cancelOrder(Long uid, String orderNo);

    void confirmReceive(Long uid, String orderNo);

    void remindShip(Long uid, String orderNo);

    void repurchase(Long uid, String orderNo);

    void applyRefund(Long uid, RefundApplyDto dto);

    RefundVO getRefundDetail(Long uid, String refundNo);

    PageVO<OrderVO> adminOrderPage(AdminOrderQry qry);

    OrderVO adminOrderDetail(String orderNo);

    void acceptOrder(String orderNo);

    void rejectOrder(String orderNo, String reason);

    void shipOrder(String orderNo, Integer deliveryType, String courierName, String courierPhone);

    void pickupVerify(String orderNo, String pickupCode);

    Object printReceipt(String orderNo);

    void approveRefund(String refundNo);

    void rejectRefund(String refundNo, String reason);

    int newOrderCount(long since);
}

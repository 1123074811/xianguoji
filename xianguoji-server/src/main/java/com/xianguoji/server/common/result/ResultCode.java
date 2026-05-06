package com.xianguoji.server.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(0, "ok"),
    PARAM_ERROR(4000, "参数错误"),
    BIZ_ERROR(4001, "业务校验失败"),
    WX_PROFILE_REQUIRED(4101, "需要补全微信资料"),
    TOKEN_INVALID(4010, "未登录或Token失效"),
    TOKEN_EXPIRED(4011, "Token已过期"),
    ACCESS_DENIED(4030, "无权限"),
    NOT_FOUND(4040, "资源不存在"),
    CONFLICT(4090, "资源冲突"),
    RATE_LIMITED(4290, "接口限流"),
    ACCOUNT_LOCKED(4031, "账户已锁定"),
    IP_LOCKED(4032, "IP已锁定"),
    SSRF_BLOCKED(4033, "URL访问受限"),
    UPLOAD_REJECTED(4034, "文件类型不允许"),
    IDOR_BLOCKED(4035, "越权访问被拦截"),
    INTERNAL_ERROR(5000, "服务器内部错误"),
    THIRD_PARTY_ERROR(5001, "第三方依赖异常"),
    STOCK_NOT_ENOUGH(6001, "库存不足"),
    ORDER_STATUS_INVALID(6002, "订单状态非法流转"),
    COUPON_NOT_AVAILABLE(6003, "优惠券不可用"),
    GROUP_BUY_ENDED(6004, "拼团已满或已结束"),
    PRODUCT_OFF_SHELF(6005, "商品已下架"),
    SHOP_CLOSED(6006, "店铺已打烊"),
    PAYMENT_DUPLICATE(6007, "重复支付请求"),
    REFUND_AMOUNT_INVALID(6008, "退款金额异常");

    private final int code;
    private final String msg;
}

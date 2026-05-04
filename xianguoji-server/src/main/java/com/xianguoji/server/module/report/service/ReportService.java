package com.xianguoji.server.module.report.service;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xianguoji.server.module.catalog.entity.Product;
import com.xianguoji.server.module.catalog.mapper.ProductMapper;
import com.xianguoji.server.module.order.entity.Order;
import com.xianguoji.server.module.order.mapper.OrderMapper;
import com.xianguoji.server.module.user.entity.User;
import com.xianguoji.server.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;

    public byte[] generate(String type, LocalDate start, LocalDate end, String format) {
        List<Map<String, Object>> rows = collect(type, start, end);
        if ("csv".equalsIgnoreCase(format)) {
            return toCsv(rows);
        }
        // 默认 xlsx；pdf 暂以 xlsx 替代
        return toXlsx(rows, type);
    }

    private List<Map<String, Object>> collect(String type, LocalDate start, LocalDate end) {
        List<Map<String, Object>> rows = new ArrayList<>();
        switch (type) {
            case "sales": {
                List<Order> list = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                        .ge(Order::getCreatedAt, start.atStartOfDay())
                        .lt(Order::getCreatedAt, end.plusDays(1).atStartOfDay()));
                for (Order o : list) {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("订单号", o.getOrderNo());
                    r.put("用户ID", o.getUserId());
                    r.put("商品金额", o.getGoodsAmount());
                    r.put("优惠金额", o.getCouponAmount());
                    r.put("配送费", o.getDeliveryFee());
                    r.put("应付", o.getPayAmount());
                    r.put("支付状态", o.getPayStatus());
                    r.put("订单状态", o.getStatus());
                    r.put("下单时间", o.getCreatedAt());
                    rows.add(r);
                }
                break;
            }
            case "inventory": {
                List<Product> list = productMapper.selectList(new LambdaQueryWrapper<Product>()
                        .eq(Product::getStatus, 1));
                for (Product p : list) {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("商品ID", p.getId());
                    r.put("商品名称", p.getName());
                    r.put("分类ID", p.getCategoryId());
                    r.put("最低价", p.getMinPrice());
                    r.put("最高价", p.getMaxPrice());
                    r.put("总库存", p.getTotalStock());
                    r.put("销量", p.getSales());
                    rows.add(r);
                }
                break;
            }
            case "customer": {
                List<User> list = userMapper.selectList(new LambdaQueryWrapper<User>()
                        .ge(User::getRegisterTime, start.atStartOfDay())
                        .lt(User::getRegisterTime, end.plusDays(1).atStartOfDay()));
                for (User u : list) {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("用户ID", u.getId());
                    r.put("昵称", u.getNickname());
                    r.put("手机号", u.getPhone());
                    r.put("标签", u.getTag());
                    r.put("状态", u.getStatus());
                    r.put("注册时间", u.getRegisterTime());
                    r.put("最后登录", u.getLastLoginTime());
                    rows.add(r);
                }
                break;
            }
            case "delivery": {
                List<Order> list = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                        .ge(Order::getCreatedAt, start.atStartOfDay())
                        .lt(Order::getCreatedAt, end.plusDays(1).atStartOfDay()));
                for (Order o : list) {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("订单号", o.getOrderNo());
                    r.put("配送方式", o.getDeliveryType());
                    r.put("收货人", o.getConsignee());
                    r.put("收货电话", o.getConsigneePhone());
                    r.put("收货地址", o.getConsigneeAddress());
                    r.put("配送员", o.getCourierName());
                    r.put("送达时间", o.getDeliveredAt());
                    r.put("完成时间", o.getFinishedAt());
                    rows.add(r);
                }
                break;
            }
            case "finance": {
                List<Order> list = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                        .eq(Order::getPayStatus, 1)
                        .ge(Order::getPayTime, start.atStartOfDay())
                        .lt(Order::getPayTime, end.plusDays(1).atStartOfDay()));
                for (Order o : list) {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("订单号", o.getOrderNo());
                    r.put("支付方式", o.getPayMethod());
                    r.put("交易号", o.getPayTradeNo());
                    r.put("应付", o.getPayAmount());
                    r.put("商品金额", o.getGoodsAmount());
                    r.put("优惠金额", o.getCouponAmount());
                    r.put("支付时间", o.getPayTime());
                    rows.add(r);
                }
                break;
            }
            default:
                Map<String, Object> r = new LinkedHashMap<>();
                r.put("提示", "未知报表类型: " + type);
                rows.add(r);
        }
        return rows;
    }

    private byte[] toCsv(List<Map<String, Object>> rows) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (OutputStreamWriter w = new OutputStreamWriter(bos, StandardCharsets.UTF_8)) {
            // UTF-8 BOM 让 Excel 正确识别中文
            bos.write(0xEF);
            bos.write(0xBB);
            bos.write(0xBF);
            if (rows.isEmpty()) {
                w.write("(空)\n");
            } else {
                List<String> headers = new ArrayList<>(rows.get(0).keySet());
                w.write(String.join(",", headers));
                w.write("\n");
                for (Map<String, Object> row : rows) {
                    List<String> values = new ArrayList<>();
                    for (String h : headers) {
                        Object v = row.get(h);
                        values.add(v == null ? "" : escape(v.toString()));
                    }
                    w.write(String.join(",", values));
                    w.write("\n");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("CSV 生成失败", e);
        }
        return bos.toByteArray();
    }

    private String escape(String s) {
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private byte[] toXlsx(List<Map<String, Object>> rows, String sheetName) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        List<List<String>> headers = new ArrayList<>();
        List<List<Object>> data = new ArrayList<>();
        if (rows.isEmpty()) {
            headers.add(List.of("无数据"));
        } else {
            for (String h : rows.get(0).keySet()) {
                headers.add(List.of(h));
            }
            for (Map<String, Object> row : rows) {
                List<Object> line = new ArrayList<>();
                for (String h : rows.get(0).keySet()) {
                    Object v = row.get(h);
                    line.add(v == null ? "" : v);
                }
                data.add(line);
            }
        }
        EasyExcel.write(bos)
                .head(headers)
                .sheet(sheetName)
                .doWrite(data);
        return bos.toByteArray();
    }

    public void writeTo(OutputStream os, byte[] bytes) {
        try {
            os.write(bytes);
            os.flush();
        } catch (Exception e) {
            throw new RuntimeException("写出失败", e);
        }
    }
}

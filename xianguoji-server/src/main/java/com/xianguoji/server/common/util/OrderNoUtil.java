package com.xianguoji.server.common.util;

import cn.hutool.core.util.RandomUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OrderNoUtil {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    public static String gen() {
        String timePart = LocalDateTime.now().format(FMT);
        String randomPart = RandomUtil.randomNumbers(6);
        return timePart + randomPart;
    }

    public static String genRefundNo() {
        String timePart = LocalDateTime.now().format(FMT);
        String randomPart = RandomUtil.randomNumbers(6);
        return "RF" + timePart + randomPart;
    }
}

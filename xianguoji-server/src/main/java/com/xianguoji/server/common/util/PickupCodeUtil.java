package com.xianguoji.server.common.util;

import cn.hutool.core.util.RandomUtil;

public class PickupCodeUtil {

    public static String gen() {
        return RandomUtil.randomNumbers(6);
    }
}

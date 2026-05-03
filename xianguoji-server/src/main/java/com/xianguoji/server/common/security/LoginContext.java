package com.xianguoji.server.common.security;

public class LoginContext {

    private static final ThreadLocal<LoginUser> CTX = new ThreadLocal<>();

    public static void set(LoginUser u) {
        CTX.set(u);
    }

    public static LoginUser get() {
        return CTX.get();
    }

    public static Long uid() {
        return get() == null ? null : get().getUid();
    }

    public static Long sid() {
        return get() == null ? null : get().getSid();
    }

    public static void clear() {
        CTX.remove();
    }
}

package com.xianguoji.server.common.result;

import java.io.Serializable;
import java.util.List;

public record R<T>(int code, String msg, T data, long ts) implements Serializable {

    public static <T> R<T> ok(T data) {
        return new R<>(0, "ok", data, System.currentTimeMillis());
    }

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> fail(int code, String msg) {
        return new R<>(code, msg, null, System.currentTimeMillis());
    }

    public static <T> R<T> fail(ResultCode rc) {
        return fail(rc.getCode(), rc.getMsg());
    }
}

package com.wikia.calabash.http;

import lombok.Data;

import java.io.Serializable;

/**
 * 返回对象
 */
@Data
public class R<T> implements Serializable {
    private static final long serialVersionUID = 4907893237411537857L;

    /**
     * 返回编码
     */
    private String code = "0";

    /**
     * 返回消息
     */
    private String msg = "success";

    /**
     * 详细信息
     */
    private String detail;

    /**
     * 返回数据
     */
    private T data;

    private String traceId;

    public R(T data) {
        this.data = data;
    }

    public R(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public R(String code, String msg, String detail) {
        this.code = code;
        this.msg = msg;
        this.detail = detail;
    }
}

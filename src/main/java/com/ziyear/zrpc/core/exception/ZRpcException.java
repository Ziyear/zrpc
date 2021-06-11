package com.ziyear.zrpc.core.exception;

/**
 * 功能描述 : 异常
 *
 * @author Ziyear 2021-06-05 16:46
 */
public class ZRpcException extends RuntimeException {
    public ZRpcException() {
        super();
    }

    public ZRpcException(String message) {
        super(message);
    }
}

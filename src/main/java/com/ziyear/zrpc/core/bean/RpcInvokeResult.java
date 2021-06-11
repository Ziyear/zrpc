package com.ziyear.zrpc.core.bean;

/**
 * 执行结果
 *
 * @author Ziyear 2021-06-05 15:32
 */
public class RpcInvokeResult {

    private boolean success;

    private Object data;

    private String msg;

    public boolean isSuccess() {
        return success;
    }

    public RpcInvokeResult() {
    }

    public RpcInvokeResult(boolean success, Object data, String msg) {
        this.success = success;
        this.data = data;
        this.msg = msg;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public static RpcInvokeResult fail(String msg) {
        return new RpcInvokeResult(false, null, msg);
    }

    public static RpcInvokeResult success(Object data) {
        return new RpcInvokeResult(true, data, null);
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RPCInvokeResult{");
        sb.append("success=").append(success);
        sb.append(", data=").append(data);
        sb.append(", msg='").append(msg).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

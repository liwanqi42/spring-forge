package ${basePackage}.common;

import java.io.Serializable;

/**
 * 统一 API 响应结果封装。
 *
 * @param <T> 响应数据类型
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int SUCCESS_CODE = 200;
    private static final int FAIL_CODE = 400;
    private static final String SUCCESS_MSG = "操作成功";

    private int code;
    private String message;
    private T data;

    private Result() {}

    public static <T> Result<T> ok() {
        Result<T> r = new Result<>();
        r.code = SUCCESS_CODE;
        r.message = SUCCESS_MSG;
        return r;
    }

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.code = SUCCESS_CODE;
        r.message = SUCCESS_MSG;
        r.data = data;
        return r;
    }

    public static <T> Result<T> fail(String message) {
        Result<T> r = new Result<>();
        r.code = FAIL_CODE;
        r.message = message;
        return r;
    }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        return r;
    }

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public boolean isSuccess() { return code == SUCCESS_CODE; }
}

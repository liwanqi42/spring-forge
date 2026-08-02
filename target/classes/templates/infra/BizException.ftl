package ${basePackage}.common;

<#if useLombok>
    import lombok.Data;
</#if>

/**
 * 自定义业务异常。
 */
<#if useLombok>
    @Data
</#if>
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String message) {
        super(message);
        this.code = 400;
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

<#if !useLombok>
    public int getCode() { return code; }
</#if>
}

package com.cloudtheon.knowflowcommon.exception;

import com.cloudtheon.knowflowcommon.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常，用于在 Service 层抛出可被 GlobalExceptionHandler 捕获的业务错误
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}

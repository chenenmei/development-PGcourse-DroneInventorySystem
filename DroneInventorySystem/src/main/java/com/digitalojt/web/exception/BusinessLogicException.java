package com.digitalojt.web.exception;

/**
 * ビジネスロジックに関する例外を表すクラス
 * 
 * @author dotlife
 */
public class BusinessLogicException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public BusinessLogicException(String message) {
        super(message);
    }
    
    public BusinessLogicException(String message, Throwable cause) {
        super(message, cause);
    }
}

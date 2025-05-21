package com.digitalojt.web.exception;

/**
 * リソースが見つからない場合の例外を表すクラス
 * 
 * @author dotlife
 */
public class ResourceNotFoundException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

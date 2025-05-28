package com.digitalojt.web.exception;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.digitalojt.web.consts.LogMessage;
import com.digitalojt.web.consts.UrlConsts;
import com.digitalojt.web.dto.ApiResponseDto;

import jakarta.servlet.http.HttpServletRequest;

/**
 * グローバル例外ハンドラー
 */
@ControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * 重複登録例外のハンドリング
	 */
	@ExceptionHandler(DuplicateEntryException.class)
	public String handleDuplicateEntryException(DuplicateEntryException ex, RedirectAttributes redirectAttributes,
			HttpServletRequest request) {
		return handleException(ex, redirectAttributes, request);
	}

	/**
	 * 共通のエラーハンドリング
	 */
	private String handleException(Exception ex, RedirectAttributes redirectAttributes, HttpServletRequest request) {
		String errorMessage = ex.getMessage();
		redirectAttributes.addFlashAttribute(LogMessage.FLASH_ATTRIBUTE_ERROR, errorMessage);

		// リファラーの取得
		String referer = request.getHeader("Referer");
		referer = referer != null ? referer.replaceAll("^(https?://[^/]+)", "") : "";

		return "redirect:" + referer;
	}

	/**
	 * 入力値不正例外のハンドリング
	 */
	@ExceptionHandler(InvalidInputException.class)
	public String handleDatabaseException(InvalidInputException ex, RedirectAttributes redirectAttributes) {
		String errorMessage = ex.getMessage();
		redirectAttributes.addFlashAttribute(LogMessage.FLASH_ATTRIBUTE_ERROR, errorMessage);
		return "redirect:" + UrlConsts.ERROR;
	}

	/**
	 * DBエラーのハンドリング
	 * @return ErrorController class
	 */
	@ExceptionHandler(DataAccessException.class)
	public String handleDatabaseException(DataAccessException ex, RedirectAttributes redirectAttributes) {
		String errorMessage = ex.getMessage();
		redirectAttributes.addFlashAttribute(LogMessage.FLASH_ATTRIBUTE_ERROR, errorMessage);
		return "redirect:" + UrlConsts.ERROR;
	}

	/**
	 * SQLエラーのハンドリング
	 */
	@ExceptionHandler(SQLException.class)
	public String handleSQLException(SQLException ex, RedirectAttributes redirectAttributes) {
		String errorMessage = ex.getMessage();
		redirectAttributes.addFlashAttribute(LogMessage.FLASH_ATTRIBUTE_ERROR, errorMessage);
		return "redirect:" + UrlConsts.ERROR;
	}

    /**
     * バリデーションエラーからエラーメッセージリストを抽出
     *
     * @param bindingResult バリデーション結果
     * @return エラーメッセージリスト
     */
    public static List<String> extractErrorMessages(BindingResult bindingResult) {
        List<String> errorMessages = new ArrayList<>();

        // フィールドエラーを取得
        for (FieldError error : bindingResult.getFieldErrors()) {
            errorMessages.add(error.getField() + ": " + error.getDefaultMessage());
        }

        // グローバルエラーを取得
        for (ObjectError error : bindingResult.getGlobalErrors()) {
            errorMessages.add(error.getDefaultMessage());
        }

        return errorMessages;
    }

    /**
     * バリデーションエラーを処理
     *
     * @param bindingResult バリデーション結果
     * @return クライアントエラーレスポンス
     */
    public static <T> ResponseEntity<ApiResponseDto<T>> handleValidationError(BindingResult bindingResult) {
        List<String> errorMessages = extractErrorMessages(bindingResult);
        ApiResponseDto<T> response = ApiResponseDto.clientError("入力値が不正です", errorMessages);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * ビジネスロジック例外のハンドラー
     *
     * @param ex 例外
     * @return クライアントエラーレスポンス
     */
    @ExceptionHandler(BusinessLogicException.class)
    public <T> ResponseEntity<ApiResponseDto<T>> handleBusinessLogicException(BusinessLogicException ex) {
        List<String> errors = new ArrayList<>();
        errors.add(ex.getMessage());

        ApiResponseDto<T> response = ApiResponseDto.clientError("ビジネスルール違反", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * リソース未検出例外のハンドラー
     *
     * @param ex 例外
     * @return Not Foundエラーレスポンス
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public <T> ResponseEntity<ApiResponseDto<T>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        List<String> errors = new ArrayList<>();
        errors.add(ex.getMessage());

        ApiResponseDto<T> response = ApiResponseDto.<T>builder()
                .message("リソースが見つかりません")
                .status(404)
                .success(false)
                .errors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

	/**
	 * 予期せぬシステムエラーのハンドリング
	 */
	@ExceptionHandler(Exception.class)
	public <T> ResponseEntity<ApiResponseDto<T>> handleGenericException(Exception ex) {
		ApiResponseDto<T> response = ApiResponseDto.serverError("サーバー内部でエラーが発生しました");
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}
}

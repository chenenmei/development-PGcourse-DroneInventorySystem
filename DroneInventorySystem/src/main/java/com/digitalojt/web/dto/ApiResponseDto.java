package com.digitalojt.web.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API応答のための共通DTOクラス
 * フロントエンドに返すレスポンスの標準形式を定義
 *
 * @author dotlife
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseDto<T> {

    /**
     * 応答メッセージ
     */
    private String message;

    /**
     * 応答ステータスコード
     */
    private int status;

    /**
     * 処理結果（成功/失敗）
     */
    private boolean success;
    
    /**
     * エラー詳細情報のリスト
     */
    private List<String> errors;

    /**
     * データペイロード
     */
    private T results;

    /**
     * 成功応答を作成する静的ファクトリーメソッド
     * 
     * @param <T> データ型
     * @param data レスポンスデータ
     * @return 成功応答DTO
     */
    public static <T> ApiResponseDto<T> success(T data) {
        return ApiResponseDto.<T>builder()
                .message("処理が成功しました")
                .status(200)
                .success(true)
                .results(data)
                .build();
    }
    
    /**
     * カスタムメッセージ付きの成功応答を作成する静的ファクトリーメソッド
     * 
     * @param <T> データ型
     * @param data レスポンスデータ
     * @param message カスタムメッセージ
     * @return 成功応答DTO
     */
    public static <T> ApiResponseDto<T> success(T data, String message) {
        return ApiResponseDto.<T>builder()
                .message(message)
                .status(200)
                .success(true)
                .results(data)
                .build();
    }

    /**
     * クライアントエラー応答を作成する静的ファクトリーメソッド
     * 
     * @param <T> データ型
     * @param message エラーメッセージ
     * @param errors エラー詳細情報のリスト
     * @return クライアントエラー応答DTO
     */
    public static <T> ApiResponseDto<T> clientError(String message, List<String> errors) {
        return ApiResponseDto.<T>builder()
                .message(message)
                .status(400)
                .success(false)
                .errors(errors)
                .build();
    }

    /**
     * サーバーエラー応答を作成する静的ファクトリーメソッド
     * 
     * @param <T> データ型
     * @param message エラーメッセージ
     * @return サーバーエラー応答DTO
     */
    public static <T> ApiResponseDto<T> serverError(String message) {
        return ApiResponseDto.<T>builder()
                .message(message)
                .status(500)
                .success(false)
                .build();
    }
}

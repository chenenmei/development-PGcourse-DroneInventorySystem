package com.digitalojt.web.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.digitalojt.web.exception.GlobalExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseBody;

import com.digitalojt.web.consts.LogMessage;
import com.digitalojt.web.consts.ModelAttributeContents;
import com.digitalojt.web.consts.Region;
import com.digitalojt.web.consts.UrlConsts;
import com.digitalojt.web.dto.ApiResponseDto;
import com.digitalojt.web.entity.CenterInfo;
import com.digitalojt.web.exception.BusinessLogicException;
import com.digitalojt.web.exception.ResourceNotFoundException;
import com.digitalojt.web.form.CenterInfoForm;
import com.digitalojt.web.service.CenterInfoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 在庫センター情報画面のコントローラークラス
 * 
 * @author dotlife
 *
 */
@Controller
@RequiredArgsConstructor
public class CenterInfoController extends AbstractController {

	/** センター情報 サービス */
	private final CenterInfoService centerInfoService;
	
	/** ロガー */
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CenterInfoController.class);

	/**
	 * 都道府県Enumをリストに変換
	 * 
	 * @return
	 */
	@ModelAttribute(ModelAttributeContents.REGIONS)
	public List<Region> populateRegions() {
		return Arrays.asList(Region.values());
	}

	/**
	 * 初期表示
	 * 
	 * @param model
	 * @retur
	 */
	@GetMapping(UrlConsts.CENTER_INFO)
	public String index(Model model) {
		logStart(LogMessage.HTTP_GET);

		// 在庫センター情報画面に表示するデータを取得
		List<CenterInfo> centerInfoList = centerInfoService.getCenterInfoData();

		// 画面表示用に商品情報リストをセット
		model.addAttribute(ModelAttributeContents.CENTER_INFO_LIST, centerInfoList);

		logEnd(LogMessage.HTTP_GET);

		return UrlConsts.CENTER_INFO_INDEX;
	}

	/**
	 * 検索結果表示（JSON形式）
	 * 
	 * @param model
	 * @param form
	 * @param bindingResult
	 * @return 標準化されたJSON応答（ApiResponseDto）
	 */
	@GetMapping(value = UrlConsts.CENTER_INFO_SEARCH, produces = "application/json")
	@ResponseBody
	public ResponseEntity<ApiResponseDto<List<CenterInfo>>> search(Model model, @Valid CenterInfoForm form, BindingResult bindingResult) {
		logStart(LogMessage.HTTP_GET);
		
		// 入力値のバリデーションチェック
		if (bindingResult.hasErrors()) {
			logger.warn("検索条件のバリデーションエラー: {}", bindingResult.getAllErrors());
			return GlobalExceptionHandler.handleValidationError(bindingResult);
		}
		
		try {
			// リクエストパラメータのログ出力
			logSearchParameters(form);
	
			// 検索条件に基づいて在庫センター情報を取得
			// 2025/05/16 機能改修 現在容量範囲が検索できるため、検索項目引数を追加する
			List<CenterInfo> centerInfoList = centerInfoService.getCenterInfoData(
					form.getCenterName(), 
					form.getRegion(),
					form.getStorageCapacityFrom(),
					form.getStorageCapacityTo());
	
			logEnd(LogMessage.HTTP_GET);
			
			// 検索結果がない場合は、成功レスポンスを空リストで返却
			if (centerInfoList.isEmpty()) {
				ApiResponseDto<List<CenterInfo>> response = ApiResponseDto.success(
						Collections.emptyList(), 
						"該当するデータはありません");
				return ResponseEntity.ok(response);
			}
			
			// 成功レスポンスを返却
			ApiResponseDto<List<CenterInfo>> response = ApiResponseDto.success(
					centerInfoList, 
					String.format("%d件のデータが見つかりました", centerInfoList.size()));
			return ResponseEntity.ok(response);
			
		} catch (Exception e) {
			// 予期せぬエラーをログ出力
			logger.error("検索処理中にエラーが発生しました", e);
			
			// サーバーエラーレスポンスを返却
			ApiResponseDto<List<CenterInfo>> errorResponse = ApiResponseDto.serverError(
					"データ検索中にエラーが発生しました。システム管理者に連絡してください。");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}
	
	/**
	 * HTML形式の検索結果表示（通常のフォーム送信時に使用）
	 * 
	 * @param model
	 * @param form
	 * @param bindingResult
	 * @return HTML応答
	 */
	@GetMapping(value = UrlConsts.CENTER_INFO_SEARCH, produces = "text/html")
	public String searchHtml(Model model, @Valid CenterInfoForm form, BindingResult bindingResult) {
		logStart(LogMessage.HTTP_GET);

		// 入力値のバリデーションチェック
		if (bindingResult.hasErrors()) {
			handleValidationError(model, bindingResult, form);
			return UrlConsts.CENTER_INFO_INDEX;
		}
		
		try {
			// リクエストパラメータのログ出力
			logSearchParameters(form);
	
			// 検索条件に基づいて在庫センター情報を取得
			List<CenterInfo> centerInfoList = centerInfoService.getCenterInfoData(
					form.getCenterName(), 
					form.getRegion(),
					form.getStorageCapacityFrom(),
					form.getStorageCapacityTo());
	
			// 画面表示用に在庫センター情報リストをセット
			model.addAttribute(ModelAttributeContents.CENTER_INFO_LIST, centerInfoList);
			
			// 検索結果が空の場合
			if (centerInfoList.isEmpty()) {
				model.addAttribute("infoMessage", "該当するデータはありません");
			} else {
				model.addAttribute("infoMessage", String.format("%d件のデータが見つかりました", centerInfoList.size()));
			}
			
			logEnd(LogMessage.HTTP_GET);
			
			return UrlConsts.CENTER_INFO_INDEX;
			
		} catch (Exception e) {
			// 予期せぬエラーをログ出力
			logger.error("検索処理中にエラーが発生しました", e);
			
			// エラーメッセージを設定して画面に戻す
			model.addAttribute(LogMessage.FLASH_ATTRIBUTE_ERROR, "データ検索中にエラーが発生しました。システム管理者に連絡してください。");
			return UrlConsts.CENTER_INFO_INDEX;
		}
	}
	
	/**
	 * 検索パラメータのログ出力
	 * 
	 * @param form 検索フォーム
	 */
	private void logSearchParameters(CenterInfoForm form) {
		// 検索条件のログ出力
		StringBuilder logMsg = new StringBuilder("検索条件: ");
		logMsg.append("センター名=").append(form.getCenterName()).append(", ");
		logMsg.append("都道府県=").append(form.getRegion()).append(", ");
		logMsg.append("容量From=").append(form.getStorageCapacityFrom()).append(", ");
		logMsg.append("容量To=").append(form.getStorageCapacityTo());
		
		logger.info(logMsg.toString());
	}

	/**
	 * バリデーションエラー処理
	 * 
	 * @param model
	 * @param bindingResult
	 * @param form
	 */
	private void handleValidationError(Model model, BindingResult bindingResult, CenterInfoForm form) {
		// エラーメッセージをリストに格納
		StringBuilder errorMsg = new StringBuilder();

		// フィールドごとのエラーメッセージを取得し、リストに追加
		bindingResult.getGlobalErrors().forEach(error -> {
			String message = error.getDefaultMessage();
			errorMsg.append(message).append("\r\n"); // メッセージを改行で区切って追加
		});

		// エラーメッセージをモデルに追加
		model.addAttribute(LogMessage.FLASH_ATTRIBUTE_ERROR, errorMsg.toString());

		logValidationError(LogMessage.HTTP_POST, form + " " + errorMsg.toString());
	}
}

package com.digitalojt.web.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.digitalojt.web.consts.LogMessage;
import com.digitalojt.web.consts.ModelAttributeContents;
import com.digitalojt.web.consts.Region;
import com.digitalojt.web.consts.UrlConsts;
import com.digitalojt.web.dto.ApiResponseDto;
import com.digitalojt.web.entity.CenterInfo;
import com.digitalojt.web.exception.BusinessLogicException;
import com.digitalojt.web.exception.GlobalExceptionHandler;
import com.digitalojt.web.exception.ResourceNotFoundException;
import com.digitalojt.web.form.CenterInfoForm;
import com.digitalojt.web.service.CenterInfoService;
import com.digitalojt.web.validation.ValidationGroups.Insert;
import com.digitalojt.web.validation.ValidationGroups.Update;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.validation.Valid;

/**
 * 在庫センター情報画面のコントローラークラス
 * 
 * @author dotlife
 *
 */
@Controller
public class CenterInfoController extends AbstractController {

	/** センター情報 サービス */
	private final CenterInfoService centerInfoService;
	
	/** ロガー */
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CenterInfoController.class);

	// Jackson ObjectMapper
	private final ObjectMapper objectMapper;
	
	/**
     * コンストラクタ
     * 
     * @param centerInfoService センター情報サービス
     */
	public CenterInfoController(CenterInfoService centerInfoService) {
		this.centerInfoService = centerInfoService;
		// ObjectMapperの設定
		this.objectMapper = new ObjectMapper()
				.registerModule(new JavaTimeModule())
				.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
	}

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
		logger.info("在庫センター情報画面の初期表示処理を開始します");

		// 在庫センター情報画面に表示するデータを取得
		List<CenterInfo> centerInfoList = centerInfoService.getCenterInfoData();
		logger.info("取得したセンター情報データ: {}件", centerInfoList.size());

		// 画面表示用に商品情報リストをセット
		model.addAttribute(ModelAttributeContents.CENTER_INFO_LIST, centerInfoList);
		
		// centerInfoListをJSON文字列に変換してモデルに追加
		try {
			// デバッグ情報
			logger.debug("JSON変換前のデータ件数: {}", centerInfoList.size());
			
			// JSON文字列に変換
			String centerInfoJson = objectMapper.writeValueAsString(centerInfoList);
			
			// デバッグログ
			if (centerInfoList.isEmpty()) {
				logger.debug("変換されたJSON (空リスト): []");
			} else {
				logger.debug("変換されたJSON（一部）: {}", 
					centerInfoJson.length() > 100 ? centerInfoJson.substring(0, 100) + "..." : centerInfoJson);
			}
			
			model.addAttribute("centerInfoJson", centerInfoJson);
			logger.info("初期データをモデルに設定しました: centerInfoJson");
		} catch (Exception e) {
			// JSON変換エラー
			logException(LogMessage.HTTP_GET, "JSON変換エラー: " + e.getMessage());
			logger.error("JSON変換エラーの詳細", e);
			model.addAttribute("centerInfoJson", "[]");
			logger.warn("JSONエラーのため、空の配列をモデルに設定しました");
		}

		logEnd(LogMessage.HTTP_GET);
		logger.info("在庫センター情報画面の初期表示処理が完了しました（表示テンプレート: {}）", UrlConsts.CENTER_INFO_INDEX);

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
		logger.info("在庫センター情報の検索APIが呼び出されました");
		
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
	
			logger.info("検索結果: {}件のデータが見つかりました", centerInfoList.size());
			
			// デバッグ：検索結果のJSONシリアライズ確認
			try {
				logger.debug("JSON変換前の検索結果: {} 件", centerInfoList.size());
				String resultJson = objectMapper.writeValueAsString(centerInfoList);
				logger.debug("検索結果のJSON変換: {} 文字", resultJson.length());
				if (centerInfoList.size() > 0) {
					logger.debug("JSON変換サンプル (最初の1件): {}",
							objectMapper.writeValueAsString(centerInfoList.get(0)));
				}
			} catch (Exception ex) {
				logger.warn("検索結果のJSON変換デバッグ中にエラー", ex);
			}
			
			logEnd(LogMessage.HTTP_GET);
			
			// 検索結果がない場合は、成功レスポンスを空リストで返却
			if (centerInfoList.isEmpty()) {
				ApiResponseDto<List<CenterInfo>> response = ApiResponseDto.success(
						Collections.emptyList(), 
						"該当するデータはありません");
				logger.info("検索結果なしのレスポンスを返します");
				return ResponseEntity.ok(response);
			}
			
			// 成功レスポンスを返却
			ApiResponseDto<List<CenterInfo>> response = ApiResponseDto.success(
					centerInfoList, 
					String.format("%d件のデータが見つかりました", centerInfoList.size()));
			logger.info("検索結果レスポンスを返します: {}件", centerInfoList.size());
			return ResponseEntity.ok(response);
			
		} catch (Exception e) {
			// 予期せぬエラーをログ出力
			logger.error("検索処理中にエラーが発生しました", e);
			
			// サーバーエラーレスポンスを返却
			ApiResponseDto<List<CenterInfo>> response = ApiResponseDto.serverError(
					String.format("検索処理中にエラーが発生しました: %s", e.getMessage()));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
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
			
			// centerInfoListをJSON文字列に変換してモデルに追加
			try {
				String centerInfoJson = objectMapper.writeValueAsString(centerInfoList);
				model.addAttribute("centerInfoJson", centerInfoJson);
			} catch (Exception e) {
				// JSON変換エラー
				logException(LogMessage.HTTP_GET, "JSON変換エラー: " + e.getMessage());
				model.addAttribute("centerInfoJson", "[]");
			}
			
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
	 * 検索結果をJSON形式で返却（APIエンドポイント用）
	 * 
	 * @param form
	 * @return
	 */
	@GetMapping(UrlConsts.CENTER_INFO_SEARCH + "/api")
	@ResponseBody
	public List<CenterInfo> searchApi(@Valid CenterInfoForm form) {
		// 検索条件に基づいて在庫センター情報を取得
		return centerInfoService.getCenterInfoData(
				form.getCenterName(), 
				form.getRegion(),
				form.getStorageCapacityFrom(), 
				form.getStorageCapacityTo());
	}
	
	
	/**
	 * 新規登録画面を表示する
	 * GET /admin/centerInfo/insert
	 */
	@GetMapping(UrlConsts.CENTER_INFO_INSERT)
	public String showInsertPage(Model model) {
		model.addAttribute("centerInfoForm", new CenterInfoForm());
		
		// templates/admin/centerInfo/centerinfo_insert.html を表示
		return "admin/centerInfo/centerinfoinsert";
		
	}
	
	/**
	 * 更新画面を表示
	 * URL例: /admin/centerInfo/update?centerId=1
	 */
	@GetMapping("/admin/centerInfo/update")
	public String updateForm(@RequestParam("centerId") int centerId,
	                         Model model) {
	    // 画面側で Ajax 用に centerId を渡すだけ。DB 取得は前画面でも OK
	    model.addAttribute("centerId", centerId);
	    return "admin/centerInfo/centerinfoupdate";   // テンプレート名と一致させる
	}
	
	/**
	 * 削除画面（または確認ダイアログ用ページ）を表示
	 * URL例: /admin/centerInfo/delete?centerId=1
	 */
	@GetMapping("/admin/centerInfo/delete")
	public String deleteForm(@RequestParam("centerId") int centerId,Model model) {
		return "admin/centerInfo/centerinfodelete";
	}
	
	/**
	 * 在庫センター情報の新規登録（JSON形式）
	 * 
	 * <pre>
	 * - Ajax から JSON ボディで CenterInfoForm を受け取り、
	 *    正常時は登録した CenterInfo を JSON で返却する。
	 * - バリデーション NG の場合は GlobalExceptionHandler で標準化済みエラーを返す。
	 * - 想定外エラー時は HTTP 500 を返却。
	 * </pre>
	 * 
	 * @param form          フロントから送られてくる入力データ
	 * @param bindingResult バリデーション結果
	 * @return ApiResponseDto (成功時: 登録済みエンティティ, 失敗時: エラーメッセージ)
	 */
	@PostMapping(
	        value = "/admin/centerInfo/insert",
	        consumes = "application/json",
	        produces = "application/json")
	@ResponseBody
	public ResponseEntity<ApiResponseDto<CenterInfo>> insert(
			@RequestBody @Validated(Insert.class) CenterInfoForm form,
	        BindingResult bindingResult) {

	    logStart(LogMessage.HTTP_POST);
	    logger.info("在庫センター情報 新規登録API 呼び出し");

	    /* バリデーション */
	    if (bindingResult.hasErrors()) {
	        logger.warn("バリデーションエラー: {}", bindingResult.getAllErrors());
	        return GlobalExceptionHandler.handleValidationError(bindingResult);
	    }

	    try {
	        /* 登録処理 */
	        CenterInfo saved = centerInfoService.insertCenterInfo(form);
	        logger.info("登録成功 ID={}", saved.getCenterId());

	        /* 成功レスポンス */
	        ApiResponseDto<CenterInfo> res =
	                ApiResponseDto.success(saved, "センター情報を登録しました");
	        logEnd(LogMessage.HTTP_POST);
	        return ResponseEntity.status(HttpStatus.CREATED).body(res);

	    } catch (BusinessLogicException | ResourceNotFoundException ex) {
	        logger.error("業務例外", ex);
	        ApiResponseDto<CenterInfo> res =
	                ApiResponseDto.clientError(ex.getMessage());
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);

	    } catch (Exception ex) {
	        logger.error("想定外例外", ex);
	        ApiResponseDto<CenterInfo> res =
	                ApiResponseDto.serverError("登録処理中にエラーが発生しました: " + ex.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
	    }
	}
	
	/**
     * 在庫センター情報の更新（JSON形式）
     *
     * <pre>
     * - Ajax から JSON ボディで CenterInfoFormを受け取り、
     *    正常時は更新後の CenterInfo を JSON で返却する。
     * - バリデーション NG の場合は GlobalExceptionHandler により標準化済みエラーを返す。
     * - 楽観ロック衝突(ObjectOptimisticLockingFailureException)時は HTTP 409 (Conflict) を返す。
     * - 想定外エラー時は HTTP 500 (Internal Server Error) を返す。
     * </pre>
     *
     * @param form          フロントから送られてくる入力データ
     * @param bindingResult バリデーション結果
     * @return ApiResponseDto
     *         (成功時: 更新済みエンティティ,
     *          排他衝突・業務エラー時: エラーメッセージ)
     */
	@PutMapping(
			value = "/admin/centerInfo/update",
	        consumes = "application/json",
	        produces = "application/json")
	@ResponseBody
	public ResponseEntity<ApiResponseDto<CenterInfo>> update(
			@RequestBody @Validated(Update.class) CenterInfoForm form,
			BindingResult bindingResult) {
		logStart(LogMessage.HTTP_PUT);
		logger.info("在庫センター情報 更新API 呼び出し");
		
		/* バリデーション */
		if (bindingResult.hasErrors()) {
			logger.warn("バリデーションエラー: {}", bindingResult.getAllErrors());
			return GlobalExceptionHandler.handleValidationError(bindingResult);
		}
		
		try {
			
			/* 更新処理 */
			CenterInfo updated = centerInfoService.updateCenterInfo(form);
			logger.info("更新成功 ID={}", updated.getCenterId());
			
			
			/* 成功レスポンス (200 OK) */
			ApiResponseDto<CenterInfo> res =
					ApiResponseDto.success(updated, "センター情報を更新しました");
			logEnd(LogMessage.HTTP_PUT);
			return ResponseEntity.ok(res);
			
					
		} catch (ObjectOptimisticLockingFailureException ex) {
			
			// 排他制御 (version 不一致) → 409
			logger.warn("排他エラー", ex);
			ApiResponseDto<CenterInfo> res =
					ApiResponseDto.clientError("他のユーザーが更新されました。再読込してやり直してください。");
			return ResponseEntity.status(HttpStatus.CONFLICT).body(res);
			
			
		} catch (BusinessLogicException | ResourceNotFoundException ex) {
			
			logger.error("業務例外", ex);
			ApiResponseDto<CenterInfo> res =
					ApiResponseDto.clientError(ex.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
			
			
		} catch (Exception ex) {
			
			logger.error("想定外例外", ex);
			ApiResponseDto<CenterInfo> res =
					ApiResponseDto.serverError("更新処理中にエラーが発生しました: " + ex.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
			
			
		}

	}
	
    /**
     * 在庫センター情報を削除します（非同期）。
     *
     * @param id 削除対象のセンターID
     * @return JSON 形式の ApiResponseDto<Void>
     */
	@DeleteMapping("/admin/centerInfo/delete/{id}")
	@ResponseBody
	public ResponseEntity<ApiResponseDto<Void>> deleteCenterInfo(
			@PathVariable int id,
			@RequestParam("version") int version) {
		
		// Service 層に削除処理を委譲
		ApiResponseDto<Void> response = centerInfoService.deleteCenterInfo(id, version);
		
		// 結果に応じて HTTP ステータスを設定
		HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(response, status);
	}

	/**
	 * 検索パラメータのログ出力
	 * 
	 * @param form 検索フォーム
	 */
	private void logSearchParameters(CenterInfoForm form) {
		logger.info("検索条件 - センター名: {}", form.getCenterName());
		logger.info("検索条件 - 都道府県: {}", form.getRegion());
		logger.info("検索条件 - 容量(From): {}", form.getStorageCapacityFrom());
		logger.info("検索条件 - 容量(To): {}", form.getStorageCapacityTo());
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

package com.digitalojt.web.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import com.digitalojt.web.entity.CenterInfo;
import com.digitalojt.web.exception.BusinessLogicException;
import com.digitalojt.web.exception.ResourceNotFoundException;
import com.digitalojt.web.form.CenterInfoForm;
import com.digitalojt.web.repository.CenterInfoRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
/**
 * 在庫センター情報画面のサービスクラス
 *
 * @author dotlife
 * 
 */
@Service
@RequiredArgsConstructor
public class CenterInfoService {
	
	/** 2025/05/21 ロガー定義 */
	private static final Logger logger = LoggerFactory.getLogger(CenterInfoService.class);

	/** センター情報テーブル リポジトリー */
	private final CenterInfoRepository repository;

	/**
	 * 在庫センター情報を全建検索で取得
	 * 
	 * @return 全在庫センター情報のリスト
	 */
	public List<CenterInfo> getCenterInfoData() {
		logger.info("在庫センター情報の全件検索を実行");
		List<CenterInfo> results = repository.findAll();
		logger.info("全件検索結果: {}件のデータを取得", results.size());
		return results;
	}

	/**
	 * 引数に合致する在庫センター情報を取得
	 * 2025/05/16 機能改修 現在容量範囲が検索できるため、検索項目引数を追加する
	 * 
	 * @param centerName センター名
	 * @param region 地域
	 * @param storageCapacityFrom 容量範囲（最小）
	 * @param storageCapacityTo 容量範囲（最大）
	 * @return 条件に合致する在庫センター情報のリスト
	 * @throws BusinessLogicException ビジネスロジックエラー発生時
	 */
	public List<CenterInfo> getCenterInfoData(String centerName, String region, Integer storageCapacityFrom, Integer storageCapacityTo) {
		// 検索条件の正規化
		String normalizedCenterName = (centerName == null) ? "" : centerName.trim();
		String normalizedRegion = (region == null) ? "" : region.trim();
		
		// 容量の範囲設定（nullの場合はデフォルト値を使用）
		Integer fromCapacity = (storageCapacityFrom == null) ? Integer.MIN_VALUE : storageCapacityFrom;
		Integer toCapacity = (storageCapacityTo == null) ? Integer.MAX_VALUE : storageCapacityTo;
		
		// 容量の範囲チェック
		if (fromCapacity > toCapacity) {
			logger.warn("容量範囲が不正: From={}, To={}", fromCapacity, toCapacity);
			throw new BusinessLogicException("容量の範囲指定が不正です。From値はTo値以下である必要があります。");
		}
		
		// 検索条件のログ出力
		logger.info("在庫センター情報の条件検索を実行: centerName=[{}], region=[{}], capacityFrom={}, capacityTo={}", 
				normalizedCenterName, normalizedRegion, fromCapacity, toCapacity);
		
		try {
			// リポジトリを使用して検索を実行
			List<CenterInfo> results = repository.findActiveCenters(
					normalizedCenterName, normalizedRegion, fromCapacity, toCapacity);
			
			// 検索結果のログ出力
			logger.info("条件検索結果: {}件のデータを取得", results.size());
			
			return results;
		} catch (Exception e) {
			// データアクセスエラーをログに記録
			logger.error("データベース検索中にエラーが発生しました", e);
			throw e; // 上位層で処理するためにスローし直す
		}
	}
	
	/**
	 * 在庫センター情報を新規登録する
	 * 
	 * <pre>
	 * 入力値をエンティティに変換
	 * 業務バリデーション 
	 * Repository#save で永続化
	 * 保存済みエンティティを返却
	 * </pre>
	 * 
	 * @param form 画面・API から受け取った入力フォーム
	 * @return 保存済みの CenterInfo エンティティ
	 * @throws BusinessLogicException 業務ロジックに違反した場合
	 */
	@Transactional
	public CenterInfo insertCenterInfo(CenterInfoForm form) {
	    logger.info("新規登録処理開始: centerName={}, region={}, MaxStorageCapacity={}, CurrentStorageCapacity={}",
	            form.getCenterName(),
	            form.getRegion(),
	            form.getMaxStorageCapacity(),
	            form.getCurrentStorageCapacity());

	    /* ---------- 業務バリデーション ---------- */
	    if (form.getCurrentStorageCapacity() != null && form.getMaxStorageCapacity() != null
	            && form.getCurrentStorageCapacity() > form.getMaxStorageCapacity()) {
	        throw new BusinessLogicException("現在容量は最大容量以下である必要があります。");
	    }

	    /* ---------- Form → Entity 変換 ---------- */
	    LocalDateTime now = LocalDateTime.now();
	    CenterInfo entity = new CenterInfo();
	    entity.setCenterName(form.getCenterName());
	    entity.setPostCode(form.getPostCode());
	    entity.setAddress(form.getAddress());
	    entity.setPhoneNumber(form.getPhoneNumber());
	    entity.setManagerName(form.getManagerName());
	    entity.setMaxStorageCapacity(form.getMaxStorageCapacity());
	    entity.setCurrentStorageCapacity(form.getCurrentStorageCapacity());
	    entity.setNotes(form.getNotes());
	    entity.setOperationalStatus(0);
	    entity.setDeleteFlag(0);
	    entity.setCreateDate(now);
	    entity.setUpdateDate(now);

	    /* ---------- 永続化 ---------- */
	    CenterInfo saved;
	    try {
	        saved = repository.save(entity);
	        logger.info("新規登録成功: id={}", saved.getCenterId());
	    } catch (Exception e) {
	        // ★ ここでスタックトレースを完全出力し、再スロー
	        logger.error("INSERT FAILED - 保存処理で例外発生", e);
	        throw e;
	    }

	    return saved;
	}
	
	/**
     * 在庫センター情報を更新する
     * 
     * <pre>
     * 1. centerId で既存エンティティ取得（存在しなければ 404）
     * 2. 業務バリデーション（現在容量 <= 最大容量）
     * 3. エンティティへ値を上書き
     * 4. save() で永続化（version 列により楽観ロック）
     * </pre>
     * 
     * @param form 更新入力
     * @return 更新後エンティティ
     * @throws BusinessLogicException             業務バリデーション NG
     * @throws ResourceNotFoundException          該当データなし
     * @throws ObjectOptimisticLockingFailureException 排他衝突
     */
	@Transactional
    public CenterInfo updateCenterInfo(CenterInfoForm form) {
		
		logger.info("更新処理開始: centerId={}, version={}",
                form.getCenterId(), form.getVersion());
		
		/* ---------- 既存データ取得 ---------- */
		CenterInfo entity = repository.findById(form.getCenterId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ID=" + form.getCenterId() + " のデータが存在しません"));
		
		/* ---------- 業務バリデーション ---------- */
        if (form.getCurrentStorageCapacity() != null
                && form.getMaxStorageCapacity() != null
                && form.getCurrentStorageCapacity() > form.getMaxStorageCapacity()) {
        	
        	throw new BusinessLogicException("現在容量は最大容量以下である必要があります。");
        	
        }
        
        /* ---------- 値の上書き ---------- */
        entity.setCenterName(form.getCenterName());
        entity.setPostCode(form.getPostCode());
        entity.setAddress(form.getAddress());
        entity.setPhoneNumber(form.getPhoneNumber());
        entity.setManagerName(form.getManagerName());
        entity.setMaxStorageCapacity(form.getMaxStorageCapacity());
        entity.setCurrentStorageCapacity(form.getCurrentStorageCapacity());
        entity.setNotes(form.getNotes());
        entity.setUpdateDate(LocalDateTime.now());
        
        /* ---------- ④ 永続化 ---------- */
        CenterInfo saved = repository.save(entity);
        logger.info("更新完了: id={}, newVersion={}", saved.getCenterId(), saved.getVersion());

        return saved;
        
	}
}

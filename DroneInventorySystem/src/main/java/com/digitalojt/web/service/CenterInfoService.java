package com.digitalojt.web.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.digitalojt.web.entity.CenterInfo;
import com.digitalojt.web.repository.CenterInfoRepository;

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

	/** センター情報テーブル リポジトリー */
	private final CenterInfoRepository repository;
	
	/** ロガー */
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CenterInfoService.class);

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
}

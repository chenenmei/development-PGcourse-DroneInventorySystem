package com.digitalojt.web.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.digitalojt.web.entity.CenterInfo;

/**
 * センター情報テーブルリポジトリー
 *
 * @author dotlife
 * 
 */
public interface CenterInfoRepository extends JpaRepository<CenterInfo, Integer> {

	/**
	 * 引数に合致する在庫センター情報を取得
	 * 2025/05/16 機能改修 現在容量範囲が検索できるため、検索項目引数を追加する
	 * 2025/05/16 機能改修 現在容量範囲が検索できるため、検索検索条件を追加する
	 * 2025/05/23 不具合修正 住所ではなく都道府県名で検索するように修正
	 * 2025/05/23 不具合修正 LIKEの前後に%を配置し、部分一致検索を正確に行う
	 * 2025/05/26 不具合修正 パラメータが空の場合はWHERE条件に含めないように修正
	 * 
	 * @param centerName センター名（部分一致検索）
	 * @param region 都道府県名（部分一致検索）
	 * @param storageCapacityFrom 容量範囲（最小）
	 * @param storageCapacityTo 容量範囲（最大）
	 * @return paramで検索した結果
	 */
	@Query("SELECT s FROM CenterInfo s WHERE " +
			"(:centerName IS NULL OR :centerName = '' OR UPPER(s.centerName) LIKE CONCAT('%', UPPER(:centerName), '%')) AND " +
			"(:region IS NULL OR :region = '' OR s.address LIKE CONCAT('%', :region, '%')) AND " +
			"(s.currentStorageCapacity >= :storageCapacityFrom) AND " +
			"(s.currentStorageCapacity <= :storageCapacityTo) AND " +
			"(s.operationalStatus = 0)")
	List<CenterInfo> findActiveCenters(
			@Param("centerName") String centerName,
			@Param("region") String region,
			@Param("storageCapacityFrom") Integer storageCapacityFrom,
			@Param("storageCapacityTo") Integer storageCapacityTo);

}

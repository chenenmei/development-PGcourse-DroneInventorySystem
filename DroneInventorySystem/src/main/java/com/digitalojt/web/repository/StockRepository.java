package com.digitalojt.web.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.digitalojt.web.entity.Stock;

/**
 * 在庫情報テーブル(stock_info)用リポジトリ
 * 
 * 新規作成
 */
public interface StockRepository extends JpaRepository<Stock, Integer> {

    /**
     * 指定したセンターIDに紐づく在庫データを全件取得
     * @param centerId センターID
     * @return 在庫リスト
     */
    List<Stock> findByCenterId(Integer centerId);

    /**
     * 指定したセンターIDに紐づく在庫データの件数を取得
     * @param centerId センターID
     * @return 件数
     */
    long countByCenterId(Integer centerId);
}

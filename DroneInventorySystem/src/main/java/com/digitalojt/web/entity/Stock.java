package com.digitalojt.web.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

/**
 * stock_infoテーブルのエンティティクラス
 * 
 * 新規作成
 */
@Entity
@Table(name = "stock_info")
@Data
public class Stock {

    /** 在庫ID（主キー） */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_id")
    private Integer stockId;

    /** カテゴリーID */
    @Column(name = "category_id")
    private Integer categoryId;

    /** 在庫名 */
    @Column(name = "name")
    private String name;

    /** センターID（外部キー） */
    @Column(name = "center_id")
    private Integer centerId;

    /** 在庫説明 */
    @Column(name = "description")
    private String description;

    /** 在庫数量 */
    @Column(name = "amount")
    private Integer amount;

    /** 削除フラグ */
    @Column(name = "delete_flag")
    private Integer deleteFlag;

    /** 作成日時 */
    @Column(name = "create_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;

    /** 更新日時 */
    @Column(name = "update_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;
}

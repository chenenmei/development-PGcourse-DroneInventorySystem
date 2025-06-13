package com.digitalojt.web.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.Comment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * センター情報Entity
 * 
 * @author dotlife
 *
 */

/**
 * 在庫センター情報エンティティ
 * 2025/05/29 変更:
 *   - 楽観ロック用フィールド version を追加（@Version）
 */
@Data
@Entity
@Table(name = "center_info")
@Getter
@Setter
public class CenterInfo {

	/**
	 * センターID（主キー、自動採番）
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	@Column(name = "center_id")
	private int centerId;
	
	/**
	 * センター名
	 */
    @Column(name = "center_name", length = 20, nullable = false)
	private String centerName;
    
    /**
     * 郵便番号
     */
    @Column(name = "post_code", length = 10, nullable = false)
    private String postCode;
	
	/**
	 * 住所
	 */
    @Column(name = "address", length = 255, nullable = false)
	private String address;
	
	/**
	 * 電話番号
	 */
    @Column(name = "phone_number", length = 100, nullable = false)
	private String phoneNumber;
	
	/**
	 * 管理者名
	 */
    @Column(name = "manager_name", length = 100, nullable = false)
	private String managerName;
	
    /**
     * 稼働状況ステータス (0:稼働中, 1:稼働停止)
     */
    @Column(name = "operational_status", columnDefinition = "TINYINT(1)", nullable = false)
	private Integer operationalStatus;
	
	/**
	 * 最大容量
	 */
    @Column(name = "max_storage_capacity", nullable = false)
	private Integer maxStorageCapacity;
	
	/**
	 * 現在容量
	 */
    @Column(name = "current_storage_capacity", nullable = false)
	private Integer currentStorageCapacity;
    
    /**
     * 備考
     */
    @Column(name = "notes", length = 255)
    private String notes;
	
	/**
	 * 論理削除フラグ (0:未削除, 1:削除済)
	 */
    @Column(name = "delete_flag", nullable = false)
    private Integer deleteFlag;

    /**
     * 作成日付
     */
    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate;

    /**
     * 更新日付
     */
    @Column(name = "update_date", nullable = false)
    private LocalDateTime updateDate;
    
 // ================================
    // 2025/05/29 追加開始 - 排他制御
    // ================================
    /**
     * バージョン番号（楽観ロック用）
     * <p>
     * 更新時に JPA が自動でインクリメントし、<br>
     * WHERE 句に version = ? が付与されることで
     * 競合更新を検知します。
     */
    @Version
    @Column(name = "version", nullable = false)
    @Comment("排他制御用バージョン番号")
    private Long version;
}

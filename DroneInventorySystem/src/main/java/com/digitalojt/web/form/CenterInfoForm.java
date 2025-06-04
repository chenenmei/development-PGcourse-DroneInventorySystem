package com.digitalojt.web.form;

import com.digitalojt.web.consts.ModelAttributeContents;
import com.digitalojt.web.validation.CenterInfoFormValidator;
import com.digitalojt.web.validation.ValidationGroups.Insert;
import com.digitalojt.web.validation.ValidationGroups.Update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 在庫センター情報画面のフォームクラス
 * 
 * @author dotlife
 *
 */
@Data
@CenterInfoFormValidator
public class CenterInfoForm {

	/**センター名*/
    @Size(max = ModelAttributeContents.MAX_CENTER_NAME_LENGTH, message = "{centerName.length.wrongInput}")
	private String centerName;

	/**都道府県*/
	private String region;

	/**
	 * 容量(From)
	 */
	private Integer storageCapacityFrom;

	/**
	 * 容量(To)
	 */
	private Integer storageCapacityTo;

	/**
	 * 容量(From)のデフォルト値を設定
	 * @return
	 */
	
	/* ---------- 追加：更新用 ---------- */
	
	/** 
	 * センターID（更新用）
	 */
	@NotNull(message = "{centerId.required}", groups = Update.class)
    private Integer centerId;
    
    /** 
	 * バージョン番号（楽観ロック用）
	 */
	@NotNull(message = "{version.required}", groups = Update.class)
    private Long version;
	
	
	/* ---------- 追加：新規登録用 ---------- */
	
	
	/**
	 * 住所
	 */
	@NotBlank(message = "{address.required}", groups = {Insert.class, Update.class})
	@Size(max = 100, message = "{address.length}")
	private String address; 
	
	/**
	 * 郵便番号
	 */
	@Pattern(regexp = "^[0-9]{3}-?[0-9]{4}$",message = "{postCode.format}")
	private String postCode;
	
	/**
	 * 電話番号
	 */
	@Pattern(regexp = "^[0-9\\-]{10,13}$",message = "{phoneNumber.format}")
	private String phoneNumber;
	
	/**
	 * 最大容量
	 */
	@NotNull(message = "{maxStorageCapacity.required}", groups = {Insert.class, Update.class})
	@PositiveOrZero(message = "{maxStorageCapacity.positive}", groups = {Insert.class, Update.class})
	private Integer maxStorageCapacity;
	
	/**
	 * 現在容量
	 */
	@NotNull(message = "{currentStorageCapacity.required}", groups = {Insert.class, Update.class})
	@PositiveOrZero(message = "{currentStorageCapacity.positive}", groups = {Insert.class, Update.class})
	private Integer currentStorageCapacity;
	
	/**
	 * 管理者名
	 */
	@Size(max = 40, message = "{managerName.length}")
	private String managerName; 
	
	/**
	 * 備考
	 */
	@Size(max = 200, message = "{notes.length}")
	private String notes; 
	
}

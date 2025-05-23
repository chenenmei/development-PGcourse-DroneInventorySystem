package com.digitalojt.web.validation;

import com.digitalojt.web.consts.ErrorMessage;
import com.digitalojt.web.consts.InvalidCharacter;
import com.digitalojt.web.consts.Region;
import com.digitalojt.web.exception.ErrorMessageHelper;
import com.digitalojt.web.form.CenterInfoForm;
import com.digitalojt.web.util.InputValidator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 在庫センター情報のバリデーション処理実装
 * CenterInfoForm のフィールドに対してバリデーションを行うクラスです。
 */
public class CenterInfoFormValidatorImpl implements ConstraintValidator<CenterInfoFormValidator, CenterInfoForm> {

    /**
     * フォームデータのバリデーション処理を行う
     * @param form バリデーション対象のフォームデータ
     * @param context バリデーションコンテキスト
     * @return フォームが有効かどうか（有効ならtrue、無効ならfalse）
     */
    @Override
    public boolean isValid(CenterInfoForm form, ConstraintValidatorContext context) {
        try {
            // フィールドがすべて空である場合にエラー処理
            if (isAllFieldsEmpty(form)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(ErrorMessageHelper.getMessage(ErrorMessage.ALL_FIELDS_EMPTY_ERROR_MESSAGE))
                       .addConstraintViolation();
                return false;
            }
            
            // センター名のバリデーション - 入力されている場合のみ検証
            if (form.getCenterName() != null && !form.getCenterName().isEmpty()) {
                // 不正文字が含まれる場合
                if (containsInvalidCharacter(form.getCenterName())) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(ErrorMessageHelper.getMessage(ErrorMessage.INVALID_INPUT_ERROR_MESSAGE))
                           .addConstraintViolation();
                    return false;
                }
            }
    
            // 都道府県のバリデーション - 入力されている場合のみ検証
            if (form.getRegion() != null && !form.getRegion().isEmpty() && !isValidRegion(form.getRegion())) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(ErrorMessageHelper.getMessage(ErrorMessage.INVALID_INPUT_ERROR_MESSAGE))
                       .addConstraintViolation();
                return false;
            }
            
            // 両方の容量が入力されている場合のみFromToの比較検証を行う
            if (form.getStorageCapacityFrom() != null && form.getStorageCapacityTo() != null) {
                if (form.getStorageCapacityFrom() > form.getStorageCapacityTo()) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(ErrorMessageHelper.getMessage(ErrorMessage.FROM_TO_ERROR_MESSAGE))
                           .addConstraintViolation();
                    return false;
                }
            }
        } catch (Exception e) {
            // 予期せぬ例外が発生した場合は、エラーメッセージを設定してfalseを返す
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("バリデーション処理中にエラーが発生しました")
                   .addConstraintViolation();
            return false;
        }

        // バリデーションが成功した場合はtrueを返す
        return true;
    }
    
    /**
     * 文字列に不正文字が含まれているかチェックする
     * @param input チェックする文字列
     * @return 不正文字が含まれていればtrue、それ以外はfalse
     */
    private boolean containsInvalidCharacter(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        
        // 文字列の各文字を1つずつチェック
        for (char c : input.toCharArray()) {
            // 不正文字が含まれているか確認
            if (isInvalidCharacter(c)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 文字が不正文字かをチェックするメソッド
     * 
     * @param character チェックする文字
     * @return 不正文字なら true, それ以外は false
     */
    private static boolean isInvalidCharacter(char character) {
        for (InvalidCharacter invalidChar : InvalidCharacter.values()) {
            if (invalidChar.getCharacter() == character) {
            	// 不正文字が見つかった
                return true;
            }
        }
        // 不正文字ではない
        return false;
    }

    /**
     * 都道府県が有効かどうかを確認
     * @param region 都道府県
     * @return 都道府県が有効かどうか
     */
    private boolean isValidRegion(String region) {
    	if (!InputValidator.isValid(region)) {
    		return false;
    	}
    	
        // Enum の名前と比較して一致するかチェック
    	boolean regionFlg = false;
        for (Region regions : Region.values()) {
        	if (regions.getName().contains(region)) {
        		regionFlg = true;
        		break;
            }
        }
        return regionFlg;
    }

    /**
     * フォームの全てのフィールドが空かどうかを確認
     * @param form フォームデータ
     * @return すべてのフィールドがnullまたは空の場合はtrue、それ以外はfalse
     */
    private boolean isAllFieldsEmpty(CenterInfoForm form) {
        // センター名、都道府県、容量Fromと容量Toが全てnullまたは空の場合にtrueを返す
        boolean centerNameEmpty = form.getCenterName() == null || form.getCenterName().isEmpty();
        boolean regionEmpty = form.getRegion() == null || form.getRegion().isEmpty();
        boolean capacityFromEmpty = form.getStorageCapacityFrom() == null;
        boolean capacityToEmpty = form.getStorageCapacityTo() == null;
        
        return centerNameEmpty && regionEmpty && capacityFromEmpty && capacityToEmpty;
    }
}

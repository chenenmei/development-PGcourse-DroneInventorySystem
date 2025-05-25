-- 在庫センター情報テーブル（center_info）のカラム型変更

-- 一時的な数値型カラムを追加
ALTER TABLE center_info ADD COLUMN max_storage_capacity_numeric INT;
ALTER TABLE center_info ADD COLUMN current_storage_capacity_numeric INT;

-- 既存データを数値型に変換（エラーを抑制するため、変換できない場合は0を設定）
UPDATE center_info SET 
    max_storage_capacity_numeric = CAST(NULLIF(TRIM(max_storage_capacity), '') AS SIGNED) + 0,
    current_storage_capacity_numeric = CAST(NULLIF(TRIM(current_storage_capacity), '') AS SIGNED) + 0;

-- 変換できなかった値（NULL）を0で置き換え
UPDATE center_info SET 
    max_storage_capacity_numeric = 0 WHERE max_storage_capacity_numeric IS NULL;
UPDATE center_info SET 
    current_storage_capacity_numeric = 0 WHERE current_storage_capacity_numeric IS NULL;

-- 元のカラムを削除
ALTER TABLE center_info DROP COLUMN max_storage_capacity;
ALTER TABLE center_info DROP COLUMN current_storage_capacity;

-- 新しいカラムをリネーム
ALTER TABLE center_info CHANGE COLUMN max_storage_capacity_numeric max_storage_capacity INT NOT NULL;
ALTER TABLE center_info CHANGE COLUMN current_storage_capacity_numeric current_storage_capacity INT NOT NULL;

-- Migration script to add review_start field to Competition table
-- This script is for existing databases that need to be updated

-- Add review_start column only if it doesn't exist
SET @column_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'Competition' 
    AND COLUMN_NAME = 'review_start'
);

SET @query = IF(
    @column_exists = 0,
    'ALTER TABLE Competition ADD COLUMN review_start datetime COMMENT ''评审开始时间（作品提交截止后开始评审）'' AFTER submit_start',
    'SELECT "Column review_start already exists" AS message'
);

PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- For existing competitions, set review_start to be equal to award_publish_start
-- This maintains backward compatibility
UPDATE Competition 
SET review_start = award_publish_start 
WHERE review_start IS NULL AND award_publish_start IS NOT NULL;

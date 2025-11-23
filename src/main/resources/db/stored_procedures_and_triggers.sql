-- Stored Procedures and Triggers for Competition Management System
-- This file contains example stored procedures and triggers
-- Execute these in your MySQL database after creating the schema

-- ============================================================================
-- STORED PROCEDURES
-- ============================================================================

-- 1. Calculate Competition Awards
DELIMITER $$
CREATE PROCEDURE IF NOT EXISTS calculate_competition_awards(IN p_competition_id INT)
BEGIN
    -- Award calculation logic based on scores
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_registration_id INT;
    DECLARE v_total_score DECIMAL(10,2);
    DECLARE v_rank INT DEFAULT 0;
    
    DECLARE cur CURSOR FOR
        SELECT r.registration_id, AVG(ja.score * ja.weight) as total_score
        FROM Registration r
        INNER JOIN Submission s ON r.registration_id = s.registration_id
        INNER JOIN JudgeAssignment ja ON s.submission_id = ja.submission_id
        WHERE r.competition_id = p_competition_id AND r.is_deleted = FALSE
        GROUP BY r.registration_id ORDER BY total_score DESC;
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO v_registration_id, v_total_score;
        IF done THEN LEAVE read_loop; END IF;
        SET v_rank = v_rank + 1;
        -- Insert award results here
    END LOOP;
    CLOSE cur;
END$$
DELIMITER ;

-- 2. Update Competition Statuses
DELIMITER $$
CREATE PROCEDURE IF NOT EXISTS update_competition_statuses()
BEGIN
    UPDATE Competition SET competition_status = 'active' 
    WHERE signup_end < NOW() AND competition_status = 'signup';
END$$
DELIMITER ;

-- ============================================================================
-- TRIGGERS
-- ============================================================================

-- Auto-update timestamps
DELIMITER $$
CREATE TRIGGER IF NOT EXISTS before_user_update
BEFORE UPDATE ON User
FOR EACH ROW BEGIN SET NEW.updated_at = NOW(); END$$
DELIMITER ;

-- Add nickname column to User table
-- Run this migration after deploying the application

ALTER TABLE User ADD COLUMN nickname VARCHAR(100) AFTER real_name;

-- 演练记录表新增字段
ALTER TABLE biz_safety_drill
    ADD COLUMN drill_location  VARCHAR(255) COMMENT '演练地点' AFTER drill_date,
    ADD COLUMN drill_commander VARCHAR(64)  COMMENT '演练指挥' AFTER drill_location,
    ADD COLUMN drill_objective TEXT         COMMENT '演练目的' AFTER drill_commander,
    ADD COLUMN participant_count INT        DEFAULT 0 COMMENT '参与人数' AFTER participants;

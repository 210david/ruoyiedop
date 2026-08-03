-- =============================================
-- DMS 备件主数据与库存拆分迁移脚本
-- 1. 新建 dms_spare_part_stock 库存表（按仓库维度）
-- 2. dms_spare_part_record 增加 stock_id 列
-- 3. 迁移已有库存数据到 stock 表
-- 4. dms_spare_part 移除库存字段（保留表结构兼容）
-- =============================================

-- 1. 新建备件库存表
DROP TABLE IF EXISTS dms_spare_part_stock;
CREATE TABLE dms_spare_part_stock (
    stock_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '库存ID',
    part_id           BIGINT       NOT NULL                 COMMENT '备件ID（关联dms_spare_part.part_id）',
    warehouse_name    VARCHAR(100) NOT NULL DEFAULT '默认备件库' COMMENT '仓库名称',
    storage_location  VARCHAR(255)                          COMMENT '存放位置',
    current_stock     DECIMAL(18,2) DEFAULT 0               COMMENT '当前库存',
    lock_qty          DECIMAL(18,2) DEFAULT 0               COMMENT '锁定库存',
    stock_min         DECIMAL(18,2) DEFAULT 0               COMMENT '库存下限',
    stock_max         DECIMAL(18,2) DEFAULT 0               COMMENT '库存上限',
    safety_stock      DECIMAL(18,2) DEFAULT 0               COMMENT '安全库存',
    version           INT          DEFAULT 0                COMMENT '乐观锁版本号',
    status            CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag          CHAR(1)      DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by         VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time       DATETIME                              COMMENT '创建时间',
    update_by         VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time       DATETIME                              COMMENT '更新时间',
    remark            VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (stock_id),
    UNIQUE KEY uk_part_warehouse (part_id, warehouse_name),
    KEY idx_stock_part (part_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='备件库存表（按仓库维度）';

-- 2. dms_spare_part_record 增加 stock_id 列
ALTER TABLE dms_spare_part_record ADD COLUMN stock_id BIGINT DEFAULT NULL COMMENT '库存ID（关联dms_spare_part_stock.stock_id）' AFTER part_id;

-- 3. 将已有备件库存数据迁移到 stock 表（每条备件生成一条默认仓库的库存记录）
INSERT INTO dms_spare_part_stock (part_id, warehouse_name, storage_location, current_stock, stock_min, stock_max, safety_stock, status, del_flag, create_by, create_time, remark)
SELECT
    part_id,
    COALESCE(NULLIF(storage_location, ''), '默认备件库') AS warehouse_name,
    storage_location,
    IFNULL(current_stock, 0),
    IFNULL(stock_min, 0),
    IFNULL(stock_max, 0),
    IFNULL(safety_stock, 0),
    status,
    del_flag,
    create_by,
    create_time,
    remark
FROM dms_spare_part
WHERE del_flag = '0';

-- 4. 更新 record 表的 stock_id（关联已迁移的库存记录）
UPDATE dms_spare_part_record r
INNER JOIN dms_spare_part_stock s ON r.part_id = s.part_id
SET r.stock_id = s.stock_id
WHERE r.stock_id IS NULL;

-- 5. 清空备件主数据表中的库存相关字段（置空，保留列避免MyBatis报错）
UPDATE dms_spare_part SET current_stock = 0, stock_min = 0, stock_max = 0, safety_stock = 0 WHERE del_flag = '0';

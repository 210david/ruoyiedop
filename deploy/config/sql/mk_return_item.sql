-- =============================================
-- 订单退货明细表
-- =============================================
DROP TABLE IF EXISTS `mk_order_return_item`;
CREATE TABLE `mk_order_return_item` (
  `item_id`      bigint        NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
  `return_id`    bigint        NOT NULL                 COMMENT '退货ID',
  `order_item_id` bigint                                COMMENT '订单明细ID',
  `product_name` varchar(200)                           COMMENT '商品名称（冗余）',
  `product_spec` varchar(255)                           COMMENT '规格型号（冗余）',
  `unit`         varchar(20)                            COMMENT '单位（冗余）',
  `order_qty`    decimal(14,2)                          COMMENT '订单数量',
  `ship_qty`     decimal(14,2)                          COMMENT '发货数量',
  `return_qty`   decimal(14,2)                          COMMENT '退货数量',
  `unit_price`   decimal(14,2)                          COMMENT '单价',
  `return_amount` decimal(14,2)                         COMMENT '退货金额',
  `return_reason` varchar(500)                          COMMENT '退货原因',
  `del_flag`     char(1)       DEFAULT '0'              COMMENT '删除标志',
  `create_by`    varchar(64)   DEFAULT ''               COMMENT '创建者',
  `create_time`  datetime                               COMMENT '创建时间',
  `update_by`    varchar(64)   DEFAULT ''               COMMENT '更新者',
  `update_time`  datetime                               COMMENT '更新时间',
  `remark`       varchar(500)                           COMMENT '备注',
  PRIMARY KEY (`item_id`),
  KEY `idx_return_item_return` (`return_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单退货明细表';

-- 更新工艺路线状态字典：将状态1的名称从'已启用'改为'待审核'
UPDATE sys_dict_data SET dict_label = '待审核', remark = '待审核' WHERE dict_type = 'mms_route_status' AND dict_value = '1';
-- 更新字典类型备注
UPDATE sys_dict_type SET remark = '工艺路线状态：草稿/待审核/已审核/已停用/已驳回' WHERE dict_type = 'mms_route_status';

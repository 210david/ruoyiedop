-- 事故类别字典（GB 6441-1986 企业职工伤亡事故分类）

-- 字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '事故类别', 'safety_accident_type', '0', 'admin', NOW(), '事故类别（GB 6441）'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'safety_accident_type');

-- 字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '物体打击', '1', 'safety_accident_type', '', 'danger', 'N', '0', 'admin', NOW(), '失控物体的重力或惯性力造成的人身伤害'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'safety_accident_type' AND dict_value = '1');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '车辆伤害', '2', 'safety_accident_type', '', 'warning', 'N', '0', 'admin', NOW(), '企业机动车辆引起的人体坠落和物体倒塌等'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'safety_accident_type' AND dict_value = '2');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '机械伤害', '3', 'safety_accident_type', '', 'warning', 'N', '0', 'admin', NOW(), '机械设备运动部件直接与人体接触引起的伤害'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'safety_accident_type' AND dict_value = '3');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '起重伤害', '4', 'safety_accident_type', '', 'warning', 'N', '0', 'admin', NOW(), '各种起重作业中发生的挤压坠落等伤亡事故'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'safety_accident_type' AND dict_value = '4');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '触电', '5', 'safety_accident_type', '', 'danger', 'N', '0', 'admin', NOW(), '电流流经人体造成生理伤害'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'safety_accident_type' AND dict_value = '5');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 6, '淹溺', '6', 'safety_accident_type', '', 'info', 'N', '0', 'admin', NOW(), '落入水中造成呼吸阻塞导致窒息死亡'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'safety_accident_type' AND dict_value = '6');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 7, '灼烫', '7', 'safety_accident_type', '', 'danger', 'N', '0', 'admin', NOW(), '火焰烧伤高温烫伤化学灼伤物理灼伤等'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'safety_accident_type' AND dict_value = '7');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 8, '火灾', '8', 'safety_accident_type', '', 'danger', 'N', '0', 'admin', NOW(), '造成人员伤亡的企业火灾事故'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'safety_accident_type' AND dict_value = '8');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 9, '高处坠落', '9', 'safety_accident_type', '', 'danger', 'N', '0', 'admin', NOW(), '由高处坠落至地面或低处造成的伤亡'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'safety_accident_type' AND dict_value = '9');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 10, '坍塌', '10', 'safety_accident_type', '', 'danger', 'N', '0', 'admin', NOW(), '物体失稳或结构破坏造成的事故'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'safety_accident_type' AND dict_value = '10');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 11, '冒顶片帮', '11', 'safety_accident_type', '', 'warning', 'N', '0', 'admin', NOW(), '矿井顶板垮落或侧壁片落'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'safety_accident_type' AND dict_value = '11');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 12, '透水', '12', 'safety_accident_type', '', 'warning', 'N', '0', 'admin', NOW(), '地下水或地表水突然涌入矿井'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'safety_accident_type' AND dict_value = '12');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 13, '放炮', '13', 'safety_accident_type', '', 'warning', 'N', '0', 'admin', NOW(), '爆破作业中发生的伤亡事故'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'safety_accident_type' AND dict_value = '13');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 14, '火药爆炸', '14', 'safety_accident_type', '', 'danger', 'N', '0', 'admin', NOW(), '火药与炸药在生产运输储藏中发生的爆炸'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'safety_accident_type' AND dict_value = '14');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 15, '瓦斯爆炸', '15', 'safety_accident_type', '', 'danger', 'N', '0', 'admin', NOW(), '瓦斯煤尘与空气混合物接触引爆源引起的爆炸'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'safety_accident_type' AND dict_value = '15');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 16, '锅炉爆炸', '16', 'safety_accident_type', '', 'danger', 'N', '0', 'admin', NOW(), '锅炉发生的物理性爆炸'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'safety_accident_type' AND dict_value = '16');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 17, '容器爆炸', '17', 'safety_accident_type', '', 'danger', 'N', '0', 'admin', NOW(), '压力容器超压或损坏引起的气体或液体爆炸'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'safety_accident_type' AND dict_value = '17');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 18, '其他爆炸', '18', 'safety_accident_type', '', 'warning', 'N', '0', 'admin', NOW(), '除火药瓦斯锅炉容器以外的爆炸事故'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'safety_accident_type' AND dict_value = '18');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 19, '中毒和窒息', '19', 'safety_accident_type', '', 'danger', 'N', '0', 'admin', NOW(), '人体接触有毒物质引起中毒或因缺氧引起窒息'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'safety_accident_type' AND dict_value = '19');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 20, '其他伤害', '20', 'safety_accident_type', '', 'info', 'N', '0', 'admin', NOW(), '上述范围之外的伤害事故'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'safety_accident_type' AND dict_value = '20');

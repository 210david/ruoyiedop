-- P3演练数据：健康证（临期30天/过期10天/正常）
INSERT INTO biz_hr_certificate (employee_id, employee_name, employee_no, dept_name, cert_type, cert_name, cert_no, issue_date, valid_from, valid_to, review_date, issue_org, remind_status, status, del_flag, create_by, create_time, remark)
SELECT e.employee_id, e.employee_name, e.employee_no, '生产车间', '1', '健康证明', 'JKZ-P3-001', DATE_SUB(CURDATE(), INTERVAL 335 DAY), DATE_SUB(CURDATE(), INTERVAL 335 DAY), DATE_ADD(CURDATE(), INTERVAL 30 DAY), NULL, '市疾病预防控制中心', '1', '0', '0', 'admin', NOW(), 'P3演练：临期30天'
FROM biz_hr_employee e WHERE e.employee_no = 'EMP0110'
UNION ALL
SELECT e.employee_id, e.employee_name, e.employee_no, '生产车间', '1', '健康证明', 'JKZ-P3-002', DATE_SUB(CURDATE(), INTERVAL 375 DAY), DATE_SUB(CURDATE(), INTERVAL 375 DAY), DATE_SUB(CURDATE(), INTERVAL 10 DAY), NULL, '市疾病预防控制中心', '2', '0', '0', 'admin', NOW(), 'P3演练：已过期10天'
FROM biz_hr_employee e WHERE e.employee_no = 'EMP0111'
UNION ALL
SELECT e.employee_id, e.employee_name, e.employee_no, '办公室', '1', '健康证明', 'JKZ-P3-003', DATE_SUB(CURDATE(), INTERVAL 165 DAY), DATE_SUB(CURDATE(), INTERVAL 165 DAY), DATE_ADD(CURDATE(), INTERVAL 200 DAY), NULL, '市疾病预防控制中心', '0', '0', '0', 'admin', NOW(), 'P3演练：正常'
FROM biz_hr_employee e WHERE e.employee_no = 'EMP0002';

-- 损耗异常演练工单（损耗率15%>阈值10%）
UPDATE mms_work_order SET plan_qty = 1000, qualified_qty = 850, defect_qty = 20 WHERE work_order_id = (SELECT t.id FROM (SELECT work_order_id AS id FROM mms_work_order ORDER BY work_order_id LIMIT 1) t);

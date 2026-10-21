-- ============================================
-- RuoYi 数据库性能索引优化
-- 针对 2核4G 服务器，增加常用查询字段索引
-- ============================================

-- 1、sys_user 表索引
CREATE INDEX idx_sys_user_status ON sys_user(status);
CREATE INDEX idx_sys_user_dept_id ON sys_user(dept_id);
CREATE INDEX idx_sys_user_phonenumber ON sys_user(phonenumber);

-- 2、sys_logininfor 表索引
CREATE INDEX idx_sys_logininfor_uname ON sys_logininfor(user_name);

-- 3、sys_oper_log 表索引
CREATE INDEX idx_sys_oper_log_oname ON sys_oper_log(oper_name);

-- 4、sys_job_log 表索引
CREATE INDEX idx_sys_job_log_ctime ON sys_job_log(create_time);

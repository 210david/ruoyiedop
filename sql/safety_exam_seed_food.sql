-- ============================================================
-- 食品行业安全考试题库种子数据（幂等，可重复执行）
-- 内容：1 个一级分类 + 5 个二级分类 + 29 道题（单选11/多选4/判断14）
-- 判重规则：分类按名称+父级，题目按题干 content
-- ============================================================

-- 1. 一级分类：食品安全生产
INSERT INTO biz_safety_exam_category (parent_id, category_name, order_num, status, del_flag, create_by, create_time)
SELECT 0, '食品安全生产', 1, '0', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_category WHERE category_name = '食品安全生产' AND parent_id = 0 AND del_flag = '0');

SET @food_root := (SELECT category_id FROM biz_safety_exam_category WHERE category_name = '食品安全生产' AND parent_id = 0 AND del_flag = '0' LIMIT 1);

-- 2. 二级分类（幂等）
INSERT INTO biz_safety_exam_category (parent_id, category_name, order_num, status, del_flag, create_by, create_time)
SELECT @food_root, '食品安全法规', 1, '0', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_category WHERE category_name = '食品安全法规' AND parent_id = @food_root AND del_flag = '0');

INSERT INTO biz_safety_exam_category (parent_id, category_name, order_num, status, del_flag, create_by, create_time)
SELECT @food_root, '卫生与消毒规范', 2, '0', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_category WHERE category_name = '卫生与消毒规范' AND parent_id = @food_root AND del_flag = '0');

INSERT INTO biz_safety_exam_category (parent_id, category_name, order_num, status, del_flag, create_by, create_time)
SELECT @food_root, '仓储与物料管理', 3, '0', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_category WHERE category_name = '仓储与物料管理' AND parent_id = @food_root AND del_flag = '0');

INSERT INTO biz_safety_exam_category (parent_id, category_name, order_num, status, del_flag, create_by, create_time)
SELECT @food_root, '添加剂与质量管理', 4, '0', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_category WHERE category_name = '添加剂与质量管理' AND parent_id = @food_root AND del_flag = '0');

INSERT INTO biz_safety_exam_category (parent_id, category_name, order_num, status, del_flag, create_by, create_time)
SELECT @food_root, '消防与作业安全', 5, '0', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_category WHERE category_name = '消防与作业安全' AND parent_id = @food_root AND del_flag = '0');

SET @cat_law := (SELECT category_id FROM biz_safety_exam_category WHERE category_name = '食品安全法规' AND parent_id = @food_root AND del_flag = '0' LIMIT 1);
SET @cat_hyg := (SELECT category_id FROM biz_safety_exam_category WHERE category_name = '卫生与消毒规范' AND parent_id = @food_root AND del_flag = '0' LIMIT 1);
SET @cat_wh  := (SELECT category_id FROM biz_safety_exam_category WHERE category_name = '仓储与物料管理' AND parent_id = @food_root AND del_flag = '0' LIMIT 1);
SET @cat_qa  := (SELECT category_id FROM biz_safety_exam_category WHERE category_name = '添加剂与质量管理' AND parent_id = @food_root AND del_flag = '0' LIMIT 1);
SET @cat_fire:= (SELECT category_id FROM biz_safety_exam_category WHERE category_name = '消防与作业安全' AND parent_id = @food_root AND del_flag = '0' LIMIT 1);

-- 3. 题目（幂等：按题干判重）
-- 语法：INSERT INTO ... SELECT ... FROM DUAL WHERE NOT EXISTS (...)

-- ============ 食品安全法规 ============
-- 单选1（简单）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '从事食品生产活动，应当依法取得（　）。',
'["营业执照","食品生产许可证","卫生登记证","安全生产许可证"]', 'B',
'《食品安全法》规定，从事食品生产活动应当依法取得食品生产许可证。',
@cat_law, '食品安全法规', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '从事食品生产活动，应当依法取得（　）。' AND del_flag = '0');

-- 单选2（中等）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '食品生产许可证的有效期为（　）年。',
'["3","5","8","10"]', 'B',
'《食品生产许可管理办法》规定，食品生产许可证有效期为5年。',
@cat_law, '食品安全法规', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '食品生产许可证的有效期为（　）年。' AND del_flag = '0');

-- 单选3（困难）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '企业发现生产的食品不符合食品安全标准时，正确的做法是（　）。',
'["继续生产，降低损失","立即停止生产、召回问题食品并按规定报告","降价促销尽快处理","更换包装后继续销售"]', 'B',
'应立即停止生产、主动召回，并按规定向监管部门报告，防止危害扩大。',
@cat_law, '食品安全法规', '3', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '企业发现生产的食品不符合食品安全标准时，正确的做法是（　）。' AND del_flag = '0');

-- 单选4（中等）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '发生食品安全事故后，涉事企业应当（　）。',
'["立即采取措施防止事故扩大，并按规定及时报告","隐瞒不报，内部消化处理","等事故平息后再向上级汇报","只通知相关客户即可"]', 'A',
'发生食品安全事故的单位应当立即处置、防止事故扩大，并按规定及时向监管部门报告，不得隐瞒。',
@cat_law, '食品安全法规', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '发生食品安全事故后，涉事企业应当（　）。' AND del_flag = '0');

-- 多选1（中等）ABCD
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '2', '食品生产企业应当建立的食品安全管理制度包括（　）。',
'["食品安全自查制度","从业人员健康管理制度","进货查验记录制度","不安全食品召回制度"]', 'ABCD',
'以上四项均为《食品安全法》要求食品生产经营者建立的食品安全管理制度。',
@cat_law, '食品安全法规', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '食品生产企业应当建立的食品安全管理制度包括（　）。' AND del_flag = '0');

-- 判断1（简单）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '从业人员应当取得健康证明后方可从事接触直接入口食品的工作。',
'["正确","错误"]', '对',
'《食品安全法》规定，从事接触直接入口食品工作的从业人员应当每年进行健康检查，取得健康证明后方可上岗。',
@cat_law, '食品安全法规', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '从业人员应当取得健康证明后方可从事接触直接入口食品的工作。' AND del_flag = '0');

-- ============ 卫生与消毒规范 ============
-- 单选5（简单）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '接触直接入口食品的人员正确的洗手消毒做法是（　）。',
'["用清水冲一下即可","按规程洗手后使用消毒液消毒并烘干双手","佩戴手套后可以不洗手","洗完手用衣袖擦干"]', 'B',
'应按规程洗手并使用消毒液消毒，防止手部污染直接入口食品。',
@cat_hyg, '卫生与消毒规范', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '接触直接入口食品的人员正确的洗手消毒做法是（　）。' AND del_flag = '0');

-- 单选6（中等）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '进入食品生产车间前的正确做法是（　）。',
'["直接进入车间开始作业","更换工作服、佩戴口罩和工作帽，洗手消毒后经消毒通道进入","穿便服快速进入","只需简单洗手即可进入"]', 'B',
'进入车间应严格执行更衣、戴口罩帽子、洗手消毒等卫生程序，防止人员带入污染。',
@cat_hyg, '卫生与消毒规范', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '进入食品生产车间前的正确做法是（　）。' AND del_flag = '0');

-- 单选7（中等）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '生产车间使用清洗消毒剂的要求是（　）。',
'["任意采购价格便宜的即可","使用符合要求的食品级清洗消毒产品，按规定浓度配制并专柜存放、专人管理","与食品原料放在一起方便取用","浓度越高消毒效果越好，无需按比例配制"]', 'B',
'清洗消毒剂应符合相关标准，按规定浓度配制，专柜存放、专人管理，防止误用和污染食品。',
@cat_hyg, '卫生与消毒规范', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '生产车间使用清洗消毒剂的要求是（　）。' AND del_flag = '0');

-- 多选2（中等）ABCD
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '2', '进入车间人员的个人卫生要求包括（　）。',
'["持有效健康证上岗","穿戴清洁的工作服、帽，头发不外露","不得佩戴首饰手表，不留长指甲、不涂指甲油","不得在车间内吸烟和饮食"]', 'ABCD',
'以上均为食品生产人员基本的个人卫生要求，目的是防止人员对食品造成污染。',
@cat_hyg, '卫生与消毒规范', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '进入车间人员的个人卫生要求包括（　）。' AND del_flag = '0');

-- 判断2（简单）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '患有痢疾、伤寒、甲型病毒性肝炎等消化道传染病的人员，不得从事接触直接入口食品的工作。',
'["正确","错误"]', '对',
'《食品安全法》明确规定，患有国务院卫生行政部门规定的有碍食品安全疾病的人员，不得从事接触直接入口食品的工作。',
@cat_hyg, '卫生与消毒规范', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '患有痢疾、伤寒、甲型病毒性肝炎等消化道传染病的人员，不得从事接触直接入口食品的工作。' AND del_flag = '0');

-- 判断3（简单）错
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '为了节省时间，工作人员可以穿着工作服直接进出卫生间。',
'["正确","错误"]', '错',
'进出卫生间必须脱去工作服，回来后重新洗手消毒，防止交叉污染。',
@cat_hyg, '卫生与消毒规范', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '为了节省时间，工作人员可以穿着工作服直接进出卫生间。' AND del_flag = '0');

-- 判断4（简单）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '直接接触食品的设备、工具和容器应按规程定期清洗消毒，并如实记录。',
'["正确","错误"]', '对',
'食品接触面应定期清洗消毒并记录，保证卫生状态可追溯。',
@cat_hyg, '卫生与消毒规范', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '直接接触食品的设备、工具和容器应按规程定期清洗消毒，并如实记录。' AND del_flag = '0');

-- ============ 仓储与物料管理 ============
-- 单选8（中等）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '仓库内物料码放一般要求离地面不低于（　）厘米、离墙面不低于（　）厘米。',
'["5、10","10、30","30、50","50、50"]', 'B',
'通行仓储卫生要求：物料离地不低于10cm、离墙不低于30cm，便于清洁、通风和检查。',
@cat_wh, '仓储与物料管理', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '仓库内物料码放一般要求离地面不低于（　）厘米、离墙面不低于（　）厘米。' AND del_flag = '0');

-- 多选3（中等）ABC
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '2', '食品仓库日常管理的基本要求包括（　）。',
'["分区分类存放，离墙离地","定期检查记录温湿度","先进先出，账物相符","与有毒有害化学品混放，提高装卸效率"]', 'ABC',
'仓库应分区分类、离墙离地存放，定期记录温湿度，执行先进先出；严禁与有毒有害物品混放。',
@cat_wh, '仓储与物料管理', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '食品仓库日常管理的基本要求包括（　）。' AND del_flag = '0');

-- 判断5（简单）错
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '原料、辅料、包装材料可以与农药、清洁剂等化学品存放在同一仓库。',
'["正确","错误"]', '错',
'化学品必须单独存放并明显标识，与食品原料、辅料、包装材料严格隔离，防止化学污染。',
@cat_wh, '仓储与物料管理', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '原料、辅料、包装材料可以与农药、清洁剂等化学品存放在同一仓库。' AND del_flag = '0');

-- 判断6（简单）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '物料发放应遵循先进先出的原则。',
'["正确","错误"]', '对',
'先进先出可防止原料过期变质，保证物料在保质期内使用。',
@cat_wh, '仓储与物料管理', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '物料发放应遵循先进先出的原则。' AND del_flag = '0');

-- 判断7（中等）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '仓库发现霉变或超过保质期的原料，应立即隔离存放并按规定处理，不得投入生产。',
'["正确","错误"]', '对',
'霉变、过期原料存在食品安全隐患，必须隔离标识并按规定处理，严禁流入生产环节。',
@cat_wh, '仓储与物料管理', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '仓库发现霉变或超过保质期的原料，应立即隔离存放并按规定处理，不得投入生产。' AND del_flag = '0');

-- ============ 添加剂与质量管理 ============
-- 单选9（中等）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '食品添加剂的使用应当符合（　）的要求。',
'["GB 2760《食品安全国家标准 食品添加剂使用标准》","GB 14881《食品生产通用卫生规范》","GB 7718《预包装食品标签通则》","企业内部自定标准"]', 'A',
'GB 2760规定了食品添加剂的允许品种、使用范围和最大使用量，是添加剂使用的强制标准。',
@cat_qa, '添加剂与质量管理', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '食品添加剂的使用应当符合（　）的要求。' AND del_flag = '0');

-- 判断8（简单）错
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '为了改善产品口感，食品添加剂可以超范围、超限量使用。',
'["正确","错误"]', '错',
'食品添加剂必须严格按照GB 2760规定的品种、范围和限量使用，超范围超限量使用属于违法行为。',
@cat_qa, '添加剂与质量管理', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '为了改善产品口感，食品添加剂可以超范围、超限量使用。' AND del_flag = '0');

-- 判断9（简单）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '企业应按规定对产品进行出厂检验，检验合格后方可出厂销售。',
'["正确","错误"]', '对',
'《食品安全法》要求食品生产企业对其产品进行检验，合格后方可出厂销售。',
@cat_qa, '添加剂与质量管理', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '企业应按规定对产品进行出厂检验，检验合格后方可出厂销售。' AND del_flag = '0');

-- 判断10（困难）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '食品生产过程的相关记录和凭证，保存期限不得少于产品保质期满后六个月；没有明确保质期的，保存期限不得少于二年。',
'["正确","错误"]', '对',
'《食品安全法》规定，记录和凭证保存期限不得少于产品保质期满后六个月；没有明确保质期的，保存期限不得少于二年。',
@cat_qa, '添加剂与质量管理', '3', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '食品生产过程的相关记录和凭证，保存期限不得少于产品保质期满后六个月；没有明确保质期的，保存期限不得少于二年。' AND del_flag = '0');

-- ============ 消防与作业安全 ============
-- 单选10（简单）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '使用干粉灭火器灭火时，应站在（　），将喷嘴对准火焰（　）喷射。',
'["上风方向、根部","下风方向、上部","上风方向、上部","任意方向、中部"]', 'A',
'应站在上风方向，对准火焰根部喷射，才能有效灭火并保障自身安全。',
@cat_fire, '消防与作业安全', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '使用干粉灭火器灭火时，应站在（　），将喷嘴对准火焰（　）喷射。' AND del_flag = '0');

-- 单选11（中等）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '车间内驾驶叉车的安全要求，正确的是（　）。',
'["速度不限，尽快完成搬运","限速行驶，禁止载人，转弯减速鸣笛","可以顺路搭载同事","倒车时无需观察后方"]', 'B',
'叉车应限速行驶、禁止载人，转弯和通道口减速鸣笛，防止碰撞伤人。',
@cat_fire, '消防与作业安全', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '车间内驾驶叉车的安全要求，正确的是（　）。' AND del_flag = '0');

-- 多选4（简单）ABCD
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '2', '灭火的基本方法包括（　）。',
'["冷却灭火法","窒息灭火法","隔离灭火法","化学抑制灭火法"]', 'ABCD',
'灭火的四种基本方法：冷却、窒息、隔离、化学抑制，实际扑救常组合使用。',
@cat_fire, '消防与作业安全', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '灭火的基本方法包括（　）。' AND del_flag = '0');

-- 判断11（简单）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '灭火时应将灭火剂喷射到火焰根部，而不是火焰上部。',
'["正确","错误"]', '对',
'对准火焰根部喷射才能隔绝燃烧源头，喷向上部无法有效灭火。',
@cat_fire, '消防与作业安全', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '灭火时应将灭火剂喷射到火焰根部，而不是火焰上部。' AND del_flag = '0');

-- 判断12（中等）错
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '电动叉车可以停放在存放易燃易爆物品的仓库内充电。',
'["正确","错误"]', '错',
'充电过程可能产生电火花，易燃易爆场所严禁充电作业，应在专用充电区域进行。',
@cat_fire, '消防与作业安全', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '电动叉车可以停放在存放易燃易爆物品的仓库内充电。' AND del_flag = '0');

-- 判断13（中等）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '电气设备发生火灾时，应先切断电源，再使用干粉或二氧化碳灭火器扑救，严禁用水直接扑救。',
'["正确","错误"]', '对',
'带电灭火有触电危险，必须先断电；水导电，严禁用水扑救电气火灾。',
@cat_fire, '消防与作业安全', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '电气设备发生火灾时，应先切断电源，再使用干粉或二氧化碳灭火器扑救，严禁用水直接扑救。' AND del_flag = '0');

-- 判断14（简单）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '员工发现食品安全隐患或安全事故隐患，应立即报告车间负责人或安全管理人员。',
'["正确","错误"]', '对',
'发现隐患及时报告是每名员工的职责，便于第一时间处置、防止事故扩大。',
@cat_fire, '消防与作业安全', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '员工发现食品安全隐患或安全事故隐患，应立即报告车间负责人或安全管理人员。' AND del_flag = '0');

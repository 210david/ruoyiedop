# -*- coding: utf-8 -*-
"""YLSP 赢隆食品各模块种子数据脚本 v1"""
import subprocess, datetime
MYSQL = r"C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe"
DB = 'ruoyi_ylsp'
NOW = "NOW()"
def s(v): return 'NULL' if v is None else ("'"+str(v).replace("'","''")+"'")
def n(v): return 'NULL' if v is None else str(v)

def build_insert(table, rows, idcol, autocols):
    """autocols: dict col->filler(idx,row)"""
    cols = q(f"SELECT column_name,is_nullable,column_default,data_type FROM information_schema.columns WHERE table_schema='{DB}' AND table_name='{table}' ORDER BY ordinal_position")
    colinfo = {c[0]:(c[1],c[2],c[3]) for c in cols}
    stmts=[]
    for i,row in enumerate(rows,1):
        vals={}
        for c,(nul,dft,typ) in colinfo.items():
            if c in row: vals[c]=row[c]
            elif c==idcol: vals[c]=i
            elif c in autocols: vals[c]=autocols[c](i,row)
            elif dft is not None: continue
            elif nul=='NO': vals[c]=autofill(c,typ,i)
        collist=','.join('`'+k+'`' for k in vals)
        vallist=','.join(('NOW()' if v=='NOW()' else s(v)) for v in vals.values())
        stmts.append(f"INSERT INTO {table} ({collist}) VALUES ({vallist});")
    return stmts

def autofill(col,typ,i):
    cl=col.lower()
    if cl in ('status','del_flag'): return '0'
    if 'flag' in cl or 'is_' in cl: return '0'
    if typ in ('datetime','timestamp'): return 'NOW()'
    if typ=='date': return '2026-10-27'
    if typ in ('decimal','double','float'): return 0
    if typ in ('bigint','int','tinyint','smallint'): return 0
    return '赢隆食品初始化数据'

def q(sql):
    r=subprocess.run([MYSQL,'-uroot','-pZBrj@2026','-N','--default-character-set=utf8mb4','-e',sql],capture_output=True,text=True,encoding='utf-8')
    if r.returncode!=0: raise RuntimeError(r.stderr[:300])
    return [l.split('\t') for l in r.stdout.strip().splitlines()] if r.stdout.strip() else []

common={'status':lambda i,r:'0','del_flag':lambda i,r:'0',
        'create_by':lambda i,r:'admin','create_time':lambda i,r:'NOW()'}

# ============ 营销 mk_customer ============
mk_customers=[
 dict(customer_no='KH2026100001',customer_name='上海联华生鲜配送中心',customer_level='A',province='上海市',city='上海市',address='上海市宝山区杨行镇',credit_limit=500000,credit_days=30,customer_status='1',remark='沪滇协作直营渠道重点客户'),
 dict(customer_no='KH2026100002',customer_name='北京京客隆商业集团',customer_level='A',province='北京市',city='北京市',address='北京市朝阳区',credit_limit=400000,credit_days=30,customer_status='1',remark='华北商超渠道'),
 dict(customer_no='KH2026100003',customer_name='南京苏果超市有限公司',customer_level='B',province='江苏省',city='南京市',address='南京市秦淮区',credit_limit=300000,credit_days=30,customer_status='1',remark='江苏商超渠道'),
 dict(customer_no='KH2026100004',customer_name='杭州世纪联华生鲜事业部',customer_level='B',province='浙江省',city='杭州市',address='杭州市拱墅区',credit_limit=250000,credit_days=15,customer_status='1',remark='浙江商超渠道'),
 dict(customer_no='KH2026100005',customer_name='苏州foodie精品餐饮连锁',customer_level='B',province='江苏省',city='苏州市',address='苏州市姑苏区',credit_limit=200000,credit_days=15,customer_status='1',remark='餐饮连锁客户'),
 dict(customer_no='KH2026100006',customer_name='富源县农特产品直营店（上海宝山）',customer_level='A',province='上海市',city='上海市',address='上海市宝山区杨行镇克山路',credit_limit=100000,credit_days=0,customer_status='1',remark='公司直营店，现款结算'),
 dict(customer_no='KH2026100007',customer_name='昆明锦华食品批发商行',customer_level='C',province='云南省',city='昆明市',address='昆明市官渡区',credit_limit=80000,credit_days=15,customer_status='1',remark='省内批发渠道'),
 dict(customer_no='KH2026100008',customer_name='曲靖优选食材供应链公司',customer_level='C',province='云南省',city='曲靖市',address='曲靖市麒麟区',credit_limit=60000,credit_days=15,customer_status='1',remark='本地餐饮供应链'),
]
stmts = build_insert('mk_customer',mk_customers,'customer_id',common)

# ============ 仓储 wms ============
wms_whs=[
 dict(warehouse_code='WH01',warehouse_name='冷冻原料库',node_type='1',area_type='冷冻',location_type='冷库',capacity=200,capacity_unit='吨',address='厂区1号冷库',dept_id=106,remark='大河乌猪原料-18℃冷冻存储'),
 dict(warehouse_code='WH02',warehouse_name='冷藏成品库',node_type='1',area_type='冷藏',location_type='冷库',capacity=150,address='厂区2号冷库',dept_id=106,remark='成品0-4℃冷藏'),
 dict(warehouse_code='WH03',warehouse_name='辅料包材库',node_type='1',area_type='常温',location_type='普通库房',capacity=80,address='厂区辅料仓库',dept_id=106,remark='肠衣/调味料/包装材料'),
 dict(warehouse_code='WH04',warehouse_name='发货暂存区',node_type='1',area_type='冷藏',location_type='冷库',capacity=30,address='厂区月台旁',dept_id=106,remark='冷链发货集货暂存'),
 dict(warehouse_code='WH05',warehouse_name='添加剂专库',node_type='1',area_type='常温',location_type='普通库房',capacity=5,address='厂区辅助仓库',dept_id=105,remark='食品添加剂专柜上锁管理'),
]
wms_suppliers=[
 dict(supplier_code='GYS20260001',supplier_name='富源县大河乌猪养殖专业合作社',supplier_short_name='大河乌猪合作社',supplier_type='原料供应商',supplier_level='A',contact_person='张应江',contact_phone='13887000001',address='曲靖市富源县后所镇',payment_method='订单预付',remark='公司+合作社模式核心供方，高于市场价10%订单收购'),
 dict(supplier_code='GYS20260002',supplier_name='富源县胜境生猪养殖场',supplier_short_name='胜境养殖场',supplier_type='原料供应商',supplier_level='A',contact_person='丰胜',contact_phone='13887000002',address='曲靖市富源县胜境街道',payment_method='月结30天'),
 dict(supplier_code='GYS20260003',supplier_name='富源永胤商贸有限公司',supplier_short_name='永胤商贸',supplier_type='辅料供应商',supplier_level='B',contact_person='罗丽波',contact_phone='13887000003',address='曲靖市富源县',payment_method='月结30天',remark='肠衣/调味辅料供应'),
 dict(supplier_code='GYS20260004',supplier_name='昆明华彩包装制品有限公司',supplier_short_name='华彩包装',supplier_type='包材供应商',supplier_level='B',contact_person='马晗',contact_phone='13887000004',address='昆明市经开区',payment_method='月结30天',remark='真空袋/礼盒包装'),
 dict(supplier_code='GYS20260005',supplier_name='云南顺捷冷链物流有限公司',supplier_short_name='顺捷冷链',supplier_type='物流服务商',supplier_level='B',contact_person='高鹏',contact_phone='13887000005',address='曲靖市沾益区',payment_method='月结15天',remark='冷链干线运输（上海/北京线）'),
]
wms_materials=[
 dict(material_code='WL20260001',material_name='大河乌猪鲜肉（冷鲜肉）',material_type='原料',spec_model='分割白条/精分割',unit='kg',is_batch_manage='1',is_expiry_manage='1',shelf_life_days=7,expiry_alert_days=2,safety_stock_min=500,safety_stock_max=3000),
 dict(material_code='WL20260002',material_name='鲜丰旺乌猪培根',material_type='成品',spec_model='1000g/袋',unit='袋',is_batch_manage='1',is_expiry_manage='1',shelf_life_days=90,expiry_alert_days=30,safety_stock_min=200,safety_stock_max=1000),
 dict(material_code='WL20260003',material_name='广味香肠',material_type='成品',spec_model='1000g/袋',unit='袋',is_batch_manage='1',is_expiry_manage='1',shelf_life_days=180,expiry_alert_days=60,safety_stock_min=300,safety_stock_max=2000),
 dict(material_code='WL20260004',material_name='大河乌猪川味香肠',material_type='成品',spec_model='500g/袋',unit='袋',is_batch_manage='1',is_expiry_manage='1',shelf_life_days=180,expiry_alert_days=60,safety_stock_min=300,safety_stock_max=2000),
 dict(material_code='WL20260005',material_name='大河乌猪五目肠',material_type='成品',spec_model='200g/袋',unit='袋',is_batch_manage='1',is_expiry_manage='1',shelf_life_days=120,expiry_alert_days=30,safety_stock_min=500,safety_stock_max=3000),
 dict(material_code='WL20260006',material_name='大河乌猪块状火腿',material_type='成品',spec_model='2000g/只',unit='只',is_batch_manage='1',is_expiry_manage='1',shelf_life_days=365,expiry_alert_days=90,safety_stock_min=100,safety_stock_max=500),
 dict(material_code='WL20260007',material_name='鸡枞火腿酱',material_type='成品',spec_model='220g/瓶',unit='瓶',is_batch_manage='1',is_expiry_manage='1',shelf_life_days=270,expiry_alert_days=60,safety_stock_min=500,safety_stock_max=3000,remark='调味品SC品类'),
 dict(material_code='WL20260008',material_name='酱香猪脸',material_type='成品',spec_model='500g/袋',unit='袋',is_batch_manage='1',is_expiry_manage='1',shelf_life_days=90,expiry_alert_days=30,safety_stock_min=200,safety_stock_max=1000),
 dict(material_code='WL20260009',material_name='天然猪肠衣',material_type='辅料',spec_model='盐渍/桶装',unit='桶',is_batch_manage='1',is_expiry_manage='1',shelf_life_days=365,expiry_alert_days=90),
 dict(material_code='WL20260010',material_name='复合真空包装袋',material_type='包材',spec_model='18*28cm',unit='个',is_batch_manage='0',is_expiry_manage='0',safety_stock_min=50000,safety_stock_max=200000),
]

# ============ 生产 mms ============
mms_workshops=[
 dict(workshop_code='SC01',workshop_name='屠宰分割车间',factory_id=1,leader_user_id=8,remark='大河乌猪专业屠宰分割'),
 dict(workshop_code='SC02',workshop_name='灌装车间',factory_id=1,leader_user_id=9,remark='香肠灌装/封装'),
 dict(workshop_code='SC03',workshop_name='炼油车间',factory_id=1,leader_user_id=9,remark='猪油炼制'),
 dict(workshop_code='SC04',workshop_name='腌制发酵车间',factory_id=1,leader_user_id=9,remark='腌腊/低温发酵'),
 dict(workshop_code='SC05',workshop_name='酱卤车间',factory_id=1,leader_user_id=9,remark='酱卤/火腿酱'),
 dict(workshop_code='SC06',workshop_name='包装车间',factory_id=1,leader_user_id=9,remark='真空包装/金属探测/装箱'),
]
mms_orders=[
 dict(work_order_no='WO20261027001',order_type='normal',source_type='order',source_order_no='XSDD202610001',product_id=2,product_code='WL20260002',product_name='鲜丰旺乌猪培根',spec_model='1000g/袋',unit='袋',plan_qty=500,plan_start='2026-10-26',plan_finish='2026-10-28',finished_qty=500,qualified_qty=498,defect_qty=2,priority='high',status='finished',dept_id=104),
 dict(work_order_no='WO20261027002',order_type='normal',source_type='order',source_order_no='XSDD202610002',product_id=3,product_code='WL20260003',product_name='广味香肠',spec_model='1000g/袋',unit='袋',plan_qty=1000,plan_start='2026-10-26',plan_finish='2026-10-30',finished_qty=800,qualified_qty=800,defect_qty=0,priority='high',status='processing',dept_id=104),
 dict(work_order_no='WO20261027003',order_type='normal',source_type='plan',product_id=5,product_code='WL20260005',product_name='大河乌猪五目肠',spec_model='200g/袋',unit='袋',plan_qty=2000,plan_start='2026-10-27',plan_finish='2026-11-02',finished_qty=0,qualified_qty=0,defect_qty=0,priority='normal',status='processing',dept_id=104),
 dict(work_order_no='WO20261027004',order_type='normal',source_type='plan',product_id=6,product_code='WL20260006',product_name='大河乌猪块状火腿',spec_model='2000g/只',unit='只',plan_qty=200,plan_start='2026-10-20',plan_finish='2026-11-20',finished_qty=0,qualified_qty=0,defect_qty=0,priority='normal',status='processing',dept_id=104,remark='低温发酵周期30天'),
 dict(work_order_no='WO20261027005',order_type='normal',source_type='order',source_order_no='XSDD202610003',product_id=7,product_code='WL20260007',product_name='鸡枞火腿酱',spec_model='220g/瓶',unit='瓶',plan_qty=3000,plan_start='2026-10-25',plan_finish='2026-10-27',finished_qty=3000,qualified_qty=2995,defect_qty=5,priority='high',status='finished',dept_id=104),
 dict(work_order_no='WO20261027006',order_type='trial',source_type='manual',product_id=4,product_code='WL20260004',product_name='大河乌猪川味香肠',spec_model='500g/袋',unit='袋',plan_qty=300,plan_start='2026-10-28',plan_finish='2026-10-29',finished_qty=0,qualified_qty=0,defect_qty=0,priority='low',status='pending',dept_id=104,remark='川味配方试产'),
 dict(work_order_no='WO20261027007',order_type='normal',source_type='plan',product_id=8,product_code='WL20260008',product_name='酱香猪脸',spec_model='500g/袋',unit='袋',plan_qty=600,plan_start='2026-10-27',plan_finish='2026-10-28',finished_qty=0,qualified_qty=0,defect_qty=0,priority='normal',status='released',dept_id=104),
 dict(work_order_no='WO20261027008',order_type='normal',source_type='order',source_order_no='XSDD202610004',product_id=2,product_code='WL20260002',product_name='鲜丰旺乌猪培根',spec_model='1000g/袋',unit='袋',plan_qty=800,plan_start='2026-10-29',plan_finish='2026-10-31',finished_qty=0,qualified_qty=0,defect_qty=0,priority='normal',status='pending',dept_id=104),
]

# ============ 质量 qms ============
qms_tasks=[
 dict(task_no='IQC20261027001',task_type='incoming',source_type='purchase',source_no='CGDD202610001',material_id=1,material_code='WL20260001',material_name='大河乌猪鲜肉（冷鲜肉）',supplier_id=1,supplier_name='富源县大河乌猪养殖专业合作社',batch_no='PC20261025A',inspect_qty=2000,inspect_method='抽样检验',task_status='inspected',inspect_result='pass',inspector_id=11,inspector_name='赵雷',inspect_time='2026-10-26 09:30:00',dept_id=105),
 dict(task_no='IQC20261027002',task_type='incoming',source_type='purchase',source_no='CGDD202610002',material_id=1,material_code='WL20260001',material_name='大河乌猪鲜肉（冷鲜肉）',supplier_id=2,supplier_name='富源县胜境生猪养殖场',batch_no='PC20261026A',inspect_qty=1500,inspect_method='抽样检验',task_status='inspected',inspect_result='pass',inspector_id=11,inspector_name='赵雷',inspect_time='2026-10-27 09:00:00',dept_id=105),
 dict(task_no='FQC20261027003',task_type='final',source_type='workorder',source_no='WO20261027001',material_id=2,material_code='WL20260002',material_name='鲜丰旺乌猪培根',batch_no='BG20261027A',inspect_qty=500,inspect_method='出厂检验',task_status='inspected',inspect_result='pass',inspector_id=11,inspector_name='赵雷',inspect_time='2026-10-27 14:00:00',dept_id=105),
 dict(task_no='FQC20261027004',task_type='final',source_type='workorder',source_no='WO20261027005',material_id=7,material_code='WL20260007',material_name='鸡枞火腿酱',batch_no='HJ20261026A',inspect_qty=3000,inspect_method='出厂检验',task_status='inspected',inspect_result='pass',inspector_id=11,inspector_name='赵雷',inspect_time='2026-10-27 15:00:00',dept_id=105),
 dict(task_no='FQC20261027005',task_type='final',source_type='workorder',source_no='WO20261027002',material_id=3,material_code='WL20260003',material_name='广味香肠',batch_no='XC20261027A',inspect_qty=800,inspect_method='出厂检验',task_status='inspecting',inspect_result=None,inspector_id=12,inspector_name='孙丽',inspect_time='2026-10-27 16:00:00',dept_id=105),
 dict(task_no='IPQC20261027006',task_type='process',source_type='workorder',source_no='WO20261027002',material_id=3,material_code='WL20260003',material_name='广味香肠（灌装工序）',batch_no='XC20261027A',inspect_qty=100,inspect_method='巡检',task_status='inspected',inspect_result='pass',inspector_id=11,inspector_name='赵雷',inspect_time='2026-10-27 10:30:00',dept_id=105),
 dict(task_no='IQC20261027007',task_type='incoming',source_type='purchase',source_no='CGDD202610003',material_id=9,material_code='WL20260009',material_name='天然猪肠衣',supplier_id=3,supplier_name='富源永胤商贸有限公司',batch_no='CY20261024A',inspect_qty=50,inspect_method='抽样检验',task_status='inspected',inspect_result='pass',inspector_id=11,inspector_name='赵雷',inspect_time='2026-10-26 14:00:00',dept_id=105),
 dict(task_no='FQC20261027008',task_type='final',source_type='workorder',source_no='WO20261027007',material_id=8,material_code='WL20260008',material_name='酱香猪脸',batch_no='JX20261027A',inspect_qty=600,inspect_method='出厂检验',task_status='pending',inspect_result=None,inspector_id=11,inspector_name='赵雷',dept_id=105),
]
qms_ncrs=[
 dict(ncr_no='NCR20261027001',source_type='workorder',source_no='WO20261027001',material_id=2,material_code='WL20260002',material_name='鲜丰旺乌猪培根',batch_no='BG20261027A',defect_qty=2,defect_level='minor',defect_desc='真空包装封口不良，漏气2袋',disposition='3',ncr_status='5',discoverer_id=11,discoverer_name='赵雷',discover_time='2026-10-27 14:20:00',resp_dept_id=104,resp_dept_name='生产部',verify_result='pass',close_time='2026-10-27 17:00:00',dispose_qty=2,dispose_remark='返工重新封口后复检合格',dept_id=105),
 dict(ncr_no='NCR20261027002',source_type='workorder',source_no='WO20261027005',material_id=7,material_code='WL20260007',material_name='鸡枞火腿酱',batch_no='HJ20261026A',defect_qty=5,defect_level='minor',defect_desc='瓶盖密封垫偏移',disposition='3',ncr_status='5',discoverer_id=11,discoverer_name='赵雷',discover_time='2026-10-27 15:30:00',resp_dept_id=104,resp_dept_name='生产部',verify_result='pass',close_time='2026-10-27 17:30:00',dispose_qty=5,dispose_remark='重新旋盖复检合格',dept_id=105),
 dict(ncr_no='NCR20261027003',source_type='incoming',source_no='CGDD202610002',material_id=1,material_code='WL20260001',material_name='大河乌猪鲜肉（冷鲜肉）',supplier_id=2,supplier_name='富源县胜境生猪养殖场',batch_no='PC20261026A',defect_qty=30,defect_level='major',defect_desc='酮体表面淤血面积超标',disposition='1',ncr_status='processing',discoverer_id=11,discoverer_name='赵雷',discover_time='2026-10-27 09:20:00',resp_dept_id=103,resp_dept_name='采购供应部',dispose_remark='与养殖场协商退货处理',dept_id=105),
 dict(ncr_no='NCR20261027004',source_type='customer',source_no='TS202610001',material_id=3,product_name=None,material_name='广味香肠',batch_no='XC20261020A',defect_qty=3,defect_level='minor',defect_desc='客户反馈口感偏咸（3袋）',disposition='5',ncr_status='processing',discoverer_id=12,discoverer_name='孙丽',discover_time='2026-10-25 10:00:00',resp_dept_id=104,resp_dept_name='生产部',dispose_remark='复核配方记录，调整腌制时间',dept_id=105),
 dict(ncr_no='NCR20261027005',source_type='process',source_no='WO20261027002',material_id=3,material_name='广味香肠（金属探测）',batch_no='XC20261027A',defect_qty=1,defect_level='critical',defect_desc='金属探测报警1次，检出碎金属屑',disposition='4',ncr_status='processing',discoverer_id=11,inspector=None,discoverer_name='赵雷',discover_time='2026-10-27 11:00:00',resp_dept_id=104,resp_dept_name='生产部',dispose_remark='产品隔离报废，排查设备来源',dept_id=105),
]
qms_targets=[
 dict(target_year=2026,target_month=10,dimension='出厂合格率',dimension_val='全品类',target_pass_rate=99.5,actual_pass_rate=99.8,target_ppm=5000,actual_ppm=2000,target_complaint_rate=0.5,actual_complaint_rate=0.2,target_capa_close_rate=90,actual_capa_close_rate=100,achievement=1),
 dict(target_year=2026,target_month=9,dimension='出厂合格率',dimension_val='全品类',target_pass_rate=99.5,actual_pass_rate=99.6,target_ppm=5000,actual_ppm=4000,target_complaint_rate=0.5,actual_complaint_rate=0.3,target_capa_close_rate=90,actual_capa_close_rate=85,achievement=1),
 dict(target_year=2026,target_month=8,dimension='出厂合格率',dimension_val='全品类',target_pass_rate=99.5,actual_pass_rate=99.2,target_ppm=5000,actual_ppm=8000,target_complaint_rate=0.5,actual_complaint_rate=0.6,target_capa_close_rate=90,actual_capa_close_rate=80,achievement=0),
 dict(target_year=2026,target_month=10,dimension='客户投诉率',dimension_val='鲜品',target_pass_rate=98,actual_pass_rate=99,target_complaint_rate=0.5,actual_complaint_rate=0.2,target_capa_close_rate=90,actual_capa_close_rate=100,achievement=1),
 dict(target_year=2026,target_month=10,dimension='批次追溯完整率',dimension_val='全品类',target_pass_rate=100,actual_pass_rate=100,target_ppm=0,actual_ppm=0,target_complaint_rate=0,target_capa_close_rate=100,actual_capa_close_rate=100,achievement=1),
 dict(target_year=2026,target_month=10,dimension='供应商来料合格率',dimension_val='原料',target_pass_rate=98,actual_pass_rate=97.5,target_ppm=20000,actual_ppm=25000,target_complaint_rate=0,target_capa_close_rate=90,actual_capa_close_rate=88,achievement=0),
]

# ============ 设备 dms ============
dms_cats=[
 dict(category_code='LB01',category_name='屠宰分割设备',category_level=1,order_num=1),
 dict(category_code='LB02',category_name='灌装/包装设备',category_level=1,order_num=2),
 dict(category_code='LB03',category_name='制冷设备',category_level=1,order_num=3),
 dict(category_code='LB04',category_name='加工设备',category_level=1,order_num=4),
 dict(category_code='LB05',category_name='检测/计量设备',category_level=1,order_num=5),
]
dms_eqs=[
 dict(equipment_code='SB20260001',equipment_name='自动屠宰分割线',category_id=1,model='TZ-500',manufacturer='河南华腾农牧装备',supplier='河南华腾农牧装备',purchase_date='2022-06-15',install_date='2022-07-01',original_value=1200000,dept_id=104,dept_name='生产部',install_location='屠宰分割车间',equipment_status='running',equipment_level='A',responsible_id=8,responsible_name='张伟'),
 dict(equipment_code='SB20260002',equipment_name='液压灌装机',category_id=2,model='GZ-30',manufacturer='诸城兴盛机械',purchase_date='2022-08-10',install_date='2022-08-20',original_value=85000,dept_id=104,dept_name='生产部',install_location='灌装车间',equipment_status='running',equipment_level='B',responsible_id=9,responsible_name='刘洋'),
 dict(equipment_code='SB20260003',equipment_name='真空包装机',category_id=2,model='DZ-800',manufacturer='青岛艾讯包装',purchase_date='2022-08-10',install_date='2022-08-22',original_value=68000,dept_id=104,dept_name='生产部',install_location='包装车间',equipment_status='running',equipment_level='B',responsible_id=9,responsible_name='刘洋'),
 dict(equipment_code='SB20260004',equipment_name='螺杆式氨制冷机组',category_id=3,model='LG-20',manufacturer='大连冰山集团',purchase_date='2022-05-20',install_date='2022-06-10',original_value=680000,dept_id=106,dept_name='仓储物流部',install_location='制冷机房',equipment_status='running',equipment_level='A',responsible_id=13,responsible_name='胡明',remark='涉氨制冷重大危险源管理点'),
 dict(equipment_code='SB20260005',equipment_name='低温发酵库',category_id=3,model='FJ-50',manufacturer='大连冰山集团',purchase_date='2022-05-20',install_date='2022-06-15',original_value=320000,dept_id=104,dept_name='生产部',install_location='腌制发酵车间',equipment_status='running',equipment_level='A',responsible_id=9,responsible_name='刘洋'),
 dict(equipment_code='SB20260006',equipment_name='炼油夹层锅',category_id=4,model='JJ-500',manufacturer='诸城兴盛机械',purchase_date='2022-09-01',install_date='2022-09-10',original_value=45000,dept_id=104,dept_name='生产部',install_location='炼油车间',equipment_status='running',equipment_level='C',responsible_id=9,responsible_name='刘洋'),
 dict(equipment_code='SB20260007',equipment_name='金属探测器',category_id=5,model='GJ-III',manufacturer='广东检验仪器',purchase_date='2022-08-25',install_date='2022-09-01',original_value=52000,dept_id=104,dept_name='生产部',install_location='包装车间',equipment_status='running',equipment_level='A',responsible_id=10,responsible_name='陈静',remark='每班开机校验'),
 dict(equipment_code='SB20260008',equipment_name='冷链运输车',category_id=4,model='CLW-5T',manufacturer='程力专用汽车',purchase_date='2023-03-15',install_date='2023-03-20',original_value=380000,dept_id=106,dept_name='仓储物流部',install_location='厂区车库',equipment_status='running',equipment_level='B',responsible_id=16,responsible_name='高鹏'),
]
# ============ 安全生产 safety ============
safety_points=[
 dict(risk_code='FXD001',risk_name='液氨储罐区',area_name='制冷机房',hazard_desc='液氨泄漏中毒/爆炸风险',accident_type='中毒和窒息',l_value=3,e_value=2,c_value=15,risk_level='较大风险',control_level='orange',is_major_hazard='1',check_frequency='每日',person_id=20,person_name='邓楠'),
 dict(risk_code='FXD002',risk_name='制冷压缩机组',area_name='制冷机房',hazard_desc='设备故障引发氨泄漏',accident_type='中毒和窒息',l_value=2,e_value=2,c_value=15,risk_level='较大风险',control_level='orange',check_frequency='每班',person_id=20,person_name='邓楠'),
 dict(risk_code='FXD003',risk_name='炼油夹层锅（高温）',area_name='炼油车间',hazard_desc='高温烫伤/燃气泄漏',accident_type='灼烫',l_value=3,e_value=3,c_value=7,risk_level='一般风险',control_level='yellow',check_frequency='每班',person_id=7,person_name='张伟'),
 dict(risk_code='FXD004',risk_name='分割刀具作业区',area_name='屠宰分割车间',hazard_desc='机械伤害/割伤',accident_type='机械伤害',l_value=3,e_value=6,c_value=3,risk_level='一般风险',control_level='yellow',check_frequency='每日',person_id=8,person_name='张伟'),
 dict(risk_code='FXD005',risk_name='叉车装卸作业区',area_name='发货月台',hazard_desc='车辆伤害',accident_type='车辆伤害',l_value=2,e_value=6,c_value=7,risk_level='一般风险',control_level='yellow',check_frequency='每日',person_id=16,person_name='高鹏'),
 dict(risk_code='FXD006',risk_name='配电室',area_name='厂区动力区',hazard_desc='触电/电气火灾',accident_type='触电',l_value=1,e_value=6,c_value=15,risk_level='重大风险',control_level='red',check_frequency='每周',person_id=21,person_name='黄健'),
]

safety_hazards=[
 dict(hazard_code='YH20261027001',risk_point_id=1,hazard_desc='液氨储罐区压力表读数异常偏高',hazard_type='设备设施缺陷',hazard_level='较大隐患',discover_person_id=20,discover_person='邓楠',discover_time='2026-10-26 09:00:00',source_type='inspection',dept_id=108,dept_name='安全生产办公室',person_id=21,person_name='黄健',rectify_req='校验压力表并检查安全阀',rectify_deadline='2026-10-28',hazard_status='rectifying',rectify_desc='已联系厂家校验中',verify_person='邓楠'),
 dict(hazard_code='YH20261027002',risk_point_id=3,hazard_desc='炼油车间燃气软管老化开裂',hazard_type='设备设施缺陷',hazard_level='一般隐患',discover_person_id=7,discover_person='张伟',discover_time='2026-10-25 10:00:00',source_type='daily_check',dept_id=104,dept_name='生产部',person_id=9,person_name='刘洋',rectify_req='更换燃气软管',rectify_deadline='2026-10-26',hazard_status='rectified',rectify_desc='已更换新软管并做检漏测试',rectify_time='2026-10-25 16:00:00',verify_person_id=20,verify_person='邓楠',verify_time='2026-10-25 17:00:00',verify_result='pass'),
 dict(hazard_code='YH20261027003',risk_point_id=4,hazard_desc='分割车间部分员工防护手套破损未更换',hazard_type='个人防护缺失',hazard_level='一般隐患',discover_person_id=8,discover_person='张伟',discover_time='2026-10-27 08:30:00',source_type='daily_check',dept_id=104,dept_name='生产部',person_id=8,person_name='张伟',rectify_req='立即更换劳保手套',rectify_deadline='2026-10-27',hazard_status='rectified',rectify_desc='已更换并补充库存',rectify_time='2026-10-27 09:00:00',verify_person_id=20,verify_person='邓楠',verify_time='2026-10-27 10:00:00',verify_result='pass'),
 dict(hazard_code='YH20261027004',risk_point_id=5,hazard_desc='发货月台叉车行驶通道标识线模糊',hazard_type='安全标识缺陷',hazard_level='一般隐患',discover_person_id=16,discover_person='高鹏',discover_time='2026-10-24 15:00:00',source_type='inspection',dept_id=106,dept_name='仓储物流部',person_id=16,person_name='高鹏',rectify_req='重新施划通道标识',rectify_deadline='2026-10-30',hazard_status='rectifying',rectify_desc='已报备行政安排划线',verify_person='邓楠'),
 dict(hazard_code='YH20261027005',risk_point_id=2,hazard_desc='制冷机组运行噪音异常',hazard_type='设备设施缺陷',hazard_level='一般隐患',discover_person_id=20,discover_person='邓楠',discover_time='2026-10-23 09:00:00',source_type='daily_check',dept_id=106,dept_name='仓储物流部',person_id=13,person_name='胡明',rectify_req='停机检修轴承',rectify_deadline='2026-10-25',hazard_status='rectified',rectify_desc='已更换轴承恢复正常',rectify_time='2026-10-24 18:00:00',verify_person_id=20,verify_person='邓楠',verify_time='2026-10-25 08:00:00',verify_result='pass'),
 dict(hazard_code='YH20261027006',risk_point_id=6,hazard_desc='配电室绝缘垫破损',hazard_type='设备设施缺陷',hazard_level='一般隐患',discover_person_id=21,discover_person='黄健',discover_time='2026-10-27 10:00:00',source_type='inspection',dept_id=104,dept_name='生产部',person_id=21,person_name='黄健',rectify_req='更换绝缘垫',rectify_deadline='2026-10-29',hazard_status='pending',verify_person='邓楠'),
 dict(hazard_code='YH20261027007',risk_point_id=1,hazard_desc='氨区应急喷淋装置测试阀内漏',hazard_type='应急设施缺陷',hazard_level='较大隐患',discover_person_id=20,discover_person='邓楠',discover_time='2026-10-20 09:30:00',source_type='drill',dept_id=108,dept_name='安全生产办公室',person_id=21,person_name='黄健',rectify_req='检修更换测试阀',rectify_deadline='2026-10-22',hazard_status='verified',rectify_desc='已更换阀门并复测正常',rectify_time='2026-10-21 15:00:00',verify_person_id=20,verify_person='邓楠',verify_time='2026-10-22 09:00:00',verify_result='pass',verify_comment='整改到位，验收通过'),
 dict(hazard_code='YH20261027008',risk_point_id=3,hazard_desc='炼油车间排油烟罩油垢积累',hazard_type='卫生/火灾隐患',hazard_level='一般隐患',discover_person_id=9,discover_person='刘洋',discover_time='2026-10-22 11:00:00',source_type='daily_check',dept_id=104,dept_name='生产部',person_id=9,person_name='刘洋',rectify_req='组织清洗油烟罩',rectify_deadline='2026-10-24',hazard_status='rectified',rectify_desc='已清洁完毕',rectify_time='2026-10-23 17:00:00',verify_person_id=20,verify_person='邓楠',verify_time='2026-10-24 09:00:00',verify_result='pass'),
]

# ============ 人事 hr ============
hr_emps=[
 dict(employee_no='YL2026001',employee_name='丰孟',gender='1',phone='13800000001',native_place='云南富源',address='曲靖市富源县胜境街道',edu_level='3',org_id=100,position_id=5,position_name='总经理',work_type='2',employee_type='0',entry_date='2022-03-10',employee_status='0',user_id=2,dept_id=100),
 dict(employee_no='YL2026002',employee_name='罗丽波',gender='2',phone='13800000002',native_place='云南富源',edu_level='4',org_id=101,position_id=4,position_name='综合管理专员',work_type='2',employee_type='0',entry_date='2022-03-10',employee_status='0',user_id=3,dept_id=101),
 dict(employee_no='YL2026003',employee_name='王雪姣',gender='2',phone='13800000003',edu_level='3',org_id=102,position_id=4,position_name='财务负责人',work_type='0',employee_type='0',entry_date='2022-03-10',employee_status='0',user_id=4,dept_id=102),
 dict(employee_no='YL2026004',employee_name='李晨',gender='1',phone='13800000004',edu_level='3',org_id=103,position_id=6,position_name='采购供应部经理',work_type='2',employee_type='0',entry_date='2023-05-15',employee_status='0',user_id=5,dept_id=103),
 dict(employee_no='YL2026005',employee_name='周强',gender='1',phone='13800000005',edu_level='4',org_id=103,position_id=4,position_name='采购专员',work_type='0',employee_type='0',entry_date='2024-02-01',employee_status='0',user_id=6,dept_id=103),
 dict(employee_no='YL2026006',employee_name='吴芳',gender='2',phone='13800000006',edu_level='3',org_id=104,position_id=6,position_name='生产部经理',work_type='2',employee_type='0',entry_date='2022-06-01',employee_status='0',user_id=7,dept_id=104),
 dict(employee_no='YL2026007',employee_name='张伟',gender='1',phone='13800000007',edu_level='4',org_id=109,position_id=6,position_name='屠宰分割车间主任',work_type='0',employee_type='0',entry_date='2022-07-01',employee_status='0',user_id=8,dept_id=109),
 dict(employee_no='YL2026008',employee_name='刘洋',gender='1',phone='13800000008',edu_level='4',org_id=110,position_id=6,position_name='加工车间主任',work_type='0',employee_type='0',entry_date='2022-07-01',employee_status='0',user_id=9,dept_id=110),
 dict(employee_no='YL2026009',employee_name='陈静',gender='2',phone='13800000009',edu_level='3',org_id=105,position_id=6,position_name='质量管理部经理',work_type='0',employee_type='0',entry_date='2022-09-01',employee_status='0',user_id=10,dept_id=105),
 dict(employee_no='YL2026010',employee_name='赵雷',gender='1',phone='13800000010',edu_level='3',org_id=105,position_id=4,position_name='质量检验员',work_type='0',employee_type='0',entry_date='2023-03-01',employee_status='0',user_id=11,dept_id=105),
 dict(employee_no='YL2026011',employee_name='杨帆',gender='1',phone='13800000012',edu_level='4',org_id=106,position_id=6,position_name='仓储物流部经理',work_type='2',employee_type='0',entry_date='2023-08-01',employee_status='0',user_id=13,dept_id=106),
 dict(employee_no='YL2026012',employee_name='黄健',gender='1',phone='13800000020',edu_level='4',org_id=104,position_id=4,position_name='设备管理员',work_type='0',employee_type='0',entry_date='2024-05-01',employee_status='0',user_id=22,dept_id=104),
]
# ============ 采购 pms ============
pms_plans=[
 dict(plan_no='CGJH20261001',title='2026年11月大河乌猪原料收购计划',status='2',plan_type='2',start_date='2026-11-01',end_date='2026-11-30',budget_amount=1800000,planner_id=5,planner_name='李晨',dept_id=103,dept_name='采购供应部',audit_by='admin',audit_time='2026-10-26 10:00:00',description='按订单收购纯种大河乌猪约200头，高于市场价10%'),
 dict(plan_no='CGJH20261002',title='11月包装材料采购计划',status='2',plan_type='2',start_date='2026-11-01',end_date='2026-11-30',budget_amount=120000,planner_id=5,planner_name='李晨',dept_id=103,dept_name='采购供应部',description='真空袋/礼盒/标签采购'),
 dict(plan_no='CGJH20261003',title='第四季度调味辅料采购计划',status='0',plan_type='2',start_date='2026-10-25',end_date='2026-12-31',budget_amount=90000,planner_id=6,planner_name='周强',dept_id=103,dept_name='采购供应部',description='肠衣/食盐/调味料'),
 dict(plan_no='CGJH20261004',title='10月大河乌猪原料收购计划',status='5',plan_type='2',start_date='2026-10-01',end_date='2026-10-31',budget_amount=2000000,actual_amount=1950000,planner_id=5,planner_name='李晨',dept_id=103,dept_name='采购供应部',audit_by='admin',audit_time='2026-09-28 10:00:00',description='本月实际收购约215头'),
 dict(plan_no='CGJH20261005',title='冷链运输服务续约采购',status='2',plan_type='3',start_date='2026-11-01',end_date='2027-10-31',budget_amount=300000,planner_id=5,planner_name='李晨',dept_id=103,dept_name='采购供应部',audit_by='admin',description='顺捷冷链沪/京干线运输年度服务'),
 dict(plan_no='CGJH20261006',title='劳保用品季度采购计划',status='0',plan_type='2',start_date='2026-11-01',end_date='2026-11-15',budget_amount=15000,planner_id=6,planner_name='周强',dept_id=108,dept_name='安全生产办公室',description='防护手套/口罩/工作服'),
]

all_stmts=[]
all_stmts+=build_insert('mk_customer',mk_customers,'customer_id',common)
all_stmts+=build_insert('wms_warehouse',wms_whs,'warehouse_id',common)
all_stmts+=build_insert('wms_supplier',wms_suppliers,'supplier_id',common)
all_stmts+=build_insert('wms_material',wms_materials,'material_id',common)
all_stmts+=build_insert('mms_workshop',mms_workshops,'workshop_id',common)
all_stmts+=build_insert('mms_work_order',mms_orders,'work_order_id',common)
all_stmts+=build_insert('qms_insp_task',qms_tasks,'task_id',common)
all_stmts+=build_insert('qms_ncr',qms_ncrs,'ncr_id',common)
all_stmts+=build_insert('qms_quality_target',qms_targets,'target_id',{'status':lambda i,r:'0','del_flag':lambda i,r:'0','create_by':lambda i,r:'admin','create_time':lambda i,r:'NOW()'})
all_stmts+=build_insert('dms_equipment_category',dms_cats,'category_id',common)
all_stmts+=build_insert('dms_equipment',dms_eqs,'equipment_id',common)
all_stmts+=build_insert('biz_safety_risk_point',safety_points,'risk_point_id',common)
all_stmts+=build_insert('biz_safety_hazard',safety_hazards,'hazard_id',common)
all_stmts+=build_insert('biz_hr_employee',hr_emps,'employee_id',common)
all_stmts+=build_insert('pms_purchase_plan',pms_plans,'plan_id',common)

sql="SET NAMES utf8mb4;\nSET FOREIGN_KEY_CHECKS=0;\n"
sql+="\n".join(f"TRUNCATE TABLE {t};" for t in ['mk_customer','wms_warehouse','wms_supplier','wms_material','mms_workshop','mms_work_order','qms_insp_task','qms_ncr','qms_quality_target','dms_equipment_category','dms_equipment','biz_safety_risk_point','biz_safety_hazard','biz_hr_employee','pms_purchase_plan'])+"\n"
sql+="\n".join(all_stmts)+"\nSET FOREIGN_KEY_CHECKS=1;\n"
open('db_backup/seed_data_20261027.sql','w',encoding='utf-8').write(sql)
print('stmts:',len(all_stmts))
r=subprocess.run([MYSQL,'-uroot','-pZBrj@2026','--default-character-set=utf8mb4',DB],stdin=open('db_backup/seed_data_20261027.sql',encoding='utf-8'),capture_output=True,text=True,encoding='utf-8')
print('exec_exit=',r.returncode)
if r.stderr: print(r.stderr[:600])

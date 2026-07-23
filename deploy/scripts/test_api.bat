@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

echo ============================================
echo  EDOP 营销模块 P0-P3 API 完整测试
echo ============================================
echo.

echo [1] 登录获取Token
curl -s -X POST http://localhost:8081/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin123\"}" > login_response.txt
for /f "tokens=2 delims=:," %%a in ('type login_response.txt ^| findstr "token"') do set TOKEN=%%~a
set TOKEN=%TOKEN: =%
echo TOKEN=%TOKEN%
echo.

echo [2] 测试回款管理 - 查询列表
echo --- GET /mk/payment/list ---
curl -s http://localhost:8081/mk/payment/list -H "Authorization: Bearer %TOKEN%"
echo.
echo.

echo [3] 测试回款管理 - 统计
echo --- GET /mk/payment/statistics ---
curl -s http://localhost:8081/mk/payment/statistics -H "Authorization: Bearer %TOKEN%"
echo.
echo.

echo [4] 测试回款管理 - 新增回款计划（独立创建）
echo --- POST /mk/payment ---
curl -s -X POST http://localhost:8081/mk/payment -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json" -d "{\"customerId\":1,\"periodNo\":5,\"planAmount\":50000,\"planDate\":\"2026-08-01\",\"paymentMethod\":\"0\",\"bankAccount\":\"622848\",\"remark\":\"独立创建测试\"}"
echo.
echo.

echo [5] 测试回款管理 - 查询回款记录列表
echo --- GET /mk/payment/record/list ---
curl -s "http://localhost:8081/mk/payment/record/list" -H "Authorization: Bearer %TOKEN%"
echo.
echo.

echo [6] 测试回款管理 - 新增回款记录（多次回款）
echo --- POST /mk/payment/record ---
curl -s -X POST http://localhost:8081/mk/payment/record -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json" -d "{\"planId\":3,\"thisAmount\":5000,\"paymentDate\":\"2026-07-22\",\"paymentMethod\":\"0\",\"bankAccount\":\"622848\",\"remark\":\"部分回款测试\"}"
echo.
echo.

echo [7] 测试订单管理 - 查询列表
echo --- GET /mk/order/list ---
curl -s http://localhost:8081/mk/order/list -H "Authorization: Bearer %TOKEN%"
echo.
echo.

echo [8] 测试订单管理 - 订单确认 (订单ID=5,状态=0待确认)
echo --- PUT /mk/order/confirm/5 ---
curl -s -X PUT http://localhost:8081/mk/order/confirm/5 -H "Authorization: Bearer %TOKEN%"
echo.
echo.

echo [9] 测试订单管理 - 订单取消 (需要取消原因)
echo --- PUT /mk/order/cancel/5 ---
curl -s -X PUT "http://localhost:8081/mk/order/cancel/5?cancelReason=测试取消" -H "Authorization: Bearer %TOKEN%"
echo.
echo.

echo [10] 测试发票管理 - 查询列表
echo --- GET /mk/invoice/list ---
curl -s http://localhost:8081/mk/invoice/list -H "Authorization: Bearer %TOKEN%"
echo.
echo.

echo [11] 测试发票管理 - 新增发票
echo --- POST /mk/invoice ---
curl -s -X POST http://localhost:8081/mk/invoice -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json" -d "{\"customerId\":1,\"invoiceType\":\"0\",\"invoiceAmount\":10000,\"invoiceTitle\":\"测试客户公司\",\"taxNo\":\"91110000123456789X\",\"remark\":\"API测试发票\"}"
echo.
echo.

echo [12] 测试订单退货 - 查询列表
echo --- GET /mk/return/list ---
curl -s http://localhost:8081/mk/return/list -H "Authorization: Bearer %TOKEN%"
echo.
echo.

echo [13] 测试合同管理 - 查询即将到期合同
echo --- GET /mk/contract/expiring/30 ---
curl -s http://localhost:8081/mk/contract/expiring/30 -H "Authorization: Bearer %TOKEN%"
echo.
echo.

echo [14] 测试合同管理 - 查询合同变更记录
echo --- GET /mk/contract/change/list/1 ---
curl -s http://localhost:8081/mk/contract/change/list/1 -H "Authorization: Bearer %TOKEN%"
echo.
echo.

echo [15] 测试合同管理 - 提交合同变更申请
echo --- POST /mk/contract/change ---
curl -s -X POST http://localhost:8081/mk/contract/change -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json" -d "{\"contractId\":1,\"changeType\":\"1\",\"fieldName\":\"contractAmount\",\"oldValue\":\"580000\",\"newValue\":\"600000\",\"changeReason\":\"金额变更测试\"}"
echo.
echo.

echo ============================================
echo  测试完成
echo ============================================
del login_response.txt

// Test HR module API endpoints
const http = require('http');

function httpRequest(options, postData) {
  return new Promise((resolve, reject) => {
    const req = http.request({
      hostname: 'localhost',
      port: 8081,
      ...options,
      headers: { 'Content-Type': 'application/json', ...(options.headers || {}) }
    }, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => resolve({ statusCode: res.statusCode, body: data, headers: res.headers }));
    });
    req.on('error', reject);
    if (postData) req.write(JSON.stringify(postData));
    req.end();
  });
}

async function main() {
  // 1. Login
  console.log('1. Logging in...');
  const loginRes = await httpRequest({
    method: 'POST',
    path: '/login'
  }, { username: 'admin', password: 'admin123' });
  
  const loginData = JSON.parse(loginRes.body);
  if (loginData.code !== 200) {
    console.error('Login failed:', loginData);
    process.exit(1);
  }
  const token = loginData.token;
  console.log('   Token obtained:', token.substring(0, 20) + '...');
  
  // 2. Test each HR module list endpoint
  const endpoints = [
    '/hr/organization/list',
    '/hr/position/list',
    '/hr/employee/list',
    '/hr/entry/list',
    '/hr/regular/list',
    '/hr/transfer/list',
    '/hr/leave/list',
    '/hr/contract/list',
    '/hr/shift/list',
    '/hr/schedule/list',
    '/hr/attendance/list',
    '/hr/leaveRequest/list',
    '/hr/overtime/list',
    '/hr/monthly/list',
    '/hr/leaveBalance/list',
    '/hr/salaryItem/list',
    '/hr/salaryPlan/list',
    '/hr/payslip/list',
    '/hr/pieceworkPlan/list',
    '/hr/pieceworkRecord/list',
    '/hr/socialSecurity/list',
    '/hr/taxRate/list',
    '/hr/specialDeduction/list',
    '/hr/payroll/list',
    '/hr/certificate/list',
    '/hr/healthCheck/list',
    '/hr/laborWorker/list',
    '/hr/tempSettlement/list',
  ];
  
  console.log('\n2. Testing HR module list endpoints:');
  let passCount = 0, failCount = 0;
  
  for (const ep of endpoints) {
    const res = await httpRequest({
      method: 'GET',
      path: ep + '?pageNum=1&pageSize=5',
      headers: { 'Authorization': 'Bearer ' + token }
    });
    
    let body;
    try {
      body = JSON.parse(res.body);
    } catch(e) {
      body = { code: -1, msg: 'JSON parse error' };
    }
    
    const status = body.code === 200 ? 'PASS' : 'FAIL';
    if (status === 'PASS') {
      passCount++;
      console.log(`   [${status}] ${ep} -> rows: ${body.rows ? body.rows.length : 0}, total: ${body.total || 0}`);
    } else {
      failCount++;
      console.log(`   [${status}] ${ep} -> ${res.statusCode}: ${body.msg || body.message || res.body.substring(0, 100)}`);
    }
  }
  
  console.log(`\n=== Results: ${passCount} passed, ${failCount} failed, total ${endpoints.length} ===`);
  
  // 3. Test creating an employee
  console.log('\n3. Testing CRUD on HrShift (班次)...');
  
  // Create
  const createRes = await httpRequest({
    method: 'POST',
    path: '/hr/shift',
    headers: { 'Authorization': 'Bearer ' + token }
  }, {
    shiftCode: 'TEST001',
    shiftName: '测试班次',
    startTime: '08:00',
    endTime: '17:00',
    crossMidnight: 0,
    workHours: 8.0,
    restHours: 1.0,
    mealMinutes: 60,
    nightShift: 0,
    nightAllowance: 0,
    applicableTo: '测试',
    orderNum: 99,
    status: '0'
  });
  let createBody = JSON.parse(createRes.body);
  console.log('   Create:', createBody.code === 200 ? 'PASS' : 'FAIL', createBody.msg || '');
  
  // List again to verify
  const listRes2 = await httpRequest({
    method: 'GET',
    path: '/hr/shift/list?pageNum=1&pageSize=10',
    headers: { 'Authorization': 'Bearer ' + token }
  });
  const listBody2 = JSON.parse(listRes2.body);
  console.log('   List after create:', listBody2.code === 200 ? 'PASS' : 'FAIL', 'rows:', listBody2.rows ? listBody2.rows.length : 0);
  
  if (listBody2.rows && listBody2.rows.length > 0) {
    const shiftId = listBody2.rows[0].shiftId;
    
    // Get by ID
    const getRes = await httpRequest({
      method: 'GET',
      path: '/hr/shift/' + shiftId,
      headers: { 'Authorization': 'Bearer ' + token }
    });
    const getBody = JSON.parse(getRes.body);
    console.log('   Get by ID:', getBody.code === 200 ? 'PASS' : 'FAIL');
    
    // Update
    const updateRes = await httpRequest({
      method: 'PUT',
      path: '/hr/shift',
      headers: { 'Authorization': 'Bearer ' + token }
    }, { ...getBody.data, shiftName: '测试班次-改' });
    const updateBody = JSON.parse(updateRes.body);
    console.log('   Update:', updateBody.code === 200 ? 'PASS' : 'FAIL');
    
    // Delete
    const delRes = await httpRequest({
      method: 'DELETE',
      path: '/hr/shift/' + shiftId,
      headers: { 'Authorization': 'Bearer ' + token }
    });
    const delBody = JSON.parse(delRes.body);
    console.log('   Delete:', delBody.code === 200 ? 'PASS' : 'FAIL');
  }
  
  console.log('\n=== All tests complete ===');
}

main().catch(console.error);

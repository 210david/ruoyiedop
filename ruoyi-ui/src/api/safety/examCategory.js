import request from '@/utils/request'

// 查询题目分类列表（分页）
export function listExamCategory(query) {
  return request({ url: '/safety/exam/category/list', method: 'get', params: query })
}
// 分类下拉/树数据（不分页，题库页左侧分类树用）
export function examCategoryOptions(query) {
  return request({ url: '/safety/exam/category/options', method: 'get', params: query })
}
// 分类详情
export function getExamCategory(categoryId) {
  return request({ url: '/safety/exam/category/' + categoryId, method: 'get' })
}
// 新增分类
export function addExamCategory(data) {
  return request({ url: '/safety/exam/category', method: 'post', data: data })
}
// 修改分类
export function updateExamCategory(data) {
  return request({ url: '/safety/exam/category', method: 'put', data: data })
}
// 删除分类（分类下有题目时后端拒绝）
export function delExamCategory(categoryId) {
  return request({ url: '/safety/exam/category/' + categoryId, method: 'delete' })
}

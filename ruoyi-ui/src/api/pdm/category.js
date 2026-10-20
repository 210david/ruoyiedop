import request from '@/utils/request'

// 查询产品分类列表（平铺）
export function listCategory(query) {
  return request({
    url: '/pdm/category/list',
    method: 'get',
    params: query
  })
}

// 查询产品分类树
export function getCategoryTree(query) {
  return request({
    url: '/pdm/category/tree',
    method: 'get',
    params: query
  })
}

// 查询产品分类详细
export function getCategory(categoryId) {
  return request({
    url: '/pdm/category/' + categoryId,
    method: 'get'
  })
}

// 新增产品分类（分类编码自动生成）
export function addCategory(data) {
  return request({
    url: '/pdm/category',
    method: 'post',
    data: data
  })
}

// 修改产品分类
export function updateCategory(data) {
  return request({
    url: '/pdm/category',
    method: 'put',
    data: data
  })
}

// 删除产品分类（校验子分类与产品引用）
export function delCategory(categoryIds) {
  return request({
    url: '/pdm/category/' + categoryIds,
    method: 'delete'
  })
}

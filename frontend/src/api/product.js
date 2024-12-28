import request from '@/utils/request'

// 获取产品列表
export function getProductList (parameter) {
  return request({
    url: '/product/list',
    method: 'get',
    params: parameter
  })
}

// 创建新产品
export function createProduct (data) {
  return request({
    url: '/product/create',
    method: 'post',
    data: data
  })
}

// 更新产品信息
export function updateProduct (data) {
  return request({
    url: '/product/update',
    method: 'post',
    data: data
  })
}

// 删除产品
export function deleteProduct (id) {
  return request({
    url: `/product/delete/${id}`,
    method: 'delete'
  })
}

// 获取产品详情
export function getProductDetail (id) {
  return request({
    url: `/product/detail/${id}`,
    method: 'get'
  })
}

// 获取产品阶段选项
export function getProductStages () {
  return request({
    url: '/product/stages',
    method: 'get'
  })
}

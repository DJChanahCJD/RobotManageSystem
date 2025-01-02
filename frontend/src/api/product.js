import request from '@/utils/request'

// 获取产品列表
export function getProductList (parameter) {
  const { productName, productOwner, productInformation, productStage, partName, id, pageNo, pageSize } = parameter
  return request({
    url: '/product/list',
    method: 'get',
    params: {
      productName,
      productOwner,
      productInformation,
      productStage,
      partName,
      id,
      pageNo,
      pageSize
    }
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
export function updateProduct (id, data) {
  return request({
    url: `/product/${id}`,
    method: 'put',
    data: data
  })
}

// 删除产品
export function deleteProduct (id) {
  return request({
    url: `/product/${id}`,
    method: 'delete'
  })
}

// 获取产品详情
export function getProductDetail (id) {
  return request({
    url: `/product/${id}`,
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

// 获取产品部件链接
export function getProductLinks (id) {
  return request({
    url: `/product/${id}/links`,
    method: 'get'
  })
}

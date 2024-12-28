import request from '@/utils/request'

export function getOrderList (parameter) {
  return request({
    url: '/order/list',
    method: 'get',
    params: parameter
  })
}

export function getOrderDetail (id) {
  return request({
    url: `/order/detail/${id}`,
    method: 'get'
  })
}

export function createOrder (data) {
  return request({
    url: '/order/create',
    method: 'post',
    data: data
  })
}

export function updateOrder (data) {
  return request({
    url: '/order/update',
    method: 'post',
    data: data
  })
}

export function deleteOrder (id) {
  return request({
    url: `/order/delete/${id}`,
    method: 'delete'
  })
}

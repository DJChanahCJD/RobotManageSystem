import request from '@/utils/request'

export function getOrderList (parameter) {
  return request({
    url: '/order/list',
    method: 'get',
    params: {
      ID: parameter.id,
      Name: parameter.name,
      Type: parameter.type,
      StartDate: parameter.startDate,
      EndDate: parameter.endDate,
      pageNo: parameter.pageNo,
      pageSize: parameter.pageSize
    }
  })
}

export function getOrderDetail (id) {
  return request({
    url: `/order/${id}`,
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
    url: `/order/${data.id}`,
    method: 'put',
    data: data
  })
}

export function deleteOrder (id) {
  return request({
    url: `/order/${id}`,
    method: 'delete'
  })
}

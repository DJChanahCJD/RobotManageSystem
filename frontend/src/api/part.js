import request from '@/utils/request'

export function getPartList (parameter) {
  return request({
    url: '/part/list',
    method: 'get',
    params: parameter
  })
}

export function createPart (data) {
  console.log('data  from createPart: ', data)
  return request({
    url: '/part/create',
    method: 'post',
    data: data
  })
}

export function updatePart (masterId, data) {
  return request({
    url: `/part/${masterId}`,
    method: 'put',
    data: data
  })
}

export function deletePart (id) {
  return request({
    url: `/part/${id}`,
    method: 'delete'
  })
}

export function getPartDetail (id) {
  return request({
    url: `/part/${id}`,
    method: 'get'
  })
}
// 获取分类
export function getClassificationList () {
  return request({
    url: `/part/classification/list`,
    method: 'get'
  })
}

// 获取零件分类属性
export function getPartAttributes (id) {
  return request({
    url: `/part/classification/${id}/attributes`,
    method: 'get'
  })
}

export function searchParts (parameter) {
  return request({
    url: '/part/search',
    method: 'get',
    params: parameter
  })
}

import request from '@/utils/request'

export function getPartList (parameter) {
  return request({
    url: '/part/list',
    method: 'get',
    params: parameter
  })
}

export function createPart (data) {
  return request({
    url: '/part/create',
    method: 'post',
    data: data
  })
}

export function updatePart (data) {
  return request({
    url: '/part/update',
    method: 'post',
    data: data
  })
}

export function deletePart (id) {
  return request({
    url: `/part/delete/${id}`,
    method: 'delete'
  })
}

export function getPartDetail (id) {
  return request({
    url: `/part/detail/${id}`,
    method: 'get'
  })
}

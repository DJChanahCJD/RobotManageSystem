import request from '@/utils/request'

export function getBlueprintList (parameter) {
  return request({
    url: '/blueprint/list',
    method: 'get',
    params: parameter
  })
}

export function createBlueprint (data) {
  return request({
    url: '/blueprint/create',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function updateBlueprint (data) {
  return request({
    url: '/blueprint/update',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function deleteBlueprint (id) {
  return request({
    url: `/blueprint/delete/${id}`,
    method: 'delete'
  })
}

export function getBlueprintDetail (id) {
  return request({
    url: `/blueprint/detail/${id}`,
    method: 'get'
  })
}

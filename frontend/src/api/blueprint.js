import request from '@/utils/request'

export function getBlueprintList (id, blueprintDescription, pageNo = 1, pageSize = 10) {
  return request({
    url: '/blueprint/list',
    method: 'get',
    params: {
      ID: id,
      BlueprintDescription: blueprintDescription,
      pageNo,
      pageSize
    }
  })
}

export function createBlueprint (data) {
  return request({
    url: '/blueprint/create',
    method: 'post',
    headers: {
      'Content-Type': 'application/json'
    },
    data: {
      blueprintDescription: data.blueprintDescription,
      fileId: data.fileId
    }
  })
}

export function updateBlueprint (id, data) {
  return request({
    url: `/blueprint/${id}`,
    method: 'put',
    headers: {
      'Content-Type': 'application/json'
    },
    data: {
      blueprintDescription: data.blueprintDescription,
      fileId: data.fileId
    }
  })
}

export function deleteBlueprint (id) {
  return request({
    url: `/blueprint/${id}`,
    method: 'delete'
  })
}

export function getBlueprintDetail (id) {
  return request({
    url: `/blueprint/${id}`,
    method: 'get'
  })
}

export function uploadBlueprint (formData) {
  return request({
    url: `/blueprint/upload`,
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data: formData
  })
}

export function downloadBlueprint (fileId, blueprintId) {
  return request({
    url: `/blueprint/download/${fileId}/${blueprintId}`,
    method: 'get',
    responseType: 'blob'
  })
}

export function searchBlueprints (parameter) {
  return request({
    url: '/blueprint/search',
    method: 'get',
    params: parameter
  })
}

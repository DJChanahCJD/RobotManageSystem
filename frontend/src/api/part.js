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

export function updatePart (masterId, data) {
  const { name, description } = data
  return request({
    url: `/part/${masterId}`,
    method: 'put',
    data: {
      name,
      description
    }
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

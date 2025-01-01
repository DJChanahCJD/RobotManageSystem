import request from '@/utils/request'

export function getUserList (parameter) {
  const { name, phone, authority, pageNo, pageSize } = parameter
  return request({
    url: '/user/list',
    method: 'get',
    params: {
      name,
      phone,
      authority,
      pageNo: pageNo || 1,
      pageSize: pageSize || 10
    }
  })
}

export function createUser (data) {
  return request({
    url: '/user/create',
    method: 'post',
    data: data
  })
}

export function updateUser (id, data) {
  return request({
    url: `/user/${id}`,
    method: 'put',
    data: data
  })
}

export function deleteUser (id) {
  return request({
    url: `/user/${id}`,
    method: 'delete'
  })
}

export function getUser (id) {
  return request({
    url: `/user/${id}`,
    method: 'get'
  })
}

export function getUserInfo () {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

export function changePassword (data) {
  return request({
    url: '/auth/changePassword',
    method: 'post',
    data: data
  })
}

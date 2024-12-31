import request from '@/utils/request'

export function getUserList (parameter) {
  return request({
    url: '/user/list',
    method: 'get',
    params: parameter
  })
}

export function createUser (data) {
  return request({
    url: '/user/create',
    method: 'post',
    data: data
  })
}

export function updateUser (data) {
  return request({
    url: '/user/update',
    method: 'post',
    data: data
  })
}

export function deleteUser (id) {
  return request({
    url: `/user/delete/${id}`,
    method: 'delete'
  })
}

export function getUserInfo () {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

import Mock from 'mockjs2'
import { builder, getQueryParameters } from '../util'

const totalCount = 50

const orderList = (options) => {
  const parameters = getQueryParameters(options)

  const result = []
  const pageNo = parseInt(parameters.pageNo)
  const pageSize = parseInt(parameters.pageSize)
  const totalPage = Math.ceil(totalCount / pageSize)
  const next = (pageNo >= totalPage ? (totalCount % pageSize) : pageSize) + 1

  for (let i = 1; i < next; i++) {
    const tmpKey = (pageNo - 1) * pageSize + i

    // 根据查询条件过滤
    const orderDate = Mock.mock('@date')
    if (parameters.startDate && parameters.endDate) {
      if (orderDate < parameters.startDate || orderDate > parameters.endDate) {
        continue
      }
    }

    result.push({
      id: tmpKey,
      name: Mock.mock('@ctitle(5, 10)'),
      quantity: Mock.mock('@integer(1, 100)'),
      type: Mock.mock('@integer(1, 2)'),
      orderDate: orderDate,
      createTime: Mock.mock('@datetime'),
      status: Mock.mock('@integer(0, 1)')
    })
  }

  return builder({
    pageSize: pageSize,
    pageNo: pageNo,
    totalCount: totalCount,
    totalPage: totalPage,
    data: result
  })
}

const getOrderDetail = (options) => {
  const id = options.url.match(/\/order\/detail\/(\d+)/)[1]

  return builder({
    id: parseInt(id),
    name: Mock.mock('@ctitle(5, 10)'),
    quantity: Mock.mock('@integer(1, 100)'),
    type: Mock.mock('@integer(1, 2)'),
    orderDate: Mock.mock('@date'),
    createTime: Mock.mock('@datetime'),
    status: Mock.mock('@integer(0, 1)'),
    remark: Mock.mock('@cparagraph(1, 3)')
  })
}

Mock.mock(/\/order\/list/, 'get', orderList)
Mock.mock(/\/order\/detail\/\d+/, 'get', getOrderDetail)
Mock.mock(/\/order\/create/, 'post', (options) => {
  return builder({ ...JSON.parse(options.body), id: Mock.mock('@id') })
})
Mock.mock(/\/order\/update/, 'post', (options) => {
  return builder(JSON.parse(options.body))
})
Mock.mock(/\/order\/delete\/\d+/, 'delete', () => {
  return builder({}, '删除成功')
})

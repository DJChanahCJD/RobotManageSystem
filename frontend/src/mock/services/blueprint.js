import Mock from 'mockjs2'
import { builder, getQueryParameters } from '../util'

const totalCount = 50

const blueprintList = (options) => {
  const parameters = getQueryParameters(options)
  const { id, name } = parameters

  const result = []
  const pageNo = parseInt(parameters.pageNo)
  const pageSize = parseInt(parameters.pageSize)
  const totalPage = Math.ceil(totalCount / pageSize)
  const next = (pageNo >= totalPage ? (totalCount % pageSize) : pageSize) + 1

  for (let i = 1; i < next; i++) {
    const tmpKey = (pageNo - 1) * pageSize + i
    if ((id && tmpKey !== parseInt(id)) ||
        (name && !Mock.mock('@word(5, 10)').includes(name))) {
      continue
    }

    result.push({
      id: tmpKey,
      name: Mock.mock('@word(5, 10)'),
      description: Mock.mock('@sentence(10, 20)'),
      createTime: Mock.mock('@datetime'),
      updateTime: Mock.mock('@datetime'),
      blueprint: Mock.mock('@image("200x200", "#50B347", "#FFF", "Blueprint")'),
      status: Mock.mock('@integer(0, 1)'),
      creator: Mock.mock('@cname')
    })
  }

  return builder({
    pageSize: pageSize,
    pageNo: pageNo,
    totalCount: id || name ? result.length : totalCount,
    totalPage: id || name ? Math.ceil(result.length / pageSize) : totalPage,
    data: result
  })
}

const createBlueprint = (options) => {
  const body = options.body
  console.log('mock: createBlueprint body', body)

  return builder({
    id: Mock.mock('@id'),
    name: body.name,
    description: body.description,
    createTime: Mock.mock('@datetime'),
    updateTime: Mock.mock('@datetime'),
    status: 1
  })
}

const updateBlueprint = (options) => {
  const body = options.body
  console.log('mock: updateBlueprint body', body)

  return builder({
    id: body.id,
    name: body.name,
    description: body.description,
    updateTime: Mock.mock('@datetime'),
    status: 1
  })
}

const deleteBlueprint = (options) => {
  const body = options.body
  console.log('mock: deleteBlueprint body', body)
  return builder({}, '删除成功')
}

const getBlueprintDetail = (options) => {
  const id = options.url.match(/\/blueprint\/detail\/(\d+)/)[1]

  return builder({
    id: parseInt(id),
    name: Mock.mock('@word(5, 10)'),
    description: Mock.mock('@sentence(10, 20)'),
    createTime: Mock.mock('@datetime'),
    updateTime: Mock.mock('@datetime'),
    blueprint: Mock.mock('@image("800x600", "#50B347", "#FFF", "Blueprint")'),
    status: Mock.mock('@integer(0, 1)'),
    creator: Mock.mock('@cname'),
    version: Mock.mock('@float(1, 10, 1, 2)'),
    size: Mock.mock('@float(1, 100, 1, 2)') + 'MB',
    downloads: Mock.mock('@integer(0, 1000)'),
    tags: Mock.mock('@range(3, 6)').map(() => Mock.mock('@word(3, 6)')),
    remarks: Mock.mock('@paragraph(2, 4)')
  })
}

Mock.mock(/\/blueprint\/list/, 'get', blueprintList)
Mock.mock(/\/blueprint\/create/, 'post', createBlueprint)
Mock.mock(/\/blueprint\/update/, 'post', updateBlueprint)
Mock.mock(/\/blueprint\/delete\/\d+/, 'delete', deleteBlueprint)
Mock.mock(/\/blueprint\/detail\/\d+/, 'get', getBlueprintDetail)

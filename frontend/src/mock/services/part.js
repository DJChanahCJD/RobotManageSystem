import Mock from 'mockjs2'
import { builder, getQueryParameters } from '../util'

const partList = (options) => {
  const parameters = getQueryParameters(options)

  const data = [
    {
      id: '689085567868788744x',
      name: '压力传感器',
      specificName: 'V2023-压力传感器增强版',
      version: 'V2023',
      description: '增强型压力传感器，适用于工业设备的压力控制，具备更高的稳定性。'
    },
    {
      id: '689085567868788441',
      name: '阀门控制器',
      specificName: 'V2022-阀门控制器优化版',
      version: 'V2022',
      description: '优化版阀门控制器，支持精确的阀门开启和关闭控制。'
    }
    // 可以添加更多预设数据
  ]

  // 处理搜索
  const list = data.filter(item => {
    if (parameters.id && !item.id.includes(parameters.id)) return false
    if (parameters.name && !item.name.includes(parameters.name)) return false
    return true
  })

  const pageNo = parseInt(parameters.pageNo)
  const pageSize = parseInt(parameters.pageSize)
  const offset = (pageNo - 1) * pageSize

  return builder({
    data: list.slice(offset, offset + pageSize),
    pageSize: pageSize,
    pageNo: pageNo,
    totalCount: list.length,
    totalPage: Math.ceil(list.length / pageSize)
  })
}

const getPartDetail = (options) => {
  const id = options.url.match(/\/part\/detail\/(\d+)/)[1]

  return builder({
    id: id,
    name: Mock.mock('@ctitle(3, 5)'),
    specificName: Mock.mock('@ctitle(5, 10)'),
    version: Mock.mock('@string("upper", 1)') + Mock.mock('@integer(2020, 2023)'),
    description: Mock.mock('@cparagraph(1, 2)'),
    createTime: Mock.mock('@datetime'),
    updateTime: Mock.mock('@datetime'),
    status: Mock.mock('@pick(["正常", "维修中", "已报废"])'),
    manufacturer: Mock.mock('@cword(3, 5)工业有限公司'),
    maintenanceRecords: Mock.mock('@range(2, 4)').map(() => ({
      date: Mock.mock('@date'),
      type: Mock.mock('@pick(["常规检修", "故障维修", "性能升级"])'),
      operator: Mock.mock('@cname'),
      description: Mock.mock('@csentence')
    }))
  })
}

Mock.mock(/\/part\/list/, 'get', partList)
Mock.mock(/\/part\/create/, 'post', (options) => {
  return builder({ ...JSON.parse(options.body), id: Mock.mock('@id') })
})
Mock.mock(/\/part\/update/, 'post', (options) => {
  return builder(JSON.parse(options.body))
})
Mock.mock(/\/part\/delete\/\d+/, 'delete', () => {
  return builder({}, '删除成功')
})
Mock.mock(/\/part\/detail\/\d+/, 'get', getPartDetail)

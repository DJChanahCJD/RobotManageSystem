import Mock from 'mockjs2'
import { builder, getQueryParameters } from '../util'

const productList = (options) => {
  const parameters = getQueryParameters(options)
  // const result = []

  const data = [
    {
      id: '68855727209526895x',
      name: '建筑工程机器人B9',
      basicInfo: '设计中考虑了人机交互的安全性，具备自动停机和退出功能，确保在意外情况中的操作安全性。',
      owner: '赵敏',
      productStage: 'InitialStage'
    },
    {
      id: '688557272095268952',
      name: '水下探测机器人U3',
      basicInfo: '采用了高精度传感器和AI算法，能够在复杂的工业环境中精确执行巡检任务，减少操作误差。',
      owner: '刘晓',
      productStage: 'InitialStage'
    },
    {
      id: '688557272095268951',
      name: '医疗辅助机器人M5',
      basicInfo: '本产品结合了最新的生命科学成果，能够高效进行辅助诊断，并提供实时环境监测数据。',
      owner: '王强',
      productStage: 'DesignStage'
    }
  ]

  // 处理搜索
  const list = data.filter(item => {
    if (parameters.id && !item.id.includes(parameters.id)) return false
    if (parameters.name && !item.name.includes(parameters.name)) return false
    return true
  })

  const pageNo = parseInt(parameters.pageNo) || 1
  const pageSize = parseInt(parameters.pageSize) || 10
  const offset = (pageNo - 1) * pageSize

  return builder({
    data: list.slice(offset, offset + pageSize),
    pageSize: pageSize,
    pageNo: pageNo,
    totalCount: list.length,
    totalPage: Math.ceil(list.length / pageSize)
  })
}

// 添加产品详情接口
const getProductDetail = (options) => {
  const id = options.url.match(/\/product\/detail\/(\d+)/)[1]

  // 模拟详细信息
  return builder({
    id: id,
    name: Mock.mock('@ctitle(5, 10)'),
    basicInfo: Mock.mock('@cparagraph(1, 2)'),
    owner: Mock.mock('@cname'),
    productStage: Mock.mock('@pick(["InitialStage", "DesignStage", "DevelopmentStage"])'),
    createTime: Mock.mock('@datetime'),
    updateTime: Mock.mock('@datetime'),
    version: Mock.mock('@float(1, 10, 1, 2)'),
    description: Mock.mock('@cparagraph(2, 4)'),
    teamMembers: Mock.mock('@range(3, 6)').map(() => ({
      name: Mock.mock('@cname'),
      role: Mock.mock('@pick(["开发", "设计", "测试", "产品"])')
    }))
  })
}

// 添加产品阶段选项接口
const getProductStages = () => {
  return builder([
    {
      value: 'InitialStage',
      label: '初始阶段',
      color: 'blue'
    },
    {
      value: 'DesignStage',
      label: '设计阶段',
      color: 'green'
    },
    {
      value: 'DevelopmentStage',
      label: '开发阶段',
      color: 'orange'
    }
  ])
}

Mock.mock(/\/product\/list/, 'get', productList)
Mock.mock(/\/product\/create/, 'post', (options) => {
  return builder({ ...JSON.parse(options.body), id: Mock.mock('@id') })
})
Mock.mock(/\/product\/update/, 'post', (options) => {
  return builder(JSON.parse(options.body))
})
Mock.mock(/\/product\/delete\/\d+/, 'delete', () => {
  return builder({}, '删除成功')
})
Mock.mock(/\/product\/detail\/\d+/, 'get', getProductDetail)
Mock.mock(/\/product\/stages/, 'get', getProductStages)

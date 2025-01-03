// eslint-disable-next-line
import { UserLayout, BasicLayout, BlankLayout } from '@/layouts'
import { bxAnaalyse } from '@/core/icons'
const RouteView = {
  name: 'RouteView',
  render: h => h('router-view')
}

export const asyncRouterMap = [
  {
    path: '/',
    name: 'index',
    component: BasicLayout,
    meta: { title: 'menu.home' },
    redirect: '/dashboard',
    children: [
      // dashboard
      {
        path: '/dashboard',
        name: 'dashboard',
        redirect: '/dashboard/analysis',
        component: RouteView,
        meta: { title: 'menu.dashboard', keepAlive: true, icon: bxAnaalyse },
        hideChildrenInMenu: true,
        hideBreadcrumb: true,
        children: [
          {
            path: '/dashboard/analysis/:pageNo([1-9]\\d*)?',
            name: 'Analysis',
            component: () => import('@/views/dashboard/Analysis'),
            meta: { title: 'menu.dashboard.analysis', keepAlive: false }
          }
        ]
      },
      { // DesignBlueprint
        path: '/blueprint',
        name: 'blueprint',
        hideBreadcrumb: true,
        component: () => import('@/views/list/BlueprintList'),
        meta: { title: 'menu.blueprint', icon: 'table' }
      },
      {
        path: '/order', // 订单管理
        name: 'order',
        hideBreadcrumb: true,
        component: () => import('@/views/list/OrderList'),
        meta: { title: 'menu.order', icon: 'shopping-cart' }
      },
      {
        path: '/part', // 部件管理
        name: 'part',
        hideBreadcrumb: true,
        component: () => import('@/views/list/PartList'),
        meta: { title: 'menu.part', icon: 'tool' }
      },
      {
        path: '/product', // 产品管理
        name: 'product',
        hideBreadcrumb: true,
        component: () => import('@/views/list/ProductList'),
        meta: { title: 'menu.product', icon: 'table' }
      },
      {
        path: '/userManage', // 用户管理
        name: 'user',
        hideBreadcrumb: true,
        component: () => import('@/views/list/UserList'),
        meta: { title: 'menu.user', icon: 'user' }
      }
    ]
  },
  {
    path: '*',
    redirect: '/404',
    hidden: true
  }
]

/**
 * 基础路由
 * @type { *[] }
 */
export const constantRouterMap = [
  {
    path: '/user',
    component: UserLayout,
    redirect: '/user/login',
    hidden: true,
    children: [
      {
        path: 'login',
        name: 'login',
        component: () => import(/* webpackChunkName: "user" */ '@/views/user/Login')
      }
    ]
  },
  {
    path: '/404',
    component: () => import(/* webpackChunkName: "fail" */ '@/views/exception/404')
  }
]

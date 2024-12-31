import Vue from 'vue'
import Router from 'vue-router'
import { asyncRouterMap, constantRouterMap } from '@/config/router.config'

Vue.use(Router)

// 合并路由
const routes = [...constantRouterMap, ...asyncRouterMap]

export default new Router({
  mode: 'history',
  routes: routes
})

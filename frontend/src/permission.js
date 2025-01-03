import router from './router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { ACCESS_TOKEN } from '@/store/mutation-types'
import storage from 'store'

NProgress.configure({ showSpinner: false })

const loginPath = '/user/login'

router.beforeEach((to, from, next) => {
  NProgress.start()

  const token = storage.get(ACCESS_TOKEN)

  // 其他页面
  if (!token && to.path !== loginPath) {
    next({
      path: loginPath,
      query: { redirect: to.fullPath }
    })
    NProgress.done()
    return
  }
  next()
})

router.afterEach(() => {
  NProgress.done()
})

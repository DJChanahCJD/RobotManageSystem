<template>
  <a-dropdown v-if="currentUser && currentUser.name" placement="bottomRight">
    <span class="ant-pro-account-avatar">
      <a-avatar size="small" src="https://gw.alipayobjects.com/zos/antfincdn/XAosXuNZyF/BiazfanxmamNRoxxVxka.png" class="antd-pro-global-header-index-avatar" />
      <span>{{ currentUser.name }}</span>
    </span>
    <template v-slot:overlay>
      <a-menu class="ant-pro-drop-down menu" :selected-keys="[]">
        <a-menu-item v-if="menu" key="settings" @click="handleToSettings">
          <a-icon type="setting" />
          {{ $t('menu.account.settings') }}
        </a-menu-item>
        <a-menu-divider v-if="menu" />
        <a-menu-item key="logout" @click="handleLogout">
          <a-icon type="logout" />
          {{ $t('menu.account.logout') }}
        </a-menu-item>
      </a-menu>
    </template>
  </a-dropdown>
  <span v-else>
    <a-spin size="small" :style="{ marginLeft: 8, marginRight: 8 }" />
  </span>
</template>

<script>
import { Modal } from 'ant-design-vue'
import { changePassword } from '@/api/user'
export default {
  name: 'AvatarDropdown',
  props: {
    currentUser: {
      type: Object,
      default: () => null
    },
    menu: {
      type: Boolean,
      default: true
    }
  },
  methods: {
    handleToSettings () {
      let newPassword = ''
      Modal.confirm({
        title: '修改密码',
        content: h => {
          return h('div', [
            h('div', { style: { marginBottom: '16px' } }, [
              h('div', { style: { marginBottom: '8px' } }, '当前密码：'),
              h('a-input', {
                props: {
                  value: this.currentUser.password,
                  disabled: true,
                  visible: true
                }
              })
            ]),
            h('div', [
              h('div', { style: { marginBottom: '8px' } }, '新密码：'),
              h('a-input-password', {
                props: {
                  placeholder: '请输入新密码'
                },
                on: {
                  change: (e) => {
                    newPassword = e.target.value
                  }
                }
              })
            ])
          ])
        },
        onOk: () => {
          if (!newPassword) {
            this.$message.error('请输入新密码')
            return Promise.reject(new Error('请输入新密码'))
          }
          // 调用修改密码API
          return changePassword({
            newPassword: newPassword
          }).then(() => {
            this.$message.success('密码修改成功，请重新登录')
            // 修改成功后登出
            return this.$store.dispatch('Logout').then(() => {
              this.$router.push({ name: 'login' })
            })
          }).catch(err => {
            this.$message.error(err.message || '密码修改失败')
            return Promise.reject(new Error('密码修改失败'))
          })
        }
      })
    },
    handleLogout (e) {
      Modal.confirm({
        title: this.$t('layouts.usermenu.dialog.title'),
        content: this.$t('layouts.usermenu.dialog.content'),
        onOk: () => {
          return this.$store.dispatch('Logout').then(() => {
            this.$router.push({ name: 'login' })
          })
        },
        onCancel () {}
      })
    }
  }
}
</script>

<style lang="less" scoped>
.ant-pro-drop-down {
  :deep(.action) {
    margin-right: 8px;
  }
  :deep(.ant-dropdown-menu-item) {
    min-width: 160px;
  }
}
</style>

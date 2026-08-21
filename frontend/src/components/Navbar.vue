<script setup>
// 顶部导航栏
// 功能:
//   1) Logo 点击返回首页
//   2) 搜索框(回车跳转首页并搜索)
//   3) 上传视频按钮(需登录)
//   4) 用户菜单: 已登录显示头像下拉(退出),未登录显示"登录/注册"
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 网站 logo URL(通过后端静态资源接口访问)
// 用常量 + :src 动态绑定,防止 Vite 把静态 src 当成 asset import 报错
const logoUrl = '/api/files/logo/logo.png'

// 搜索关键词(从路由 query 同步)
const keyword = ref(route.query.title || '')

// 执行搜索 -> 跳转首页带参数
const onSearch = () => {
  router.push({ path: '/', query: { title: keyword.value } })
}

// 跳转上传页(未登录会被路由守卫拦截到登录页)
const goUpload = () => {
  router.push('/upload')
}

// 退出登录
const onLogout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/')
}
</script>

<template>
  <header class="navbar">
    <div class="container nav-inner">
      <!-- 左侧: Logo + 搜索 -->
      <div class="nav-left">
        <router-link to="/" class="logo">
          <img
            class="logo-img"
            :src="logoUrl"
            alt="DEO Logo"
            @error="e => (e.target.style.display = 'none')"
          />
          <span class="logo-text">DEO</span>
        </router-link>

        <el-input
          v-model="keyword"
          placeholder="搜索视频标题"
          class="search-input"
          clearable
          @keyup.enter="onSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <!-- 右侧: 上传 + 用户菜单 -->
      <div class="nav-right">
        <el-button type="primary" round @click="goUpload">
          <el-icon><Upload /></el-icon>
          <span>投稿</span>
        </el-button>

        <template v-if="userStore.isLoggedIn">
          <el-dropdown>
            <span class="user-trigger">
              <el-avatar :size="32" class="avatar">
                {{ userStore.userInfo?.userName?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="user-name">{{ userStore.userInfo?.userName }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/')">首页</el-dropdown-item>
                <el-dropdown-item divided @click="onLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>

        <template v-else>
          <el-button text @click="router.push('/login')">登录</el-button>
          <el-button type="primary" plain @click="router.push('/register')">注册</el-button>
        </template>
      </div>
    </div>
  </header>
</template>

<style scoped>
.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  height: 56px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}
.nav-inner {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.nav-left {
  display: flex;
  align-items: center;
  gap: 24px;
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  font-size: 18px;
  color: #7388ff;
}
.logo-img {
  width: 32px;
  height: 32px;
  object-fit: contain;
  border-radius: 6px;
  flex-shrink: 0;
}
.search-input {
  width: 320px;
}
.nav-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 18px;
  transition: background 0.2s;
}
.user-trigger:hover {
  background: #f1f2f3;
}
.avatar {
  background: #7388ff;
  color: #fff;
}
.user-name {
  font-size: 14px;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.upload-btn {
  background: #fb7299;
  border-color: #fb7299;
  color: #fff;
}
.upload-btn:hover {
  background: #f25d8e;
  border-color: #f25d8e;
}
</style>

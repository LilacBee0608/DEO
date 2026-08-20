<script setup>
// 404 页面
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<template>
  <div class="not-found">
    <div class="content">
      <p class="code">404</p>
      <p class="msg">糟糕,页面不存在</p>
      <el-button type="primary" round @click="router.push('/')">
        返回首页
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f9f9f9;
}
.content {
  text-align: center;
}
.code {
  font-size: 120px;
  font-weight: bold;
  color: #fb7299;
  margin-bottom: 12px;
}
.msg {
  font-size: 18px;
  color: #61666d;
  margin-bottom: 24px;
}
</style>

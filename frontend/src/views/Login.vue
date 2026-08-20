<script setup>
// 登录页
// 表单提交 -> userStore.login -> 成功跳转 redirect 或首页
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 表单数据
const form = reactive({
  userName: '',
  userPswd: ''
})

// 表单引用,用于触发表单校验
const formRef = ref(null)

// 校验规则
const rules = {
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度2-20位', trigger: 'blur' }
  ],
  userPswd: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度6-50位', trigger: 'blur' }
  ]
}

// 提交中状态(防重复提交)
const loading = ref(false)

// 提交登录
const onSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await userStore.login({ ...form })
      ElMessage.success('登录成功')
      // 跳转到 redirect 参数或首页
      const redirect = route.query.redirect || '/'
      router.push(redirect)
    } finally {
      loading.value = false
    }
  })
}

// 一键填充测试账号
const fillTest = () => {
  form.userName = 'admin'
  form.userPswd = '123456'
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="logo">
        <span class="logo-icon">📺</span>
        <h1>登录 bilibili-demo</h1>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        size="large"
        label-position="top"
        @submit.prevent="onSubmit"
      >
        <el-form-item label="用户名" prop="userName">
          <el-input
            v-model="form.userName"
            placeholder="请输入用户名"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item label="密码" prop="userPswd">
          <el-input
            v-model="form.userPswd"
            type="password"
            show-password
            placeholder="请输入密码"
            prefix-icon="Lock"
            @keyup.enter="onSubmit"
          />
        </el-form-item>

        <el-button
          type="primary"
          class="submit-btn"
          :loading="loading"
          @click="onSubmit"
        >
          登录
        </el-button>
      </el-form>

      <div class="footer">
        <span>没有账号?</span>
        <router-link to="/register" class="link">立即注册</router-link>
      </div>

      <el-divider>
        <el-button text type="primary" @click="fillTest">使用测试账号</el-button>
      </el-divider>
      <p class="tip">测试账号: admin / 123456</p>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #fb7299 0%, #f25d8e 100%);
}
.login-card {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
}
.logo {
  text-align: center;
  margin-bottom: 30px;
}
.logo-icon {
  font-size: 48px;
}
.logo h1 {
  margin-top: 8px;
  font-size: 22px;
  color: #18191c;
}
.submit-btn {
  width: 100%;
  margin-top: 8px;
}
.footer {
  text-align: center;
  margin-top: 16px;
  color: #9499a0;
}
.link {
  color: #fb7299;
  margin-left: 4px;
}
.tip {
  text-align: center;
  color: #9499a0;
  font-size: 12px;
}
</style>

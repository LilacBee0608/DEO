<script setup>
// 注册页
// 表单提交 -> userStore.register -> 成功跳转登录页
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  userName: '',
  userPswd: '',
  confirmPswd: ''
})
const formRef = ref(null)
const loading = ref(false)

// 确认密码校验
const validateConfirm = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.userPswd) {
    callback(new Error('两次密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度2-20位', trigger: 'blur' }
  ],
  userPswd: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度6-50位', trigger: 'blur' }
  ],
  confirmPswd: [
    { required: true, validator: validateConfirm, trigger: 'blur' }
  ]
}

const onSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await userStore.register({
        userName: form.userName,
        userPswd: form.userPswd
      })
      ElMessage.success('注册成功,请登录')
      router.push('/login')
    } finally {
      loading.value = false
    }
  })
}
</script>

<template>
  <div class="register-page">
    <div class="register-card">
      <div class="logo">
        <span class="logo-icon">📺</span>
        <h1>注册 bilibili-demo</h1>
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
            placeholder="请输入用户名(2-20位)"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item label="密码" prop="userPswd">
          <el-input
            v-model="form.userPswd"
            type="password"
            show-password
            placeholder="请输入密码(6-50位)"
            prefix-icon="Lock"
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPswd">
          <el-input
            v-model="form.confirmPswd"
            type="password"
            show-password
            placeholder="请再次输入密码"
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
          注册
        </el-button>
      </el-form>

      <div class="footer">
        <span>已有账号?</span>
        <router-link to="/login" class="link">去登录</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #fb7299 0%, #f25d8e 100%);
}
.register-card {
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
.logo-icon { font-size: 48px; }
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
</style>

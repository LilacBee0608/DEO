<script setup>
// 视频上传页
// 功能:
//   1) 表单: 标题 / 标签 / 简介 / 封面URL / 视频URL
//   2) 提交 createVideo API
//   3) 成功跳转视频详情页
// 注: 视频文件上传到 OSS 暂未集成,实训项目支持直接填写 URL
//     可使用 w3schools 的测试视频 URL,或本地静态资源
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import Navbar from '@/components/Navbar.vue'
import { createVideo } from '@/api/video'

const router = useRouter()

const form = reactive({
  title: '',
  tags: '',
  description: '',
  coverUrl: '',
  videoUrl: ''
})
const formRef = ref(null)
const submitting = ref(false)

const rules = {
  title: [
    { required: true, message: '请输入视频标题', trigger: 'blur' },
    { max: 100, message: '标题最长100字符', trigger: 'blur' }
  ],
  tags: [
    { max: 10, message: '标签最长10字符', trigger: 'blur' }
  ],
  videoUrl: [
    { required: true, message: '请填写视频URL', trigger: 'blur' }
  ]
}

// 快速填入测试视频URL(方便实训)
const fillTestVideo = () => {
  form.videoUrl = 'https://www.w3schools.com/html/mov_bbb.mp4'
  form.coverUrl = 'https://via.placeholder.com/1280x720?text=Video+Cover'
}

const onSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const res = await createVideo({ ...form })
      ElMessage.success('上传成功')
      router.push(`/video/${res.data.vId}`)
    } finally {
      submitting.value = false
    }
  })
}
</script>

<template>
  <div class="upload-page">
    <Navbar />
    <div class="container upload-card">
      <h1 class="page-title">投稿视频</h1>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
      >
        <el-form-item label="视频标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="请输入视频标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="标签" prop="tags">
          <el-input
            v-model="form.tags"
            placeholder="编程 / 生活 / 动漫 / 游戏 / 音乐"
            maxlength="10"
          />
        </el-form-item>

        <el-form-item label="简介" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="介绍一下你的视频"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="封面URL" prop="coverUrl">
          <el-input
            v-model="form.coverUrl"
            placeholder="封面图片URL(可空)"
          />
        </el-form-item>

        <el-form-item label="视频URL" prop="videoUrl">
          <el-input
            v-model="form.videoUrl"
            placeholder="视频文件URL"
          />
          <el-button text type="primary" @click="fillTestVideo">
            使用测试视频URL
          </el-button>
        </el-form-item>

        <div class="form-footer">
          <el-button @click="router.back()">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="onSubmit">
            立即投稿
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.upload-page {
  min-height: 100vh;
  padding-bottom: 40px;
}
.upload-card {
  max-width: 720px;
  background: #fff;
  border-radius: 8px;
  padding: 30px;
  margin-top: 20px;
}
.page-title {
  font-size: 22px;
  margin-bottom: 24px;
  color: #18191c;
}
.form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 12px;
}
</style>

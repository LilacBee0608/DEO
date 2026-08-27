<script setup>
// 视频上传页
// 功能:
//   1) 表单: 标题 / 标签 / 简介 / 封面URL / 视频URL
//   2) 分P: 默认单视频(不分P); 点击"添加分P"后切换为多P模式,可填入多个视频URL
//   3) 提交 createVideo API
//   4) 成功跳转视频详情页
// 注: 视频文件上传到 OSS 暂未集成,实训项目支持直接填写 URL
//     可使用 w3schools 的测试视频 URL,或本地静态资源
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import Navbar from '@/components/Navbar.vue'
import { createVideo } from '@/api/video'
// 标签统一配置(与首页 Home.vue 共用同一数据源)
// 修改 src/config/categories.js 后,首页筛选栏与投稿页可选标签会自动同步添加/删除
import { categories as tagOptions } from '@/config/categories'

const router = useRouter()

// 是否为分P模式(多视频)
const isMultiPart = ref(false)

// 单视频模式下的 URL
const singleVideoUrl = ref('')

// 分P模式下的视频列表(每个P包含一个URL)
const parts = ref([])

const form = reactive({
  title: '',
  tags: [], // 标签改为列表选择(数组),提交时 join 为字符串
  description: '',
  coverUrl: '',
  videoUrl: '' // 提交时根据模式动态填充
})
const formRef = ref(null)
const submitting = ref(false)

// 校验规则: 仅校验标题(视频URL在提交时统一校验)
const rules = {
  title: [
    { required: true, message: '请输入视频标题', trigger: 'blur' },
    { max: 100, message: '标题最长100字符', trigger: 'blur' }
  ],
  tags: [
    { type: 'array', max: 5, message: '最多选择5个标签', trigger: 'change' }
  ]
}

// ============= 分P模式操作 =============
// 切换为分P模式: 将单视频URL迁移为第1个分P,并补充第2个空分P
const enableMultiPart = () => {
  isMultiPart.value = true
  parts.value = [{ url: singleVideoUrl.value.trim() }, { url: '' }]
}

// 切换回单视频模式: 保留第1个分P的URL
const disableMultiPart = () => {
  const first = parts.value[0]?.url || ''
  singleVideoUrl.value = first
  parts.value = []
  isMultiPart.value = false
}

// 追加一个空分P
const addPart = () => {
  parts.value.push({ url: '' })
}

// 删除指定分P(至少保留1个)
const removePart = (index) => {
  if (parts.value.length <= 1) {
    ElMessage.warning('至少保留一个分P')
    return
  }
  parts.value.splice(index, 1)
}

// 计算最终提交的 videoUrl:
//   - 单视频模式: 直接用 singleVideoUrl
//   - 分P模式: 多个非空URL用换行符 \n 拼接
const finalVideoUrl = computed(() => {
  if (!isMultiPart.value) {
    return singleVideoUrl.value.trim()
  }
  return parts.value
    .map(p => p.url.trim())
    .filter(url => url.length > 0)
    .join('\n')
})

// 快速填入测试视频URL(方便实训)
const fillTestVideo = () => {
  if (isMultiPart.value) {
    // 分P模式: 给当前最后一个空分P填入测试URL
    const last = parts.value[parts.value.length - 1]
    if (last && !last.url) {
      last.url = 'https://www.w3schools.com/html/mov_bbb.mp4'
    } else {
      parts.value.push({ url: 'https://www.w3schools.com/html/mov_bbb.mp4' })
    }
  } else {
    singleVideoUrl.value = 'https://www.w3schools.com/html/mov_bbb.mp4'
  }
  if (!form.coverUrl) {
    form.coverUrl = 'https://via.placeholder.com/1280x720?text=Video+Cover'
  }
}

const onSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    // 统一校验视频URL
    const url = finalVideoUrl.value
    if (!url) {
      ElMessage.warning(isMultiPart.value ? '请至少填写一个分P的视频URL' : '请填写视频URL')
      return
    }
    submitting.value = true
    try {
      // 动态填充 videoUrl 后提交
      // 标签: 数组(列表选择)以逗号拼接为字符串存储,与首页 tags 模糊搜索匹配
      const res = await createVideo({
        ...form,
        tags: form.tags.join(','),
        videoUrl: url
      })
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
          <!-- 列表选择: 可选标签与首页分类栏同步(共同引用 config/categories.js) -->
          <el-select
            v-model="form.tags"
            multiple
            collapse-tags
            collapse-tags-tooltip
            placeholder="请选择视频标签(可多选)"
            style="width: 100%"
          >
            <el-option
              v-for="cat in tagOptions"
              :key="cat.value"
              :label="cat.label"
              :value="cat.value"
            />
          </el-select>
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

        <el-form-item label="视频URL">
          <!-- 单视频模式(不分P) -->
          <div v-if="!isMultiPart" class="single-video">
            <el-input
              v-model="singleVideoUrl"
              placeholder="视频文件URL"
            />
            <div class="url-actions">
              <el-button text type="primary" @click="fillTestVideo">
                使用测试视频URL
              </el-button>
              <el-button text type="primary" @click="enableMultiPart">
                添加分P
              </el-button>
            </div>
          </div>

          <!-- 分P模式(多视频) -->
          <div v-else class="multi-parts">
            <div
              v-for="(part, index) in parts"
              :key="index"
              class="part-row"
            >
              <span class="part-index">P{{ index + 1 }}</span>
              <el-input
                v-model="part.url"
                :placeholder="`分P${index + 1} 视频URL`"
              />
              <el-button
                v-if="parts.length > 1"
                type="danger"
                text
                @click="removePart(index)"
              >
                删除
              </el-button>
            </div>
            <div class="url-actions">
              <el-button type="primary" plain @click="addPart">
                + 添加分P
              </el-button>
              <el-button text type="primary" @click="fillTestVideo">
                填入测试URL
              </el-button>
              <el-button text @click="disableMultiPart">
                取消分P(保留第1个)
              </el-button>
            </div>
          </div>
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
/* ============= 视频URL / 分P ============= */
.single-video {
  width: 100%;
}
.url-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  flex-wrap: wrap;
}
.multi-parts {
  width: 100%;
}
.part-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.part-index {
  width: 32px;
  flex-shrink: 0;
  font-size: 13px;
  font-weight: 500;
  color: #6CA4F9;
  text-align: center;
}
.part-row :deep(.el-input) {
  flex: 1;
}
</style>

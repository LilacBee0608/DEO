<script setup>
// 首页
// 功能:
//   1) 顶部导航栏 Navbar
//   2) 视频分类标签栏(点击切换 tags 搜索)
//   3) 视频网格(响应式 5 列)
//   4) 分页器
//   5) 搜索(从路由 query.title 读取,Navbar 搜索框触发)
//   6) 加载/空状态
import { ref, reactive, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getVideoList } from '@/api/video'
import Navbar from '@/components/Navbar.vue'
import VideoCard from '@/components/VideoCard.vue'

const route = useRoute()

// 分类标签(用于按 tags 筛选,可扩展)
const categories = [
  { label: '全部', value: '' },
  { label: '编程', value: '编程' },
  { label: '生活', value: '生活' },
  { label: '动漫', value: '动漫' },
  { label: '游戏', value: '游戏' },
  { label: '音乐', value: '音乐' },
  { label: '鬼畜', value: '鬼畜' }
]

// 当前激活的分类
const activeCategory = ref('')

// 查询参数
const query = reactive({
  page: 1,
  size: 12,
  title: '',
  tags: ''
})

// 视频列表与状态
const videoList = ref([])
const total = ref(0)
const loading = ref(false)

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getVideoList(query)
    videoList.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

// 切换分类
const switchCategory = (cat) => {
  activeCategory.value = cat
  query.tags = cat
  query.page = 1
  loadData()
}

// 翻页
const onPageChange = (p) => {
  query.page = p
  loadData()
  // 回到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 监听路由 query 变化(搜索框触发)
watch(
  () => route.query.title,
  (newTitle) => {
    query.title = newTitle || ''
    query.page = 1
    loadData()
  }
)

onMounted(() => {
  query.title = route.query.title || ''
  loadData()
})
</script>

<template>
  <div class="home-page">
    <Navbar />

    <!-- 分类标签栏 -->
    <div class="category-bar">
      <div class="container">
        <ul class="cat-list">
          <li
            v-for="cat in categories"
            :key="cat.value"
            class="cat-item"
            :class="{ active: activeCategory === cat.value }"
            @click="switchCategory(cat.value)"
          >
            {{ cat.label }}
          </li>
        </ul>
      </div>
    </div>

    <!-- 视频网格 -->
    <div class="container content">
      <div v-loading="loading" class="video-grid">
        <VideoCard
          v-for="v in videoList"
          :key="v.vId"
          :video="v"
        />
      </div>

      <!-- 空状态 -->
      <el-empty
        v-if="!loading && videoList.length === 0"
        description="暂无视频"
      />

      <!-- 分页 -->
      <div v-if="total > query.size" class="pagination">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="query.size"
          :current-page="query.page"
          @current-change="onPageChange"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-page {
  min-height: 100vh;
  padding-bottom: 40px;
}
.category-bar {
  background: #fff;
  border-bottom: 1px solid #f1f2f3;
  margin-bottom: 20px;
}
.cat-list {
  display: flex;
  gap: 8px;
  padding: 12px 0;
  overflow-x: auto;
}
.cat-item {
  padding: 6px 14px;
  border-radius: 16px;
  background: #f1f2f3;
  color: #61666d;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}
.cat-item:hover {
  background: #e3e5e7;
}
.cat-item.active {
  background: #fb7299;
  color: #fff;
}
.video-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
  min-height: 200px;
}
@media (max-width: 1280px) {
  .video-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}
@media (max-width: 1024px) {
  .video-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
@media (max-width: 768px) {
  .video-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style>

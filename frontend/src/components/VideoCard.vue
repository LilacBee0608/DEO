<script setup>
// 视频卡片组件
// 用于首页视频网格中的单个视频展示
import { useRouter } from 'vue-router'

const props = defineProps({
  video: {
    type: Object,
    required: true
  }
})

const router = useRouter()

// 格式化播放量(1.2万)
const formatNum = (n) => {
  if (!n) return '0'
  if (n >= 10000) return (n / 10000).toFixed(1) + '万'
  return String(n)
}

// 点击跳转详情
const goDetail = () => {
  router.push(`/video/${props.video.vId}`)
}
</script>

<template>
  <div class="video-card" @click="goDetail">
    <!-- 封面 -->
    <div class="cover">
      <img
        :src="video.coverUrl || 'https://via.placeholder.com/300x200?text=No+Cover'"
        :alt="video.title"
        loading="lazy"
      />
      <div class="cover-mask">
        <span class="play-count">
          <el-icon><VideoPlay /></el-icon>
          {{ formatNum(video.playNum) }}
        </span>
        <span class="duration">{{ video.tags || '综合' }}</span>
      </div>
    </div>

    <!-- 信息 -->
    <div class="info">
      <h3 class="title" :title="video.title">{{ video.title }}</h3>
      <p class="author">
        <el-icon><User /></el-icon>
        <span>UP主: {{ video.id || '匿名' }}</span>
      </p>
      <div class="stats">
        <span><el-icon><Star /></el-icon> {{ formatNum(video.likeNum) }}</span>
        <span><el-icon><Share /></el-icon> {{ formatNum(video.shareNum) }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.video-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.video-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.12);
}
.cover {
  position: relative;
  width: 100%;
  padding-top: 62.5%; /* 16:9 */
  background: #f1f2f3;
  overflow: hidden;
}
.cover img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.cover-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  padding: 8px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.5), transparent 40%);
  color: #fff;
  font-size: 12px;
}
.play-count {
  display: flex;
  align-items: center;
  gap: 4px;
}
.info {
  padding: 10px 12px;
}
.title {
  font-size: 14px;
  font-weight: 500;
  line-height: 1.4;
  height: 40px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin-bottom: 6px;
}
.author {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #9499a0;
  font-size: 12px;
  margin-bottom: 4px;
}
.stats {
  display: flex;
  gap: 12px;
  color: #9499a0;
  font-size: 12px;
}
.stats span {
  display: flex;
  align-items: center;
  gap: 2px;
}
</style>

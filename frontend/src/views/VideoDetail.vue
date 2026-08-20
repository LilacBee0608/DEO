<script setup>
// 视频详情页
// 功能:
//   1) DanmuPlayer 播放视频(弹幕层)
//   2) 视频信息: 标题、UP主、播放量、点赞、收藏按钮
//   3) 简介
//   4) 评论区: 列表 / 发送 / 点赞
//   5) 进入页面自增播放量(仅首次进入)
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import Navbar from '@/components/Navbar.vue'
import DanmuPlayer from '@/components/DanmuPlayer.vue'
import { useUserStore } from '@/stores/user'
import {
  getVideoDetail,
  incrPlay,
  toggleLike,
  toggleFavorite
} from '@/api/video'
import { getDanmuList, sendDanmu } from '@/api/danmu'
import { getCommentList, sendComment, likeComment } from '@/api/comment'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 视频详情
const video = ref(null)
// 弹幕列表
const danmuList = ref([])
// 评论列表
const commentList = ref([])
// 加载状态
const loading = ref(false)
// 已自增播放量(避免重复)
let playIncrd = false

// ============= 加载视频详情 =============
const loadVideo = async () => {
  loading.value = true
  try {
    const res = await getVideoDetail(route.params.vId)
    video.value = res.data
    // 进入页面 +1 播放量(只调一次)
    if (!playIncrd) {
      playIncrd = true
      try { await incrPlay(route.params.vId) } catch (e) {}
    }
  } finally {
    loading.value = false
  }
}

// ============= 加载弹幕 =============
const loadDanmu = async () => {
  const res = await getDanmuList(route.params.vId)
  danmuList.value = res.data || []
}

// ============= 加载评论 =============
const loadComments = async () => {
  const res = await getCommentList(route.params.vId)
  commentList.value = res.data || []
}

// ============= 点赞 / 收藏 =============
const onLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  try {
    const res = await toggleLike(route.params.vId)
    video.value.liked = res.data.liked
    // 本地同步数字
    video.value.likeNum += res.data.liked ? 1 : -1
  } catch (e) {}
}

const onFavorite = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  try {
    const res = await toggleFavorite(route.params.vId)
    video.value.favorited = res.data.favorited
  } catch (e) {}
}

// ============= 弹幕发送 =============
const onSendDanmu = async (payload) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后发送弹幕')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  try {
    await sendDanmu({
      vId: route.params.vId,
      ...payload
    })
    // 同步加入弹幕列表(避免重新请求)
    danmuList.value.push({
      did: 'temp-' + Date.now(),
      danmuContent: payload.danmuContent,
      danmuFrame: payload.danmuFrame,
      color: payload.color
    })
    ElMessage.success('弹幕已发送')
  } catch (e) {}
}

// ============= 评论 =============
const commentInput = ref('')
const sending = ref(false)

const onSendComment = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后评论')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  const text = commentInput.value.trim()
  if (!text) return
  sending.value = true
  try {
    await sendComment({
      vId: route.params.vId,
      commentContent: text
    })
    ElMessage.success('评论成功')
    commentInput.value = ''
    await loadComments()
  } finally {
    sending.value = false
  }
}

// 评论点赞
const onCommentLike = async (c) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  try {
    await likeComment(c.cid)
    c.likeNum += 1
  } catch (e) {}
}

// ============= 工具方法 =============
const formatNum = (n) => {
  if (!n) return '0'
  if (n >= 10000) return (n / 10000).toFixed(1) + '万'
  return String(n)
}

const formatDate = (s) => {
  if (!s) return ''
  return String(s).replace('T', ' ').substring(0, 16)
}

// 初始化
onMounted(async () => {
  await loadVideo()
  await Promise.all([loadDanmu(), loadComments()])
})
</script>

<template>
  <div class="detail-page">
    <Navbar />

    <div v-loading="loading" class="container detail-content">
      <div v-if="video" class="main">
        <!-- 播放器 -->
        <DanmuPlayer
          :src="video.videoUrl"
          :poster="video.coverUrl"
          :danmu-list="danmuList"
          @send-danmu="onSendDanmu"
        />

        <!-- 视频标题 -->
        <h1 class="video-title">{{ video.title }}</h1>

        <!-- 信息条: UP主 / 点赞 / 收藏 / 分享 -->
        <div class="info-bar">
          <div class="left-info">
            <el-avatar :size="40" class="up-avatar">
              {{ video.authorName?.charAt(0) || 'U' }}
            </el-avatar>
            <span class="up-name">UP主: {{ video.authorName || video.id }}</span>
          </div>

          <div class="right-actions">
            <!-- 点赞 -->
            <el-button
              :type="video.liked ? 'primary' : 'default'"
              round
              @click="onLike"
            >
              <el-icon><Star /></el-icon>
              <span>{{ formatNum(video.likeNum) }}</span>
            </el-button>

            <!-- 收藏 -->
            <el-button
              :type="video.favorited ? 'primary' : 'default'"
              round
              @click="onFavorite"
            >
              <el-icon><CollectionTag /></el-icon>
              <span>收藏</span>
            </el-button>

            <!-- 分享(简化版) -->
            <el-button round>
              <el-icon><Share /></el-icon>
              <span>{{ formatNum(video.shareNum) }}</span>
            </el-button>
          </div>
        </div>

        <!-- 简介 -->
        <div class="description">
          <div class="desc-header">
            <span class="play-count">{{ formatNum(video.playNum) }} 观看</span>
            <span class="tags" v-if="video.tags">标签: {{ video.tags }}</span>
          </div>
          <p class="desc-text">{{ video.description || '暂无简介' }}</p>
        </div>

        <!-- 评论区 -->
        <div class="comments-section">
          <h2 class="section-title">
            评论
            <span class="comment-count">{{ commentList.length }}</span>
          </h2>

          <!-- 发送评论 -->
          <div class="comment-input">
            <el-avatar :size="36" class="user-avatar">
              {{ userStore.userInfo?.userName?.charAt(0) || '?' }}
            </el-avatar>
            <el-input
              v-model="commentInput"
              type="textarea"
              :rows="2"
              placeholder="发一条友善的弹幕 / 评论吧~"
              maxlength="100"
              show-word-limit
              resize="none"
            />
            <el-button
              type="primary"
              :loading="sending"
              :disabled="!commentInput.trim()"
              @click="onSendComment"
            >
              发送
            </el-button>
          </div>

          <!-- 评论列表 -->
          <div class="comment-list">
            <div v-for="c in commentList" :key="c.cid" class="comment-item">
              <el-avatar :size="36" class="user-avatar">
                {{ c.userName?.charAt(0) || 'U' }}
              </el-avatar>
              <div class="comment-body">
                <div class="comment-meta">
                  <span class="comment-user">{{ c.userName || '匿名用户' }}</span>
                  <span class="comment-time">{{ formatDate(c.commentFrame) }}</span>
                </div>
                <p class="comment-content">{{ c.commentContent }}</p>
                <div class="comment-actions">
                  <el-button text size="small" @click="onCommentLike(c)">
                    <el-icon><Star /></el-icon>
                    <span>{{ c.likeNum || 0 }}</span>
                  </el-button>
                </div>
              </div>
            </div>

            <el-empty
              v-if="commentList.length === 0"
              description="还没有评论,快来抢沙发~"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.detail-page {
  min-height: 100vh;
  padding-bottom: 40px;
}
.detail-content {
  padding-top: 20px;
}
.video-title {
  font-size: 20px;
  font-weight: 500;
  margin: 16px 0;
  line-height: 1.4;
}
.info-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f1f2f3;
}
.left-info {
  display: flex;
  align-items: center;
  gap: 10px;
}
.up-avatar {
  background: #fb7299;
  color: #fff;
}
.up-name {
  font-size: 15px;
  font-weight: 500;
}
.right-actions {
  display: flex;
  gap: 8px;
}
.description {
  margin: 16px 0;
  padding: 14px;
  background: #fff;
  border-radius: 8px;
}
.desc-header {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #9499a0;
  margin-bottom: 8px;
}
.desc-text {
  color: #18191c;
  line-height: 1.6;
}
.comments-section {
  margin-top: 24px;
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}
.section-title {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.comment-count {
  font-size: 14px;
  color: #9499a0;
  font-weight: normal;
}
.comment-input {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  align-items: flex-start;
}
.comment-input :deep(.el-textarea) {
  flex: 1;
}
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.comment-item {
  display: flex;
  gap: 12px;
}
.user-avatar {
  background: #00a1d6;
  color: #fff;
  flex-shrink: 0;
}
.comment-body {
  flex: 1;
}
.comment-meta {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 6px;
}
.comment-user {
  font-size: 14px;
  color: #61666d;
  font-weight: 500;
}
.comment-time {
  font-size: 12px;
  color: #9499a0;
}
.comment-content {
  font-size: 14px;
  line-height: 1.6;
  color: #18191c;
  margin-bottom: 6px;
}
.comment-actions {
  display: flex;
  gap: 16px;
}
</style>

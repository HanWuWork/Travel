<template>
  <div class="home">
    <van-nav-bar title="主页" />
    <NoticeBar
      left-icon="volume-o"
      text="本旅游助手基于AI制作，旨在提供更好的旅游体验"
    />
    <div class="banner-wrapper">
      <van-swipe
        ref="swipeRef"
        class="banner"
        :autoplay="3000"
        :prev-next-margin="24"
        indicator-color="white"
      >
        <van-swipe-item v-for="b in banners" :key="b.id" @click="goBanner(b)">
          <div class="banner-slide" :class="{ 'banner-slide--fallback': bannerFailed[b.id] }">
            <img
              v-if="!bannerFailed[b.id]"
              class="banner-slide__img"
              :src="b.image"
              :alt="b.title"
              @error="onBannerImgError(b)"
            />
            <div class="banner-slide__mask">
              <div class="banner-slide__text">
                <div class="banner-slide__title">{{ b.title }}</div>
                <div class="banner-slide__sub">{{ b.subtitle }}</div>
              </div>
              <span class="banner-slide__more">
                查看详情
                <Icon name="arrow" />
              </span>
            </div>
          </div>
        </van-swipe-item>
      </van-swipe>
      <Icon
        name="arrow-left"
        class="swipe-arrow swipe-arrow--left"
        @click.stop="onPrev"
      />
      <Icon
        name="arrow"
        class="swipe-arrow swipe-arrow--right"
        @click.stop="onNext"
      />
    </div>

    <JourneyCard
      :journey="journeyData"
      @update:journey="journeyData = $event"
      @edit-plan="onEditPlan"
    />

    <TripCountdown />

    <RecommendSection />

    <FeatureEntry />

    <QuickEntryCard />

    <ScenicSpotCard :spots="hotSpots" @select="onSelectSpot" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, NoticeBar, Icon } from 'vant'
import JourneyCard from '../components/JourneyCard.vue'
import QuickEntryCard from '../components/QuickEntryCard.vue'
import ScenicSpotCard from '../components/ScenicSpotCard.vue'
import FeatureEntry from '../components/FeatureEntry.vue'
import TripCountdown from '../components/TripCountdown.vue'
import RecommendSection from '../components/RecommendSection.vue'
import { hotSpots as fetchHotSpots } from '../api/dest'

const router = useRouter()
const swipeRef = ref(null)

/** 热门景点（来自后端真实数据，保证名称与点击后的详情一致） */
const hotSpots = ref([])

onMounted(async () => {
  try {
    const list = await fetchHotSpots(10)
    hotSpots.value = (list || []).map((s) => ({
      id: s.id,
      name: s.name,
      description: s.description,
      cityName: s.cityName,
      rating: s.rating != null ? Number(s.rating).toFixed(1) : '',
      // 本地景区图片（public/spots/<id>.jpg）；缺失时组件会自动回退为渐变占位
      image: `/spots/${s.id}.jpg`,
      tag: s.rating != null && s.rating >= 4.8 ? '热门' : '推荐'
    }))
  } catch (e) {
    hotSpots.value = []
  }
})

/** 顶部轮播横幅：可点击跳转到对应景点详情（图片使用本地景区图，缺失自动回退） */
const banners = [
  { id: 16, title: '西湖风景名胜区', subtitle: '杭州 · 四季皆美的城市湖山', image: '/spots/16.jpg' },
  { id: 1, title: '故宫博物院', subtitle: '北京 · 明清两代皇家宫殿', image: '/spots/1.jpg' },
  { id: 9, title: '外滩', subtitle: '上海 · 万国建筑与江畔天际线', image: '/spots/9.jpg' }
]

/** 横幅图片加载失败记录（回退为渐变底） */
const bannerFailed = reactive({})
const onBannerImgError = (b) => { bannerFailed[b.id] = true }

/** 点击横幅 → 景点详情 */
const goBanner = (b) => {
  if (b && b.id) router.push(`/dest/attraction/${b.id}`)
}

const journeyData = ref({
  destination: '',
  budget: '',
  days: ''
})

const onPrev = () => {
  swipeRef.value?.prev()
}

const onNext = () => {
  swipeRef.value?.next()
}

const onEditPlan = () => {
  journeyData.value = { destination: '', budget: '', days: '' }
  showToast('已重置')
}

const onSelectSpot = (spot) => {
  if (spot && spot.id) {
    router.push(`/dest/attraction/${spot.id}`)
  } else {
    router.push('/dest')
  }
}
</script>

<style scoped>
.home {
  padding-bottom: 16px;
}

.banner-wrapper {
  position: relative;
  padding: 0 20px;
  margin: 12px 0;
}

.banner {
  height: 140px;
  border-radius: 8px;
  overflow: hidden;
}

/* 轮播单页：图片 + 底部渐变遮罩 + 可点击提示 */
.banner-slide {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  cursor: pointer;
  background: linear-gradient(135deg, #2ba6d8, #04c489);
}

.banner-slide--fallback {
  background: linear-gradient(135deg, #2ba6d8, #04c489);
}

.banner-slide__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.banner-slide__mask {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 20px 12px 9px;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 8px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.72) 0%, rgba(0, 0, 0, 0.35) 55%, rgba(0, 0, 0, 0) 100%);
  color: #fff;
}

.banner-slide__title {
  font-size: 15px;
  font-weight: 700;
  line-height: 1.3;
}

.banner-slide__sub {
  margin-top: 2px;
  font-size: 11px;
  opacity: 0.9;
}

.banner-slide__more {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  gap: 2px;
  padding: 3px 9px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
  color: #fff;
  background: rgba(255, 255, 255, 0.22);
  border: 1px solid rgba(255, 255, 255, 0.35);
  backdrop-filter: blur(4px);
}

.swipe-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 30px;
  height: 30px;
  line-height: 30px;
  text-align: center;
  background: rgba(0, 0, 0, 0.45);
  color: white;
  border-radius: 50%;
  font-size: 14px;
  z-index: 10;
  cursor: pointer;
}

.swipe-arrow--left {
  left: 6px;
}

.swipe-arrow--right {
  right: 6px;
}
</style>

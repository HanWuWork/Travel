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
        <van-swipe-item v-for="(img, idx) in bannerImages" :key="idx">
          <Image
            :src="img"
            width="100%"
            height="100%"
            fit="cover"
            radius="8px"
          />
        </van-swipe-item>
      </van-swipe>
      <Icon
        name="arrow-left"
        class="swipe-arrow swipe-arrow--left"
        @click="onPrev"
      />
      <Icon
        name="arrow"
        class="swipe-arrow swipe-arrow--right"
        @click="onNext"
      />
    </div>

    <JourneyCard
      :journey="journeyData"
      @update:journey="journeyData = $event"
      @edit-plan="onEditPlan"
    />

    <QuickEntryCard />

    <ScenicSpotCard @select="onSelectSpot" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { showToast, NoticeBar, Image, Icon } from 'vant'
import JourneyCard from '../components/JourneyCard.vue'
import QuickEntryCard from '../components/QuickEntryCard.vue'
import ScenicSpotCard from '../components/ScenicSpotCard.vue'

const swipeRef = ref(null)

const bannerImages = [
  'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=%E6%9D%AD%E5%B7%9E%E8%A5%BF%E6%B9%96%E9%A3%8E%E6%99%AF%20%E6%B9%96%E5%85%89%E5%B1%B1%E8%89%B2%20%E8%93%9D%E5%A4%A9%E7%99%BD%E4%BA%91&image_size=landscape_16_9',
  'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=%E5%8C%97%E4%BA%AC%E6%95%85%E5%AE%AB%E5%8D%9A%E7%89%A9%E9%99%A2%20%E5%8F%A4%E5%BB%BA%E7%AD%91%20%E7%BA%A2%E5%A2%99%E9%BB%84%E7%93%A6&image_size=landscape_16_9',
  'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=%E5%BC%A0%E5%AE%B6%E7%95%8C%E5%9B%BD%E5%AE%B6%E6%A3%AE%E6%9E%97%E5%85%AC%E5%9B%AD%20%E5%A5%87%E5%B3%B0%E5%BC%82%E7%9F%B3%20%E4%BA%91%E9%9B%BE%E7%BC%95%E7%BB%95&image_size=landscape_16_9'
]

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
  showToast(`查看${spot.name}`)
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

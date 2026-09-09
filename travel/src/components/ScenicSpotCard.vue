<template>
  <div class="scenic-spot-card">
    <Card>
      <template #title>
        <div class="card-title">
          <Icon name="fire-o" />
          <span>热门景点</span>
        </div>
      </template>
      <template #desc>
        <div
          v-for="(spot, index) in spots"
          :key="spot.id"
          class="spot-item"
          :class="{ 'spot-item--last': index === spots.length - 1 }"
          @click="$emit('select', spot)"
        >
          <div class="spot-cover">
            <Image
              :src="spot.image"
              width="120px"
              height="90px"
              fit="cover"
              radius="8px"
            />
            <div class="spot-tag">{{ spot.tag }}</div>
          </div>
          <div class="spot-content">
            <div class="spot-title">
              <span class="spot-name">{{ spot.name }}</span>
              <span class="spot-rating">
                <Icon name="good-job-o" /> {{ spot.rating }}
              </span>
            </div>
            <div class="spot-desc ellipsis-2">{{ spot.description }}</div>
            <div class="spot-footer">
              <span class="spot-price">¥{{ spot.price }}<span class="price-unit">起</span></span>
              <Icon name="arrow" class="arrow-icon" />
            </div>
          </div>
        </div>
      </template>
    </Card>
  </div>
</template>

<script setup>
import { Card, Icon, Image } from 'vant'

defineProps({
  spots: {
    type: Array,
    default: () => [
      {
        id: 1,
        name: '西湖风景区',
        description: '杭州著名景点，湖光山色美不胜收',
        image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=%E6%9D%AD%E5%B7%9E%E8%A5%BF%E6%B9%96%E9%A3%8E%E6%99%AF%EF%BC%8C%E5%A4%96%E6%B1%AD%E6%98%8E%E6%9F%B3%E6%98%A0%E6%9C%88%EF%BC%8C%E5%8F%A4%E5%85%B8%E4%BA%AD%E5%8F%B0%EF%BC%8C%E8%BF%9C%E5%B1%B1%E5%A6%82%E7%94%BB%EF%BC%8C%E6%B8%85%E6%99%A8%E5%BE%AE%E9%9B%A8%EF%BC%8C%E9%AB%98%E8%80%83%E5%86%99%E5%AE%9E%E6%91%84%E5%BD%B1&image_size=landscape_4_3',
        price: 80,
        rating: '4.9',
        tag: '热门'
      },
      {
        id: 2,
        name: '故宫博物院',
        description: '北京明清两代皇家宫殿，世界文化遗产',
        image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=%E5%8C%97%E4%BA%AC%E6%95%85%E5%AE%AB%E5%8D%9A%E7%89%A9%E9%99%A2%EF%BC%8C%E7%BA%A2%E5%A2%99%E9%BB%84%E7%93%A6%EF%BC%8C%E4%B8%87%E5%A6%84%E6%97%A7%E5%AD%90%EF%BC%8C%E9%87%91%E5%9F%8E%E7%9A%87%E5%AE%AB%EF%BC%8C%E9%98%B3%E5%85%89%E8%BE%89%E7%85%A7%EF%BC%8C%E4%B8%AD%E5%A4%AE%E5%A4%AA%E5%92%8C%E6%AE%BF%EF%BC%8C%E9%AB%98%E8%80%83%E5%86%99%E5%AE%9E%E6%91%84%E5%BD%B1&image_size=landscape_4_3',
        price: 60,
        rating: '4.8',
        tag: '必去'
      },
      {
        id: 3,
        name: '张家界国家森林公园',
        description: '奇峰异石，云雾缭绕，自然风光壮丽',
        image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=%E5%BC%A0%E5%AE%B6%E7%95%8C%E5%9B%BD%E5%AE%B6%E6%A3%AE%E6%9E%97%E5%85%AC%E5%9B%AD%EF%BC%8C%E5%A5%87%E5%B3%B0%E5%BC%82%E7%9F%B3%EF%BC%8C%E4%BA%91%E6%B5%AE%E7%BC%95%E7%BB%95%EF%BC%8C%E7%AB%B9%E5%AD%90%E8%87%AA%E6%92%92%E7%9A%84%E7%9F%B3%E5%B3%B0%EF%BC%8C%E7%BB%BF%E8%89%B2%E6%A3%AE%E6%9E%97%EF%BC%8C%E9%98%B3%E5%85%89%E4%B8%8B%E5%8D%8A%E5%B1%B1%E4%BA%91%E6%B5%B7%EF%BC%8C%E9%AB%98%E8%80%83%E5%86%99%E5%AE%9E%E6%91%84%E5%BD%B1&image_size=landscape_4_3',
        price: 228,
        rating: '4.7',
        tag: '推荐'
      }
    ]
  }
})

defineEmits(['select'])
</script>

<style scoped>
.scenic-spot-card {
  margin: 12px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  font-weight: 600;
}

.spot-item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f2f3f5;
  cursor: pointer;
}

.spot-item--last {
  border-bottom: none;
}

.spot-cover {
  position: relative;
  flex-shrink: 0;
}

.spot-tag {
  position: absolute;
  top: 4px;
  left: 4px;
  background: linear-gradient(135deg, #ff7043, #ff5252);
  color: white;
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 4px;
}

.spot-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.spot-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.spot-name {
  font-size: 15px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.spot-rating {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 13px;
  color: #ffb300;
  flex-shrink: 0;
}

.spot-desc {
  font-size: 13px;
  color: #969799;
  margin: 4px 0;
}

.spot-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.spot-price {
  color: #ee0a24;
  font-size: 18px;
  font-weight: 600;
}

.price-unit {
  font-size: 12px;
  font-weight: 400;
}

.arrow-icon {
  color: #c8c9cc;
  font-size: 14px;
}
</style>

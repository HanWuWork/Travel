<template>
  <div class="scenic-spot-card">
    <Card>
      <template #title>
        <div class="card-title">
          <Icon name="fire-o" class="card-title__icon" />
          <span>热门景点</span>
          <span class="card-title__hint">左右滑动查看</span>
        </div>
      </template>
      <template #desc>
        <Empty v-if="!spots.length" description="暂无推荐景点" image-size="60" />
        <div v-else class="spot-scroll">
          <div
            v-for="spot in spots"
            :key="spot.id"
            class="spot-card"
            @click="$emit('select', spot)"
          >
            <!-- 景区图片；加载失败时回退为渐变占位（本地资源，直接加载） -->
            <img
              v-if="spot.image && !failed[spot.id]"
              class="spot-card__img"
              :src="spot.image"
              :alt="spot.name"
              decoding="async"
              @error="onError(spot.id)"
            />
            <div v-else class="spot-card__ph">{{ (spot.name || '景').slice(0, 1) }}</div>

            <div class="spot-card__tag">{{ spot.tag }}</div>

            <div class="spot-card__mask">
              <div class="spot-card__name">{{ spot.name }}</div>
              <div class="spot-card__meta">
                <span v-if="spot.cityName" class="spot-card__city">{{ spot.cityName }}</span>
                <span v-if="spot.rating" class="spot-card__rating">
                  <Icon name="good-job-o" /> {{ spot.rating }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </template>
    </Card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { Card, Empty, Icon } from 'vant'

/**
 * 热门景点：横向滚动的图片卡片
 * spots 由父组件传入（来自 /api/dest/hot-spots 的真实数据），
 * 保证「展示的景点」与「点击后打开的详情」是同一条记录。
 */
defineProps({
  spots: { type: Array, default: () => [] }
})

defineEmits(['select'])

/** 记录加载失败的图片 id，回退为渐变占位 */
const failed = reactive({})
const onError = (id) => { failed[id] = true }
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

.card-title__icon {
  color: var(--brand-deep);
}

.card-title__hint {
  margin-left: auto;
  font-size: 11px;
  font-weight: 400;
  color: var(--text-3);
}

/* 横向滚动容器：卡片向左溢出，右侧露出下一张的一部分 */
.spot-scroll {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  overflow-y: hidden;
  padding: 4px 2px 8px;
  margin: 0 -4px;
  scroll-snap-type: x mandatory;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
}

.spot-scroll::-webkit-scrollbar {
  display: none;
}

/* 单张图片卡片：宽度约 68%，形成「下一张露一角」的效果 */
.spot-card {
  position: relative;
  flex: 0 0 68%;
  max-width: 300px;
  height: 168px;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  scroll-snap-align: start;
  background: var(--surface-2);
  box-shadow: var(--shadow);
  transition: transform 0.18s ease;
}

.spot-card:active {
  transform: scale(0.98);
}

.spot-card__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

/* 无图占位 */
.spot-card__ph {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 44px;
  font-weight: 800;
  color: rgba(255, 255, 255, 0.9);
  background: linear-gradient(135deg, #2ba6d8, #04c489);
}

.spot-card__tag {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 1px 8px;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #e5484d, #c73b40);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.18);
}

/* 底部渐变遮罩 + 文字，保证图片上文字可读 */
.spot-card__mask {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 22px 10px 9px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.78) 0%, rgba(0, 0, 0, 0.45) 45%, rgba(0, 0, 0, 0) 100%);
  color: #fff;
}

.spot-card__name {
  font-size: 14px;
  font-weight: 700;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.spot-card__meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 3px;
  font-size: 11px;
  opacity: 0.92;
}

.spot-card__city {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.spot-card__rating {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  flex-shrink: 0;
  color: #ffd88a;
  font-weight: 600;
}
</style>

<template>
  <div class="quick-entry-card">
    <Card>
      <template #title>
        <div class="card-title">
          <Icon name="guide-o" />
          <span>旅游攻略</span>
        </div>
      </template>
      <template #desc>
        <div class="guide-grid">
          <div
            v-for="site in guideSites"
            :key="site.name"
            class="guide-item"
            @click="openSite(site.url)"
          >
            <div class="guide-logo">
              <img :src="site.icon" :alt="site.name" class="guide-icon" />
            </div>
            <span class="guide-name">{{ site.name }}</span>
          </div>
        </div>
      </template>
    </Card>
  </div>
</template>

<script setup>
import { Card, Icon } from 'vant'

const getFavicon = (domain) => `https://favicon.im/${domain}?larger=true`

const guideSites = [
  {
    name: '马蜂窝',
    url: 'https://www.mafengwo.cn',
    icon: getFavicon('mafengwo.cn')
  },
  {
    name: '携程',
    url: 'https://www.ctrip.com',
    icon: getFavicon('ctrip.com')
  },
  {
    name: '去哪儿',
    url: 'https://www.qunar.com',
    icon: getFavicon('qunar.com')
  },
  {
    name: '途牛',
    url: 'https://www.tuniu.com',
    icon: getFavicon('tuniu.com')
  },
  {
    name: '飞猪',
    url: 'https://www.fliggy.com',
    icon: getFavicon('fliggy.com')
  },
  {
    name: '穷游网',
    url: 'https://www.qyer.com',
    icon: getFavicon('qyer.com')
  },
  {
    name: '小红书',
    url: 'https://www.xiaohongshu.com',
    icon: getFavicon('xiaohongshu.com')
  },
  {
    name: '大众点评',
    url: 'https://www.dianping.com',
    icon: getFavicon('dianping.com')
  }
]

const openSite = (url) => {
  window.open(url, '_blank')
}
</script>

<style scoped>
.quick-entry-card {
  margin: 12px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  font-weight: 600;
}

.guide-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px 8px;
  padding: 8px 0;
}

.guide-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.guide-logo {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.guide-icon {
  width: 40px;
  height: 40px;
  object-fit: contain;
}

.guide-name {
  font-size: 12px;
  color: var(--text);
}
</style>

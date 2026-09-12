<script setup>
/**
 * 通用地图容器：基于 Leaflet 封装
 * - 底图使用高德栅格瓦片（GCJ-02，无需 Key），标记点自动做 WGS-84 → GCJ-02 转换
 * - 通过 props.markers 渲染标记，marker-click 事件交给父组件处理
 */
import * as L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { wgs84ToGcj02 } from '../utils/coord'

const props = defineProps({
  /** 地图中心 [lat, lng]（WGS-84） */
  center: { type: Array, default: () => [35.0, 105.0] },
  zoom: { type: Number, default: 11 },
  /** 标记数组：{id, lat, lng, color, badge, active, title} */
  markers: { type: Array, default: () => [] },
  /** 路线（WGS-84 点串），非空则绘制折线 */
  path: { type: Array, default: () => [] },
  pathColor: { type: String, default: '#0c0c0c' }
})

const emit = defineEmits(['marker-click'])

const mapEl = ref(null)
let map = null
let markerLayer = null
let pathLine = null

onMounted(() => {
  const [lat, lng] = wgs84ToGcj02(props.center[0], props.center[1])
  map = L.map(mapEl.value, {
    center: [lat, lng],
    zoom: props.zoom,
    zoomControl: false,
    attributionControl: false
  })
  L.tileLayer('https://webrd0{s}.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}', {
    subdomains: ['1', '2', '3', '4'],
    minZoom: 3,
    maxZoom: 18
  }).addTo(map)
  markerLayer = L.layerGroup().addTo(map)
  renderMarkers()
  renderPath()
  // 容器尺寸可能在挂载后才稳定，延迟校正一次
  setTimeout(() => map && map.invalidateSize(), 200)
})

onBeforeUnmount(() => {
  if (map) {
    map.remove()
    map = null
  }
})

watch(() => props.markers, renderMarkers, { deep: true })
watch(() => props.path, renderPath, { deep: true })

function renderMarkers() {
  if (!map || !markerLayer) return
  markerLayer.clearLayers()
  props.markers.forEach((m, idx) => {
    const [lat, lng] = wgs84ToGcj02(m.lat, m.lng)
    const badge = m.badge != null && m.badge !== '' ? m.badge : idx + 1
    const icon = L.divIcon({
      className: '',
      html: `<div class="tr-pin ${m.active ? 'tr-pin--active' : ''}" style="background:${m.color || '#0c0c0c'}"><span>${badge}</span></div>`,
      iconSize: [26, 26],
      iconAnchor: [13, 26]
    })
    const marker = L.marker([lat, lng], { icon, title: m.title || '' })
    marker.on('click', () => emit('marker-click', m))
    marker.addTo(markerLayer)
  })
}

function renderPath() {
  if (!map) return
  if (pathLine) {
    map.removeLayer(pathLine)
    pathLine = null
  }
  if (!props.path || props.path.length < 2) return
  const points = props.path.map((p) => wgs84ToGcj02(p.lat, p.lng))
  pathLine = L.polyline(points, { color: props.pathColor, weight: 3, opacity: 0.75, dashArray: '6 6' }).addTo(map)
}

/** 飞行到指定位置（WGS-84） */
function flyTo(lat, lng, zoom) {
  if (!map) return
  const [gLat, gLng] = wgs84ToGcj02(lat, lng)
  map.flyTo([gLat, gLng], zoom || map.getZoom(), { duration: 0.8 })
}

/** 自动缩放到包含所有标记点 */
function fitMarkers() {
  if (!map || !props.markers.length) return
  const bounds = L.latLngBounds(props.markers.map((m) => wgs84ToGcj02(m.lat, m.lng)))
  map.flyToBounds(bounds, { padding: [40, 40], duration: 0.8, maxZoom: 14 })
}

defineExpose({ flyTo, fitMarkers })
</script>

<template>
  <div ref="mapEl" class="map-container"></div>
</template>

<style scoped>
.map-container {
  width: 100%;
  height: 100%;
  background: #e8edf2;
}
</style>

<style>
/* 地图标记（divIcon 由 Leaflet 动态插入，不能使用 scoped） */
.tr-pin {
  width: 26px;
  height: 26px;
  border-radius: 50% 50% 50% 0;
  transform: rotate(-45deg);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
  border: 2px solid #fff;
  transition: transform 0.15s ease;
}

.tr-pin > span {
  transform: rotate(45deg);
  display: block;
  line-height: 22px;
  text-align: center;
}

.tr-pin--active {
  transform: rotate(-45deg) scale(1.25);
  z-index: 1000;
}

.leaflet-container {
  font-family: inherit;
}
</style>

/**
 * 主题管理：默认浅色（白天），可切换深色（黑色）
 * - 通过给 <html> 加 / 去 .dark 类切换
 * - 同时加上 Vant 官方暗色类 van-theme-dark，获得组件级暗色兜底
 * - 选择持久化在 localStorage
 */
const THEME_KEY = 'travel_theme'
const DARK = 'dark'
const LIGHT = 'light'

/** 读取已保存的主题；未设置时默认为浅色 */
export function getTheme() {
  return localStorage.getItem(THEME_KEY) === DARK ? DARK : LIGHT
}

export function isDark() {
  return getTheme() === DARK
}

/** 应用主题到 <html> */
export function applyTheme(theme) {
  const dark = theme === DARK
  const root = document.documentElement
  root.classList.toggle('dark', dark)
  root.classList.toggle('van-theme-dark', dark)
  // 移动端浏览器地址栏 / 状态栏配色跟随主题
  const meta = document.querySelector('meta[name="theme-color"]')
  if (meta) {
    meta.setAttribute('content', dark ? '#0b0b0b' : '#f4f7f8')
  }
}

/** 设置并持久化主题 */
export function setTheme(theme) {
  localStorage.setItem(THEME_KEY, theme === DARK ? DARK : LIGHT)
  applyTheme(theme)
}

/** 切换主题，返回切换后的主题名 */
export function toggleTheme() {
  const next = isDark() ? LIGHT : DARK
  setTheme(next)
  return next
}

/** 应用启动时调用：把已保存的主题同步到 DOM */
export function initTheme() {
  applyTheme(getTheme())
}

export { THEME_KEY }

/**
 * 行程导出工具：日历(.ics) 下载 与 分享海报(html2canvas) 生成
 */
import html2canvas from 'html2canvas'

/** 触发浏览器下载 */
function download(blob, filename) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  setTimeout(() => URL.revokeObjectURL(url), 2000)
}

const pad = (n) => String(n).padStart(2, '0')

/** yyyy-MM-dd → yyyymmdd */
function compact(dateStr) {
  return dateStr.replace(/-/g, '')
}

/** 日期加 n 天，返回 yyyy-MM-dd */
function addDays(dateStr, n) {
  const d = new Date(dateStr + 'T00:00:00')
  d.setDate(d.getDate() + n)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

/**
 * 导出行程为 .ics 日历文件（全天事件，逐日一条）
 * @param {Object} plan 行程数据
 */
export function exportIcs(plan) {
  if (!plan) return
  const baseDate = plan.startDate || new Date().toISOString().slice(0, 10)
  const stamp = new Date().toISOString().replace(/[-:]/g, '').split('.')[0] + 'Z'

  const lines = [
    'BEGIN:VCALENDAR',
    'VERSION:2.0',
    'PRODID:-//AI Travel Assistant//CN',
    'CALSCALE:GREGORIAN',
    'METHOD:PUBLISH'
  ]

  ;(plan.itinerary || []).forEach((item, idx) => {
    const start = addDays(baseDate, idx)
    const dayPois = (plan.pois || []).filter((p) => Number(p.day) === Number(item.day))
    const location = dayPois.map((p) => p.name).join('、')
    const desc = [item.description, item.tip ? `提示：${item.tip}` : ''].filter(Boolean).join('\\n')
    lines.push(
      'BEGIN:VEVENT',
      `UID:travel-${plan.id || 'new'}-${item.day}-${stamp}@travel.local`,
      `DTSTAMP:${stamp}`,
      `DTSTART;VALUE=DATE:${compact(start)}`,
      `DTEND;VALUE=DATE:${compact(addDays(start, 1))}`,
      `SUMMARY:${escapeIcs(`第${item.day}天 ${item.title || plan.destination}`)}`,
      `DESCRIPTION:${escapeIcs(desc)}`,
      location ? `LOCATION:${escapeIcs(location)}` : '',
      'END:VEVENT'
    )
  })

  lines.push('END:VCALENDAR')
  const content = lines.filter(Boolean).join('\r\n')
  download(new Blob([content], { type: 'text/calendar;charset=utf-8' }), `${plan.destination || '行程'}.ics`)
}

/** ics 文本转义 */
function escapeIcs(text) {
  return String(text || '')
    .replace(/\\/g, '\\\\')
    .replace(/;/g, '\\;')
    .replace(/,/g, '\\,')
    .replace(/\n/g, '\\n')
}

/**
 * 将指定 DOM 节点渲染为 PNG 并下载
 * @param {HTMLElement} el 海报容器
 * @param {string} filename 文件名
 */
export async function exportPoster(el, filename = 'travel-poster.png') {
  if (!el) return
  const canvas = await html2canvas(el, {
    backgroundColor: '#ffffff',
    scale: 2,
    useCORS: true,
    logging: false
  })
  await new Promise((resolve) => {
    canvas.toBlob((blob) => {
      if (blob) download(blob, filename)
      resolve()
    }, 'image/png')
  })
}

import { createApp } from 'vue'
import App from './App.vue'
import {
  Button,
  NavBar,
  Tabbar,
  TabbarItem,
  Swipe,
  SwipeItem,
  Grid,
  GridItem,
  Cell,
  CellGroup,
  Field,
  Image,
  Icon,
  Popup,
  Switch,
  NoticeBar,
  Loading,
  Empty
} from 'vant'
import 'vant/lib/index.css'
// 主题样式必须在 Vant 样式之后导入，否则 :root 变量会被 Vant 默认值覆盖
import './style.css'
import router from './router'
import { initTheme } from './utils/theme'

// 应用启动前同步已保存的主题（默认浅色）
initTheme()

createApp(App)
  .use(Button)
  .use(NavBar)
  .use(Tabbar)
  .use(TabbarItem)
  .use(Swipe)
  .use(SwipeItem)
  .use(Grid)
  .use(GridItem)
  .use(Cell)
  .use(CellGroup)
  .use(Field)
  .use(Image)
  .use(Icon)
  .use(Popup)
  .use(Switch)
  .use(NoticeBar)
  .use(Loading)
  .use(Empty)
  .use(router)
  .mount('#app')

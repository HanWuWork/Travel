import { createApp } from 'vue'
import './style.css'
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
  NoticeBar,
  Loading,
  Empty
} from 'vant'
import 'vant/lib/index.css'
import router from './router'

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
  .use(NoticeBar)
  .use(Loading)
  .use(Empty)
  .use(router)
  .mount('#app')

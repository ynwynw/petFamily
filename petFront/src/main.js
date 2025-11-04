import Vue from 'vue'
import App from './App.vue'
import ElementUI from "element-ui";
import router from "./router";
import store from "./store";
import '@/assets/css/init.css'
import "element-ui/lib/theme-chalk/index.css";
import "@/assets/css/scrollbar.css"
Vue.use(ElementUI);
Vue.config.productionTip = false
Vue.prototype.HOST = "/api"
new Vue({
  router,
  store,
  render: h => h(App),
}).$mount('#app')

router.afterEach((to, from, next) => {
  document.querySelector("body").setAttribute("style", "overflow: auto !important;")
});
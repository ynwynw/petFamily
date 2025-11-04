import Vue from 'vue'
import VueRouter, { RouteConfig } from 'vue-router'
import back from '../layout/back/index.vue';
import ShowView from '@/views/showView.vue';
import frontHome from '@/views/front/FrontHome.vue'
import Menu from '@/views/Menu.vue';
import petDetail from '@/components/front/petDetail.vue';
import AdoptPet from '@/views/AdoptPet.vue';
import petGoods from '@/views/GoodsBuy.vue';
Vue.use(VueRouter)

const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push(location) {
  return originalPush.call(this, location).catch(err => err)
}
const routes = [

  {
    path: '/',
    name: 'loginLayout',
    meta: { name: '登陆注册布局' },
    redirect: '/login',
    component: () => import('../layout/LoginLayout.vue'),
    children: [

      { path: '/login', name: 'Login', meta: { name: '登录' }, component: () => import('../views/Login.vue') },
      { path: '/forget', name: 'Forget', meta: { name: '找回密码' }, component: () => import('../views/Forget.vue') },
      {
        path: "/completeInfo",
        name: "CompleteInfo",
        meta: { name: '完善信息' },
        component: () => import('../views/CompleteInfo.vue')

      },
    ],

  },

  {
    path: '/front',
    name: 'front',
    redirect: '/frontHome',
    component: () => import('../layout/front/index.vue'),
    children: [
      {
        path: '/frontHome',
        name: 'frontHome',
        component: frontHome,

      },
      {
        path: '/petDetail/:id',
        name: 'petDetail',
        component: petDetail,

      },
      {
        path:'/petAdoptFront',
        name: 'petAdoptFront',
        component:AdoptPet,
      },
      {
        path:'/petGoods',
        name: 'petGoods',
        component:petGoods,
      },
      {
        // 服务
        path:'/petServices',
        name: 'petServices',
        component: () => import('../views/front/Service.vue'),
      },

    ]
  },

  {
    path: '/404',
    name: '404',
    component: () => import('../views/404.vue')
  },


]
const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})
export const setRoutes = () => {
  const userMenuListStr = sessionStorage.getItem("userMenuList")
  if (userMenuListStr) {
    const currentRoutes = router.getRoutes().map(v => v.name)
    if (!currentRoutes.includes('Layout')) {

      const libRouter = {
        path: '/',
        name: 'home',
        component: back,
        redirect: '/showView',  //重定向到homeView
        children: [
          {
            path: "/showView",
            name: "homeView",
            component: ShowView,
            meta: {
              title: "首页"
            },
          },
          {
            path: '/menu',
            name: 'Menu',
            component: Menu,
            meta: {
              title: "菜单管理"
            },
          },


        ]

      }


      const menus = JSON.parse(userMenuListStr);


      menus.forEach((item) => {
        if (item.path) {
          const componentPath = `../views/${item.pagePath}.vue`;
          const itemMenu = {
            path: item.path.replace("/", ""),
            name: item.name,
            component: () => import(`../views/${item.pagePath}.vue`),
            meta: {
              title: item.name
            }
          };
          libRouter.children.push(itemMenu);
        } else if (item.children?.length) {
          item.children.forEach((item) => {
            if (item.path) {
              const itemMenu = {
                path: item.path.replace("/", ""),
                name: item.name,
                component: () => import(`../views/${item.pagePath}.vue`),
                meta: {
                  title: item.name
                }
              };

              libRouter.children.push(itemMenu);
            }
          });
        }
      });

      router.addRoute(libRouter)

      console.log('1---重新加入manage路由')

    } else {
      console.log('未重新加入manage路由')

    }
  }

}
setRoutes()
// router.onError((error) => {
//   if (error.message.includes('Cannot find module')) {
//     // 如果错误是因为找不到模块，跳转到404页面
//     router.replace('/404');
//   }
// });
import ElementUI from "element-ui";
router.beforeEach((to, from, next) => {
  if (to.name) {
    localStorage.setItem('currentPathName', to.name);
  }

  const user = sessionStorage.getItem("user");

  if (!to.matched.length) {
    // 没有匹配到路由（也就是未找到路由)
    next('/404'); // 重定向到404页面
  } else {
    if (user) {
      // 用户已登录
      next(); // 允许访问
    } else {
      // 用户未登录
      if (to.path === '/login' || to.path === '/forget' || to.path === '/completeInfo') {
        // 允许访问登录、找回密码和完善信息页面
        next();
      } else {
        // 其他页面重定向到登录页面
        ElementUI.Message({
          message: '请先登录',
          type: 'warning'
        });
        next('/login');
      }
    }
  }
});


export default router
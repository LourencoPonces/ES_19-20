import Vue from 'vue';
import CKEditor from 'ckeditor4-vue';
import App from '@/App.vue';
import router from '@/router';
import store from '@/store';
import vuetify from '@/vuetify';

Vue.config.productionTip = false;

Vue.use(CKEditor);

new Vue({
  vuetify,
  router,
  store,
  render: h => h(App)
}).$mount('#app');

/** @format */

const path = require('path');
const CompressionPlugin = require('compression-webpack-plugin');
const TerserPlugin = require('terser-webpack-plugin');
const PurgecssPlugin = require('purgecss-webpack-plugin');
const CKEditorWebpackPlugin = require('@ckeditor/ckeditor5-dev-webpack-plugin');
const glob = require('glob-all');

const isProductionEnvFlag = process.env.NODE_ENV === 'production';

const { styles } = require('@ckeditor/ckeditor5-dev-utils');

let custompaths = {
  host: 'http://localhost:8081/',
  port: 8081,
  publicdomain: 'localhost:8081'
};

module.exports = {
  publicPath: '/',
  outputDir: 'dist',
  lintOnSave: true,
  runtimeCompiler: false,

  // generate sourceMap for production build?
  productionSourceMap: process.env.NODE_ENV !== 'production',

  chainWebpack: config => {
    const svgRule = config.module.rule('svg');

    svgRule.uses.clear();
    svgRule.use('raw-loader').loader('raw-loader');

    //svgRule.exclude.add(path.join(__dirname, 'node_modules', '@ckeditor'));

    /*config.module
      .rule('cke-svg')
      .test(/ckeditor5-[^/\\]+[/\\]theme[/\\]icons[/\\][^/\\]+\.svg$/)
      .use('raw-loader')
      .loader('raw-loader');*/

    config.module
      .rule('cke-css')
      .test(/ckeditor5-[^/\\]+[/\\].+\.css$/)
      .use('postcss-loader')
      .loader('postcss-loader')
      .tap(() => {
        return styles.getPostCssConfig({
          themeImporter: {
            themePath: require.resolve('@ckeditor/ckeditor5-theme-lark')
          },
          minify: true
        });
      });

    config.resolve.alias
      .set('vue$', 'vue/dist/vue.esm.js')
      .set('@assets', path.join(__dirname, 'src/assets'))
      .set('@models', path.join(__dirname, 'src/models'))
      .set('@plugins', path.join(__dirname, 'src/plugins'))
      .set('@services', path.join(__dirname, 'src/services'))
      .set('@components', path.join(__dirname, 'src/components'))
      .set('@css', path.join(__dirname, 'src/css'))
      .set('@views', path.join(__dirname, 'src/views'));

    const splitOptions = config.optimization.get('splitChunks');
    config.optimization.splitChunks(
      Object.assign({}, splitOptions, {
        maxAsyncRequests: 16,
        maxInitialRequests: 5,
        minChunks: 1,
        minSize: 50000,
        maxSize: 245000,
        automaticNameDelimiter: '~',
        name: true,
        cacheGroups: {
          default: false,
          common: {
            name: 'chunk-common',
            minChunks: 2,
            priority: -20,
            chunks: 'initial',
            reuseExistingChunk: true
          },
          element: {
            name: 'element',
            test: /[\\/]node_modules[\\/]element-ui[\\/]/,
            chunks: 'initial',
            priority: -30
          }
        }
      })
    );

    // https://github.com/webpack-contrib/webpack-bundle-analyzer
    if (process.env.npm_config_report) {
      config
        .plugin('webpack-bundle-analyzer')
        .use(require('webpack-bundle-analyzer').BundleAnalyzerPlugin);
    }
  },

  transpileDependencies: ['vuetify', /ckeditor5-[^/\\]+[/\\]src[/\\].+\.js$/],
  configureWebpack: {
    plugins: [
      // https://github.com/FullHuman/purgecss/issues/67
      isProductionEnvFlag
        ? new PurgecssPlugin({
            paths: glob.sync(
              [
                path.join(__dirname, './public/index.html'),
                path.join(__dirname, './**/*.vue'),
                path.join(__dirname, './src/**/*.js'),
                path.join(__dirname, './node_modules/vuetify/src/**/*.ts'),
                path.join(
                  __dirname,
                  './node_modules/vue-ctk-date-time-picker/**/*.js'
                )
              ],
              { nodir: true }
            )
          })
        : () => {},
      isProductionEnvFlag ? new CompressionPlugin() : () => {},
      isProductionEnvFlag ? new TerserPlugin() : () => {},
      new CKEditorWebpackPlugin({
        buildAllTranslationsToSeparateFiles: true,
        language: 'en'
      })
    ]
  },

  // use thread-loader for babel & TS in production build
  // enabled by default if the machine has more than 1 cores
  parallel: require('os').cpus().length > 1,

  // options for the PWA plugin.
  // see => https://github.com/vuejs/vue-cli/tree/dev/packages/%40vue/cli-plugin-pwa
  // https://developers.google.com/web/tools/workbox/modules/workbox-webpack-plugin
  /*  pwa: {
    name: "Vue-Cli3 实践",
    themeColor: "#4DBA87",
    msTileColor: "#000000",
    appleMobileWebAppCapable: "yes",
    appleMobileWebAppStatusBarStyle: "black",
    iconPaths: {
      favicon32: "img/icons/fuji-mountain-32x32.png",
      favicon16: "img/icons/fuji-mountain-16x16.png",
      appleTouchIcon: "img/icons/apple-touch-icon-152x152.png",
      maskIcon: "img/icons/safari-pinned-tab.svg",
      msTileImage: "img/icons/msapplication-icon-144x144.png"
    },
    // configure the workbox plugin (GenerateSW or InjectManifest)
    workboxPluginMode: "InjectManifest",
    workboxOptions: {
      // swSrc is required in InjectManifest mode.
      swSrc: "public/service-worker.js"
      // ...other Workbox options...
    }
  },*/

  // configure webpack-dev-server behavior
  devServer: {
    allowedHosts: ['.localhost', 'localhost', '127.0.0.1'],
    open: process.platform === 'darwin',
    host: '0.0.0.0',
    port: custompaths.port,
    public: custompaths.publicdomain,
    publicPath: custompaths.host,
    https: false,
    hotOnly: false,
    disableHostCheck: true,
    proxy: null, // string | Object
    before: app => {}
  },

  // options for 3rd party plugins
  pluginOptions: {}
};

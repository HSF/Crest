process.env.VUE_APP_CRESTAPI_URL = (typeof process.env.VUE_APP_CRESTAPI_URL != 'undefined') ? process.env.VUE_APP_CRESTAPI_URL : 'http://localhost:8090'
process.env.VUE_APP_BASEURL = (typeof process.env.VUE_APP_BASEURL != 'undefined') ? process.env.VUE_APP_BASEURL : '/ext/web/crestui/'

module.exports = {
  publicPath: process.env.NODE_ENV === 'production' ? process.env.VUE_APP_BASEURL : '/crestui',
  devServer: {
    proxy: {
      '^/crestui': {
        target: process.env.VUE_APP_CRESTAPI_URL,
        ws: true,
        changeOrigin: true
      },
      '^/ext': {
        target: process.env.VUE_APP_CRESTAPI_URL,
        ws: true,
        changeOrigin: true
      }
    }
  },
}

process.env.VUE_APP_CRESTAPI_URL = (typeof process.env.VUE_APP_API_URL != 'undefined') ? process.env.VUE_APP_API_URL : 'http://localhost:8090'
//process.env.VUE_APP_CRESTBASE_URL = (typeof process.env.VUE_APP_BASEURL != 'undefined') ? process.env.VUE_APP_BASEURL : '/ext/web/crestui/'

module.exports = {
  publicPath: process.env.NODE_ENV === 'production' ? '/ext/web/crestui/' : '/crestui',
  devServer: {
    proxy: {
      '^/crestapi': {
        target: process.env.VUE_APP_CRESTAPI_URL,
        secure: false,
        changeOrigin: true,
        headers: {
          Connection: 'keep-alive'
        }
      },
      '^/api': {
        target: process.env.VUE_APP_CRESTAPI_URL,
        secure: false,
        changeOrigin: true,
        headers: {
          Connection: 'keep-alive'
        }
      },
      '^/crestui': {
        target: process.env.VUE_APP_CRESTBASE_URL,
        ws: true,
        changeOrigin: true
      },
      '^/ext': {
        target: process.env.VUE_APP_CRESTBASE_URL,
        ws: true,
        changeOrigin: true
      }
    }
  },
}

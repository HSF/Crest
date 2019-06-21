process.env.VUE_APP_CRESTAPI_URL = (typeof process.env.VUE_APP_CRESTAPI_URL != 'undefined') ? process.env.VUE_APP_CRESTAPI_URL : 'http://localhost:8090'

module.exports = {
  /*baseUrl: process.env.NODE_ENV === 'production'
    ? process.env.VUE_APP_BASEURL
    : '/crestui'*/
    	publicPath: process.env.NODE_ENV === 'production' ? '/ext/web/crestui/' : '/crestui',
    			  devServer: {
    			    proxy: {
    			      '^/crestapi': {
    			        target: process.env.VUE_APP_CRESTAPI_URL,
    			        ws: true,
    			        changeOrigin: true
    			      }
    			    }
    			  },
}

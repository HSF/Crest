import tags from './db/tags'
import iovs from './db/iovs'
import payloads from './db/payloads'

export default {
	namespaced: true,
	modules: {
		tags,
		iovs,
		payloads
	},
}


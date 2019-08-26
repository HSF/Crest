import tags from './db/tags'
import iovs from './db/iovs'
import payloads from './db/payloads'
import globaltags from './db/globaltags'
import globaltagmaps from './db/globaltagmaps'
import folders from './db/folders'

export default {
	namespaced: true,
	modules: {
		tags,
		iovs,
		payloads,
		globaltags,
		globaltagmaps,
		folders
	},
}


class UrlMappings {

	static mappings = {
		"/ping"{
			controller = "ping"
			action = "ping"
		}
		"/check/$site/$city"{
			controller="Checker"
			action="checkSpelling"
		}
		"/suggestState/$site/$state"{
			controller="Checker"
			action="getStateCorrectionAndSuggestion"
		}
		"/suggestCity/$site/$stateHash/$city"{
			controller="Checker"
			action="getCityCorrectionAndSuggestion"
		}
		"/suggestStreet/$site/$stateHash/$cityHash/$street"{
			controller="Checker"
			action="getStreetCorrectionAndSuggestion"
		}
		"/"(view:"/index")
		"500"(view:'/error')
	}
}
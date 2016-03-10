package checker

import com.google.gson.Gson
import com.googlecode.concurrenttrees.common.Iterables;
import com.meli.spelling.checker.SpellingCorrector
import com.meli.spelling.checker.entities.Suggestion
import com.meli.spelling.suggestor.Suggestor;
import com.meli.spelling.utils.Utils;

/**
 * This controller handle request related to spelling checker and corrections.
 * 
 * @author Fabian Bertetto
 *
 */
class CheckerController {
	/**
	 * Some utilities for this class.
	 */
	private Utils utils = new Utils();

	/**
	 * Instance of Gson used internally on this class.
	 */
	private Gson gson = new Gson();

	/**
	 * Constructor for the controller, getting the instance of the Suggestor and the SpellingCorrector it initialize these objects.
	 */
	public CheckerController() {
		SpellingCorrector.getInstance();
		Suggestor.getInstance();
	}

	/**
	 * This method corrects the spelling of a word and return it spelling correctly.
	 * 
	 * @return correction.
	 */
	def checkSpelling() {
		String city = params.city;
		String site = params.site;
		SpellingCorrector sc = SpellingCorrector.getInstance();
		render (sc.correctSpelling(city, site));
	}

	/**
	 * Given an state on a site, returns a list of suggestions and a hashcode to get the cities in this state.
	 * 
	 * @return suggestions for the state
	 */
	def getStateCorrectionAndSuggestion() {
		String state = params.state;
		String site = params.site;
		Suggestor suggestor = Suggestor.getInstance();
		ArrayList<Suggestion> suggestions = suggestor.getSuggestion(state, site, true);
		if (suggestions.size() > 0) {
			render (gson.toJson(suggestions));
		}
		else {
			SpellingCorrector corrector = SpellingCorrector.getInstance();
			String correctState = corrector.correctSpelling(state, site);
			render (gson.toJson(suggestor.getSuggestion(correctState, site, true)));
		}
	}

	/**
	 * Given a city on a state, returns a list of suggestions and a hashcode to get the streets in this city.
	 * 
	 * @return suggestions for the city.
	 */
	def getCityCorrectionAndSuggestion() {
		String state = params.stateHash;
		String city = params.city;
		String site = params.site;
		Suggestor suggestor = Suggestor.getInstance();
		ArrayList<Suggestion> suggestions = suggestor.getSuggestion(city, state, true);
		if (suggestions.size() > 0) {
			render (gson.toJson(suggestions));
		}
		else {
			SpellingCorrector corrector = SpellingCorrector.getInstance();
			String correctCity = corrector.correctSpelling(city, site);
			render (gson.toJson(suggestor.getSuggestion(correctCity, state, true)));
		}
	}

	/**
	 * Given an street on a city, returns a list of suggestions for the street name.
	 * 
	 * @return suggestions for the street.
	 */
	def getStreetCorrectionAndSuggestion() {
		String city = params.cityHash;
		String state = params.stateHash;
		String street = params.street;
		String site = params.site;
		Suggestor suggestor = Suggestor.getInstance();
		ArrayList<Suggestion> suggestions = suggestor.getSuggestion(street,state+"-"+city, false);
		if (suggestions.size() > 0) {
			render (gson.toJson(suggestions));
		}
		else {
			SpellingCorrector corrector = SpellingCorrector.getInstance();
			String correctStreet = corrector.correctSpelling(street, site);
			render (gson.toJson(suggestor.getSuggestion(correctStreet, state+"-"+city, false)));
		}
	}
}

package com.meli.spelling.suggestor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import com.meli.spelling.utils.Utils;
import com.googlecode.concurrenttrees.common.Iterables;
import com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharArrayNodeFactory;
import com.googlecode.concurrenttrees.suffix.ConcurrentSuffixTree;
import com.googlecode.concurrenttrees.suffix.SuffixTree;
import com.meli.spelling.checker.entities.Suggestion;
import com.meli.spelling.config.CheckerConfig;

/**
 * This class provides a suggestor for states, cities and cities. It is trained
 * when the class is instantiated using some configuration parameters.
 * 
 * @author Fabian Bertetto
 *
 */
public class Suggestor {
	/**
	 * Directory that contains files with name of the cities separated by site
	 * (MLA).
	 */
	private final static String DIR_STATES = CheckerConfig
			.getDirSuggestorNamesStates();

	/**
	 * Directory that contains files with name of the cities separated by state
	 * (Cordoba, Buenos Aires).
	 */
	private final static String DIR_CITIES = CheckerConfig
			.getDirSuggestorNamesCities();

	/**
	 * Directory that contains files with name of the streets separated by city
	 * (Rio Cuarto, Ushuaia).
	 */
	private final static String DIR_STREETS = CheckerConfig
			.getDirSuggestorNamesStreets();
	/**
	 * Unique instance of the class.
	 */
	private static final Suggestor INSTANCE = new Suggestor();

	/**
	 * Map containing Suggestion Trees by context
	 */
	HashMap<String, SuffixTree<Integer>> suggestionTreesMap = new HashMap<String, SuffixTree<Integer>>();

	/**
	 * Some utilities.
	 */
	private Utils utils = new Utils();

	/**
	 * Logger utility
	 */
	Logger log = Logger.getLogger("Logger");

	/**
	 * Constructor of the class. It will train the corrector if it is not
	 * initiated;
	 */
	private Suggestor() {
		if (INSTANCE != null) {
			throw new IllegalStateException("Already Instantiated");
		} else {
			train();
		}
	}

	/**
	 * Gets trained instance of the class.
	 * 
	 * @return SpellingCorrector.
	 */
	public static Suggestor getInstance() {
		return INSTANCE;
	}

	/**
	 * Trains the suggestor with the corresponding names.
	 */
	public void train() {
		trainStates();
		trainCities();
		trainStreets();
	}

	/**
	 * Trains the suggestor with the corresponding cities on the specified
	 * directory.
	 */
	private void trainCities() {
		File dir = new File(DIR_CITIES);
		File[] directoryListing = dir.listFiles();
		for (File child : directoryListing) {
			SuffixTree<Integer> suggestorTree = new ConcurrentSuffixTree<Integer>(
					new DefaultCharArrayNodeFactory());
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(child));
				String line;
				int entry = 0;
				while ((line = br.readLine()) != null) {
					entry++;
					suggestorTree.put(line.toLowerCase(), entry);
				}
				int code = utils
						.firstLetterUpperCaseOfEachWord(child.getName())
						.hashCode();
				code = (code < 0 ? -code : code);
				System.out.println("STATE: " + String.valueOf(code) + " - "
						+ child.getName());
				suggestionTreesMap.put(String.valueOf(code), suggestorTree);
			} catch (Exception e) {
				log.info("Could not finish trainning of cities on state:"
						+ child);
			}
		}
	}

	/**
	 * Trains the suggestor with the corresponding states on the specified
	 * directory.
	 */
	private void trainStates() {
		File dir = new File(DIR_STATES);
		File[] directoryListing = dir.listFiles();
		for (File child : directoryListing) {
			SuffixTree<Integer> suggestorTree = new ConcurrentSuffixTree<Integer>(
					new DefaultCharArrayNodeFactory());
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(child));
				String line;
				int entry = 0;
				while ((line = br.readLine()) != null) {
					entry++;
					suggestorTree.put(line.toLowerCase(), entry);
				}
				suggestionTreesMap.put(child.getName(), suggestorTree);
			} catch (Exception e) {
				log.info("Could not finish trainning of states on site:"
						+ child);
			}
		}
	}

	/**
	 * Trains the suggestor with the corresponding streets on the specified
	 * directory.
	 */
	private void trainStreets() {
		File dir = new File(DIR_STREETS);
		File[] states = dir.listFiles();
		for (File state : states) {
			File[] cities = state.listFiles();
			for (File city : cities) {
				SuffixTree<Integer> suggestorTree = new ConcurrentSuffixTree<Integer>(
						new DefaultCharArrayNodeFactory());
				BufferedReader br = null;
				try {
					br = new BufferedReader(new FileReader(city));
					String line;
					int entry = 0;
					while ((line = br.readLine()) != null) {
						entry++;
						try {
							suggestorTree.put(line.toLowerCase(), entry);
						} catch (Exception e) {
							log.info("Error adding street:" + line + city + "-"
									+ state);
						}
					}
					int cityCode = utils.firstLetterUpperCaseOfEachWord(
							city.getName()).hashCode();
					int stateCode = utils.firstLetterUpperCaseOfEachWord(
							state.getName()).hashCode();
					cityCode = (cityCode < 0 ? -cityCode : cityCode);
					stateCode = (stateCode < 0 ? -stateCode : stateCode);
					System.out.println("CITY: " + String.valueOf(cityCode)
							+ " - " + city.getName());
					suggestionTreesMap.put(
							stateCode + "-" + String.valueOf(cityCode),
							suggestorTree);
				} catch (Exception e) {
					log.info("Could not finish trainning of streets on" + city
							+ "-" + state);
				}
			}
		}
	}

	/**
	 * Gets a list with all possible suggestions for this key.
	 * 
	 * @param key
	 *            to get suggestions.
	 * @param site
	 *            where it is contained (site, state, city)
	 * @param hash
	 *            , if the hashcode is needed.
	 * @return list of suggestions.
	 */
	public List<Suggestion> getSuggestion(String key, String site, boolean hash) {
		List<Suggestion> cities = new ArrayList<Suggestion>();
		try {
			List<CharSequence> suggestions = Iterables
					.toList(suggestionTreesMap.get(site).getKeysContaining(
							key.toLowerCase()));
			if (hash) {
				for (CharSequence suggestion : suggestions) {
					Suggestion city = new Suggestion();
					city.setName(utils
							.firstLetterUpperCaseOfEachWord(suggestion
									.toString()));
					int code = city.getName().hashCode();
					code = (code < 0 ? -code : code);
					city.setHashCode(String.valueOf(code));
					cities.add(city);
				}
			} else {
				for (CharSequence suggestion : suggestions) {
					Suggestion city = new Suggestion();
					city.setName(utils
							.firstLetterUpperCaseOfEachWord(suggestion
									.toString()));
					cities.add(city);
				}
			}
		} catch (NullPointerException e) {
			log.info("Collection data was not found. " + site);
		}
		return cities;
	}
}

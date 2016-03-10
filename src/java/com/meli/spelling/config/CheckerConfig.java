package com.meli.spelling.config;

import grails.util.Holders;

/**
 * This class contains configuration for the solution (spelling corrector and
 * suggestor).
 * 
 * @author Fabian Bertetto.
 *
 */
public class CheckerConfig {

	private static final String DIR_SPELLING_NAMES = "com.meli.checker.spellingCorrector.names.cities";
	private static final String DIR_SUGGESTOR_NAMES_STREETS = "com.meli.checker.suggestor.names.streets";
	private static final String DIR_SUGGESTOR_NAMES_CITIES = "com.meli.checker.suggestor.names.cities";
	private static final String DIR_SUGGESTOR_NAMES_STATES = "com.meli.checker.suggestor.names.states";

	/**
	 * Gets Directory containing names for training the spelling corrector.
	 * 
	 * @return directory containing names for training spelling corrector.
	 */
	public static String getDirSpellingNames() {
		return (String) Holders.getFlatConfig().get(DIR_SPELLING_NAMES);
	}

	/**
	 * Gets Directory containing street names for training the suggestor.
	 * 
	 * @return directory containing street names for training the suggestor.
	 */
	public static String getDirSuggestorNamesStreets() {
		return (String) Holders.getFlatConfig()
				.get(DIR_SUGGESTOR_NAMES_STREETS);
	}

	/**
	 * Gets Directory containing city names for training the suggestor.
	 * 
	 * @return directory containing city names for training the suggestor.
	 */
	public static String getDirSuggestorNamesCities() {
		return (String) Holders.getFlatConfig().get(DIR_SUGGESTOR_NAMES_CITIES);
	}

	/**
	 * Gets Directory containing state names for training the suggestor.
	 * 
	 * @return directory containing state names for training the suggestor.
	 */
	public static String getDirSuggestorNamesStates() {
		return (String) Holders.getFlatConfig().get(DIR_SUGGESTOR_NAMES_STATES);
	}

}

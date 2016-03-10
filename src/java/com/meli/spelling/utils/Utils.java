package com.meli.spelling.utils;

import com.google.gson.Gson;
import com.meli.spelling.checker.entities.Suggestion;

/**
 * This class is used to put utils methods that are used in more than one part
 * of the solution. Should be separated and organized by type if it grows.
 * 
 * @author Fabian Bertetto
 *
 */
public class Utils {
	/**
	 * This method Marshals an object to a json structure.
	 * 
	 * @param Suggestion
	 *            .
	 * @return json that represents the object.
	 */
	public String marshallCity(Suggestion city) {
		Gson gson = new Gson();
		String json = gson.toJson(city);
		return json;
	}

	/**
	 * Given a string return the same information but first letter of each word
	 * in upper case.
	 * 
	 * @param string
	 *            to change.
	 * @return input with first letter of each word in upper case.
	 */
	public String firstLetterUpperCaseOfEachWord(String string) {
		String[] nameParts = string.split(" ");
		for (int i = 0; i < nameParts.length; i++) {
			nameParts[i] = (nameParts[i].charAt(0) + "").toUpperCase()
					+ nameParts[i].substring(1, nameParts[i].length());
			if (i < nameParts.length - 1) {
				nameParts[i] += " ";
			}
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < nameParts.length; i++) {
			sb.append(nameParts[i]);
		}
		return sb.toString();
	}
}

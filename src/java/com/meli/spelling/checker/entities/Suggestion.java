package com.meli.spelling.checker.entities;

/**
 * This class represents a suggestion with just the necessary attributes for
 * this solution.
 * 
 * @author Fabian Bertetto
 *
 */
public class Suggestion {

	/**
	 * Name of the suggestion.
	 */
	private String name;
	/**
	 * HashCode of the suggestion.
	 */
	private String hashCode;

	/**
	 * Gets Name of the suggestion.
	 * 
	 * @return name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets name of the suggestion.
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets HashCode.
	 * 
	 * @return HashCode.
	 */
	public String getHashCode() {
		return hashCode;
	}

	/**
	 * Sets HashCode.
	 * 
	 * @param hashCode
	 *            .
	 */
	public void setHashCode(String hashCode) {
		this.hashCode = hashCode;
	}

}

package com.meli.spelling.checker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;

import com.meli.spelling.config.CheckerConfig;

/**
 * This class provides an spelling corrector for states, cities and cities. It
 * is trained when the class is instantiated using some configuration
 * parameters.
 * 
 * @author Fabian Bertetto
 *
 */
public class SpellingCorrector {
	/**
	 * Directory that contains files with name of the cities separated by site.
	 */
	private final static String DIR_CITIES = CheckerConfig
			.getDirSpellingNames();
	/**
	 * Distance to calculate possible spelling corrections (2 is suggested when
	 * training).
	 */
	private static int editDistanceMax = 2;
	/**
	 * TODO check if we can delete this.
	 */
	private static int verbose = 2;
	/**
	 * Dictionary that contains both the original words and the deletes derived
	 * from them. A term might be both word and delete from another word at the
	 * same time. For space reduction a item might be either of type
	 * dictionaryItem or Int A dictionaryItem is used for word, word/delete, and
	 * delete with multiple suggestions. Int is used for deletes with a single
	 * suggestion (the majority of entries).
	 */
	private static HashMap<String, Object> dictionary = new HashMap<String, Object>();

	/**
	 * List of unique words. By using the suggestions (Int) as index for this
	 * list they are translated into the original String.
	 */
	private static List<String> wordlist = new ArrayList<String>();

	/**
	 * Maximum dictionary term length. This attribute is important to avoid
	 * overflow.
	 */
	public static int maxlength = 0;

	/**
	 * Unique instance of the class.
	 */
	private static final SpellingCorrector INSTANCE = new SpellingCorrector();

	/**
	 * Constructor of the class. It will train the corrector if it is not
	 * initiated;
	 */
	private SpellingCorrector() {
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
	public static SpellingCorrector getInstance() {
		return INSTANCE;
	}

	/**
	 * Logger utility
	 */
	Logger log = Logger.getLogger("Logger");

	/**
	 * for every word there all deletes with an edit distance of editDistanceMax
	 * created and added to the dictionary every delete entry has a suggestions
	 * list, which points to the original term(s) it was created from The
	 * dictionary may be dynamically updated (word frequency and new words) at
	 * any time by calling createDictionaryEntry
	 * 
	 * @param key
	 *            to insert on the dictionary.
	 * @param language
	 *            .
	 * @return success status.
	 */
	private static boolean CreateDictionaryEntry(String key, String language) {
		boolean result = false;
		dictionaryItem value = null;
		Object valueo;
		valueo = dictionary.get(language + key);
		if (valueo != null) {
			// int or dictionaryItem? delete existed before word!
			if (valueo instanceof Integer) {
				int tmp = ((Integer) valueo).intValue();
				value = new dictionaryItem();
				value.suggestions.add(tmp);
				dictionary.put(language + key, value);
			}
			// already exists:
			// 1. word appears several times
			// 2. word1==deletes(word2)
			else {
				value = (dictionaryItem) valueo;
			}
			// prevent overflow
			if (value.count < Integer.MAX_VALUE)
				value.count++;
		} else if (wordlist.size() < Integer.MAX_VALUE) {
			value = new dictionaryItem();
			value.count++;
			dictionary.put(language + key, value);

			if (key.length() > maxlength)
				maxlength = key.length();
		}

		// edits/suggestions are created only once, no matter how often word
		// occurs
		// edits/suggestions are created only as soon as the word occurs in the
		// corpus,
		// even if the same term existed before in the dictionary as an edit
		// from another word
		// a treshold might be specifid, when a term occurs so frequently in the
		// corpus that it is considered a valid word for spelling correction
		if (value.count == 1) {
			// word2index
			wordlist.add(key);
			int keyint = (int) (wordlist.size() - 1);

			result = true;

			// create deletes
			for (String delete : Edits(key, 0, new HashSet<String>())) {
				Object value2;
				value2 = dictionary.get(language + delete);
				if (value2 != null) {
					// already exists:
					// 1. word1==deletes(word2)
					// 2. deletes(word1)==deletes(word2)
					// int or dictionaryItem? single delete existed before!
					if (value2 instanceof Integer) {
						// transformes int to dictionaryItem
						int tmp = ((Integer) value2).intValue();
						;
						dictionaryItem di = new dictionaryItem();
						di.suggestions.add(tmp);
						dictionary.put(language + delete, di);
						if (!di.suggestions.contains(keyint))
							AddLowestDistance(di, key, keyint, delete);
					} else if (!((dictionaryItem) value2).suggestions
							.contains(keyint))
						AddLowestDistance((dictionaryItem) value2, key, keyint,
								delete);
				} else {
					dictionary.put(language + delete, keyint);
				}

			}
		}
		return result;
	}

	// create a frequency dictionary from a corpus
	/**
	 * Create a frequency dictionary from a corpus file.
	 * 
	 * @param corpus
	 *            file.
	 * @param language
	 *            .
	 */
	private void CreateDictionary(String corpus, String language) {
		File f = new File(corpus);
		if (!(f.exists() && !f.isDirectory())) {
			log.info("File not found: " + corpus);
			return;
		}

		log.info("Creating dictionary...");
		long startTime = System.currentTimeMillis();
		long wordCount = 0;

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(corpus));
			String line;
			while ((line = br.readLine()) != null) {

				String words[] = line.split(" ");
				for (String word : words) {
					if (CreateDictionaryEntry(word.toLowerCase(), language))
						wordCount++;
				}

				if (CreateDictionaryEntry(line.toLowerCase(), language))
					wordCount++;
			}

		} catch (Exception e) {
			log.info("There was a problem creating dictionary" + corpus);
		}
		long endTime = System.currentTimeMillis();
		log.info("\rDictionary: " + wordCount + " words, " + dictionary.size()
				+ " entries, edit distance=" + editDistanceMax + " in "
				+ (endTime - startTime) + "ms ");
	}

	/**
	 * save some time and space
	 * 
	 * @param item
	 *            .
	 * @param suggestion
	 *            .
	 * @param suggestionint
	 *            .
	 * @param delete
	 *            .
	 */
	private static void AddLowestDistance(dictionaryItem item,
			String suggestion, int suggestionint, String delete) {
		// remove all existing suggestions of higher distance, if verbose<2
		if ((verbose < 2)
				&& (item.suggestions.size() > 0)
				&& (wordlist.get(item.suggestions.get(0)).length()
						- delete.length() > suggestion.length()
						- delete.length()))
			item.suggestions.clear();
		// do not add suggestion of higher distance than existing, if verbose<2
		if ((verbose == 2)
				|| (item.suggestions.size() == 0)
				|| (wordlist.get(item.suggestions.get(0)).length()
						- delete.length() >= suggestion.length()
						- delete.length()))
			item.suggestions.add(suggestionint);
	}

	/**
	 * Edits possibility.
	 * 
	 * @param word
	 * @param editDistance
	 * @param deletes
	 * @return
	 */
	private static HashSet<String> Edits(String word, int editDistance,
			HashSet<String> deletes) {
		editDistance++;
		if (word.length() > 1) {
			for (int i = 0; i < word.length(); i++) {
				// delete ith character
				String delete = word.substring(0, i) + word.substring(i + 1);
				if (deletes.add(delete)) {
					// recursion, if maximum edit distance not yet reached
					if (editDistance < editDistanceMax)
						Edits(delete, editDistance, deletes);
				}
			}
		}
		return deletes;
	}

	/**
	 * Gets possible suggestions for an input in an specific language.
	 * 
	 * @param input
	 * @param language
	 * @param editDistanceMax
	 * @return
	 */
	private List<suggestItem> lookup(String input, String language,
			int editDistanceMax) {
		if (input.length() - editDistanceMax > maxlength)
			return new ArrayList<suggestItem>();

		List<String> candidates = new ArrayList<String>();
		HashSet<String> hashset1 = new HashSet<String>();

		List<suggestItem> suggestions = new ArrayList<suggestItem>();
		HashSet<String> hashset2 = new HashSet<String>();
		Object valueo;
		candidates.add(input);

		while (candidates.size() > 0) {
			String candidate = candidates.get(0);
			candidates.remove(0);
			nosort: {

				if ((verbose < 2)
						&& (suggestions.size() > 0)
						&& (input.length() - candidate.length() > suggestions
								.get(0).distance))
					break nosort;

				// read candidate entry from dictionary
				valueo = dictionary.get(language + candidate);
				if (valueo != null) {
					dictionaryItem value = new dictionaryItem();
					if (valueo instanceof Integer)
						value.suggestions.add(((Integer) valueo).intValue());
					else
						value = (dictionaryItem) valueo;

					// if count>0 then candidate entry is correct dictionary
					// term, not only delete item
					if ((value.count > 0) && hashset2.add(candidate)) {
						// add correct dictionary term term to suggestion list
						suggestItem si = new suggestItem();
						si.term = candidate;
						si.count = value.count;
						si.distance = input.length() - candidate.length();
						suggestions.add(si);
						// early termination
						if ((verbose < 2)
								&& (input.length() - candidate.length() == 0))
							break nosort;
					}
					Object value2;
					for (int suggestionint : value.suggestions) {
						String suggestion = wordlist.get(suggestionint);
						if (hashset2.add(suggestion)) {
							int distance = 0;
							if (suggestion != input) {
								if (suggestion.length() == candidate.length())
									distance = input.length()
											- candidate.length();
								else if (input.length() == candidate.length())
									distance = suggestion.length()
											- candidate.length();
								else {
									int ii = 0;
									int jj = 0;
									while ((ii < suggestion.length())
											&& (ii < input.length())
											&& (suggestion.charAt(ii) == input
													.charAt(ii)))
										ii++;
									while ((jj < suggestion.length() - ii)
											&& (jj < input.length() - ii)
											&& (suggestion.charAt(suggestion
													.length() - jj - 1) == input
													.charAt(input.length() - jj
															- 1)))
										jj++;
									if ((ii > 0) || (jj > 0)) {
										distance = DamerauLevenshteinDistance(
												suggestion.substring(ii,
														suggestion.length()
																- jj),
												input.substring(ii,
														input.length() - jj));
									} else
										distance = DamerauLevenshteinDistance(
												suggestion, input);
								}
							}

							if ((verbose < 2) && (suggestions.size() > 0)
									&& (suggestions.get(0).distance > distance))
								suggestions.clear();
							if ((verbose < 2) && (suggestions.size() > 0)
									&& (distance > suggestions.get(0).distance))
								continue;
							if (distance <= editDistanceMax) {
								value2 = dictionary.get(language + suggestion);
								if (value2 != null) {
									suggestItem si = new suggestItem();
									si.term = suggestion;
									si.count = ((dictionaryItem) value2).count;
									si.distance = distance;
									suggestions.add(si);
								}
							}
						}
					}
				}

				if (input.length() - candidate.length() < editDistanceMax) {
					if ((verbose < 2)
							&& (suggestions.size() > 0)
							&& (input.length() - candidate.length() >= suggestions
									.get(0).distance))
						continue;

					for (int i = 0; i < candidate.length(); i++) {
						String delete = candidate.substring(0, i)
								+ candidate.substring(i + 1);
						if (hashset1.add(delete))
							candidates.add(delete);
					}
				}
			}
		}

		if (verbose < 2)
			Collections.sort(suggestions, new Comparator<suggestItem>() {
				public int compare(suggestItem f1, suggestItem f2) {
					return -(f1.count - f2.count);
				}
			});
		else
			Collections.sort(suggestions, new Comparator<suggestItem>() {
				public int compare(suggestItem x, suggestItem y) {
					return ((2 * x.distance - y.distance) > 0 ? 1 : 0)
							- ((x.count - y.count) > 0 ? 1 : 0);
				}
			});
		if ((verbose == 0) && (suggestions.size() > 1))
			return suggestions.subList(0, 1);
		else
			return suggestions;
	}

	/**
	 * Correct an input in an specific language.
	 * 
	 * @param input
	 *            .
	 * @param language
	 *            .
	 * @return correction (just the word).
	 */
	public String correctSpelling(String input, String language) {
		try {
			List<suggestItem> suggestions = null;
			// check in dictionary for existence and frequency; sort by
			// ascending
			// edit distance, then by descending word frequency
			suggestions = lookup(input, language, 3);
			// display term and frequency
			for (suggestItem suggestion : suggestions) {
				return suggestion.term;
			}

			String[] words = input.split(" ");
			if (words.length > 1) {
				StringBuilder sb = new StringBuilder();
				for (String word : words) {
					sb.append((lookup(word, language, 2)).get(0).term);
					sb.append(" ");
				}
				suggestions = lookup(sb.toString(), language, 3);
				for (suggestItem suggestion : suggestions) {
					return suggestion.term;
				}
			}
		} catch (Exception e) {
			log.info("There was a problem trying to correct spelling of "
					+ input);
		}
		return input;
	}

	/**
	 * This method will train the spelling corrector.
	 */
	public void train() {
		File dir = new File(DIR_CITIES);
		File[] directoryListing = dir.listFiles();
		for (File child : directoryListing) {
			CreateDictionary(child.getAbsolutePath(),
					FilenameUtils.removeExtension(child.getName()));
		}
	}

	/**
	 * Damerau–Levenshtein distance algorithm and code from
	 * http://en.wikipedia.org/wiki/Damerau%E2%80%93Levenshtein_distance (as
	 * retrieved in June 2012)
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public int DamerauLevenshteinDistance(String a, String b) {
		final int inf = a.length() + b.length() + 1;
		int[][] H = new int[a.length() + 2][b.length() + 2];
		for (int i = 0; i <= a.length(); i++) {
			H[i + 1][1] = i;
			H[i + 1][0] = inf;
		}
		for (int j = 0; j <= b.length(); j++) {
			H[1][j + 1] = j;
			H[0][j + 1] = inf;
		}
		HashMap<Character, Integer> DA = new HashMap<Character, Integer>();
		for (int d = 0; d < a.length(); d++)
			if (!DA.containsKey(a.charAt(d)))
				DA.put(a.charAt(d), 0);

		for (int d = 0; d < b.length(); d++)
			if (!DA.containsKey(b.charAt(d)))
				DA.put(b.charAt(d), 0);

		for (int i = 1; i <= a.length(); i++) {
			int DB = 0;
			for (int j = 1; j <= b.length(); j++) {
				final int i1 = DA.get(b.charAt(j - 1));
				final int j1 = DB;
				int d = 1;
				if (a.charAt(i - 1) == b.charAt(j - 1)) {
					d = 0;
					DB = j;
				}
				H[i + 1][j + 1] = min(H[i][j] + d, H[i + 1][j] + 1,
						H[i][j + 1] + 1, H[i1][j1] + ((i - i1 - 1)) + 1
								+ ((j - j1 - 1)));
			}
			DA.put(a.charAt(i - 1), i);
		}
		return H[a.length() + 1][b.length() + 1];
	}

	/**
	 * Just method to get minimum between four numbers (used internally).
	 * 
	 * @return minumun number.
	 */
	private int min(int a, int b, int c, int d) {
		return Math.min(a, Math.min(b, Math.min(c, d)));
	}

	/**
	 * Internal class that represents an item of the dictionary.
	 */
	private static class dictionaryItem {
		public List<Integer> suggestions = new ArrayList<Integer>();
		public int count = 0;
	}

	/**
	 * Internal class that represents an item to suggest.
	 */
	private static class suggestItem {
		public String term = "";
		public int distance = 0;
		public int count = 0;

		@Override
		public boolean equals(Object obj) {
			return term.equals(((suggestItem) obj).term);
		}

		@Override
		public int hashCode() {
			return term.hashCode();
		}
	}
}
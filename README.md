===============
Spelling Checker
===============

This is an spelling corrector and also provides autocompletion for states, cities and streets.

**Use the API**
____

Get suggestions for a state
-

* URL

**/suggestState/{site}/{state}**

* Method

"GET"

* URL PARAMETERS

Required:

    * site: this is the site id. (MLA)

    * state: key word to get suggestions.

* SUCCESS RESPONSE

Code: 200

This will return a list of suggestions with the following structure.

      [{"name":"stateSuggestion1","hashCode":"658176199"},{"name":"stateSuggestion2","hashCode":"658176198"}]

----

Get suggestions for a city
-

* URL

**/suggestCity/{site}/{stateHash}/{city}**

* Method

"GET"

* URL PARAMETERS

Required:

    * site: this is the site id. (MLA)

    * stateHash: hash that represents the state where to get suggestions for the city.

    * city: key word to get suggestions.

* SUCCESS RESPONSE

Code: 200

This will return a list of suggestions with the following structure.

      [{"name":"cityCuggestion1","hashCode":"1818586562"},{"name":"cityCuggestion2","hashCode":"1818586563"}]

----

Get suggestions for a street
-

* URL

**/suggestStreet/{site}/{stateHash}/{cityHash}/{street}**

* Method

"GET"

* URL PARAMETERS

Required:

    * site: this is the site id. (MLA)

    * stateHash: hash that represents the state where to get suggestions for the city.

    * cityHash: hash that represents the city where to get suggestions for the street.

    * street: key word to get suggestions.

* SUCCESS RESPONSE

Code: 200

This will return a list of suggestions with the following structure.

      [{"name":"streetCuggestion1"},{"name":"streetCuggestion2"}]

----

* NOTES

If the collection where you are trying to get states, cities or streets does not existit will retrun an empty list. The same as if it doesn't have any object.

* Questions?

Ask fabianbertetto@gmail.com

Pull requests are welcome

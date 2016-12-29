# Verily

Map validation library for Clojure and ClojureScript

## Why

Most other validation libraries (such as [valip](https://github.com/weavejester/valip)) assume validation is done per key, rather than on a map as a whole. This makes it difficult to write multi-key validations. Validation functions in Verily take an entire map, making multi-key (or single-key) validations easy.

Verily also provides a declarative, data-oriented API in addition to a conventional, function-oriented API. Using data to describe validations has benefits in certain use cases. For instance, you can share validation specifications between Clojure and ClojureScript.

## Installation

Leiningen:

```clj
[jkkramer/verily "0.6.0"]
```

## Usage

```clj
(ns example.core
  (:require [jkkramer.verily :as v]))
```

There are two ways to validate a map:

1. With a validation specification - i.e., data, which Verily turns into a validation function for you
2. With a validation function

To validate using a validation specification:

```clj
(def validations
  [[:required [:foo :bar :password]]
   [:equal [:password :confirm-password] "Passwords don't match, dummy"]
   [:min-length 8 :password]])

(v/validate {:foo "foo"
             :password "foobarbaz"
             :password-confirm "foobarba"}
            validations)
;; Returns:
({:keys (:bar), :msg "must not be blank"}
 {:keys [:password :confirm-password], :msg "Passwords don't match, dummy"})
```

To turn a validation specification into a function yourself:

```clj
(def validator (v/validations->fn validations))
```

You can use validation functions instead of data if you prefer. This makes it easier to write your own:

```clj
;; Custom validator
(defn validate-password [m]
  (when (#{"12345" "password" "hunter2"} (:password m))
    {:keys [:password] :msg "You can't use that password"}))

;; Combine several built-in validators and our own custom one
(def validator
  (v/combine
    (v/required [:foo :bar :password])
    (v/equal [:password :password-confirm])
    (v/min-length 8 :password)
    validate-password))

(validator {:foo "foo"
            :password "foobarbaz"
            :password-confirm "foobarba"})
;; Returns:
({:keys (:bar), :msg "must not be blank"}
 {:keys [:password :password-confirm], :msg "must be equal"})
```

### Validation Function Contract

The contract for a validation function is:

* Take a map as an argument
* If validation succeeded, return nil or an empty collection. If there was a problem, return a problem map or collection of problem maps. Each problem is a map with the following keys:
	* `:keys` (optional) - relevant map keys
	* `:msg` - description of the problem


### Built-in Validations

All validation specifications accept a key or sequence of keys. The message is always optional.

Keys can be dotted keywords such as `:foo.bar`, which will be interpreted as a nested path (if the `:foo.bar` key does not exist in the top-level map). Strings such as "foo[bar]" are also allowed, and have the same semantics.

Unless `:required` is used, all validations allow the keys to be absent from the map, or have a `nil` value (or blank if a string-based type).

* `:required <keys> [msg]` - must not be absent, blank, or nil
* `:contains <keys> [msg]` - must not be absent, but can be blank or nil
* `:not-blank <keys> [msg]` - may be absent but not blank or nil
* `:exact <value> <keys> [msg]` - must be a particular value
* `:equal <keys> [msg]` - all keys must be equal
* `:email <keys> [msg]` - must be a valid email
* `:url <keys> [msg]` - must be a valid URL
* `:web-url <keys> [msg]` - must be a valid website URL (http or https)
* `:link-url <keys> [msg]` - must be a valid link URL (can be relative, http: or https: may be omitted)
* `:matches <regex> <keys> [msg]` - must match a regular expression
* `:min-length <len> <keys> [msg]` - must be a certain length (for strings or collections)
* `:max-length <len> <keys> [msg]` - must not exceed a certain length (for strings or collections)
* `:complete <keys> [msg]` - must be a collection with no blank or nil values
* `:min-val <min> <keys> [msg]` - must be at least a certain value
* `:max-val <max> <keys> [msg]` - must be at most a certain value
* `:within <min> <max> <keys> [msg]` - must be within a certain range (inclusive)
* `:positive <keys> [msg]` - must be a positive number
* `:negative <keys> [msg]` - must be a negative number
* `:after <date> <keys> [msg]` - must be after a certain date
* `:before <date> <keys> [msg]` - must be before a certain date
* `:in <coll> <keys> [msg]` - must be contained within a collection
* `:every-in <coll> <keys> [msg]` - each value must be within a collection (for values that are themselves collections)
* `:us-zip <keys> [msg]` - must be a valid US zip code
* `:luhn <keys> [msg]` - must be pass the Luhn check (e.g., for credit card numbers)
* Datatype validations: `:string`, `:boolean`, `:integer`, `:float`, `:decimal`, `:date` (plus aliases)
* Datatype collection validations: `:strings`, `:booleans`, `:integers`, `:floats`, `:decimals`, `:dates` (plus aliases)

All validation specifications have corresponding validator functions in the `jkkramer.verily` namespace, if you prefer to use those directly.


## License

Copyright © 2012-2013 Justin Kramer

Distributed under the Eclipse Public License, the same as Clojure.

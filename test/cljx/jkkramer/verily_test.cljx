(ns jkkramer.verily-test
  #+cljs (:require-macros [cemerick.cljs.test :refer [is are deftest run-tests]])
  (:require [jkkramer.verily :as v]
            #+cljs [cemerick.cljs.test :as t]
            #+clj [clojure.test :refer [is are deftest run-tests]])
  #+clj (:import java.util.Date))

(def m
  {:a 1
   :b 2
   :c 3
   :d "foo"
   :e "bar"
   :f ""
   :g nil
   :h " "
   :i "foo"
   :j [1 2 3]
   :k ["a" "b" "c"]
   :l []})

(deftest test-problems
  (is (= #{{:keys [:a] :msg "must be a string"}
           {:keys [:f :g] :msg "custom message"}
           {:msg "custom problem"}}
         (set
           ((v/combine
              (v/string :a)
              (v/required [:f :g] "custom message")
              (v/contains :x {:msg "custom problem"}))
             m)))))

(deftest test-contains
  (is (empty? ((v/contains :a) m)))
  (is (empty? ((v/contains [:a :c :e]) m)))
  (is (empty? ((v/contains :f) m)))
  (is (empty? ((v/contains :g) m)))
  (is (seq ((v/contains :x) m)))
  (is (seq ((v/contains [:a :x]) m))))

(deftest test-required
  (is (empty? ((v/required :a) m)))
  (is (empty? ((v/required [:a :c :e]) m)))
  (is (seq ((v/required :x) m)))
  (is (seq ((v/required [:a :x]) m)))
  (is (seq ((v/required :f) m)))
  (is (seq ((v/required :g) m))))

(deftest test-not-blank
  (is (empty? ((v/not-blank :a) m)))
  (is (empty? ((v/not-blank [:a :c :e]) m)))
  (is (empty? ((v/not-blank :x) m)))
  (is (empty? ((v/not-blank [:a :x]) m)))
  (is (seq ((v/not-blank :f) m)))
  (is (seq ((v/not-blank :g) m)))
  (is (seq ((v/not-blank :h) m))))

(deftest test-exact
  (is (empty? ((v/exact 1 :a) m)))
  (is (empty? ((v/exact "foo" :d) m)))
  (is (empty? ((v/exact "foo" [:d :i]) m)))
  (is (empty? ((v/exact "whatever" :x) m)))
  (is (empty? ((v/exact "" :f) m)))
  (is (empty? ((v/exact nil :g) m))))

(deftest test-equal
  (is (empty? ((v/equal [:d :i]) m)))
  (is (seq ((v/equal [:a :b]) m)))
  (is (seq ((v/equal [:f :g]) m)))
  (is (seq ((v/equal [:d :x]) m)))
  (is (empty? ((v/equal [:g :x]) m)))
  (is (empty? ((v/equal [:x :y]) m))))

(deftest test-matches
  (is (empty? ((v/matches #".oo" :d) m)))
  (is (seq ((v/matches #".oo" :a) m)))
  (is (empty? ((v/matches #".oo" :f) m)))
  (is (empty? ((v/matches #".oo" :g) m)))
  (is (empty? ((v/matches #".oo" :x) m))))

(deftest test-min-length
  (is (empty? ((v/min-length 3 :d) m)))
  (is (empty? ((v/min-length 3 [:d :e]) m)))
  (is (seq ((v/min-length 4 :d) m)))
  (is (seq ((v/min-length 3 :a) m)))
  (is (empty? ((v/min-length 3 :g) m)))
  (is (empty? ((v/min-length 3 :x) m))))

(deftest test-max-length
  (is (empty? ((v/max-length 3 :d) m)))
  (is (seq ((v/max-length 2 :d) m)))
  (is (seq ((v/max-length 3 :a) m)))
  (is (empty? ((v/max-length 3 :g) m)))
  (is (empty? ((v/max-length 3 :x) m))))

(deftest test-in
  (is (empty? ((v/in [1 2 3] :a) m)))
  (is (empty? ((v/in [1 2 3] [:a :b :c]) m)))
  (is (seq ((v/in [1 2 3] :d) m)))
  (is (empty? ((v/in [1 2 3] :g) m)))
  (is (empty? ((v/in [1 2 3] :x) m))))

(deftest test-us-zip
  (is (empty? ((v/us-zip :a) {:a "12345"})))
  (is (empty? ((v/us-zip :a) {:a "12345-6789"})))
  (is (seq ((v/us-zip :a) {:a "12345x"})))
  (is (seq ((v/us-zip :a) {:a 12345})))
  (is (empty? ((v/us-zip :a) {:a ""})))
  (is (empty? ((v/us-zip :a) {:a nil})))
  (is (empty? ((v/us-zip :x) m))))

(deftest test-email
  (is (empty? ((v/email :a) {:a "foo@bar.com"})))
  (is (empty? ((v/email :a) {:a "foo+baz@bar.com"})))
  (is (seq ((v/email :a) {:a "foo@"})))
  (is (seq ((v/email :a) {:a "@bar"})))
  (is (seq ((v/email :a) {:a "foobar"})))
  (is (empty? ((v/email :a) {:a nil})))
  (is (empty? ((v/email :a) {:a ""})))
  (is (empty? ((v/email :x) m))))

(deftest test-web-url
  (is (empty? ((v/web-url :a) {:a "http://example.com/"})))
  (is (empty? ((v/web-url :a) {:a "https://example.com/foo"})))
  (is (seq ((v/web-url :a) {:a "ftp://example.com/"})))
  (is (seq ((v/web-url :a) {:a "example.com/"})))
  (is (empty? ((v/web-url :a) {:a nil})))
  (is (empty? ((v/web-url :a) {:a ""})))
  (is (empty? ((v/web-url :x) m))))

(deftest test-url
  (is (empty? ((v/url :a) {:a "http://example.com/"})))
  (is (empty? ((v/url :a) {:a "https://example.com/foo"})))
  (is (empty? ((v/url :a) {:a "ftp://example.com/"})))
  (is (seq ((v/url :a) {:a "example.com/"})))
  (is (empty? ((v/url :a) {:a nil})))
  (is (empty? ((v/url :a) {:a ""})))
  (is (empty? ((v/url :x) m))))

(deftest test-string
  (is (empty? ((v/string :d) m)))
  (is (empty? ((v/string [:d :e]) m)))
  (is (seq ((v/string :a) m)))
  (is (seq ((v/string [:a :b]) m)))
  (is (empty? ((v/string :g) m)))
  (is (empty? ((v/string :x) m))))

(deftest test-strings
  (is (empty? ((v/strings :k) m)))
  (is (seq ((v/strings :j) m)))
  (is (seq ((v/strings :a) m)))
  (is (seq ((v/strings :f) m)))
  (is (empty? ((v/strings :g) m)))
  (is (empty? ((v/strings :l) m)))
  (is (empty? ((v/strings :x) m))))

(deftest test-bool
  (is (empty? ((v/bool :a) {:a true})))
  (is (empty? ((v/bool :a) {:a false})))
  (is (empty? ((v/bool :a) {:a nil})))
  (is (empty? ((v/bool :x) m)))
  (is (seq ((v/bool [:a :d :j :f]) m))))

(deftest test-bools
  (is (empty? ((v/bools :a) {:a [true true false]})))
  (is (empty? ((v/bools :a) {:a []})))
  (is (empty? ((v/bools :a) {:a nil})))
  (is (empty? ((v/bools :x) m)))
  (is (seq ((v/bools [:a :d :f :j :k]) m))))

(deftest test-integer
  #+clj (is (empty? ((v/integer [:a :b :c :d :e :f])
                      {:a 1 :b 1N :c (int 1)
                       :d (Byte. (byte 1))
                       :e (Short. (short 1))
                       :f (Integer. 1)})))
  (is (empty? ((v/integer :g) m)))
  (is (empty? ((v/integer :x) m)))
  #+clj (is (seq ((v/integer :a) {:a 1.0})))
  #+clj (is (seq ((v/integer :a) {:a 1.0M})))
  #+clj (is (seq ((v/integer :a) {:a (float 1.0)})))
  (is (seq ((v/integer :d) m)))
  (is (seq ((v/integer :f) m)))
  (is (seq ((v/integer :j) m)))
  (is (seq ((v/integer :k) m)))
  (is (seq ((v/integer :l) m))))

(deftest test-integers
  (is (empty? ((v/integers :j) m)))
  (is (empty? ((v/integers :g) m)))
  (is (empty? ((v/integers :l) m)))
  (is (empty? ((v/integers :x) m)))
  (is (seq ((v/integers [:d :f :k]) m))))

(deftest test-floating-point
  (is (empty? ((v/floating-point :a) {:a 1.1})))
  (is (empty? ((v/floating-point :a) {:a (float 1.1)})))
  #+clj (is (seq ((v/floating-point :a) {:a 1.1M})))
  (is (empty? ((v/floating-point :a) {:a nil})))
  (is (empty? ((v/floating-point :x) m)))
  (is (seq ((v/floating-point [:a :d :j :f]) m))))

(deftest test-floating-points
  (is (empty? ((v/floating-points :a) {:a [1.0 1.1 1.2]})))
  (is (empty? ((v/floating-points :a) {:a []})))
  (is (empty? ((v/floating-points :a) {:a nil})))
  (is (empty? ((v/floating-points :x) m)))
  (is (seq ((v/floating-points [:a :d :f :j :k]) m))))

(deftest test-decimal
  #+clj (is (empty? ((v/decimal :a) {:a 1.1M})))
  #+cljs (is (empty? ((v/decimal :a) {:a 1.1})))
  #+clj (is (seq ((v/decimal :a) {:a 1.1})))
  #+clj (is (seq ((v/decimal :a) {:a (float 1.1)})))
  (is (empty? ((v/decimal :a) {:a nil})))
  (is (empty? ((v/decimal :x) m)))
  (is (seq ((v/decimal [:a :d :j :f]) m))))

(deftest test-decimals
  #+clj (is (empty? ((v/decimals :a) {:a [1.0M 1.1M 1.2M]})))
  #+cljs (is (empty? ((v/decimals :a) {:a [1.0 1.1 1.2]})))
  (is (empty? ((v/decimals :a) {:a []})))
  (is (empty? ((v/decimals :a) {:a nil})))
  (is (empty? ((v/decimals :x) m)))
  (is (seq ((v/decimals [:a :d :f :j :k]) m))))

(deftest test-min-val
  #+clj (is (empty? ((v/min-val 3 [:a :b :c]) {:a 3 :b 3.0 :c 3.0M})))
  (is (seq ((v/min-val 3 :a) {:a 2})))
  (is (empty? ((v/min-val 3 :a) {:a 4})))
  (is (seq ((v/min-val 3 :a) {:a "3"})))
  (is (seq ((v/min-val 3 :a) {:a ""})))
  (is (empty? ((v/min-val 3 :g) m)))
  (is (empty? ((v/min-val 3 :x) m))))

(deftest test-max-val
  #+clj (is (empty? ((v/max-val 3 [:a :b :c]) {:a 3 :b 3.0 :c 3.0M})))
  (is (seq ((v/max-val 3 :a) {:a 4})))
  (is (empty? ((v/max-val 3 :a) {:a 2})))
  (is (seq ((v/max-val 3 :a) {:a "3"})))
  (is (seq ((v/max-val 3 :a) {:a ""})))
  (is (empty? ((v/max-val 3 :g) m)))
  (is (empty? ((v/max-val 3 :x) m))))

(deftest test-within
  (is (empty? ((v/within 1 10 [:a :b :c :d]) {:a 1 :b 9 :c 10 :d 5})))
  (is (seq ((v/within 1 10 :a) {:a 0})))
  (is (seq ((v/within 1 10 :a) {:a 11})))
  (is (seq ((v/within 1 10 :a) {:a ""})))
  (is (seq ((v/within 1 10 :a) {:a "1"})))
  (is (empty? ((v/within 1 10 :a) {:a nil})))
  (is (empty? ((v/within 1 10 :x) m))))

(deftest test-positive
  (is (empty? ((v/positive [:a :b :c :d]) #+clj {:a 3 :b 3.0 :c 3.0M :d 0.000001}
                                          #+cljs {:a 3 :b 3.0 :c 3.01 :d 0.000001})))
  (is (seq ((v/positive :a) {:a 0})))
  (is (seq ((v/positive :a) {:a -1})))
  (is (seq ((v/positive :a) {:a -1.0})))
  #+clj (is (seq ((v/positive :a) {:a -1.0M})))
  (is (seq ((v/positive :a) {:a "3"})))
  (is (seq ((v/positive :a) {:a ""})))
  (is (empty? ((v/positive 3 :g) m)))
  (is (empty? ((v/positive 3 :x) m))))

(deftest test-negative
  (is (seq ((v/negative [:a :b :c :d]) #+clj {:a 3 :b 3.0 :c 3.0M :d 0.000001}
                                       #+cljs {:a 3 :b 3.0 :c 3.01 :d 0.000001})))
  (is (seq ((v/negative :a) {:a 0})))
  (is (empty? ((v/negative :a) {:a -1})))
  (is (empty? ((v/negative :a) {:a -1.0})))
  #+clj (is (empty? ((v/negative :a) {:a -1.0M})))
  (is (seq ((v/negative :a) {:a "3"})))
  (is (seq ((v/negative :a) {:a ""})))
  (is (empty? ((v/negative 3 :g) m)))
  (is (empty? ((v/negative 3 :x) m))))

(defn- mkdate
  ([]
    #+clj (Date.) #+cljs (js/Date.))
  ([y m d]
    #+clj (Date. (+ y 1900) (dec m) d)
    #+cljs (doto (js/Date.) (.setFullYear y (dec m) d 0 0 0))))

(deftest test-date
  (is (empty? ((v/date :a) {:a (mkdate)})))
  (is (seq ((v/date :a) {:a "2012-12-25"})))
  (is (seq ((v/date :a) {:a ""})))
  (is (empty? ((v/date :a) {:a nil})))
  (is (empty? ((v/date :x) m))))

(deftest test-dates
  (is (empty? ((v/dates :a) {:a [(mkdate) (mkdate)]})))
  (is (seq ((v/dates :a) {:a ["2012-12-25" (mkdate)]})))
  (is (seq ((v/dates :a) {:a (mkdate)})))
  (is (empty? ((v/dates :a) {:a []})))
  (is (empty? ((v/dates :a) {:a nil})))
  (is (empty? ((v/dates :x) m))))

(deftest test-after
  (is (empty? ((v/after (mkdate 2012 1 1) :a)
                {:a (mkdate 2012 1 2)})))
  (is (seq ((v/after (mkdate 2012 1 1) :a)
             {:a (mkdate 2012 1 1)})))
  (is (seq ((v/after (mkdate 2012 1 1) :a)
             {:a (mkdate 2011 1 1)})))
  (is (empty? ((v/after (mkdate 2012 1 1) :a) {:a nil})))
  (is (seq ((v/after (mkdate 2012 1 1) :a) {:a ""})))
  (is (empty? ((v/after (mkdate 2012 1 1) :x) m))))

(deftest test-before
  (is (empty? ((v/before (mkdate 2012 1 2) :a)
                {:a (mkdate 2012 1 1)})))
  (is (seq ((v/before (mkdate 2012 1 2) :a)
             {:a (mkdate 2012 1 2)})))
  (is (seq ((v/before (mkdate 2011 1 1) :a)
             {:a (mkdate 2012 1 1)})))
  (is (empty? ((v/before (mkdate 2012 1 1) :a) {:a nil})))
  (is (seq ((v/before (mkdate 2012 1 1) :a) {:a ""})))
  (is (empty? ((v/before (mkdate 2012 1 1) :x) m))))

(deftest test-luhn
  (is (empty? ((v/luhn :a) {:a "4111111111111111"})))
  (is (empty? ((v/luhn :a) {:a 4111111111111111})))
  (is (empty? ((v/luhn [:a :b :c :d :e :f :g :h])
                {:a 378282246310005
                 :b 30569309025904
                 :c 6011111111111117
                 :d 6011000990139424
                 :e 3530111333300000
                 :f 5555555555554444
                 :g 4222222222222
                 :h 5019717010103742})))
  (is (empty? ((v/luhn :a) {:a nil})))
  (is (empty? ((v/luhn :x) m)))
  (is (seq ((v/luhn :a) {:a 12831287390328})))
  (is (seq ((v/luhn :a) {:a 4111111111111112})))
  (is (empty? ((v/luhn :a) {:a ""})))
  (is (empty? ((v/luhn :a) {:a " "})))
  (is (empty? ((v/luhn :a) {:a "kljalksdjalskjd"}))))

;(run-tests)
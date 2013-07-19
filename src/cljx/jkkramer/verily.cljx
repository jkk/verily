(ns jkkramer.verily
  (:require [clojure.string :as string])
  #+clj (:import java.util.Date))

(defn seqify [x]
  (if-not (sequential? x) [x] x))

(defn- expand-name
  "Expands a name like \"foo[bar][baz]\" into [:foo :bar :baz]"
  [name]
  (if (or (string? name) (keyword? name))
    (let [[_ name1 more-names] (re-matches #"^([^\[]+)((?:\[[^\]]+?\])*)$"
                                           (clojure.core/name name))]
      (if (seq more-names)
        (into [(keyword name1)] (map (comp keyword second)
                                     (re-seq #"\[([^\]]+)\]" more-names)))
        [name]))
    [name]))

(defn make-validator [keys bad-pred msg]
  (let [bad-pred* #(try
                     (bad-pred %)
                     (catch #+clj Exception #+cljs js/Error _ true))]
    (fn [m]
      (let [bad-keys (filter #(bad-pred* (get-in m (expand-name %) ::absent))
                             (seqify keys))]
        (when (seq bad-keys)
          (if (map? msg)
            msg
            {:keys bad-keys :msg msg}))))))

(defn contains
  "The keys must be present in the map but may be blank."
  [keys & [msg]]
  (make-validator keys #{::absent}
                  (or msg "must be present")))

(defn required
  "The keys must be present in the map AND not be blank."
  [keys & [msg]]
  (make-validator keys #(or (= ::absent %)
                            (nil? %)
                            (and (string? %) (string/blank? %)))
                  (or msg "must not be blank")))

(defn not-blank
  "If present, the keys must not be blank."
  [keys & [msg]]
  (make-validator keys #(or (nil? %)
                            (and (string? %) (string/blank? %)))
                  (or msg "must not be blank")))

(defn exact [val keys & [msg]]
  (make-validator keys #(and (not= ::absent %) (not= val %))
                  (or msg "incorrect value")))

(defn equal [keys & [msg]]
  (let [keys (seqify keys)]
    (fn [m]
      (when-not (apply = (map #(get-in m (expand-name %)) keys))
        (if (map? msg)
          msg
          {:keys keys :msg (or msg "must be equal")})))))

(defn matches [re keys & [msg]]
  (make-validator
    keys #(and (not= ::absent %)
               (not (string/blank? %))
               (not (re-matches re %)))
    (or msg "incorrect format")))

(defn min-length [len keys & [msg]]
  (make-validator
    keys #(and (not= ::absent %)
               (not (nil? %))
               (not (<= len (count %))))
    (or msg (str "must be at least " len " characters"))))

(defn max-length [len keys & [msg]]
  (make-validator
    keys #(and (not= ::absent %)
               (not (nil? %))
               (not (>= len (count %))))
    (or msg (str "cannot exceed " len " characters"))))

(defn in [coll keys & [msg]]
  (let [coll-set (if (set? coll)
                   coll (set coll))]
    (make-validator
      keys #(and (not= ::absent %)
                 (not (nil? %))
                 (not (contains? coll-set %)))
      (or msg (str "not an accepted value")))))

(defn every-in [coll keys & [msg]]
  (let [coll-set (if (set? coll)
                   coll (set coll))]
    (make-validator
      keys #(and (not= ::absent %)
                 (not (nil? %))
                 (not (every? (fn [x] (contains? coll-set x)) %)))
      (or msg (str "not an accepted value")))))

(def ^:private zip-regex #"^\d{5}(?:[-\s]\d{4})?$")

(defn us-zip [keys & [msg]]
  (make-validator keys #(and (not= ::absent %)
                             (not (string/blank? %))
                             (not (re-matches zip-regex %)))
                  (or msg "must be a valid US zip code")))

(defn email [keys & [msg]]
  (make-validator
    keys #(and (not= ::absent %)
               (not (string/blank? %))
               (not (re-matches #"[^^]+@[^$]+" %))) ;RFC be damned
    (or msg "must be a valid email")))

(defn web-url [keys & [msg]]
  (make-validator
    keys #(and (not= ::absent %)
               (not (string/blank? %))
               (or (not (try
                          ;; TODO: better cljs impl
                          #+clj (java.net.URL. %) #+cljs true
                          (catch #+clj Exception #+cljs js/Error _)))
                   (not (re-find #"^https?://" %))))
    (or msg "must be a valid website URL")))

(defn url [keys & [msg]]
  (make-validator
    keys #(and (not= ::absent %)
               (not (string/blank? %))
               (not (try
                      ;; TODO: better cljs impl
                      #+clj (java.net.URL. %) #+cljs (re-find #"^[a-zA-Z]+://" %)
                      (catch #+clj Exception #+cljs js/Error _))))
    (or msg "must be a valid URL")))

(defn string [keys & [msg]]
  (make-validator
    keys #(and (not= ::absent %) (not (nil? %)) (not (string? %)))
    (or msg "must be a string")))

(defn strings [keys & [msg]]
  (make-validator
    keys (fn [v]
           (and (not= ::absent v)
                (or (and (not (nil? v)) (not (sequential? v)))
                    (some #(not (string? %)) v))))
    (or msg "must be strings")))

(defn bool [keys & [msg]]
  (make-validator
    keys #(and (not= ::absent %) (not (nil? %)) (not (true? %)) (not (false? %)))
    (or msg "must be true or false")))

(defn bools [keys & [msg]]
  (make-validator
    keys (fn [v]
           (and (not= ::absent v)
                (or (and (not (nil? v)) (not (sequential? v)))
                    (some #(and (not (true? %)) (not (false? %))) v))))
    (or msg "must be all true or false")))

(defn integer [keys & [msg]]
  (make-validator
    keys #(and (not= ::absent %) (not (nil? %)) (not (integer? %)))
    (or msg "must be a number")))

(defn integers [keys & [msg]]
  (make-validator
    keys (fn [v]
           (and (not= ::absent v)
                (or (and (not (nil? v)) (not (sequential? v)))
                    (some #(not (integer? %)) v))))
    (or msg "must be numbers")))

(defn floating-point [keys & [msg]]
  (make-validator
    keys #(and (not= ::absent %) (not (nil? %)) (not (#+clj float? #+cljs number? %)))
    (or msg "must be a decimal number")))

(defn floating-points [keys & [msg]]
  (make-validator
    keys (fn [v]
           (and (not= ::absent v)
                (or (and (not (nil? v)) (not (sequential? v)))
                    (some #(not (#+clj float? #+cljs number? %)) v))))
    (or msg "must be decimal numbers")))

#+cljs
(defn decimal-str? [x]
  (and (string? x)
       (re-matches #"[0-9\.]+" x)))

(defn decimal [keys & [msg]]
  (make-validator
    keys #(and (not= ::absent %) (not (nil? %)) (not (#+clj decimal? #+cljs decimal-str? %)))
    (or msg "must be a decimal number")))

(defn decimals [keys & [msg]]
  (make-validator
    keys (fn [v]
           (and (not= ::absent v)
                (or (and (not (nil? v)) (not (sequential? v)))
                    (some #(not (#+clj decimal? #+cljs decimal-str? %)) v))))
    (or msg "must be decimal numbers")))

(defn min-val [min keys & [msg]]
  (make-validator
    keys #(and (not= ::absent %)
               (not (nil? %))
               (or (not (number? %)) (> min %)))
    (or msg (str "cannot be less than " min))))

(def at-least min-val)

(defn max-val [max keys & [msg]]
  (make-validator
    keys #(and (not= ::absent %)
               (not (nil? %))
               (or (not (number? %)) (< max %)))
    (or msg (str "cannot be more than " max))))

(def at-most max-val)

(defn within [min max keys & [msg]]
  (make-validator
    keys #(and (not= ::absent %)
               (not (nil? %))
               (or (not (number? %)) (or (> min %) (< max %))))
    (or msg (str "must be within " min " and " max))))

(defn positive [keys & [msg]]
  (make-validator
    keys #(and (not= ::absent %)
               (not (nil? %))
               (or (not (number? %)) (not (pos? %))))
    (or msg "must be a positive number")))

(defn negative [keys & [msg]]
  (make-validator
    keys #(and (not= ::absent %)
               (not (nil? %))
               (or (not (number? %)) (not (neg? %))))
    (or msg "must be a negative number")))

(defn date [keys & [msg]]
  (make-validator
    keys #(and (not= ::absent %) (not (nil? %))
               (not (instance? #+clj Date #+cljs js/Date %)))
    (or msg "must be a date")))

(defn dates [keys & [msg]]
  (make-validator
    keys (fn [v]
           (and (not= ::absent v)
                (or (and (not (nil? v)) (not (sequential? v)))
                    (some #(not (instance? #+clj Date #+cljs js/Date %)) v))))
    (or msg "must be dates")))

(defn- after? [d1 d2]
  #+clj (.after ^Date d1 d2)
  #+cljs (< d2 d1))

(defn after [date keys & [msg]]
  (make-validator
    keys #(and (not= ::absent %)
               (not (nil? %))
               (or
                 (not (instance? #+clj Date #+cljs js/Date %))
                 (not (after? % date))))
    (or msg (str "must be after " date))))

(defn- before? [d1 d2]
  #+clj (.before ^Date d1 d2)
  #+cljs (< d1 d2))

(defn before [date keys & [msg]]
  (make-validator
    keys #(and (not= ::absent %)
               (not (nil? %))
               (or
                 (not (instance? #+clj Date #+cljs js/Date %))
                 (not (before? % date))))
    (or msg (str "must be before " date))))

(defn- digits [n]
  #+clj (map #(Character/digit % 10) (str n))
  #+cljs (map #(- (.charCodeAt % 0) 48) (str n)))
 
(defn- luhn? [x]
  (let [n (if (string? x)
            (string/replace x #"[^0-9]" "")
            x)
        sum (reduce + (map
                        (fn [d idx]
                          (if (even? idx)
                            (reduce + (digits (* d 2)))
                           d))
                        (reverse (digits n))
                        (iterate inc 1)))]
    (zero? (mod sum 10))))

(defn luhn [keys & [msg]]
  (make-validator
    keys #(and (not= ::absent %) (not (nil? %)) (not (luhn? %)))
    (or msg "number is not valid")))

(defn combine [& validators]
  (fn [m]
    (apply concat (map seqify (keep #(% m) validators)))))

(def validations-map
  {:contains contains
   :required required
   :not-blank not-blank
   :exact exact
   :equal equal
   :matches matches
   :min-length min-length
   :max-length max-length
   :in in
   :every-in every-in
   :us-zip us-zip
   :email email
   :url url
   :web-url web-url
   :str string
   :string string
   :strs strings
   :strings strings
   :bool bool
   :boolean bool
   :bools bools
   :booleans bools
   :int integer
   :ints integers
   :integer integer
   :integers integers
   :floating-point floating-point
   :floating-points floating-points
   :float floating-point
   :floats floating-points
   :bigint decimal
   :bigints decimals
   :decimal decimal
   :decimals decimals
   :min-val min-val
   :at-least at-least
   :max-val max-val
   :at-most at-most
   :within within
   :positive positive
   :negative negative
   :date date
   :dates dates
   :after after
   :before before
   :luhn luhn})

(defmulti validation->fn (fn [vspec] (first vspec)))

(defmethod validation->fn :default [vspec]
  (if-let [vfn (get validations-map (first vspec))]
    (apply vfn (rest vspec))
    (throw (ex-info
             (str "Unknown validation " (first vspec))))))

(defn validations->fn [validations]
  (apply combine (map validation->fn validations)))

(defn validate [values validations]
  ((validations->fn validations) values))
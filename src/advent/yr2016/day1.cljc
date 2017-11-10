(ns advent.yr2016.day1
  (:require [clojure.core.matrix :as cm]
            [clojure.string :as cs]
            [advent.util :as util]))

;; Strategy: compute the answer using translation
;; and rotation matrixes.

;; Vectors have 4 dimensions: [x y dx dy]
;; x and y are coordinates
;; dx and dy are directions for x and y.
;; East is when dx = 1; west when dx = -1.
;; North is when dy = 1; south when dy = -1.

;; e.g. Vector [2 3 0 1]
;; At coordinate [2 3], facing north.

(defn parse-direction
  "Parse a direction string into a keyword-int vector.
  e.g. R4 -> [:R 4]"
  [dstr]
  [(-> (subs dstr 0 1)
       (keyword))
   (-> (subs dstr 1 (count dstr))
       (util/parse-int))])

(defn forward
  "Matrix which translates position forward by n steps."
  [n]
  [[1 0 n 0]
   [0 1 0 n]
   [0 0 1 0]
   [0 0 0 1]])

(def turn-left
  "Matrix which rotates direction left."
  [[1 0 0 0]
   [0 1 0 0]
   [0 0 0 -1]
   [0 0 1 0]])

(def turn-right
  "Matrix which rotates direction right."
  [[1 0 0 0]
   [0 1 0 0]
   [0 0 0 1]
   [0 0 -1 0]])

(def turn-mapping
  {:L turn-left
   :R turn-right})

(defn direction-matrix
  "Convert direction vector into its matrix equivalent."
  [[turn steps]]
  (cm/mmul (forward steps) (turn-mapping turn)))

(defn travel
  "Given a initial vector and a sequence of direction
  vectors, returns the vector of the position travled
  to."
  [initial directions]
  (transduce (map direction-matrix)
             (completing #(cm/mmul %2 %1))
             initial
             directions))

(defn int-coords
  "Coords of vector, cast to int."
  [[x y _ _]]
  [(int x) (int y)])

(defn distance
  "Grid distance"
  [[xa ya] [xb yb]]
  (+ (util/abs (- xb xa))
     (util/abs (- yb ya))))

(defn answer1
  [input]
  (->> (cs/split input #",")
       (eduction (comp (map cs/trim)
                       (map parse-direction)))
       (travel [0 0 0 1]) ; at origin, facing north
       (int-coords)
       (distance [0 0])
       (str)))


;; Part 2

(defmulti segment-steps
  "Given a vertical or horizontal line, return all
  steps between the line's endpoints (inclusive)."
  (fn [[[xa ya] [xb yb]]]
    (cond
      (= xa xb) :segment/vertical
      (= ya yb) :segment/horizontal)))

(defmethod segment-steps :segment/vertical
  [[[x ya] [_ yb]]]
  (let [step (if (< ya yb) 1 -1)]
    (map #(vector x %) (range ya (+ yb step) step))))

(defmethod segment-steps :segment/horizontal
  [[[xa y] [xb _]]]
  (let [step (if (< xa xb) 1 -1)]
    (map #(vector % y) (range xa (+ xb step) step))))

(defn first-duplicate
  "Returns the first duplicate item in a sequence."
  [xs]
  (loop [seen #{} items (seq xs)]
    (if items
      (let [item (first items)]
        (if (contains? seen item)
          item
          (recur (conj seen item) (next items)))))))

(defn travel-stops
  "Returns all intermediary vectors for each direction
  traveled, including the initial origin vector."
  [initial directions]
  (->> directions
       (map direction-matrix)
       (reductions #(cm/mmul %2 %1) initial)))

(defn trail
  "Sequence of intermediary integer coordinates
  between the given integer coords."
  [coords]
  (->> coords
       (partition 2 1)
       (map segment-steps)
       (map-indexed #(if (zero? %1) %2 (rest %2)))
       (mapcat identity)))

(defn first-intersect
  [initial directions]
  (->> directions
       (travel-stops initial)
       (map int-coords)
       (trail)
       (first-duplicate)))

(defn answer2
  [input]
  (->> (cs/split input #",")
       (map (comp parse-direction cs/trim))
       (first-intersect [0 0 0 1])
       (distance [0 0])
       (str)))


(ns codenames.util)

(defn in?
  "True if the collection contains the element."
  [collection element]
  (some? (some #(= element %) collection)))

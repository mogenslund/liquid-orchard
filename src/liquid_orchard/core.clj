(ns liquid-orchard.core
  (:require [clojure.string :as str]
            [liq.editor :as editor]
            [liq.buffer :as buffer]
            [liq.modes.clojure-mode :as clojure-mode]
            [orchard.namespace :as ons]
            [orchard.meta :as ometa]
            [orchard.info :as oinfo]
            [orchard.apropos :as oap]))

(defn orchard-goto-definition
  [buf]
  (try
    (let [namesp (clojure-mode/get-namespace buf)
          v (re-find #"\w.*\w" (-> buf buffer/left buffer/word))
          info (meta (ometa/resolve-var (symbol namesp) (symbol v)))
          p (when
              (and
                info
                (not= (info :file) "NO_SOURCE_FILE")
                (not (re-find #"\.jar!" (.getPath (ons/canonical-source (info :ns))))))
              (.getPath (ons/canonical-source (info :ns))))]
      (editor/message info)
      (when p
        (editor/open-file p)
        (editor/apply-to-buffer #(-> %
                                     (buffer/beginning-of-buffer (info :line))
                                     (assoc ::buffer/tow {::buffer/row (info :line) ::buffer/col 1})))))
    (catch Exception e (str e))))

(defn load-liquid-orchard
  []
  (editor/add-key-bindings
    :clojure-mode
    :normal
    {"g" {"D" #(orchard-goto-definition (editor/current-buffer))}}))

(ns ^:figwheel-no-load codenames.dev
  (:require
    [codenames.core :as core]
    [devtools.core :as devtools]))

(devtools/install!)

(enable-console-print!)

(core/init!)

(ns scape.core
  (:gen-class)
  (:require [etaoin.api :as api]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.tools.logging :as log])
  (:import [java.util.concurrent TimeUnit]))

(defonce cli-options
  [["-t" "--threshold THRESHOLD" "Search value as %"
    :default 25
    :parse-fn #(Integer/parseInt %)]
   ["-s" "--sleep MINUTES" "Interval in minutes for refreshing"
    :default 15
    :parse-fn #(Integer/parseInt %)]
   ["-u" "--url SITE" "URL of website to use"
    :default "https://www.myherbology.com/pennsylvania/dubois/med-menu/"
    :parse-fn #(.toString (java.net.URL. %))]
   ["-h" "--help"]])

(defn create-driver []
  (log/info "Creating driver")
  (let [driver (api/firefox-headless)]
    (log/info "adding shutdown hook")
    (.addShutdownHook (Runtime/getRuntime)
                      (Thread. (fn []
                                 (println "Shutting down driver")
                                 (api/quit driver)
                                 (println "Quit driver"))))
    (log/info "added shutdown hook")
    driver))

(defn goto-site [driver url]
  (log/info "Going to url: " url)
  (api/go driver url) ;; get iframe of menu from url
  (api/wait-visible driver [{:tag :iframe :id :jane-menu}])
  (log/info "Getting site iframe")
  (let [site (str (api/get-element-property driver {:tag :iframe :id :jane-menu} :src) "refinementList%5Broot_types%5D%5B0%5D=flower")]
    (log/info "Going to site: " site)
    (api/go driver site)))

(defn get-max [driver]
  (log/info "Getting max %")
  (api/wait-visible driver [{:fn/has-text "Max:"}]) ;;"https://www.iheartjane.com/embed/stores/1259/?refinementList%5Broot_types%5D%5B0%5D=flower"
  (let [max (atom (Integer/parseInt (re-find #"\d+" (api/get-element-text driver [{:fn/has-text "Max:"}]))))]
    (log/info "Max is: " @max)
    max))

(defn -main
  [& args]
  (log/info "Ensure Geckodriver and Firefox with headless support are available.")
  (let [{:keys [options arguments summary errors]} (parse-opts args cli-options)
        driver (create-driver)
        max (do
              (goto-site driver (:url options))
              (get-max driver))]
    (while (< @max (:threshold options))
      (try
        (log/info "Sleeping for: " (:sleep options) " mins")
        (.sleep TimeUnit/MINUTES (:sleep options))
        (log/info "Refreshing...")
        (api/refresh)
        (reset! max (get-max driver))
        (catch Throwable e (str " error : " (.getMessage e))))))
  (log/info "Done"))

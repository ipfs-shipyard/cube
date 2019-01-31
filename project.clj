(defproject cube "0.2.0-SNAPSHOT"
  :description "IPFS Cube will help people deploy and manage their own IPFS pinning services on top of existing cheap hardware, or cloud storage."
  :url "https://github.com/ipfs-shipyard/cube"
  :jvm-opts ["-Dclojure.compiler.direct-linking=true"]
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :plugins [[lein-ring "0.12.4"]
            [lein-cljsbuild "1.1.7"]
            [lein-figwheel "0.5.18"]
            [lein-auto "0.1.3"]
            [lein-less "1.7.5"]
            [lein-shell "0.5.0"]
            [io.taylorwood/lein-native-image "0.3.0"]
            [lein-cloverage "1.0.13"]]
  :native-image {:name "cube"
                 :graal-bin "graalvm-ce-1.0.0-rc11/"
                 :opts ["--verbose"
                        "--enable-url-protocols=http,https"
                        "-Dclojure.compiler.direct-linking=true"
                        "--report-unsupported-elements-at-runtime"
                        "--allow-incomplete-classpath"]}
  :dependencies [[javax.xml.bind/jaxb-api "2.3.0"]
                 [org.clojure/clojure "1.10.0"]
                 [http-kit "2.3.0"]
                 [ring "1.7.1"]
                 [ring/ring-core "1.7.1"]
                 [ring/ring-json "0.4.0"]
                 [clj-http "3.9.1"]
                 [com.stuartsierra/component "0.4.0"]
                 [compojure "1.6.1"]
                 [crypto-random "1.2.0"]
                 [seesaw "1.5.0"]
                 [korma "0.4.3"]
                 [ragtime "0.7.2"]
                 [digitalocean "1.2" :exclusions [midje]]
                 [javax.servlet/servlet-api "2.5"]
                 [tea-time "1.0.1"]
                 [clj-ssh "0.5.14"]
                 [ring-json-response "0.2.0"]
                 [aleph "0.4.6"]
                 [lispyclouds/clj-docker-client "0.1.12"]
                 [com.fasterxml.jackson.core/jackson-core "2.9.8"]
                 [org.clojure/tools.trace "0.7.10"]
                 [buddy/buddy-auth "2.1.0"]
                 [crypto-password "0.2.0"]
                 [org.clojure/clojurescript "1.10.238"]
                 [day8.re-frame/re-frame-10x "0.3.6-react16"]
                 [reagent "0.8.1"]
                 [reagent-utils "0.3.2"]
                 [hiccup "1.0.5"]
                 [re-frame "0.10.6"]
                 [bidi "2.1.5"]
                 [day8.re-frame/tracing "0.5.1"]
                 [org.clojure/test.check "0.10.0-alpha3"]
                 [figwheel-sidecar "0.5.18"]
                 [cider/piggieback "0.3.10"]
                 [binaryage/devtools "0.9.10"]
                 [clojure-humanize "0.2.2"]]
  :profiles {:uberjar {:hooks [leiningen.cljsbuild leiningen.less]}}
  :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]
                 :init-ns cube.dev}
  :less {:source-paths ["src/ui/less"]
         :target-path "resources/public/css"}
  :figwheel { :css-dirs ["resources/public/css"]}
  :cljsbuild {:builds [{
                        :id "main"
                        :source-paths ["src/ui"]
                        :figwheel {:on-jsload "ui.main/on-js-reload"}
                        :compiler {
                                   :output-dir "./resources/public/js"
                                   :output-to "./resources/public/js/cljs-file.js"
                                   :source-map true
                                   ;; TODO enable production builds without devtools
                                   ;; :optimizations :advanced
                                   :optimizations :none
                                   :preloads [day8.re-frame-10x.preload figwheel.preload]
                                   :pretty-print true
                                   }}]}
  :main cube.cli
  ;; TODO aot disabled for now as corrupts refresh of namespaces in repl
  ;; :aot [cube.cli]
  )

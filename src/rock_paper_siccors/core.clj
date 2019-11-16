(ns rock-paper-siccors.core (:require [
                                        clj-http.client :as client
                                       ]
                                       ))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(client/post "http://go-bot-server.herokuapp.com/hello"
             { 
                :debug true
              :form-params { :game {
                                    :name "rps"
                                    :connectionToken "jappiejappi"
                                    }
                             :playerName "jappiejappie"
                             :eventCallback "https://jappieklooster.nl"
                            }
              :content-type :json
              }

             )

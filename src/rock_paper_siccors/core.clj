(ns rock-paper-siccors.core
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [ring.adapter.jetty :as jet]))

;; BOTFLOW:
;; 1. Aquire ngrok
;; 2. Setup server w/ gameid
;; 3. Send hello
;;    capture gameId*	string($uuid) player* string($uuid)
;; 4. Whenver we get a stargame play a move
;; Optional: If we get any error or gamefinshed, exit
(def ids (atom nil)) ;; for storing the gameid and playerid
(def settings {:player-name "jappie-68"
               :connection "japjap"
               :port 6968
               :calback-uri "https://92f75bfd.ngrok.io"})
(def options #{:rock :paper :scissors})

(defn always-rock [roundnr, lastmove] ;; Gauranteed winning, rock beats everyone
  {:curplay :paper, ;; got you sour!
   :nextstrat always-rock})
(defn random [roundnr, lastmove] ;; Gauranteed winning, rock beats everyone
  {:curplay (rand-nth (seq options)), 
   :nextstrat random})


(defn playAgainst [model]
  (let [ choice (rand)]
    (if (<= choice (:paper model))
      :sciccors
      (let [nextChoice (- choice (:paper model))]
        (if (<= nextChoice (:rock model))
          :paper
          :rock)))))

(defn mkModel [prevmodel, roundnr, lastplayed]
  (let [others (disj options lastplayed)
        updateVal (/ 1 roundnr)
        updatePlayed (update prevmodel lastplayed + updateVal) 
        ]
    (reduce (fn [a b] 
              (update a b - (/ updateVal (count others)))
              ) updatePlayed others)
    ))

(defn ficticious-play [prevmodel, roundnr, lastmove]
  (let [ lastplayed (keyword (other-player-move lastmove))
        model (if lastmove (mkModel prevmodel roundnr lastplayed) prevmodel)]
    {:curplay (playAgainst model) :nextstrat (partial ficticious-play model)}
    )
  )

(defn other-player-move [lastmove]
  (let [{:keys [player-name]} settings
        otherplayer (vals (dissoc lastmove (keyword player-name)))
        ]
    (:value (first otherplayer))
    )
  )

(defn beat-last [roundnr, lastmove] ;; Gauranteed winning, rock beats everyone
  (if lastmove
    (do
      (println "playing beat-last" roundnr lastmove)
      (let [lastplayed (other-player-move lastmove)
            ]
        {:curplay (get {"paper" :scissors, ;; TODO use keywords in maps causeof symetry
                        "rock" :paper,
                        "scissors" :rock
                        } lastplayed)
         :nextstrat beat-last}))
    (do
      (println "not playing beat-last")
      {:curplay (:curplay (always-rock roundnr lastmove))
       :nextstrat beat-last
       }
      ))
  )

(defn post [endpoint body]
  (println "enter the post")
  (println (str endpoint body))
  (client/post (str "http://go-bot-server.herokuapp.com/" endpoint)
               {:form-params  body
                :content-type :json}))

(defn play-move [nextRound lastmove]
  (let [{:keys [gameId
                player strat]} @ids
        stratResult (strat nextRound lastmove)]
    (reset!
     ids {:gameId gameId :player player :strat (:nextstrat stratResult)})
    (post "play" {:gameId gameId
                  :playerId (:id player)
                  :round	nextRound
                  :move { :value (name (:curplay stratResult))} ; oneof [rock, paper, scissors]
                  })))

(defn handler [request]
  (try
    (do (println request)
        (if (not= (:content-type request) "application/json")
          (do
            (println (str "invalid request" request))
            {:status 200
             :headers {"Content-Type" "text/plain"}
             :body (str "Rejecting invalid request" request)})
          (let
              [son
               (json/parse-string (slurp (:body request) :encoding "UTF-8") true)
               inner (:nextRound son)
               next-round (or inner (-> son :body :nextRound))
               moves (-> son :body :roundResult :moves)
               ]
            (println (str "printing json" son))
            (if (#{"startGame" "roundFinished"} (:type son))
              (do
                (if (= "roundFinished" (:type son))
                  (do (println "sleeping ~~~")
                      )
                  )
                (println (str "playing a move" next-round))
                (play-move next-round moves))
              )
            (do
              println 
              {:status 200
               :headers {"Content-Type" "text/plain"}
               :body "Success ~"}))))
    (catch Exception e (do
                         (clojure.stacktrace/print-stack-trace e)
                         (println (str "caught exception: " e))))
    )
  )

(def port (:port settings))
(jet/run-jetty #'handler {:port port :join? false})

(def ficticious-ini (partial ficticious-play
                             {:rock 0.334, :scissors 0.333, :paper 0.333}))

(let [{:keys [player-name
              connection
              calback-uri]} settings]
  (def reqres
    (post "hello" {:game          {:name            "rps"
                                   :connectionToken connection}
                   :playerName    player-name
                   :eventCallback calback-uri}))
  (let [body (json/parse-string (:body reqres) true)]
    (reset! ids (merge body
                       {:strat always-rock}))))

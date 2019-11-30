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
(def settings {:player-name "jappiejappie2"
               :connection "cmondude"
               :calback-uri "https://94b69ff7.ngrok.io"})
(def options #{:rock :paper :scissors})

(defn always-rock [roundnr, lastmove] ;; Gauranteed winning, rock beats everyone
  {:curplay :paper, ;; got you sour!
   :nextstrat always-rock})
(defn random [roundnr, lastmove] ;; Gauranteed winning, rock beats everyone
  {:curplay (rand-nth (seq options)), 
   :nextstrat random})

(defn beat-last [roundnr, lastmove] ;; Gauranteed winning, rock beats everyone
  (if lastmove
    (do
      (println "playing beat-last" roundnr lastmove)
      (let [{:keys [player-name]} settings
            otherplayer (vals (dissoc lastmove (keyword player-name)))
            lastplayed (:value (first otherplayer))
            ]
        (println (str lastplayed otherplayer))
        {:curplay (get {"paper" :scissors, ;; TODO use keywords in maps causeof symetry
                        "rock" :paper,
                        "scissors" :rock
                        } lastplayed),
         :nextstrat beat-last}))
    (do
      (println "not playing beat-last")
      (always-rock roundnr lastmove) ;; TODO nextrat problamaitc like this
      ))
  )

(defn post [endpoint body]
  (do
    (println "enter the post")
    (println (str endpoint body))
    (client/post (str "http://go-bot-server.herokuapp.com/" endpoint)
                 {:form-params  body
                  :content-type :json})))

(defn play-move [nextRound lastmove strat]
  (let [{:keys [gameId
                player]} @ids
        stratResult (strat nextRound lastmove)]
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
                (play-move next-round moves beat-last))
              )
            (do
              println 
              {:status 200
               :headers {"Content-Type" "text/plain"}
               :body "Success ~"}))))
    (catch Exception e (do
                         (clojure.stacktrace/print-stack-trace e)
                         (println (str "caught exception: " e)))))
  )

(jet/run-jetty #'handler {:port 6969 :join? false})

; TODO run ngrok here and put the url in eventCallback
(let [{:keys [player-name
              connection
              calback-uri]} settings]
  (def reqres
    (post "hello" {:game          {:name            "rps"
                                   :connectionToken connection}
                   :playerName    player-name
                   :eventCallback calback-uri}))
  (let [body (json/parse-string (:body reqres) true)]
    (reset! ids body)))

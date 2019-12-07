[![https://jappieklooster.nl](https://img.shields.io/badge/blog-jappieklooster.nl-lightgrey)](https://jappieklooster.nl/tag/haskell.html)
[![Jappiejappie](https://img.shields.io/badge/twitch.tv-jappiejappie-purple?logo=twitch)](https://www.twitch.tv/jappiejappie)
[![Jappiejappie](https://img.shields.io/badge/youtube-jappieklooster-red?logo=youtube)](https://www.youtube.com/channel/UCQxmXSQEYyCeBC6urMWRPVw)
[![Paren Xkcb](https://img.shields.io/badge/%28-%20%20%20-red.svg)](https://xkcd.com/859)

This is a bot that will play rock paper siccors
games according to this [protocol](https://app.swaggerhub.com/apis/Szetty/BotServer/1.0.0#/).

It was built on saturday exploration day for jappie
his streams where the clojure langauge was being explored
. In line with showing off some game theory.

This bot implements:

+ always-rock: Will always play paper.
+ random: A 'random' move.
+ beat-last: Play the move whatever beats the previous move from the opponent.
  If the openent played rock last move the current move will be paper.
+ ficticious: Make a probabilistic model of the oponent moves and play against that model.
  Eg start with {:rock 33%, :paper 33% :scissors 34%} oponent plays :rock, {:rock 100%, :paper 0%, :scissors 0%}, openent plays scissors {:rock 50%, :paper 0%, :scissors 50%}.

# Usage
Enter a nix-shell, start a lein repl.
Use cider-connect in emacs.

C-c C-k in emacs makes it eval everything.
C-c C-c makes it eval particular functions.

The nix shell also makes ngrok available which can be used for the callback http server.

# License
MIT: Do whatever you want, just don't blame me.

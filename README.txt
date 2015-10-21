OVERVIEW
--------
This is my stab at writing a chess engine in Java.
I want the AI to be able to play better than me, which isn't setting the bar too high.
This will be my third attempt at writing something like this (first in C, then in C++, now in Java). Hopefully I can make it work this time.
Should help me get familiar with Java abstract classes, nested/local classes, subclasses and interfaces.
Not going to win any awards.

DC-CHESS GAME PLAN
------------------
- Write a .pgn parser (translate from a human-readable format into a move sequence)
- Write a method to follow the sequence and confirm that it is legal
- Write tests to make sure that the parser only accepts valid input (purposely introduce bugs into the file) //make sure that it properly handles comments and other nonmove text
- Write tests and add .pgn files to check normal and special piece moves, checks, checkmates and stalemates
- Write a method to "screenshot" games and write them to a log file (important for visually inspecting test results)
- Implement all arbiter methods (stalemates, checkmates included)
- Write a command-line interface for human play
- Make sure that all tests run, games can be played from start to finish against oneself
- Check into prod/master
- Add a random move AI, write a test which has two of them fight each other (print the results)
- Check into prod/master
- Write a method to evaluate the value of a board for a given player
- Add an arbitrary-number-of-folds AI
- Write a method for confirming that the "better" AIs overwhelmingly outplay worse AIs
- Check into prod/master
- Add an optional randomness factor to prevent matches from being totally deterministic (assign next-move probabilities based on scores, with best having the highest probability)
- Check into prod/master
- Write glue scripts to play against another chess engine (preferably one which is open-source)
- Gauge capabilities of various AIs based on a number of games (script a number of playthroughs), further verify correctness
- Check into prod/master
- Add the capability to save games (in .pgn format), and to load them for a human or AI player
- Check into prod/master
- Add game-tree trimming capability to AIs (start with N-fold-cost-analysis brute-force, start trimming)
- Confirm that the new AIs beat the old ones a certain percentage of the time
- Check into prod/master
- Networked play? Client-to-AI-server play? GUI version (who cares)?

FIRST "SUCCESSFUL" AI vs. AI MATCH
----------------------------------
<game>
7■□■□■□■□
6□■□■♟■□■
5♟♟♙♗■♕■□
4♙■□■□■♟♟
3■□♙♖♙♙■♙
2□■♞♘□♙□■
1■♘■□♔□♖□
 abcdefgh

White player wins after 110 plies
</game>

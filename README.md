## JP Morgan Chess
![Static Badge](https://img.shields.io/badge/Scala-sbt-red?style=for-the-badge&logo=Scala&logoColor=%23dc322f&color=%23dc322f)
![Coverage Status](https://coveralls.io/repos/github/philippeZim/JP_Morgan_Chess/badge.svg?branch=feature/CI)
![GitHub contributors](https://img.shields.io/github/contributors/philippeZim/JP_Morgan_Chess)
![GitHub Created At](https://img.shields.io/github/created-at/philippeZim/JP_Morgan_Chess)
![GitHub last commit](https://img.shields.io/github/last-commit/philippeZim/JP_Morgan_Chess)
![GitHub repo size](https://img.shields.io/github/repo-size/philippeZim/JP_Morgan_Chess)

## Table of Contents

- [General Description](#general-Description)
- [Outlook](#Outlook)
- [How to setup our Chess-Game](#How-to-setup-our-Chess-Game)
- [How to play](#How-to-play)
- [Preview](#preview)

### General Description
This repository marks the plane on which the glorious quest of phillipe and jakob takes place.
A Quest to build their own Chess Game!!!
They are only armed with the programming language scala, the build tool sbt and their wits.
Will the classes of Marco Boger and the depths of the internet be enough for them to figure it out?
Will they prevail in their journey to a full working chess game?
Will they achieve their goal and resist the sweet temptation of Variables and loops to ascend to fully functional programming?
Find yourselves in our comfy repository on a regular basis to see their latest achievements.

### Outlook
![ChessBoardView](https://github.com/philippeZim/JP_Morgan_Chess/blob/developer/src/main/resources/githubImages/ChessBoardImage.png)

```scala
   +-----+-----+-----+-----+-----+-----+-----+-----+
8  |  r  |  n  |  b  |  q  |  k  |  b  |  n  |  r  |
   +-----+-----+-----+-----+-----+-----+-----+-----+
7  |  p  |  p  |  p  |  p  |  p  |  p  |  p  |  p  |
   +-----+-----+-----+-----+-----+-----+-----+-----+
6  |  .  |  .  |  .  |  .  |  .  |  .  |  .  |  .  |
   +-----+-----+-----+-----+-----+-----+-----+-----+
5  |  .  |  .  |  .  |  .  |  .  |  .  |  .  |  .  |
   +-----+-----+-----+-----+-----+-----+-----+-----+
4  |  .  |  .  |  .  |  .  |  .  |  .  |  .  |  .  |
   +-----+-----+-----+-----+-----+-----+-----+-----+
3  |  .  |  .  |  .  |  .  |  .  |  .  |  .  |  .  |
   +-----+-----+-----+-----+-----+-----+-----+-----+
2  |  P  |  P  |  P  |  P  |  P  |  P  |  P  |  P  |
   +-----+-----+-----+-----+-----+-----+-----+-----+
1  |  R  |  N  |  B  |  Q  |  K  |  B  |  N  |  R  |
   +-----+-----+-----+-----+-----+-----+-----+-----+
      a     b     c     d     e     f     g     h
```
### How to setup our Chess-Game

To play Chess with our glorious implementation one must prepare.
Follow these steps, young lad':

First, one must download the scala programming language from it's source.
Look here:
https://www.scala-lang.org/download/

Secondly, one must install the SBT! The Scala Build Tool!
This is used to actually run our code in a virtual machine?

As a third step one should create a new folder to save our little chess game files.

And as a fourth step: 
open a terminal of your choosing and go to your created folder
Now, behold the mighty words as you type them into your console: 
```bash
 git clone https://github.com/philippeZim/JP_Morgan_Chess
  ```

### How to play

Even the dumbest bafoon could perform the necessary steps.
Simply type:
```bash
sbt run
```
into your console.

Should you be one of those fellas who prefer the ways of the docker. Behold:
If you dare: Enter the Docker directory.
Now type the magic words:
```bash
docker build -t chess .
```
You will have to prove your patience now young lad. For docker will not rest until it has downloaded all the necessary libraries.
If you were successful: You are ready!
Start the game by:
```bash
docker run -it `
      --volume /tmp/.X11-unix:/tmp/.X11-unix `
--device /dev/dri `
      chess
```
However: Should you have forgotten to start your Xming in time and don't have it opened. Do so quickly!


Before you will rise a chessboard, forming as a Desktop-Application as well as a Accumulation of Letters in your console.

If you dare to use the Desktop-Application you simply have to use the powers of your mighty mouse by clicking
your desired piece and then declaring your order by clicking on the desired square.

If you retreat to the cowardly option of using your Keyboard:
You will be asked to move. To make a move you simply have to type in the field from
which you wish to move and the field that marks the new intended position of your chosen Piece.
Choose your formatting carefully. It should always look like this: e2e4 

But be warned: should your command defy the holy rules of Chess. The Game will not hesiate
to scold you in worse ways than your tiny brain could comprehend!

Now:

### Start Playing

If you are unfamiliar with the the ways of Chess one must ask the question of what deficiencies you must suffer from
and what exactly your father has done to deserve an offspring like you.

### Our Journey

For those few Fellows that are want to take a peek at our journey and the challenges we had to face.
I will indulge in sharing our goals and struggles with you.

The requirements for the result of our hardship were the following:
- **TUI**: Text User Interface.
- **GUI**: Graphical User Interface.
- **Concurrency**: Running TUI and GUI simultaneously in influence of each other.
- **Documentation**: Documented and maintained on GitHub.
- **MVC Architecture**: Model-view-controller design pattern.
- **Coverage**: almost 100% Code coverage.

# Java 'Kanji Quiz' program

In order to refresh my Java knowledge, I am writing a quiz program for Japanese
Kanji based on C++ code in the 'kanji-tools' repo.

The project is built using Gradle in Intellij with Junit 5 for testing and
JaCoCo for code coverage. I am trying to use modern features from **Java 7**
(July 2011) and **Java 8** (March 2014) whenever possible as well as a few
more features from later releases.

Here are examples of new Java features per version:

- Java 7: strings in switch, diamond operator, java.nio, binary literals
- Java 8: lambdas, language support for collections, streams
- Java 10: [var for local variables](https://openjdk.java.net/jeps/286)
- Java 14: [switch expressions](https://openjdk.java.net/jeps/361)
- Java 15: [text blocks](https://openjdk.java.net/jeps/378)
- Java 16: [pattern matching for instance of](https://openjdk.java.net/jeps/394)
- Java 17: [sealed classes](https://openjdk.java.net/jeps/409)

The project JDK version is currently set to **Java 17** which is the latest
*LTS* version (September 2021). I tried using **Java 20** (March 2023), but ran
into issues with the IDE and Gradle.

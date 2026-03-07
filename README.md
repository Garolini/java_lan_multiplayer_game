# Java LAN Multiplayer Game

> ⚠️ **Note:** This repository is a public transformation of a university project originally developed for a Software Engineering course.  
> All proprietary assets (images, graphics, etc.) have been removed.  
> The game is therefore **not fully playable**; this version serves only as an educational example of a Java LAN multiplayer implementation.

## Project Overview

This project was initially developed as a final university project in Java.  
It demonstrates a client-server structure for LAN multiplayer games, including components for resource management and game logic.  
The focus is on illustrating software architecture, networking, and object-oriented programming in Java.

### Original Team
- Michele Garolini
- Michael Jafari
- Cesario Migliaccio


## How to Compile with Maven

To compile the project using Maven:

1. Ensure Maven is installed: [Maven official website](https://maven.apache.org/download.cgi)
2. Navigate to the root directory containing `pom.xml`.
3. Run:

```bash
mvn clean package
```

This command will generate a main JAR inside the `target` folder

## How to Run the Program

You can run the program using the compiled JAR file:

### Running the Server

```bash
    java -jar java-lan-multiplayer-game-x.x.jar server [options]
```

### Running the Client

```bash
    java -jar java-lan-multiplayer-game-x.x.jar client [options]
```

## Server CLI Arguments

| Argument        | Description                              | Accepted Values          | Default Value |
|-----------------|------------------------------------------|--------------------------|---------------|
| `--port`, `-p`  | TCP port the server listens on.          | Any valid port (0–65535) | 8080          |
| `--debug`, `-d` | Enables debug mode with verbose logging. | (flag only)              | Off           |

## Client CLI Arguments

| Argument        | Description                              | Accepted Values | Default Value |
|-----------------|------------------------------------------|-----------------|---------------|
| `--lang`, `-l`  | Sets the client UI language.             | `en`, `it`      | `en`          |
| `--debug`, `-d` | Enables debug mode with verbose logging. | (flag only)     | Off           |


## Legal Disclaimer

This project is a personal, educational implementation and does not include any proprietary assets from the original game.
It is provided for learning and demonstration purposes only. No part of the original game’s images, graphics, or commercial content is included.
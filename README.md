# 2048 AI Game

A Java implementation of the 2048 game with an intelligent AI player using the Expectimax algorithm.

## Features

- **Complete 2048 Game**: Fully functional implementation of the classic 2048 puzzle game
- **Intelligent AI Player**: Uses Expectimax algorithm with sophisticated heuristics
- **Graphical Interface**: Clean GUI built with Java Swing
- **Victory Detection**: Game stops automatically when reaching the 2048 tile
- **Human vs AI**: Choose between human player or AI player

## AI Strategy

The AI player uses an Expectimax algorithm with multiple evaluation heuristics:

- **Highest Tile Score**: Prioritizes growing the largest tile
- **Empty Space Management**: Values keeping multiple empty spaces
- **Merge Potential**: Looks for opportunities to merge identical tiles
- **Tile Smoothness**: Prefers smooth transitions between adjacent tiles
- **Monotonicity**: Encourages ordered tile arrangements
- **Corner Strategy**: Rewards keeping the highest tile in corners
- **Future Move Potential**: Values having multiple possible moves

## How to Build and Run

### Compile:
```bash
javac -cp src src/ttfe/*.java
```

### Run with AI Player:
```bash
java -cp src ttfe.TTFE --player c
```

### Run with Human Player:
```bash
java -cp src ttfe.TTFE --player h
```

### Custom Options:
```bash
java -cp src ttfe.TTFE --seed 42 --width 4 --height 4 --player c
```

## Game Controls (Human Player)

- **Arrow Keys**: Move tiles (↑ ↓ ← →)
- **Goal**: Combine tiles to reach 2048

## AI Performance

The AI typically achieves:
- Consistent performance reaching 1024+ tiles
- Good chance of reaching the 2048 goal
- Strategic tile placement using corner strategy
- Efficient empty space management

## Technical Details

- **Language**: Java
- **Algorithm**: Expectimax with depth-limited search
- **Search Depth**: Configurable (default: 2)
- **Heuristics**: Multi-criteria evaluation function
- **Architecture**: Clean separation of game logic, AI, and UI

## Credits

This project implements the classic 2048 game concept with custom AI algorithms.
The Expectimax algorithm and evaluation heuristics are original implementations.

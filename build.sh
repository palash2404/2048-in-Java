#!/bin/bash

echo "Building 2048 AI Game..."
javac -cp src src/ttfe/*.java

if [ $? -eq 0 ]; then
    echo "Build successful!"
    echo ""
    echo "To run with AI player: java -cp src ttfe.TTFE --player c"
    echo "To run with human player: java -cp src ttfe.TTFE --player h"
else
    echo "Build failed!"
    exit 1
fi

#!/bin/bash
set -e

echo "Building core module..."
cd core
mvn clean package
cd ..

echo "Copying core JAR to all handlers..."

for handler_dir in handlers/*/; do
    [ ! -d "$handler_dir" ] && continue

    handler_name=$(basename "$handler_dir")

    echo "Processing handler: $handler_name"

    mkdir -p "handlers/$handler_name/.jebi"
    cp core/target/core-1.0.jar "handlers/$handler_name/.jebi/"

    echo "  ✓ Copied JAR to handlers/$handler_name/.jebi/"
done

echo "Building with SAM..."
sam build --use-container

echo "Build complete!"

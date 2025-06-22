#!/bin/bash

MIGRATION_DIR="./src/main/resources/db/migration"
LATEST_VERSION=$(ls $MIGRATION_DIR/V*.sql | sed -E 's/.*V([0-9]+)__.*\.sql/\1/' | sort -n | tail -1)
NEXT_VERSION=$((LATEST_VERSION + 1))

read -p "Enter migration name (e.g. add_index_to_product): " NAME
FILENAME="V${NEXT_VERSION}__${NAME// /_}.sql"

touch "$MIGRATION_DIR/$FILENAME"
echo "-- Migration: $NAME" > "$MIGRATION_DIR/$FILENAME"

echo "✅ Created: $MIGRATION_DIR/$FILENAME"

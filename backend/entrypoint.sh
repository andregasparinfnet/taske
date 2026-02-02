#!/bin/sh

# If DB_HOST is explicitly set (e.g., from render.yaml), use the existing logic
if [ -n "$DB_HOST" ]; then
    echo "Using explicit env vars for DB connection..."
    export SPRING_DATASOURCE_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT:-5432}/${DB_NAME:-taske}"
    
# If DB_HOST is missing but DATABASE_URL is present (Render standard manual deploy), parse it
elif [ -n "$DATABASE_URL" ]; then
    echo "DB_HOST missing, parsing DATABASE_URL..."
    
    # Format: postgres://user:pass@host:port/dbname
    # Remove protocol
    url_no_proto=${DATABASE_URL#*://}
    
    # Split user:pass and host:port/dbname
    user_pass_part=${url_no_proto%@*}
    host_db_part=${url_no_proto#*@}
    
    # Extract user and pass
    export DB_USER=${user_pass_part%:*}
    export DB_PASS=${user_pass_part#*:}
    
    # Extract host:port and dbname
    host_port=${host_db_part%/*}
    export DB_NAME=${host_db_part#*/}
    
    # Extract host and port
    export DB_HOST=${host_port%:*}
    export DB_PORT=${host_port#*:}
    
    export SPRING_DATASOURCE_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}"
    export SPRING_DATASOURCE_USERNAME="${DB_USER}"
    export SPRING_DATASOURCE_PASSWORD="${DB_PASS}"
    
    echo "Parsed DB config: Host=$DB_HOST, Port=$DB_PORT, DB=$DB_NAME"
else
    echo "WARNING: Neither DB_HOST nor DATABASE_URL found. Defaulting to localhost (will likely fail on Render)."
    export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:${DB_PORT:-5432}/${DB_NAME:-taske}"
fi

# Run the application
exec java -Xmx350m -jar app.jar

#!/bin/sh

echo "--- ENTRYPOINT CONFIGURATION ---"

# Function to parse a standard postgres connection string and set Spring env vars
# Accepts a URL like: postgres://user:pass@host:port/dbname
parse_and_export() {
    input_url="$1"
    
    echo "Parsing connection string..."
    
    # Remove protocol (http://, postgres://, jdbc:postgresql://, etc.)
    # identifying where the authority part starts
    url_no_proto=$(echo "$input_url" | sed -E 's|^[a-zA-Z0-9]+:(//)?||')
    
    # Check if credentials exist (contains @)
    if echo "$url_no_proto" | grep -q "@"; then
        # user:pass matches everything up to the last @
        user_pass_part=${url_no_proto%@*}
        # host:port/db matches everything after the last @
        host_db_part=${url_no_proto##*@}
        
        # Extract user and pass
        # user matches everything up to the first :
        db_user=${user_pass_part%%:*}
        # pass matches everything after the first :
        db_pass=${user_pass_part#*:}
        
        # Export credentials
        export SPRING_DATASOURCE_USERNAME="$db_user"
        export SPRING_DATASOURCE_PASSWORD="$db_pass"
        
        echo "Extracted credentials (User: $db_user)"
    else
        echo "No credentials found in URL authority."
        host_db_part="$url_no_proto"
        # If no credentials in URL, we assume they might be set via other env vars
        # or we leave them alone.
    fi
    
    # Ensure the remaining part is clean for JDBC
    # Format needs to be: jdbc:postgresql://host:port/dbname
    export SPRING_DATASOURCE_URL="jdbc:postgresql://$host_db_part"
}

# 1. Decide source of truth
if [ -n "$SPRING_DATASOURCE_URL" ]; then
    echo "Using existing SPRING_DATASOURCE_URL as source."
    SOURCE_URL="$SPRING_DATASOURCE_URL"
    HAS_SOURCE=true

elif [ -n "$DATABASE_URL" ]; then
    echo "Using DATABASE_URL as source."
    SOURCE_URL="$DATABASE_URL"
    HAS_SOURCE=true

elif [ -n "$DB_HOST" ]; then
    echo "Constructing URL from explicit DB_* vars."
    # Case: Granular vars (Blueprint or manual split vars)
    export SPRING_DATASOURCE_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT:-5432}/${DB_NAME:-taske}"
    # In this case we trust the other env vars are already set (DB_USER/DB_PASS)
    # or that Spring picks them up if referenced in application.properties
    HAS_SOURCE=false 
else
    echo "WARNING: No database variables found. Defaulting to localhost."
    export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:${DB_PORT:-5432}/${DB_NAME:-taske}"
    HAS_SOURCE=false
fi

# 2. Parse if we have a raw source URL
if [ "$HAS_SOURCE" = true ]; then
    parse_and_export "$SOURCE_URL"
fi

echo "Final JDBC Info:"
echo "URL: $SPRING_DATASOURCE_URL"
# Do not echo password, obviously
echo "User: $SPRING_DATASOURCE_USERNAME"

echo "--- STARTING APPLICATION ---"
exec java -Xmx350m -jar app.jar

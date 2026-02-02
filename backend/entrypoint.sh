#!/bin/sh

echo "--- ENTRYPOINT CONFIGURATION ---"

# 1. Scenario: SPRING_DATASOURCE_URL is already set (Manually or via Render Environment)
if [ -n "$SPRING_DATASOURCE_URL" ]; then
    echo "Detected existing SPRING_DATASOURCE_URL."
    
    # Check if it starts with 'jdbc:'. If not, we fix it.
    case "$SPRING_DATASOURCE_URL" in
        jdbc:*) 
            echo "URL is already in JDBC format."
            ;;
        *)
            echo "Converting URL protocol to JDBC format..."
            # Remove existing protocol (postgres:// or postgresql://)
            CLEAN_URL=$(echo "$SPRING_DATASOURCE_URL" | sed -E 's|^[a-zA-Z]+://||')
            # Prepend jdbc:postgresql://
            export SPRING_DATASOURCE_URL="jdbc:postgresql://$CLEAN_URL"
            ;;
    esac

# 2. Scenario: Granular Env Vars (DB_HOST, etc.) - Typically from Blueprint
elif [ -n "$DB_HOST" ]; then
    echo "Constructing URL from DB_HOST..."
    export SPRING_DATASOURCE_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT:-5432}/${DB_NAME:-taske}"

# 3. Scenario: Standard Render DATABASE_URL (Auto-injected by Render)
elif [ -n "$DATABASE_URL" ]; then
    echo "Parsing DATABASE_URL..."
    # Format: postgres://user:pass@host:port/dbname
    
    # Remove protocol
    url_no_proto=${DATABASE_URL#*://}
    
    # Split user:pass and host:port/dbname
    user_pass_part=${url_no_proto%@*}
    host_db_part=${url_no_proto#*@}
    
    # Extract user and pass
    export DB_USER=${user_pass_part%:*}
    export DB_PASS=${user_pass_part#*:}
    
    # Set Spring Boot env vars for User/Pass
    export SPRING_DATASOURCE_USERNAME="${DB_USER}"
    export SPRING_DATASOURCE_PASSWORD="${DB_PASS}"
    
    # Construct JDBC URL with the host/db part
    export SPRING_DATASOURCE_URL="jdbc:postgresql://$host_db_part"
    
else
    echo "WARNING: No database configuration variables found!"
    echo "Defaulting to localhost (likely to fail)."
    export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:${DB_PORT:-5432}/${DB_NAME:-taske}"
fi

echo "Final Connection Info:"
echo "URL: $SPRING_DATASOURCE_URL"
echo "User: ${SPRING_DATASOURCE_USERNAME:-$(echo $DB_USER)}"

echo "--- STARTING APPLICATION ---"
exec java -Xmx350m -jar app.jar

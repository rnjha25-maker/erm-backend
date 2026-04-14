#!/bin/bash

#!/bin/bash

# Simple local development script for ERM services
# Usage: ./local-dev.sh [start|stop|status|logs]

set -e

COMPOSE_FILE="docker-compose.local.yml"
PROJECT_NAME="erm"

case "$1" in
    start)
        echo "Starting all services..."
        docker compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" up -d
        echo "✓ Services started"
        echo "API Gateway: http://localhost:8080"
        ;;
    stop)
        echo "Stopping all services..."
        docker compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" down
        echo "✓ Services stopped"
        ;;
    status)
        docker compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" ps
        ;;
    logs)
        docker compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" logs -f --tail=50
        ;;
    *)
        echo "Usage: $0 {start|stop|status|logs}"
        echo ""
        echo "Commands:"
        echo "  start   - Start all services"
        echo "  stop    - Stop all services"
        echo "  status  - Show service status"
        echo "  logs    - Show service logs"
        ;;
esac

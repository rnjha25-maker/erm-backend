#!/bin/bash

# Simple EC2 deployment script for ERM services
# Usage: ./deploy-services.sh [status|logs|deploy-all]

set -e

COMPOSE_FILE="docker-compose.ec2.yml"
PROJECT_NAME="erm"

case "$1" in
    status)
        echo "Service Status:"
        docker compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" ps
        ;;
    logs)
        echo "Service Logs:"
        docker compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" logs -f --tail=50
        ;;
    deploy-all)
        echo "Deploying all services..."
        # Load environment if available
        if [ -f ".env.ec2" ]; then
            export $(cat .env.ec2 | grep -v '#' | xargs)
        fi

        # Deploy in order
        docker compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" up -d --build discovery-server
        sleep 10
        docker compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" up -d --build \
            erm-backend erm-command-organization erm-query-organization \
            org-setup-command user-setup storage user-query org-setup-query
        sleep 15
        docker compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" up -d --build erm-api-gateway

        echo "✓ All services deployed"
        docker compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" ps
        ;;
    *)
        echo "Usage: $0 {status|logs|deploy-all}"
        echo ""
        echo "Commands:"
        echo "  status      - Show service status"
        echo "  logs        - Show service logs"
        echo "  deploy-all  - Deploy all services (manual)"
        ;;
esac
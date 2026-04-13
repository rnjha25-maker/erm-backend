# ERM – Microservices Setup

## Architecture Overview

- **Discovery Server (`discovery-server`)**: Eureka registry where all services register.
- **API Gateway (`erm-api-gateway`)**: Single entry point for clients; routes requests to backend services.
- **Command Services**:
  - `ERM_backend`
  - `command/user-command`
  - `command/org-setup-command`
  - `command/erm-command-organization`
  - `command/storage`
- **Query Services**:
  - `query/user-query`
  - `query/org-setup-query`
  - `query/erm-query-organization`

**Request flow**:
Client → `erm-api-gateway` → (Eureka service discovery) → command/query services → database/cache.

## Prerequisites

- Java 17 (JDK)
- Maven
- MySQL running locally or reachable from the services
- Redis (for caching / token storage where used) - can be used docker

## Quick Start

### Build All Services
From the root directory:

**Windows:**
```cmd
build.bat
```

**Linux/Mac:**
```bash
./build.sh
```

Or manually:
```bash
mvn clean compile -DskipTests
```

### Run Individual Services
```bash
# Start discovery server first
mvn spring-boot:run -pl discovery-server -am

# Then start API gateway
mvn spring-boot:run -pl erm-api-gateway -am

# Then start other services
mvn spring-boot:run -pl ERM_backend -am
mvn spring-boot:run -pl command/user-command -am
# ... etc
```

## Configuration

Update configuration files with your local settings:

- `discovery-server/src/main/resources/application.yaml`
- `erm-api-gateway/src/main/resources/application.yaml`
- `ERM_backend/src/main/resources/application-dev.yaml`
- `command/*/src/main/resources/application-dev.yaml`
- `query/*/src/main/resources/application.properties`

Make sure to set:

- Database URL, username, and password.
- Redis host and port (where required).
- Ports for each service so they don't conflict.

## Build

From root directory of each service directory:

```bash
mvn clean install
```

## Running the Application

### Profiles

The application supports the following profiles:
- `dev`: Development environment (default database: localhost MySQL, Redis on localhost)
- `qa`: QA environment (configure accordingly in application-qa.yaml)

### Sequence to Start Services

To run the microservices, start them in the following order to ensure proper service discovery:

1. **Discovery Server** (Eureka registry)
2. **API Gateway**
3. **Command and Query Services** (can be started in parallel after discovery)
4. **ERM Backend** (if not included in commands)

### Manual Startup

Navigate to each service directory and run:

For **dev** profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

For **qa** profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=qa
```

### Ports

Ensure the following default ports are available or configured:
- Discovery Server: 8761
- API Gateway: (check config)
- ERM Backend: 8086 (dev)
- Other services: Check their application.yaml files

## Testing

After starting all services, test the API Gateway endpoint (e.g., http://localhost:<gateway-port>).

## Troubleshooting

- Ensure MySQL and Redis are running.
- Check Eureka dashboard at http://localhost:8761 for registered services.
- View logs in `logs-local/` directory.  

## Docker Compose

The repository now includes a root `Dockerfile` and `docker-compose.yml` for local containerized deployment and testing of the ERM services.

Start the full stack:

```bash
docker compose up --build
```

Run it in the background:

```bash
docker compose up --build -d
```

Stop the stack:

```bash
docker compose down
```

Remove containers plus database/cache volumes:

```bash
docker compose down -v
```

### Exposed Ports

- Discovery Server: `8761`
- API Gateway: `8080`
- ERM Command Organization: `8081`
- ERM Query Organization: `8082`
- Org Setup Command: `8083`
- User Setup: `8084`
- Storage: `8085`
- ERM Backend: `8086`
- User Query: `8087`
- Org Setup Query: `8088`
- MySQL: `3307` by default on the host (`MYSQL_HOST_PORT` can override it)
- Redis: `6379`

### Docker Notes

- Containers use the `dev` Spring profile where that profile already exists in the repo.
- MySQL runs locally in Docker with database `erm2`.
- The MySQL container listens on `3306` internally, but publishes to host port `3307` by default to avoid conflicts with a locally installed MySQL server. Override it with `MYSQL_HOST_PORT=<port>`.
- Local first-time setup imports [`docker/mysql/init/erm2.sql`](./docker/mysql/init/erm2.sql) automatically when the MySQL data volume is empty, so tables are created on the first local deployment.
- If you need to re-run the dump from scratch locally, remove the MySQL volume with `docker compose down -v` and start again.
- Redis runs as a companion cache service.
- Eureka clients are pointed at `http://discovery-server:8761/eureka/` inside the Docker network.

## GitHub Actions EC2 Deploy

The repo now includes [`.github/workflows/deploy-ec2.yml`](./.github/workflows/deploy-ec2.yml) to deploy the Docker Compose stack to an EC2 instance on every push to `main`, or manually through GitHub Actions.

### EC2 prerequisites

- Install Docker and Docker Compose on the EC2 machine.
- Create a target application directory on EC2, for example `/home/ubuntu/erm`.
- Ensure the EC2 security group allows the ports you want to expose, such as `8080`, `8761`, `3307`, and `6379`.

### GitHub Secrets

Add these repository secrets before running the workflow:

- `EC2_HOST`: Public IP or DNS of the EC2 instance
- `EC2_USER`: SSH user, for example `ubuntu`
- `EC2_SSH_KEY`: Private SSH key used by GitHub Actions
- `EC2_APP_DIR`: Absolute deploy path on the EC2 server, for example `/home/ubuntu/erm`
- `EC2_PORT`: Optional SSH port, default is `22`
- `MYSQL_HOST_PORT`: Optional host port for MySQL on EC2, default is `3307`

### Deploy behavior

- The workflow copies the project files to the EC2 instance.
- It then runs `docker compose up --build -d --remove-orphans` in the target directory.
- On the first deployment with a fresh MySQL volume, MySQL will also import `docker/mysql/init/erm2.sql`.
 

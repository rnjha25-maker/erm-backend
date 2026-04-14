# ERM Application - Deployment Guide

## 🚀 Quick Start

### Local Development (1 minute)
```bash
# Start all services
docker compose -f docker-compose.local.yml -p erm up -d

# Access API Gateway
open http://localhost:8080

# View logs
docker compose -f docker-compose.local.yml -p erm logs -f

# Stop services
docker compose -f docker-compose.local.yml -p erm down
```

### EC2 Production (Automatic)
```bash
# Push to main branch - GitHub Actions deploys automatically
git push origin main

# Takes ~12 minutes, monitor in GitHub Actions tab
```

---

## 📋 Service Architecture

### All 10 Microservices
- **API Gateway** (8080) - Entry point
- **Discovery Server** (8761) - Service registry
- **ERM Backend** (8086) - Core business logic
- **Command Services** (8081-8085) - Write operations
- **Query Services** (8082, 8087-8088) - Read operations

### Deployment Order
1. **Discovery Server** (infrastructure)
2. **Microservices** (8 services in parallel)
3. **API Gateway** (depends on all services)

---

## 🏠 Local Development

### Prerequisites
- Docker Desktop installed
- 2GB+ free disk space
- Ports 8080, 8761, 3307, 6379 available

### Start Services
```bash
# Full startup (includes MySQL & Redis)
docker compose -f docker-compose.local.yml -p erm up -d

# Or use helper script
./local-dev.sh start
```

### Access Points
- **API Gateway**: http://localhost:8080
- **Discovery Server**: http://localhost:8761
- **MySQL**: localhost:3307 (root/2116)
- **Redis**: localhost:6379

### Useful Commands
```bash
# View service status
docker compose -f docker-compose.local.yml -p erm ps

# View logs for specific service
docker compose -f docker-compose.local.yml -p erm logs -f erm-backend

# Rebuild specific service
docker compose -f docker-compose.local.yml -p erm up -d --build erm-backend

# Stop and clean up
docker compose -f docker-compose.local.yml -p erm down -v
```

---

## ☁️ EC2 Production Deployment

### Prerequisites
1. **AWS Resources** (already created):
   - RDS MySQL database
   - ElastiCache Redis cluster
   - EC2 instance with Docker

2. **GitHub Secrets** (configure in repository):
   ```
   EC2_HOST              - EC2 public IP
   EC2_USER              - SSH username (ec2-user/ubuntu)
   EC2_SSH_KEY           - SSH private key
   EC2_PORT              - SSH port (22)
   EC2_APP_DIR           - App directory (/opt/erm)
   AWS_REGION            - AWS region
   AWS_RDS_ENDPOINT      - RDS database endpoint
   AWS_RDS_USERNAME      - RDS username
   AWS_RDS_PASSWORD      - RDS password
   AWS_REDIS_ENDPOINT    - ElastiCache endpoint
   AWS_REDIS_PORT        - Redis port (6379)
   ```

### Automatic Deployment
1. **Push to main branch**
2. **GitHub Actions automatically**:
   - Copies code to EC2
   - Creates `.env.ec2` with AWS credentials
   - Deploys services in 3 phases
   - Verifies deployment

### Manual Deployment (if needed)
```bash
# SSH to EC2
ssh -i key.pem ec2-user@your-ec2-ip

# Navigate to app directory
cd /opt/erm

# Create environment file
cat > .env.ec2 << 'EOF'
AWS_REGION=us-east-1
AWS_RDS_ENDPOINT=your-rds-endpoint.rds.amazonaws.com
AWS_RDS_USERNAME=admin
AWS_RDS_PASSWORD=your-password
AWS_REDIS_ENDPOINT=your-redis-endpoint.cache.amazonaws.com
AWS_REDIS_PORT=6379
EOF

# Deploy all services
export $(cat .env.ec2 | xargs)
docker compose -f docker-compose.ec2.yml -p erm up -d --build
```

---

## 🔧 Configuration Files

### Local Environment
- **docker-compose.local.yml** - Complete local setup with MySQL & Redis
- **Profile**: `dev`
- **Database**: Local MySQL container
- **Cache**: Local Redis container

### EC2 Environment
- **docker-compose.ec2.yml** - Production setup with AWS services
- **Profile**: `prod`
- **Database**: AWS RDS MySQL
- **Cache**: AWS ElastiCache Redis
- **Logging**: CloudWatch

### Environment Template
- **.env.ec2.example** - Template for EC2 environment variables

---

## 🛠️ Helper Scripts

### Local Development
```bash
./local-dev.sh start    # Start all services
./local-dev.sh stop     # Stop all services
./local-dev.sh logs     # View logs
./local-dev.sh status   # Show status
./local-dev.sh rebuild  # Rebuild services
./local-dev.sh clean    # Remove containers
```

### EC2 Deployment
```bash
./deploy-services.sh . all      # Deploy all services
./deploy-services.sh . status   # Show deployment status
./deploy-services.sh . logs     # View logs
```

---

## 🏗️ AWS Setup (One-time)

### 1. Create RDS MySQL
```bash
aws rds create-db-instance \
    --db-instance-identifier erm-mysql-db \
    --db-instance-class db.t3.micro \
    --engine mysql \
    --engine-version 8.0 \
    --master-username admin \
    --master-user-password your-secure-password \
    --allocated-storage 20 \
    --vpc-security-group-ids sg-xxxxxxxx
```

### 2. Create ElastiCache Redis
```bash
aws elasticache create-cache-cluster \
    --cache-cluster-id erm-redis-cluster \
    --cache-node-type cache.t3.micro \
    --engine redis \
    --num-cache-nodes 1 \
    --security-group-ids sg-xxxxxxxx
```

### 3. Launch EC2 Instance
```bash
aws ec2 run-instances \
    --image-id ami-0c55b159cbfafe1f0 \
    --instance-type t3.medium \
    --key-name your-key-pair \
    --security-group-ids sg-xxxxxxxx \
    --user-data "#!/bin/bash
sudo apt update && sudo apt install -y docker.io docker-compose-plugin
sudo usermod -aG docker ubuntu
mkdir -p /opt/erm"
```

### 4. Configure Security Groups
- **EC2**: Allow inbound SSH (22) and HTTP (80/443)
- **RDS**: Allow MySQL (3306) from EC2 security group
- **ElastiCache**: Allow Redis (6379) from EC2 security group

---

## 🔍 Troubleshooting

### Local Issues
```bash
# Check Docker is running
docker --version

# View container logs
docker compose -f docker-compose.local.yml -p erm logs erm-backend

# Check service health
docker compose -f docker-compose.local.yml -p erm ps

# Clean and restart
docker compose -f docker-compose.local.yml -p erm down -v
docker compose -f docker-compose.local.yml -p erm up -d
```

### EC2 Issues
```bash
# Check GitHub Actions logs
# Verify AWS credentials in secrets
# Check EC2 security groups
# Verify RDS/Redis endpoints

# Manual verification on EC2
ssh -i key.pem ec2-user@your-ec2-ip
cd /opt/erm
docker compose -f docker-compose.ec2.yml -p erm ps
docker compose -f docker-compose.ec2.yml -p erm logs
```

### Common Problems
- **Services not connecting**: Check database credentials and endpoints
- **Out of disk space**: `docker system prune -a --volumes -f`
- **Port conflicts**: Ensure ports 8080-8088 are available
- **AWS connectivity**: Verify security groups and VPC settings

---

## 📊 File Structure

```
ERM/
├── docker-compose.local.yml      # Local development
├── docker-compose.ec2.yml        # EC2 production
├── .env.ec2.example              # Environment template
├── local-dev.sh                  # Local helper script
├── deploy-services.sh            # EC2 deployment script
├── .github/workflows/
│   └── deploy-ec2.yml           # GitHub Actions workflow
└── README.md                     # This file
```

---

## 🚀 Deployment Workflow

### Development
```
Code Changes → docker-compose.local.yml → Test Locally → Push to Main
```

### Production
```
Push to Main → GitHub Actions → Copy to EC2 → Deploy Services → Verify
```

### Service Dependencies
```
Discovery Server → Microservices → API Gateway
     ↓                    ↓            ↓
Infrastructure     Business Logic   Entry Point
```

---

## 📞 Support

### Quick Help
- **Local setup**: See [Local Development](#-local-development) section
- **EC2 setup**: See [EC2 Production Deployment](#-ec2-production-deployment) section
- **AWS setup**: See [AWS Setup](#-aws-setup-one-time) section
- **Issues**: See [Troubleshooting](#-troubleshooting) section

### Key Commands
```bash
# Local
docker compose -f docker-compose.local.yml -p erm up -d
docker compose -f docker-compose.local.yml -p erm logs -f
docker compose -f docker-compose.local.yml -p erm down

# EC2
git push origin main  # Automatic deployment
```

---

## ✅ Checklist

### Before Local Development
- [ ] Docker Desktop installed and running
- [ ] Ports 8080-8088 available
- [ ] 2GB+ free disk space

### Before EC2 Deployment
- [ ] AWS resources created (RDS, ElastiCache, EC2)
- [ ] GitHub secrets configured (11 secrets)
- [ ] EC2 has Docker installed
- [ ] Security groups configured

### After Deployment
- [ ] Services accessible at http://localhost:8080 (local)
- [ ] Services accessible at your-EC2-ip:8080 (EC2)
- [ ] All 10 services running
- [ ] Database connections working

---

**Happy deploying!** 🚀
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
 

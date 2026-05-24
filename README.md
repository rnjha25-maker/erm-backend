# ERM Application - Deployment Guide

This guide provides instructions to build, run, and deploy the **ERM (Enterprise Resource Management) Application**. It covers both local development setups and production deployment on AWS EC2.

---

## 📋 Table of Contents
1. [Service Architecture](#-service-architecture)
2. [Project Structure](#-project-structure)
3. [Local Development](#-local-development)
   - [Option A: Docker Compose (Recommended)](#option-a-docker-compose-recommended)
   - [Option B: Native Maven Execution](#option-b-native-maven-execution)
4. [Production / AWS EC2 Deployment](#-production--aws-ec2-deployment)
   - [AWS Infrastructure Setup](#aws-infrastructure-setup-one-time)
   - [GitHub Secrets Configuration](#github-secrets-configuration)
   - [Deployment Workflows](#deployment-workflows)
5. [Troubleshooting & Support](#-troubleshooting--support)
6. [Deployment Checklists](#-deployment-checklists)

---

## 📋 Service Architecture

The ERM application is composed of **10 microservices** built on top of Spring Boot, using a CQRS-inspired architecture (separated Command and Query services).

### Service Port Mapping
* **Discovery Server (Eureka)**: Port `8761`
* **API Gateway**: Port `8080` (Entry point)
* **ERM Backend**: Port `8086` (Core logic)
* **Command Services** (Write Operations):
  * **ERM Command Organization**: Port `8081`
  * **Org Setup Command**: Port `8083`
  * **User Setup**: Port `8084`
  * **Storage Service**: Port `8085`
* **Query Services** (Read Operations):
  * **ERM Query Organization**: Port `8082`
  * **User Query**: Port `8087`
  * **Org Setup Query**: Port `8088`
* **Databases & Cache**:
  * **MySQL Database**: Port `3307` (Default host mapping)
  * **Redis Cache**: Port `6379`

### Deployment & Startup Order
To ensure proper service registration, always start services in this order:
1. **Discovery Server** (Infrastructure layer)
2. **Databases and Cache** (MySQL & Redis)
3. **Command & Query Microservices** (Runs in parallel)
4. **ERM Backend** (Natively or in container)
5. **API Gateway** (Gateway routing depends on active services)

---

## 📊 Project Structure

Below is the directory layout of key deployment and configuration files:

```
ERM/
├── docker-compose.local.yml      # Local development container configuration
├── docker-compose.ec2.yml        # Production EC2 Docker configuration
├── docker-compose.yml            # Core Docker stack definition
├── .env.ec2.example              # Environment variables template for EC2
├── local-dev.sh                  # Local orchestration helper script
├── deploy-services.sh            # Production EC2 deployment script
├── .github/workflows/
│   └── deploy-ec2.yml            # GitHub Actions CI/CD deployment pipeline
└── README.md                     # This file
```

---

## 🏠 Local Development

### Prerequisites
Before starting, ensure your local environment meets these requirements:
* **Docker Desktop** is installed and running
* **Ports 8080 to 8088, 3307, and 6379** are free and available
* Minimum **2GB+ of free disk space** is available

---

### Option A: Docker Compose (Recommended)

This option spins up the entire application stack including preconfigured database and cache containers in under a minute.

#### Quick Start Commands
```bash
# 1. Start all services in background
docker compose -f docker-compose.local.yml -p erm up -d

# 2. View streaming logs
docker compose -f docker-compose.local.yml -p erm logs -f

# 3. Stop and clean up containers
docker compose -f docker-compose.local.yml -p erm down
```

#### Running via Local Helper Script
Alternatively, you can manage the local Docker environment using the `./local-dev.sh` script:
```bash
./local-dev.sh start    # Spin up the complete stack
./local-dev.sh status   # Show status of all services
./local-dev.sh logs     # View service logs
./local-dev.sh rebuild  # Rebuild services after code changes
./local-dev.sh stop     # Stop services
./local-dev.sh clean    # Destroy containers and cleanup resources
```

#### Exposed Local Ports
* **Discovery Server**: `http://localhost:8761`
* **API Gateway (Public)**: `http://localhost:8080`
* **MySQL Database**: `localhost:3307` (Credentials: `root` / password: `2116`)
* **Redis**: `localhost:6379`

#### Container Integration Details
* **Database Init**: First-time startup automatically imports [`docker/mysql/init/erm2.sql`](./docker/mysql/init/erm2.sql) when the database volume is empty.
* **Database Reset**: If you need to re-run the initialization SQL, destroy the database volume using:
  ```bash
  docker compose down -v
  ```
* **Profiles**: Docker containers execute using the pre-configured `dev` Spring profile.
* **Discovery**: Client microservices communicate using `http://discovery-server:8761/eureka/` inside the isolated Docker network.

---

### Option B: Native Maven Execution

Use this approach if you are actively editing individual microservices and prefer to run them natively on your host machine.

#### 1. Configuration Check
Update the configuration files with your local database URL, Redis credentials, and custom port settings:
* **Discovery Server**: `discovery-server/src/main/resources/application.yaml`
* **API Gateway**: `erm-api-gateway/src/main/resources/application.yaml`
* **ERM Backend**: `ERM_backend/src/main/resources/application-dev.yaml`
* **Command Services**: `command/*/src/main/resources/application-dev.yaml`
* **Query Services**: `query/*/src/main/resources/application.properties`

#### 2. Build the Code
Compile and package the maven projects from the root workspace directory:
```bash
mvn clean install
```

#### 3. Start Services Sequentially
Open separate terminal tabs and launch services in the following order:

```bash
# 1. Start the Service Registry
mvn spring-boot:run -pl discovery-server -am

# 2. Start the API Gateway
mvn spring-boot:run -pl erm-api-gateway -am

# 3. Start the Core ERM Backend
mvn spring-boot:run -pl ERM_backend -am

# 4. Start Individual Command and Query Services
mvn spring-boot:run -pl command/user-command -am
# (Repeat the spring-boot:run command for other command and query service directories)
```

#### Running Specific Profiles
By default, native execution uses the default configurations. You can explicitly activate profiles using:
* **Development (`dev`)**: `mvn spring-boot:run -Dspring-boot.run.profiles=dev`
* **QA (`qa`)**: `mvn spring-boot:run -Dspring-boot.run.profiles=qa`

---

## ☁️ Production / AWS EC2 Deployment

### AWS Infrastructure Setup (One-Time)

Run these AWS CLI commands to set up the necessary production resources inside your VPC:

#### 1. Create RDS MySQL Instance
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

#### 2. Create ElastiCache Redis Cluster
```bash
aws elasticache create-cache-cluster \
    --cache-cluster-id erm-redis-cluster \
    --cache-node-type cache.t3.micro \
    --engine redis \
    --num-cache-nodes 1 \
    --security-group-ids sg-xxxxxxxx
```

#### 3. Launch EC2 Compute Instance
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

#### 4. Configure Firewall / Security Groups
* **EC2 Instance**: Allow inbound SSH (`22`) and HTTP/HTTPS (`80` / `443`).
* **RDS MySQL**: Allow inbound traffic on port `3306` only from the EC2 security group.
* **ElastiCache Redis**: Allow inbound traffic on port `6379` only from the EC2 security group.

---

### GitHub Secrets Configuration

To enable automatic CI/CD deployment, configure these **Repository Secrets** under `Settings > Secrets and variables > Actions`:

| Secret Key | Description | Example / Default |
| :--- | :--- | :--- |
| **`EC2_HOST`** | Public IP or DNS address of the EC2 Instance | `54.210.xx.xx` |
| **`EC2_USER`** | SSH username of the EC2 instance | `ec2-user` or `ubuntu` |
| **`EC2_SSH_KEY`** | Complete Private SSH key used to access EC2 | `-----BEGIN RSA PRIVATE KEY-----...` |
| **`EC2_PORT`** | SSH connection port | `22` |
| **`EC2_APP_DIR`** | Deployment directory path on EC2 target server | `/opt/erm` or `/home/ubuntu/erm` |
| **`AWS_REGION`** | AWS Region where resources are hosted | `us-east-1` |
| **`AWS_RDS_ENDPOINT`** | Endpoint URL of the Amazon RDS MySQL database | `erm-mysql-db.xxxx.rds.amazonaws.com` |
| **`AWS_RDS_USERNAME`** | Master username for the RDS database | `admin` |
| **`AWS_RDS_PASSWORD`** | Master password for the RDS database | `your-secure-password` |
| **`AWS_REDIS_ENDPOINT`** | Primary endpoint URL of the ElastiCache Redis cluster | `erm-redis-cluster.xxxx.cache.amazonaws.com` |
| **`AWS_REDIS_PORT`** | Redis connection port | `6379` |
| **`MYSQL_HOST_PORT`** | Host port for mapping MySQL on EC2 | `3307` |

---

### Deployment Workflows

#### A. Automatic CI/CD Deployment (Recommended)
Simply push changes to the main repository branch.
```bash
git push origin main
```
* **Process**: GitHub Actions runs the workflow in `.github/workflows/deploy-ec2.yml`.
* **Duration**: Takes approximately **12 minutes**.
* **Automation steps**: Copies code to EC2, generates `.env.ec2` with environment configurations, triggers a rolling 3-phase deployment, and runs automated verification.

#### B. Manual Deployment
If you need to deploy directly from your machine or debug the EC2 instance, execute these commands:

```bash
# 1. SSH into the EC2 instance
ssh -i key.pem ec2-user@your-ec2-ip

# 2. Navigate to the application folder
cd /opt/erm

# 3. Create the production environment variables file
cat > .env.ec2 << 'EOF'
AWS_REGION=us-east-1
AWS_RDS_ENDPOINT=your-rds-endpoint.rds.amazonaws.com
AWS_RDS_USERNAME=admin
AWS_RDS_PASSWORD=your-password
AWS_REDIS_ENDPOINT=your-redis-endpoint.cache.amazonaws.com
AWS_REDIS_PORT=6379
EOF

# 4. Export environment variables and deploy via Docker Compose
export $(cat .env.ec2 | xargs)
docker compose -f docker-compose.ec2.yml -p erm up -d --build
```

#### Production EC2 Helper Script
Use the `./deploy-services.sh` script on the EC2 server to manage running containers:
```bash
./deploy-services.sh . all      # Deploy/redeploy all microservices
./deploy-services.sh . status   # Inspect deployment health and status
./deploy-services.sh . logs     # View live production container logs
```

---

## 🔍 Troubleshooting & Support

### Local Environment Issues

* **Docker Health Check**: Verify that Docker is running by running `docker --version`.
* **Check Service Registry**: Visit Eureka Dashboard at `http://localhost:8761` to verify if all microservices have successfully registered.
* **Logs Inspection**: Run `docker compose -f docker-compose.local.yml -p erm logs erm-backend` or check the `logs-local/` directory.
* **Complete Reset**: If services are in a broken state, run:
  ```bash
  docker compose -f docker-compose.local.yml -p erm down -v
  docker compose -f docker-compose.local.yml -p erm up -d
  ```

### Production EC2 Issues

* **Pipeline Status**: Review the **GitHub Actions** build logs for build errors.
* **Check Server Connectivity**: Make sure your AWS Security Group allows inbound traffic from GitHub runner IPs or your local IP.
* **AWS Services Verification**: Check RDS MySQL & ElastiCache Redis endpoints inside your AWS Console to ensure they are available.
* **Manual Server Checks**: SSH to the EC2 box and check docker service status:
  ```bash
  ssh -i key.pem ec2-user@your-ec2-ip
  cd /opt/erm
  docker compose -f docker-compose.ec2.yml -p erm ps
  docker compose -f docker-compose.ec2.yml -p erm logs
  ```

### Common Problem Solving Table

| Problem | Cause | Solution |
| :--- | :--- | :--- |
| **Services cannot connect** | Invalid credentials or unreachable endpoints | Check RDS/Redis network security groups, check database passwords in environment configurations. |
| **Containers fail to start** | Out of system disk space | Run `docker system prune -a --volumes -f` to free up space. |
| **Port Conflicts** | Host ports are already occupied | Make sure no local database or application is using ports `8080-8088` or `3307`/`6379`. |
| **AWS Connectivity Error** | VPC or Subnet Security Group mismatch | Ensure both RDS/Redis and EC2 instances are on the same VPC and security groups allow cross-access. |

---

## ✅ Deployment Checklists

### 1. Before Local Development
- [ ] Docker Desktop is installed, running, and has resource allocations (2GB+ space).
- [ ] Local ports `8080-8088`, `3307`, and `6379` are free and unoccupied.

### 2. Before Production EC2 Deployment
- [ ] AWS RDS, ElastiCache, and EC2 instances are successfully provisioned and running.
- [ ] All 11 Repository Secrets are correctly entered in GitHub.
- [ ] Docker and the Docker Compose plugin are installed on the EC2 machine.
- [ ] Security Groups are configured to permit traffic between EC2 and RDS/ElastiCache.

### 3. After Deployment Verification
- [ ] Service registry is visible (Local: `http://localhost:8761` | EC2: `http://your-ec2-ip:8761`).
- [ ] API gateway is responding (Local: `http://localhost:8080` | EC2: `http://your-ec2-ip:8080`).
- [ ] All **10 microservices** are green and showing as registered.
- [ ] Database queries are processing without connection errors.

---

**Happy deploying!** 🚀

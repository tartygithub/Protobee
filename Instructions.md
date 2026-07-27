# Setup and Instructions Guide

This document contains instructions on building, packaging, and running the application in local, Docker, and Kubernetes environments.

## 1. Prerequisites
- **Java Development Kit:** Version 21 (Latest).
- **Build Tool:** Apache Maven 3.8+.
- **Container Tool:** Docker & Docker Compose.
- **Orchestration Tool:** Kubectl and ArgoCD (Included in the packages).

---

## 2. Local Execution (Development Mode)
Build and run the Spring Boot application using Maven:
```bash
# Build the JAR file
mvn clean package -DskipTests

# Run the application
mvn spring-boot:run
```
Open your browser and navigate to:
- **Application Web UI:** `http://localhost:8080/`
- **Swagger UI Console:** `http://localhost:8080/swagger-ui/index.html`

---

## 3. Running in a Docker Container
A multi-stage `Dockerfile` is included to compile the application inside a build environment and package it inside a lightweight JDK 21 alpine runtime.

### Build and Run with Docker:
```bash
# Build the Docker image
docker build -t corporate-doc-processor:1.0.0 .

# Run the container
docker run -d -p 8080:8080 --name doc-processor-app corporate-doc-processor:1.0.0
```

### Run using Docker Compose (Multi-DB Configurable Demo):
A `docker-compose.yml` file is provided which allows booting the application alongside configurable backend databases (e.g., PostgreSQL).
```bash
docker-compose up -d
```

---

## 4. Kubernetes and GitOps (ArgoCD) Deployment
All necessary manifests to deploy the system onto a Kubernetes cluster are provided inside the `k8s/` folder directory.

### Deploy Manifests manually:
```bash
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
```

### ArgoCD GitOps Setup:
An ArgoCD Application manifest is provided in `k8s/argocd-app.yaml`. Apply it to sync your cluster automatically with git:
```bash
kubectl apply -f k8s/argocd-app.yaml
```
ArgoCD will automatically monitor changes in the repository and sync the services, deployment replica sets, and configurations onto your production namespace.

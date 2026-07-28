# Deployment Guide

This document describes the deployment steps for the Doc Processor application using Docker and Kubernetes.

## 1. Prerequisites

- Java 21 JDK (for building the application if not using pre-built image)
- Apache Maven 3.8+
- Docker Engine
- Docker Compose
- Kubernetes cluster access
- `kubectl` configured for the cluster
- Optional: ArgoCD installed in the cluster for GitOps deployment

## 2. Docker Deployment

The repository includes a multi-stage `Dockerfile` that builds the application and packages it into a runtime image.

### 2.1 Build the Docker image

```bash
cd /workspaces/Protobee
docker build -t corporate-doc-processor:1.0.0 .
```

### 2.2 Run the container

```bash
docker run -d \
  -p 8080:8080 \
  --name doc-processor-app \
  corporate-doc-processor:1.0.0
```

### 2.3 Verify the container

```bash
docker ps
```

### 2.4 Access the application

Open a browser to:

- `http://127.0.0.1:8080/login`

### 2.5 Stop and remove the container

```bash
docker stop doc-processor-app
docker rm doc-processor-app
```

## 3. Docker Compose Deployment

A `docker-compose.yml` file is provided for easier local container launch.

### 3.1 Start the stack

```bash
cd /workspaces/Protobee
docker-compose up -d
```

### 3.2 View running services

```bash
docker-compose ps
```

### 3.3 Stop the stack

```bash
docker-compose down
```

### 3.4 Compose environment configuration

The compose service defines environment variables for Spring Boot and application metadata. Modify these values to change datasource settings or metadata values.

## 4. Kubernetes Deployment

The Kubernetes manifests are located in the `k8s/` directory. These include a `ConfigMap`, `Deployment`, `Service`, and optional ArgoCD application manifest.

### 4.1 Build or push the Docker image first

If you are deploying to a local or private cluster, ensure the image is available to the cluster.

For local Docker-based clusters, the image may already be accessible.

For remote clusters, push the image to a registry:

```bash
docker tag corporate-doc-processor:1.0.0 <registry>/corporate-doc-processor:1.0.0
docker push <registry>/corporate-doc-processor:1.0.0
```

Then update the image reference in `k8s/deployment.yaml` if required.

### 4.2 Apply Kubernetes manifests

```bash
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
```

### 4.3 Verify deployment

```bash
kubectl get pods -l app=doc-processor
kubectl get svc doc-processor-service
kubectl describe deployment doc-processor-deployment
```

### 4.4 Access the service

The service exposes port `80` and forwards traffic to container port `8080`.

If your cluster supports `LoadBalancer`, get the external IP:

```bash
kubectl get svc doc-processor-service
```

If using port forwarding:

```bash
kubectl port-forward svc/doc-processor-service 8080:80
```

Then open:

- `http://127.0.0.1:8080/login`

### 4.5 Kubernetes configuration values

The `ConfigMap` provides runtime values via environment variables:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `APP_INFO_TITLE`
- `APP_INFO_DESCRIPTION`
- `APP_INFO_VERSION`
- `APP_INFO_DOCUMENTCODE`
- `APP_AUTH_LDAPENABLED`

Update these values as needed before deployment.

## 5. ArgoCD GitOps Deployment

A GitOps manifest for ArgoCD is available at `k8s/argocd-app.yaml`.

### 5.1 Apply the ArgoCD Application

```bash
kubectl apply -f k8s/argocd-app.yaml
```

### 5.2 Confirm sync status

Use the ArgoCD UI or CLI to verify the application sync state and health.

### 5.3 Update repository and sync

ArgoCD will watch the repository path and deploy changes automatically when the manifests are updated.

## 6. Verification and Troubleshooting

### 6.1 Check logs

```bash
# Docker container logs
docker logs -f doc-processor-app

# Kubernetes pod logs
kubectl logs -f deployment/doc-processor-deployment
```

### 6.2 Confirm application health

Verify the application responds on port 8080:

```bash
curl -I http://127.0.0.1:8080
```

A redirect to `/login` indicates the app is running and secured.

### 6.3 Common issues

- If the browser cannot connect, confirm the service port mapping and firewall settings.
- If the app starts but fails to access the database, check the datasource environment variables.
- If Kubernetes pods fail to start, inspect the pod events and container startup logs.

## 7. Notes

- The default runtime configuration uses an H2 in-memory database.
- For production deployments, replace H2 with a persistent external database in `ConfigMap` or environment configuration.
- Use a registry-accessible Docker image for remote Kubernetes clusters.

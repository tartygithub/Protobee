# Installation Guide

This document describes how to build, run, and install the Doc Processor application using Docker and Kubernetes.

## 1. Prerequisites

- Java 21 JDK (for local build in Docker image)
- Apache Maven 3.8+
- Docker Engine
- Docker Compose
- Kubernetes cluster access
- `kubectl` configured for the target cluster
- Optional: ArgoCD installed in the target cluster for GitOps deployment

## 2. Build and Run with Docker

The repository includes a multi-stage `Dockerfile`.

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

### 2.3 Access the application

Open the browser to:

- `http://127.0.0.1:8080/login`

The application redirects to the login page because Spring Security is enabled.

### 2.4 Default user credentials

The application includes a startup hook that creates a default user if it does not exist:

- username: `admin`
- password: `admin123`

## 3. Run with Docker Compose

A `docker-compose.yml` file is available for simplified container run.

### 3.1 Start the application

```bash
cd /workspaces/Protobee
docker-compose up -d
```

### 3.2 Verify startup

```bash
docker-compose ps
```

### 3.3 Stop the application

```bash
docker-compose down
```

### 3.4 Configuration

The compose service defines environment variables for the application, including datasource configuration and app metadata. If you want to use a different database, replace the `SPRING_DATASOURCE_*` values or provide a separate database service.

## 4. Kubernetes Deployment

The `k8s/` folder contains the Kubernetes manifests required to deploy the application.

### 4.1 Confirm the manifests

- `k8s/configmap.yaml`
- `k8s/deployment.yaml`
- `k8s/service.yaml`
- `k8s/argocd-app.yaml`

### 4.2 Deploy manually with kubectl

```bash
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
```

### 4.3 Verify deployment

```bash
kubectl get pods -l app=doc-processor
kubectl get svc doc-processor-service
```

### 4.4 Access the service

The service exposes port `80` and forwards to container port `8080`.

- If your cluster supports `LoadBalancer`, use the external IP returned by:

```bash
kubectl get svc doc-processor-service
```

- If using port forwarding:

```bash
kubectl port-forward svc/doc-processor-service 8080:80
```

Then open:

- `http://127.0.0.1:8080/login`

### 4.5 Kubernetes environment settings

The `ConfigMap` defines runtime environment variables that map to Spring Boot properties:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `APP_INFO_TITLE`
- `APP_INFO_DESCRIPTION`
- `APP_INFO_VERSION`
- `APP_INFO_DOCUMENTCODE`
- `APP_AUTH_LDAPENABLED`

Modify these values as needed before applying the manifests.

## 5. ArgoCD GitOps Deployment

If ArgoCD is installed in the cluster, the `k8s/argocd-app.yaml` manifest can be used to register the application.

### 5.1 Apply the ArgoCD application

```bash
kubectl apply -f k8s/argocd-app.yaml
```

### 5.2 Confirm sync status

Use the ArgoCD UI or CLI to confirm the application syncs successfully.

### 5.3 Notes

The ArgoCD manifest points at `https://github.com/example/doc-processor.git` and the `k8s` path. Update `repoURL` if your repository location differs.

## 6. Notes and Troubleshooting

- The application uses an in-memory H2 database by default, which is ideal for quick demos but not persistent.
- For production, configure a persistent external database and update `SPRING_DATASOURCE_URL` / driver settings.
- If the container is listening on `8080` but the browser cannot reach it, verify Docker port mapping or Kubernetes service exposure.
- To view logs in Docker:

```bash
docker logs -f doc-processor-app
```

- To view pod logs in Kubernetes:

```bash
kubectl logs -f deployment/doc-processor-deployment
```

## 7. Summary

- Docker build: `docker build -t corporate-doc-processor:1.0.0 .`
- Docker run: `docker run -d -p 8080:8080 --name doc-processor-app corporate-doc-processor:1.0.0`
- Docker Compose: `docker-compose up -d`
- Kubernetes: `kubectl apply -f k8s/configmap.yaml -f k8s/deployment.yaml -f k8s/service.yaml`
- ArgoCD: `kubectl apply -f k8s/argocd-app.yaml`

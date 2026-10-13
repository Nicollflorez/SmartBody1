#!/usr/bin/env sh
# Despliega Smartbody en el cluster actual de kubectl (Etapa 6 + 7).
#
#   k8s/desplegar.sh                                   # imagen :latest del Container Registry
#   BACKEND_IMAGE=registry.gitlab.com/nicollflorez/smartbody/backend:1.0.3 k8s/desplegar.sh
#
# 1. ConfigMaps con los scripts SQL (/database) y la configuracion de Nginx (/devops/nginx).
# 2. Secret con el par de claves RSA del JWT (se genera una sola vez y lo comparten todas las replicas).
# 3. kubectl apply -k k8s/base y espera a que todo este listo.
set -eu

RAIZ=$(cd "$(dirname "$0")/.." && pwd)
NS=smartbody

kubectl apply -f "$RAIZ/k8s/base/namespace.yaml"

kubectl -n $NS create configmap smartbody-sql --from-file="$RAIZ/database" \
    --dry-run=client -o yaml | kubectl apply -f -
kubectl -n $NS create configmap smartbody-nginx --from-file="$RAIZ/devops/nginx" \
    --dry-run=client -o yaml | kubectl apply -f -
kubectl -n $NS create configmap smartbody-prometheus --from-file="$RAIZ/devops/prometheus" \
    --dry-run=client -o yaml | kubectl apply -f -
kubectl -n $NS create configmap smartbody-grafana-datasources \
    --from-file="$RAIZ/devops/grafana/provisioning/datasources" --dry-run=client -o yaml | kubectl apply -f -
kubectl -n $NS create configmap smartbody-grafana-dashboards \
    --from-file="$RAIZ/devops/grafana/provisioning/dashboards" --dry-run=client -o yaml | kubectl apply -f -

if ! kubectl -n $NS get secret smartbody-jwt-keys >/dev/null 2>&1; then
    TMP=$(mktemp -d)
    openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out "$TMP/private_key.pem" 2>/dev/null
    openssl rsa -pubout -in "$TMP/private_key.pem" -out "$TMP/public_key.pem" 2>/dev/null
    kubectl -n $NS create secret generic smartbody-jwt-keys \
        --from-file="$TMP/private_key.pem" --from-file="$TMP/public_key.pem"
    rm -rf "$TMP"
    echo "Claves RSA del JWT generadas en el Secret smartbody-jwt-keys."
fi

kubectl apply -k "$RAIZ/k8s/base"

if [ -n "${BACKEND_IMAGE:-}" ]; then
    kubectl -n $NS set image deployment/backend backend="$BACKEND_IMAGE"
fi
# BACKEND_REPLICAS=1 en clusters pequenos (por ejemplo el kind del pipeline).
if [ -n "${BACKEND_REPLICAS:-}" ]; then
    kubectl -n $NS scale deployment/backend --replicas="$BACKEND_REPLICAS"
fi

kubectl -n $NS rollout status statefulset/mysql --timeout=300s
kubectl -n $NS rollout status deployment/backend --timeout=420s
kubectl -n $NS rollout status deployment/nginx --timeout=120s
kubectl -n $NS rollout status deployment/prometheus --timeout=120s
kubectl -n $NS rollout status deployment/grafana --timeout=180s
kubectl -n $NS get pods,svc
echo "Smartbody desplegado: API http://<nodo>:30080/api/ping - Grafana http://<nodo>:30300"

#!/usr/bin/env sh
# Despliega Smartbody en microservicios en el cluster actual de kubectl.
#
#   k8s/desplegar-microservicios.sh                       # imagenes :latest del Container Registry
#   VERSION=1.0.4-SNAPSHOT k8s/desplegar-microservicios.sh
#
# 1. ConfigMaps: scripts SQL de los tres esquemas, Nginx como gateway, Prometheus y Grafana.
# 2. Secrets: par de claves RSA del JWT (una sola vez) y token interno entre servicios.
# 3. kubectl apply -k k8s/microservicios y espera a que todo este listo.
set -eu

RAIZ=$(cd "$(dirname "$0")/.." && pwd)
NS=smartbody
REGISTRO=${REGISTRO:-registry.gitlab.com/nicollflorez/smartbody}

kubectl apply -f "$RAIZ/k8s/comun/namespace.yaml"

kubectl -n $NS create configmap smartbody-sql --from-file="$RAIZ/database/microservicios" \
    --dry-run=client -o yaml | kubectl apply -f -
kubectl -n $NS create configmap smartbody-nginx \
    --from-file=nginx.conf="$RAIZ/devops/nginx/gateway.conf" \
    --from-file=proxy_params_smartbody="$RAIZ/devops/nginx/proxy_params_smartbody" \
    --dry-run=client -o yaml | kubectl apply -f -
kubectl -n $NS create configmap smartbody-prometheus \
    --from-file=prometheus.yml="$RAIZ/devops/prometheus/prometheus.microservicios.yml" \
    --from-file=alertas.yml="$RAIZ/devops/prometheus/alertas.yml" \
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
if ! kubectl -n $NS get secret smartbody-interno >/dev/null 2>&1; then
    kubectl -n $NS create secret generic smartbody-interno --from-literal=token="$(openssl rand -hex 32)"
    echo "Token interno entre servicios generado en el Secret smartbody-interno."
fi

# Con VERSION se aplica directamente la etiqueta de imagen pedida (un solo rollout).
if [ -n "${VERSION:-}" ]; then
    kubectl kustomize "$RAIZ/k8s/microservicios" \
        | sed -E "s#registry\.gitlab\.com/nicollflorez/smartbody/(auth|planes|progreso|admin):latest#$REGISTRO/\1:$VERSION#g" \
        | kubectl apply -f -
else
    kubectl apply -k "$RAIZ/k8s/microservicios"
fi

kubectl -n $NS rollout status statefulset/mysql --timeout=300s
for s in auth planes progreso admin; do
    kubectl -n $NS rollout status deployment/$s --timeout=600s
done
kubectl -n $NS rollout status deployment/nginx --timeout=120s
kubectl -n $NS rollout status deployment/prometheus --timeout=120s
kubectl -n $NS rollout status deployment/grafana --timeout=180s
kubectl -n $NS get pods,svc
echo "Smartbody (microservicios) desplegado: API http://<nodo>:30080/api/ping - Grafana http://<nodo>:30300"

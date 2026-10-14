#!/usr/bin/env bash
# Borra del proyecto de GitLab los pipelines fallidos, omitidos o cancelados (conserva los exitosos).
#
#   GITLAB_TOKEN=glpat-xxxx devops/gitlab/limpiar-pipelines.sh            # borra
#   GITLAB_TOKEN=glpat-xxxx devops/gitlab/limpiar-pipelines.sh --listar   # solo muestra lo que borraria
#
# Token: Settings > Access tokens > rol Maintainer, scope "api" (revocalo despues de usarlo).
set -euo pipefail

PROYECTO=${GITLAB_PROJECT_ID:-87583815}
API="https://gitlab.com/api/v4/projects/$PROYECTO/pipelines"
SOLO_LISTAR=${1:-}

if [ -z "${GITLAB_TOKEN:-}" ]; then
    echo "Define GITLAB_TOKEN (token con scope api)." >&2
    exit 1
fi

borrados=0
for estado in failed skipped canceled; do
    # Se pagina hasta que no queden pipelines en ese estado.
    while :; do
        ids=$(curl -sf --header "PRIVATE-TOKEN: $GITLAB_TOKEN" "$API?status=$estado&per_page=100" \
              | grep -oE '"id":[0-9]+' | cut -d: -f2)
        [ -z "$ids" ] && break
        for id in $ids; do
            if [ "$SOLO_LISTAR" = "--listar" ]; then
                echo "$estado  https://gitlab.com/Nicollflorez/smartbody/-/pipelines/$id"
            else
                curl -sf -X DELETE --header "PRIVATE-TOKEN: $GITLAB_TOKEN" "$API/$id" >/dev/null \
                    && echo "borrado $estado #$id" || echo "no se pudo borrar #$id" >&2
            fi
            borrados=$((borrados + 1))
        done
        [ "$SOLO_LISTAR" = "--listar" ] && break
    done
done
echo "pipelines procesados: $borrados"

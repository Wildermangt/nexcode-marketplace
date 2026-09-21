#!/usr/bin/env bash
# ---------------------------------------------------------------------------
#  NEXCODE Marketplace - paso 2: habilitar Firebase Storage
#
#  Crea el bucket por defecto del proyecto y publica storage.rules.
#  Requiere: firebase-tools con sesion iniciada (firebase login) y que el
#  proyecto este en plan Blaze (Google exige facturacion activa para crear
#  buckets, aunque el uso siga dentro de la capa gratuita).
#
#  Uso:  bash herramientas/habilitar_storage.sh
# ---------------------------------------------------------------------------
set -euo pipefail

PROYECTO="nexcode-marketplace"
REGION="us-central1"
RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

token() {
  python -c "import json,os;print(json.load(open(os.path.expanduser('~/.config/configstore/firebase-tools.json')))['tokens']['access_token'])"
}

echo "== 1/4  Comprobando la sesion de Firebase =="
firebase login:list >/dev/null
TOK="$(token)"

echo "== 2/4  Comprobando el plan del proyecto =="
FACTURACION=$(curl -s -H "Authorization: Bearer $TOK" \
  "https://cloudbilling.googleapis.com/v1/projects/$PROYECTO/billingInfo" \
  | python -c "import sys,json;print(json.load(sys.stdin).get('billingEnabled'))")

if [ "$FACTURACION" != "True" ]; then
  cat <<'AVISO'
El proyecto sigue en plan Spark y Google no deja crear el bucket sin una
cuenta de facturacion activa.

  1. Abrir https://console.firebase.google.com/project/nexcode-marketplace/usage/details
  2. "Modificar plan" -> Blaze, y asociar una cuenta de facturacion abierta.
  3. Volver a ejecutar este script.

Mientras tanto la aplicacion funciona: las fotografias se leen de
app/src/main/assets/productos con el prefijo asset:// (ver CargadorImagenes).
AVISO
  exit 1
fi

echo "== 3/4  Creando el bucket por defecto ($REGION) =="
RESPUESTA=$(curl -s -X POST -H "Authorization: Bearer $TOK" \
  -H "Content-Type: application/json" \
  -d "{\"location\":\"$REGION\"}" \
  "https://firebasestorage.googleapis.com/v1beta/projects/$PROYECTO/defaultBucket")
echo "$RESPUESTA"

echo "== 4/4  Publicando storage.rules =="
cd "$RAIZ"
firebase deploy --only storage --project "$PROYECTO"

echo
echo "Listo. Verificacion:"
curl -s -H "Authorization: Bearer $(token)" \
  "https://firebasestorage.googleapis.com/v1beta/projects/$PROYECTO/buckets"
echo
echo "Ahora, con el catalogo vacio, entrar con la cuenta de administrador para"
echo "que SembradorCatalogo suba las 20 fotografias y guarde sus URL https://."

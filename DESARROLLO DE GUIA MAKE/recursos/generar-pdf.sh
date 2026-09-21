#!/bin/sh
# Regenera INSTRUCTIVO.pdf a partir de INSTRUCTIVO.md
# Uso: sh recursos/generar-pdf.sh   (desde la carpeta DESARROLLO DE GUIA MAKE)
set -e
python - <<'PY'
import io, markdown, re, base64, os
md = io.open('INSTRUCTIVO.md', encoding='utf-8').read()
body = markdown.markdown(md, extensions=['tables','fenced_code','attr_list'])
def embed(m):
    src = m.group(1)
    if src.startswith('http'): return m.group(0)
    p = src.replace('/', os.sep)
    if not os.path.exists(p): return m.group(0)
    return 'src="data:image/png;base64,%s"' % base64.b64encode(open(p,'rb').read()).decode()
body = re.sub(r'src="([^"]+)"', embed, body)
CSS = open('recursos/pdf.css', encoding='utf-8').read()
io.open('INSTRUCTIVO.html','w',encoding='utf-8').write(
  '<!DOCTYPE html><html lang="es"><head><meta charset="utf-8"><title>Instructivo</title>'
  '<style>%s</style></head><body>%s</body></html>' % (CSS, body))
print('HTML listo')
PY
DIR=$(pwd -W 2>/dev/null || pwd)
"/c/Program Files/Google/Chrome/Application/chrome.exe" --headless --disable-gpu \
  --no-pdf-header-footer --user-data-dir="$TEMP/chrome-pdf-tmp" \
  --print-to-pdf="$DIR/INSTRUCTIVO.pdf" "file:///$DIR/INSTRUCTIVO.html"
echo "PDF regenerado"

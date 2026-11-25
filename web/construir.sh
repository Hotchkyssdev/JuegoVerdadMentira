#!/usr/bin/env bash
set -euo pipefail

raiz="$(cd "$(dirname "$0")/.." && pwd)"
temporal="$(mktemp -d)"
trap 'rm -rf "$temporal"' EXIT

herramienta_jar() {
    if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/jar" || -x "${JAVA_HOME:-}/bin/jar.exe" ]]; then
        echo "$JAVA_HOME/bin/jar"
    elif command -v jar >/dev/null; then
        command -v jar
    else
        local hogar
        hogar="$(java -XshowSettings:properties -version 2>&1 | sed -n 's/^ *java.home = //p' | tr -d '
')"
        echo "$hogar/bin/jar"
    fi
}

mapfile -t fuentes < <(find "$raiz/src" -name '*.java')
javac --release 11 -encoding UTF-8 -d "$temporal/clases" "${fuentes[@]}"
(cd "$raiz/src" && find . -type f ! -name '*.java' -exec cp --parents {} "$temporal/clases" \;)
"$(herramienta_jar)" --create --file "$raiz/web/verdad-o-mentira.jar" --main-class juego.Main -C "$temporal/clases" .
echo "Listo: web/verdad-o-mentira.jar"

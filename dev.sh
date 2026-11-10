#!/usr/bin/env bash
# Sobe backend (Spring Boot) e frontend (Vite) juntos, numa só janela.
# Ctrl+C para os dois.
set -euo pipefail

cd "$(dirname "$0")"

cleanup() {
  echo
  echo "Parando backend e frontend..."
  kill "$BACKEND_PID" "$FRONTEND_PID" 2>/dev/null || true
  wait "$BACKEND_PID" "$FRONTEND_PID" 2>/dev/null || true
}
trap cleanup EXIT INT TERM

echo "Iniciando backend (Spring Boot) em http://localhost:8080 ..."
(cd backend && ./gradlew bootRun) &
BACKEND_PID=$!

echo "Iniciando frontend (Vite) em http://localhost:5173 ..."
(cd frontend && npm run dev) &
FRONTEND_PID=$!

wait

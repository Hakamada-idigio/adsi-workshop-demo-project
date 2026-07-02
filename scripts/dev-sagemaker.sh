#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
PIDFILE="$PROJECT_DIR/.sagemaker.pids"
BASE_PATH="/codeeditor/default/absports/3000"

export SAGEMAKER=1
export NEXT_PUBLIC_BASE_PATH="$BASE_PATH"

cleanup() {
  echo ""
  echo "=== Stopping SageMaker dev processes ==="
  if [ -f "$PIDFILE" ]; then
    while IFS= read -r pid; do
      if kill -0 "$pid" 2>/dev/null; then
        kill "$pid" 2>/dev/null && echo "Stopped PID $pid"
      fi
    done < "$PIDFILE"
    rm -f "$PIDFILE"
  fi
  # Kill any leftover processes on our ports
  for port in 3000 3001 8080; do
    lsof -ti :"$port" 2>/dev/null | xargs -r kill 2>/dev/null || true
  done
  echo "Done."
}

stop_existing() {
  if [ -f "$PIDFILE" ]; then
    echo "=== Stopping previous session ==="
    while IFS= read -r pid; do
      if kill -0 "$pid" 2>/dev/null; then
        kill "$pid" 2>/dev/null && echo "Stopped PID $pid"
      fi
    done < "$PIDFILE"
    rm -f "$PIDFILE"
  fi
  for port in 3000 3001 8080; do
    lsof -ti :"$port" 2>/dev/null | xargs -r kill 2>/dev/null || true
  done
}

usage() {
  echo "Usage: $0 [command]"
  echo ""
  echo "Commands:"
  echo "  start   Build frontend & start all services (default)"
  echo "  stop    Stop all running services"
  echo "  restart Rebuild & restart all services"
  echo ""
  echo "Services started:"
  echo "  - Backend:  Spring Boot (workshop profile, H2) on :8080"
  echo "  - Frontend: Next.js on :3001"
  echo "  - Proxy:    SageMaker proxy :3000 → :3001"
}

do_start() {
  stop_existing

  echo ""
  echo "=== Building frontend ==="
  cd "$PROJECT_DIR/packages/frontend"
  npx next build

  echo ""
  echo "=== Starting backend (workshop profile) ==="
  cd "$PROJECT_DIR/packages/backend"
  ./gradlew bootRun --args='--spring.profiles.active=workshop' &
  BACKEND_PID=$!
  echo "$BACKEND_PID" > "$PIDFILE"
  echo "Backend PID: $BACKEND_PID"

  echo ""
  echo "=== Starting frontend (Next.js on :3001) ==="
  cd "$PROJECT_DIR/packages/frontend"
  npx next start -p 3001 &
  FRONTEND_PID=$!
  echo "$FRONTEND_PID" >> "$PIDFILE"
  echo "Frontend PID: $FRONTEND_PID"

  echo ""
  echo "=== Starting SageMaker proxy (:3000 → :3001) ==="
  node "$SCRIPT_DIR/sagemaker-proxy.mjs" &
  PROXY_PID=$!
  echo "$PROXY_PID" >> "$PIDFILE"
  echo "Proxy PID: $PROXY_PID"

  trap cleanup EXIT INT TERM

  echo ""
  echo "=========================================="
  echo "  All services running!"
  echo "  Access: https://<studio-domain>${BASE_PATH}/"
  echo "  Press Ctrl+C to stop all services"
  echo "=========================================="
  echo ""

  wait
}

CMD="${1:-start}"

case "$CMD" in
  start)
    do_start
    ;;
  stop)
    stop_existing
    echo "All services stopped."
    ;;
  restart)
    do_start
    ;;
  -h|--help|help)
    usage
    ;;
  *)
    echo "Unknown command: $CMD"
    usage
    exit 1
    ;;
esac

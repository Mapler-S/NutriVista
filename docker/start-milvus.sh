#!/bin/bash
# NutriVista - Milvus 启动脚本
# 运行前确保 Docker Desktop 已启动
# 在 Git Bash / WSL 中执行: bash docker/start-milvus.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="$SCRIPT_DIR/docker-compose.yml"

case "${1:-start}" in
  start)
    echo ">>> 启动 Milvus 环境..."
    docker compose -f "$COMPOSE_FILE" up -d
    echo ""
    echo ">>> 等待 Milvus 就绪 (最多 90 秒)..."
    for i in $(seq 1 18); do
      if curl -sf http://localhost:9091/healthz > /dev/null 2>&1; then
        echo ">>> Milvus 已就绪！"
        echo ""
        echo "  gRPC 端口: localhost:19530"
        echo "  REST 端口: localhost:9091"
        echo "  MinIO 控制台: http://localhost:9001  (minioadmin / minioadmin)"
        exit 0
      fi
      echo "    等待中... ($((i * 5))s)"
      sleep 5
    done
    echo "!!! Milvus 启动超时，请检查: docker compose -f $COMPOSE_FILE logs standalone"
    exit 1
    ;;
  stop)
    echo ">>> 停止 Milvus 环境..."
    docker compose -f "$COMPOSE_FILE" down
    ;;
  clean)
    echo ">>> 停止并删除所有数据卷..."
    docker compose -f "$COMPOSE_FILE" down -v
    ;;
  status)
    docker compose -f "$COMPOSE_FILE" ps
    ;;
  logs)
    docker compose -f "$COMPOSE_FILE" logs -f standalone
    ;;
  *)
    echo "用法: $0 {start|stop|clean|status|logs}"
    exit 1
    ;;
esac

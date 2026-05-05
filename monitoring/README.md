# Monitoramento Basico (Prometheus)

Suba backend + monitoramento no mesmo projeto Docker Compose:

```bash
docker compose -f docker-compose.yml -f docker-compose.monitoring.yml up -d --build
```

Depois acesse:

- Prometheus: `http://SEU_IP:9090`
- Health backend: `http://SEU_IP:8080/actuator/health`
- Metrics backend: `http://SEU_IP:8080/actuator/prometheus`

Consultas uteis no Prometheus:

- `up{job="soulsurf-backend"}` (backend no ar)
- `up{job="soulsurf-node"}` (node exporter no ar)
- `process_resident_memory_bytes` (memoria do processo Java)
- `system_cpu_usage` (uso de CPU da aplicacao)

Observacao:

- O `node-exporter` foi configurado para Linux (ideal para sua VM Oracle Cloud).
- O Prometheus ficou com retencao curta (`3d`, limite `512MB`) para ficar leve na VM de 1GB.

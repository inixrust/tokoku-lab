# wrk2 (open model, -R wajib) + wrk asli (closed model, untuk D12.1).
# Windows tidak punya wrk2 native, jadi dijalankan lewat Docker (rencana §2 temuan #8).
FROM debian:bookworm-slim AS build
RUN apt-get update && apt-get install -y --no-install-recommends \
        build-essential git ca-certificates libssl-dev zlib1g-dev unzip \
    && rm -rf /var/lib/apt/lists/*
RUN git clone --depth 1 https://github.com/giltene/wrk2.git /src/wrk2 && make -C /src/wrk2 \
    && git clone --depth 1 --branch 4.2.0 https://github.com/wg/wrk.git /src/wrk && make -C /src/wrk WITH_OPENSSL=/usr

FROM debian:bookworm-slim
RUN apt-get update && apt-get install -y --no-install-recommends libssl3 zlib1g ca-certificates \
    && rm -rf /var/lib/apt/lists/*
COPY --from=build /src/wrk2/wrk /usr/local/bin/wrk2
COPY --from=build /src/wrk/wrk /usr/local/bin/wrk
WORKDIR /scripts
ENTRYPOINT ["wrk2"]

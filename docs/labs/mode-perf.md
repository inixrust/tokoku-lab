# Mode perf (Hari 5–6)

Mulai Hari 5, service berjalan **di container** dengan batas `cpus`/`mem_limit` tetap, supaya hasil pengukuran bisa
diulang dan dibandingkan. Checkpoint awal: `L12-perf` (dipakai L12.1, L12.2, dan L13.1).

## Menyalakan

```powershell
git fetch --tags
git checkout L12-perf
.\mvnw.cmd package -DskipTests
docker compose -f infra/compose.yaml --profile db --profile kafka --profile obs --profile security --profile apps up -d --build --wait
.\scripts\seed-perf.ps1        # 10.000 order alice/bob (setelah order-service start sekali)
```

Hentikan service yang masih jalan dari IDE dulu: port host sama (8081–8084, 9081–9084).

| Service | Container | Host (app / management) | Batas |
|---|---|---|---|
| product-service | `product-service:8080` | 8081 / 9081 | 2 CPU, 1 GiB |
| order-service | `order-service:8080` | 8082 / 9082 | 2 CPU, 1 GiB |
| inventory-service | `inventory-service:8080` | 8083 / 9083 | 2 CPU, 1 GiB |
| notification-service | `notification-service:8080` | 8084 / 9084 | 1 CPU, 512 MiB |
| PostgreSQL | `postgres:5432` | 5432 | 2 CPU |

- Image memakai base **`eclipse-temurin:21-jdk`** (bukan `-jre`), supaya `jcmd` dan `jfr` tersedia di dalam container.
- Alamat antar-container memakai nama service: `postgres`, `kafka:29092`, `lgtm:4318`.
- JWT: `issuer-uri` tetap `http://localhost:8180/realms/tokoku` (sama dengan klaim `iss`), sedangkan kunci diambil
  lewat jaringan internal (`jwk-set-uri=http://keycloak:8080/...`).
- `JAVA_TOOL_OPTIONS` kosong: heap memakai default JVM (25% dari limit). Diatur di L13.1.

## Load generator

wrk2 berjalan di container dan bergabung ke jaringan compose:

```powershell
$alice = .\scripts\get-token.ps1 -User alice     # berumur 30 menit: ambil ulang sebelum setiap sesi ukur
.\loadtest\wrk2\run.ps1 -Network tokoku_default -Rate 200 -Duration 60s -Url http://order-service:8080/api/orders -Token $alice
```

JMeter: `.\loadtest\jmeter\run.ps1` (JMeter lokal, atau `-Docker` untuk image `alpine/jmeter:5.6.3`).

## Tips mengukur di laptop

- Tutup aplikasi lain (browser dengan banyak tab, IDE indexing). Bandingkan hasil secara **relatif**, bukan absolut.
- Warm-up 1–2 menit sebelum setiap pengukuran. Ulangi 3×.
- Status non-2xx berarti header/token salah: hasil uji tidak dipakai.

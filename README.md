# TokoKu Lab

Repositori lab untuk pelatihan **Spring Boot 4 & Java 21: Microservices dan Performance Tuning**.
Semua lab membangun satu sistem: **TokoKu** (product, order + payment, inventory, notification).

| Komponen | Versi |
|---|---|
| Spring Boot | 4.1.2 (Framework 7, Jakarta EE 11) |
| Spring Cloud | 2025.1.4 |
| JDK | 21 (default) dan 25 |
| Resilience4j | 2.4.0 (core) |
| PostgreSQL | 17 |

## Persiapan (H-7)

```powershell
git clone <url-repo> tokoku-lab
cd tokoku-lab
.\scripts\prepull.ps1      # unduh image Docker + dependency Maven
.\scripts\verify-env.ps1   # harus HIJAU (tanpa [FAIL])
```

Bila PowerShell menolak skrip: `powershell -ExecutionPolicy Bypass -File .\scripts\verify-env.ps1`.

## Menyalakan infrastruktur

```powershell
docker compose -f infra/compose.yaml --profile db up -d
```

Profil per hari ada di panduan lab. Service dijalankan dari IDE (IntelliJ: folder `.run/`, VS Code: `.vscode/launch.json`).

| Service | Port app / management | Schema / user DB |
|---|---|---|
| product-service | 8081 / 9081 | `product` / `product_user` |
| order-service | 8082 / 9082 | `orders` / `order_user` |
| inventory-service | 8083 / 9083 | `inventory` / `inventory_user` |
| notification-service | 8084 / 9084 | — |

## Checkpoint

Setiap lab punya dua tag: `<lab>-start` dan `<lab>-solution`, dirilis bertahap oleh instruktur (`start` saat lab
dimulai, `solution` setelah lab dibahas). Tertinggal? Lompat ke solusi lab sebelumnya:

```powershell
git fetch --tags             # ambil checkpoint terbaru
git stash -u                 # simpan pekerjaan Anda (opsional)
git checkout L3.1-solution
.\scripts\reset-db.ps1       # kosongkan database agar Flyway mulai bersih
```

Setelah `git checkout <tag>`, Git berada di *detached HEAD*. Commit yang dibuat di sana tidak dimiliki branch mana
pun dan tidak terlihat lagi setelah pindah checkpoint; bila ingin menyimpan commit sendiri, buat branch dulu:
`git switch -c kerja/<lab>`.

Panduan setiap lab ada di [docs/labs/](docs/labs/).

| Hari | Lab |
|---|---|
| 1 | [L3.1](docs/labs/L3.1.md) product-service di Boot 4.1 |
| 2 | [L5.1](docs/labs/L5.1.md) API product berversi + RFC 9457 |
| 2 | [L6.1](docs/labs/L6.1.md) Persistence order-service |
| 2 | [L8.1-A](docs/labs/L8.1-A.md) order → inventory via `@HttpExchange` |
| 3 | [L7.1](docs/labs/L7.1.md) Saga order → inventory (outbox, Kafka) |

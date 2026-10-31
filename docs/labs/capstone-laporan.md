# Laporan capstone · Kelompok: ________

## 1. Baseline (sebelum perubahan apa pun)

Kondisi: commit `________`, mode perf, image JDK 21, data 200.000 order, warm-up ___ menit.

| Endpoint | -R | Run | Achieved rps | p50 | p99 | p99.9 | max | Non-2xx | CPU order | Heap order | Query/request | Hikari pending |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| GET /api/orders | | 1 | | | | | | | | | | |
| GET /api/orders | | 2 | | | | | | | | | | |
| GET /api/orders | | 3 | | | | | | | | | | |
| POST /api/orders | | 1 | | | | | | | | | | |
| POST /api/orders | | 2 | | | | | | | | | | |
| POST /api/orders | | 3 | | | | | | | | | | |

## 2. Log eksperimen (satu baris = satu perubahan = satu commit)

| # | Hipotesis (dari data apa?) | Perubahan | Commit | Hasil (p50/p99/p99.9/max, CPU, heap, query) | Dipertahankan? |
|---|---|---|---|---|---|
| 1 | | | | | |
| 2 | | | | | |
| 3 | | | | | |
| 4 | | | | | |

## 3. Akhir vs baseline

| Endpoint | -R | p50 | p99 | p99.9 | max | CPU | Heap | Query/request |
|---|---|---|---|---|---|---|---|---|
| GET /api/orders (baseline) | | | | | | | | |
| GET /api/orders (akhir) | | | | | | | | |
| POST /api/orders (baseline) | | | | | | | | |
| POST /api/orders (akhir) | | | | | | | | |

## 4. Trade-off dan langkah berikutnya

- 

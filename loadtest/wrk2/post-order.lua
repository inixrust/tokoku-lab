-- POST /api/orders dengan body acak (product 1..18, qty 1..2) agar stok tidak cepat habis di satu product.
-- Sejak L11.1 wajib token user (alice): run.ps1 -Token $alice. customerId diambil dari token, bukan dari body.
math.randomseed(os.time())

wrk.method = "POST"
wrk.headers["Content-Type"] = "application/json"

request = function()
  local body = string.format(
    '{"lines":[{"productId":%d,"quantity":%d}]}',
    math.random(1, 18), math.random(1, 2))
  return wrk.format(nil, nil, nil, body)
end

-- Cek baris "Non-2xx or 3xx responses" di output: harus tidak ada agar hasil uji dipakai (rencana L12.1).

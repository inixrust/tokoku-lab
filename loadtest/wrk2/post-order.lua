-- POST /api/orders dengan body acak (product 1..18, qty 1..2) agar stok tidak cepat habis di satu product.
-- Header tambahan (mis. Authorization: Bearer ...) dikirim run.ps1 lewat -H.
math.randomseed(os.time())

wrk.method = "POST"
wrk.headers["Content-Type"] = "application/json"

local customer = os.getenv("CUSTOMER_ID") or "7f2c1a4e-0b1d-4c55-9a6e-a11ce0000001"

request = function()
  local body = string.format(
    '{"customerId":"%s","lines":[{"productId":%d,"quantity":%d}]}',
    customer, math.random(1, 18), math.random(1, 2))
  return wrk.format(nil, nil, nil, body)
end

-- Cek baris "Non-2xx or 3xx responses" di output: harus tidak ada agar hasil uji dipakai (rencana L12.1).

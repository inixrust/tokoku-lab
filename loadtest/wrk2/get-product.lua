-- GET /api/products/{id} dengan id acak 1..20 (L17.1). Header API-Version wajib sejak L5.1.
math.randomseed(os.time())
wrk.headers["API-Version"] = "1"

request = function()
  return wrk.format("GET", "/api/products/" .. math.random(1, 20))
end

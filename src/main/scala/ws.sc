import redis.clients.jedis.UnifiedJedis

import java.nio.file.Files
import java.nio.file.Paths

import com.google.gson.Gson
import com.google.gson.JsonObject

val gson = new Gson

val reader = Files.newBufferedReader(Paths.get("Users/tiagoooliveira/Documents/dev/scala/akka-http-quickstart-scala/src/main/resources/patents-13062022.json"))
val pat = gson.fromJson(reader, classOf[JsonObject])
println(pat.keySet())
pat.keySet().forEach(a => println(pat.get(a)))
println(pat.keySet().size())
println(pat.isJsonObject)
println(pat.get("WO2021147165A1"))


//val unifiedJedis: UnifiedJedis = new UnifiedJedis("redis://localhost:6379")

//val r = unifiedJedis.jsonSet("ka:patent:CN112310387B", pat)
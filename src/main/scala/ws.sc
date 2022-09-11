import com.google.gson.stream.JsonReader
import redis.clients.jedis.UnifiedJedis

import java.nio.file.Files
import java.nio.file.Paths
import com.google.gson.{Gson, JsonElement, JsonObject}
import ujson.InputStreamParser

import java.io.InputStreamReader


val gson = new Gson

//val reader = Files.newBufferedReader(Paths.get("Users/tiagoooliveira/Documents/dev/scala/akka-http-quickstart-scala/src/main/resources/patent-13062022-1.json"))
//val pat = gson.fromJson(reader, classOf[JsonObject])

val is = Files.newInputStream(Paths.get("Users/tiagoooliveira/Documents/dev/scala/akka-http-quickstart-scala/src/main/resources/patents-13062022.json"))
val reader = new JsonReader(new InputStreamReader(is))

val unifiedJedis: UnifiedJedis = new UnifiedJedis("redis://localhost:6379")

reader.beginObject()

var i = 0
while(reader.hasNext){
  i+=1
  println(i)
  println(reader.nextName())
  val r = unifiedJedis.jsonSet(s"ka:patent:$i", gson.fromJson(reader, classOf[JsonObject]).toString)
  println(r)
}

//val unifiedJedis: UnifiedJedis = new UnifiedJedis("redis://localhost:6379")

//println(pat.keySet())
//val patentsList = pat.keySet()

//println(pat.get("CN112310387A"))
//pat.keySet().forEach(a => println(pat.get(a)))
//println(patentsList.size())
//println(pat.isJsonObject)
//println(pat.get("CN111295360A"))
//println(pat.get("US20180151876A1"))

//println(pat.get("US20180151876A1"))

//val r = unifiedJedis.jsonSet("ka:patent:CN112310387B", pat)


//unifiedJedis.jsonSet("ka:patent:US20180151876A1", pat.get("US20180151876A1"))

//pat.keySet().forEach(pname => unifiedJedis.jsonSet(s"ka:patent:$pname", pat.get(pname)))


//val r = unifiedJedis.jsonSet("ka:patent:CN112310387B", pat)

private def sendRedis(jsonName: String, json: JsonElement, uri: UnifiedJedis)={
  uri.jsonSet(s"ka:patent:$jsonName", json)
}

//https://stackoverflow.com/questions/52667358/scalac-out-of-memory-during-building-breeze
//https://youtrack.jetbrains.com/issue/SCL-19036/Scala-Java-build-hangs-on-Reading-compilation-settings
//https://github.com/google/gson/issues/1176
//https://www.amitph.com/java-parse-large-json-files/
//https://www.programcreek.com/java-api-examples/?class=com.google.gson.stream.JsonReader&method=close
import com.google.gson.stream.JsonReader
import redis.clients.jedis.UnifiedJedis

import java.nio.file.Files
import java.nio.file.Paths
import com.google.gson.{Gson, JsonElement, JsonObject}

import java.io.InputStreamReader


val gson = new Gson

val is = Files.newInputStream(Paths.get(getClass.getClassLoader.getResource("patents-13062022.json").getPath))
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

def solution(arr: Array[Int]): Int = {

  val vmi = (x: Int, y: Int) => x min y
  val vma = (x: Int, y: Int) => x max y

  //val minRlf = arr.foldLeft(Int.MaxValue)(_ min _)
  //val minRlf = arr.foldLeft(Int.MaxValue)(vmi)
  val minRlf = arr.reduceLeft(vmi)
  //val maxRlf = arr.foldLeft(Int.MinValue)(_ max _)
  //val maxRlf = arr.foldLeft(Int.MinValue)(vma)
  val maxRlf = arr.reduceLeft(vma)

  val positiveMax = Math.abs(maxRlf)

  if(Math.abs(minRlf) == positiveMax) positiveMax else 0
}

private def sendRedis(jsonName: String, json: JsonElement, uri: UnifiedJedis)={
  uri.jsonSet(s"ka:patent:$jsonName", json)
}

//https://stackoverflow.com/questions/52667358/scalac-out-of-memory-during-building-breeze
//https://youtrack.jetbrains.com/issue/SCL-19036/Scala-Java-build-hangs-on-Reading-compilation-settings
//https://github.com/google/gson/issues/1176
//https://www.amitph.com/java-parse-large-json-files/
//https://www.programcreek.com/java-api-examples/?class=com.google.gson.stream.JsonReader&method=close
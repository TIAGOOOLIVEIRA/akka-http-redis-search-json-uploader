package com.actor

import akka.actor.{Actor, ActorSystem, Props}

import com.google.gson.stream.JsonReader
import redis.clients.jedis.UnifiedJedis

import java.nio.file.Files
import java.nio.file.Paths
import com.google.gson.{Gson, JsonElement, JsonObject}

import java.io.InputStreamReader

object  JsonPipelineActor {
  def props = Props[JsonPipelineActor]

  case class LoadJsonToRedis(pathFile: String)
  case class ResponseLoadJsonToRedis(hash: String, records: Int, timeElaspsed: Int)
  case class ExtractHash(pathFile: String)
  case class ResponseHashFile(hash: String)
}

class JsonPipelineActor  extends Actor{
  import com.actor.JsonPipelineActor._

  //val nerActor = ActorSystem("pdf-processor").actorOf(Props[JsonPipelineActor], "NER_pdf")


  override def receive: Receive = {
    case LoadJsonToRedis(path) =>
      val countDoc = readStreamFile(path)
      sender() !  ResponseLoadJsonToRedis("", countDoc, 0)
    case ExtractHash(filePath) =>
      sender() ! ResponseHashFile("")
      //leverage UUID unique https://github.com/ulid/spec
  }

  private def sendRedis(path: String): Unit ={
    val unifiedJedis: UnifiedJedis = new UnifiedJedis("redis://localhost:6379")
    val gson = new Gson

    val is = Files.newInputStream(Paths.get(path))
    val reader = new JsonReader(new InputStreamReader(is))

    reader.beginObject()

    var i = 0
    while(reader.hasNext){
      i+=1
      println(i)
      println(reader.nextName())
      val r = unifiedJedis.jsonSet(s"ka:patent:$i", gson.fromJson(reader, classOf[JsonObject]).toString)
    }

    reader.close()
    unifiedJedis.close()
  }

  def readStreamFile(path: String): Int ={
    val unifiedJedis: UnifiedJedis = new UnifiedJedis("redis://localhost:6379")

    val is = Files.newInputStream(Paths.get(path))
    val reader = new JsonReader(new InputStreamReader(is))
    reader.beginObject()

    val countDoc = iterateCount(reader, unifiedJedis)

    reader.close()
    unifiedJedis.close()

    countDoc
  }

  def iterateCount(reader: JsonReader,unifiedJedis: UnifiedJedis): Int = {
    val gson = new Gson

    def icount(acc: Int): Int = {
      if (reader.hasNext) {
        val r = unifiedJedis.jsonSet(s"ka:patent:$acc", gson.fromJson(reader, classOf[JsonObject]).toString)
        icount(1 + acc)
      } else acc
    }

    icount(0)
  }
}

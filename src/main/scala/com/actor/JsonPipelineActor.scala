package com.actor

import akka.actor.{Actor, ActorSystem, Props}

import com.google.gson.stream.JsonReader
import redis.clients.jedis.UnifiedJedis

import java.nio.file.Files
import java.nio.file.Paths
import com.google.gson.{Gson, JsonElement, JsonObject}

import java.io.InputStreamReader

import com.commons._

class JsonPipelineActor  extends Actor{
  import com.commons._

  //val nerActor = ActorSystem("pdf-processor").actorOf(Props[JsonPipelineActor], "NER_pdf")

  override def receive: Receive = {
    case LoadJsonToRedis(path) =>
      val countDoc = readStreamFile(path)
      sender() !  ResponseLoadJsonToRedis("", countDoc, 0)
    case ExtractHash(filePath) =>
      sender() ! ResponseHashFile("")
      //leverage UUID unique https://github.com/ulid/spec
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

object JsonPipelineActor {
  def initiate(port: Int): Unit = {
  }
}
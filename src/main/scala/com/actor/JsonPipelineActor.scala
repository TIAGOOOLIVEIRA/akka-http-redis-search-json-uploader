package com.actor

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.google.gson.stream.JsonReader
import redis.clients.jedis.UnifiedJedis

import java.nio.file.Files
import java.nio.file.Paths
import com.google.gson.{Gson, JsonElement, JsonObject}

import java.io.InputStreamReader
import com.typesafe.config.ConfigFactory
import com.commons._

class JsonPipelineActor  extends Actor{
  import com.commons._

  //val nerActor = ActorSystem("pdf-processor").actorOf(Props[JsonPipelineActor], "NER_pdf")

  override def receive: Receive = {
    case SendRecordToRedis(reader, unifiedJedis, aggregator) =>
      //send to redissearch
      //return to sender the counter
    case LoadJsonToRedis(path) =>
      val countDoc = readStreamFile(path)
      sender() !  ResponseLoadJsonToRedis("", countDoc, 0)
    case ExtractHash(filePath) =>
      sender() ! ResponseHashFile("")
      //leverage UUID unique https://github.com/ulid/spec
  }

  def readStreamFile(path: String): Int ={
    val redissearchprotocol = ConfigFactory.load().getString("RedisSearch.protocol")
    val redissearchhost = ConfigFactory.load().getString("RedisSearch.host")
    val redissearchport = ConfigFactory.load().getString("RedisSearch.port")

    val unifiedJedis: UnifiedJedis = new UnifiedJedis(s"$redissearchprotocol://$redissearchhost:$redissearchport")

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

  private var _jsonPipeline: ActorRef = _

  def initiate(port: Int): Unit = {
    print("Json Pipeline Actor cluster initializing...")

    val config = ConfigFactory.load("cluster_loadbalancer.conf").getConfig("JsonPipeline")

    val system = ActorSystem("ClusterSystem", config)

    _jsonPipeline = system.actorOf(Props[JsonPipelineActor], name = "JsonPipeline")

  }

  def getJsonPipelineActor: ActorRef = _jsonPipeline
}
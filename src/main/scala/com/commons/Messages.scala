package com.commons

import akka.actor.ActorRef
import com.google.gson.stream.JsonReader
import redis.clients.jedis.UnifiedJedis

case class LoadJsonToRedis(pathFile: String)
case class SendRecordToRedis(reader: JsonReader,unifiedJedis: UnifiedJedis, aggregator: ActorRef)
case class ProcessRecordResult(count: Int)
case class ResponseLoadJsonToRedis(hash: String, records: Int, timeElaspsed: Int)
case class ExtractHash(pathFile: String)
case class ResponseHashFile(hash: String)
package com.commons

//An Akka, Cats and Cassandra Project in Scala - Part 1  https://www.youtube.com/watch?v=PPIPGzrc2wo&list=PLmtsMNDRU0BwOoOByyvdDanace6rltT2e
//create commands, events, responses
//https://scalajobs.com/blog/functional-data-modeling-in-scala/

import akka.actor.ActorRef
import com.google.gson.stream.JsonReader
import redis.clients.jedis.UnifiedJedis
case class LoadJsonToRedis(pathFile: String)
case class SendRecordToRedis(reader: JsonReader,unifiedJedis: UnifiedJedis, aggregator: ActorRef)
case class ProcessRecordResult(count: Int)
case class ResponseLoadJsonToRedis(hash: String, records: Int, timeElaspsed: Int)
case class ExtractHash(pathFile: String)
case class ResponseHashFile(hash: String)
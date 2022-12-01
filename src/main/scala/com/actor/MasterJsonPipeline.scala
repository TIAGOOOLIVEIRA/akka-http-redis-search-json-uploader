package com.actor

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Address, Props, ReceiveTimeout}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.dispatch.{PriorityGenerator, UnboundedPriorityMailbox}
import akka.util.Timeout

import scala.concurrent.duration._
import akka.pattern.pipe
import com.commons.{LoadJsonToRedis, ProcessRecordResult, SendRecordToRedis}
import com.google.gson.stream.JsonReader
import com.typesafe.config.{Config, ConfigFactory}
import redis.clients.jedis.UnifiedJedis

import java.io.InputStreamReader
import java.nio.file.{Files, Paths}
import scala.language.postfixOps
import scala.util.Random

class MasterJsonPipeline extends Actor with ActorLogging {

  import context.dispatcher
  implicit val timeout = Timeout(3 seconds)

  val cluster = Cluster(context.system)

  var workers: Map[Address, ActorRef] = Map()
  var pendingRemoval: Map[Address, ActorRef] = Map()

  override def preStart(): Unit = {
    cluster.subscribe(
      self,
      initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent],
      classOf[UnreachableMember]
    )
  }
  override def postStop(): Unit = {
    cluster.unsubscribe(self)
  }

  override def receive: Receive =
    handleClusterEvents
      .orElse(handleWorkerRegistration)
      .orElse(handleJob)

  def handleClusterEvents: Receive = {
    case MemberUp(member) if member.hasRole("worker") =>
      log.info(s"Member is up: ${member.address}")
      if (pendingRemoval.contains(member.address)) {
        pendingRemoval = pendingRemoval - member.address
      } else {
        val workerSelection = context.actorSelection(s"${member.address}/user/worker")
        workerSelection.resolveOne().map(ref => (member.address, ref)).pipeTo(self)
      }

    case UnreachableMember(member) if member.hasRole("worker") =>
      log.info(s"Member detected as unreachable: ${member.address}")
      val workerOption = workers.get(member.address)
      workerOption.foreach { ref =>
        pendingRemoval = pendingRemoval + (member.address -> ref)
      }

    case MemberRemoved(member, previousStatus) =>
      log.info(s"Member ${member.address} removed after $previousStatus")
      workers = workers - member.address

    case m: MemberEvent =>
      log.info(s"Another member event I don't care about: $m")
  }

  def handleWorkerRegistration: Receive = {
    case pair: (Address, ActorRef) =>
      log.info(s"Registering worker: $pair")
      workers = workers + pair
  }

  def handleJob: Receive = {
    case LoadJsonToRedis(filename) =>
      val aggregator = context.actorOf(Props[AggregatorActor], "aggregator")

      val redissearchprotocol = ConfigFactory.load().getString("RedisSearch.protocol")
      val redissearchhost = ConfigFactory.load().getString("RedisSearch.host")
      val redissearchport = ConfigFactory.load().getString("RedisSearch.port")

      val unifiedJedis: UnifiedJedis = new UnifiedJedis(s"$redissearchprotocol://$redissearchhost:$redissearchport")

      val is = Files.newInputStream(Paths.get(filename))
      val reader = new JsonReader(new InputStreamReader(is))
      reader.beginObject()

      //here to avoid recursion at function level. So one handler sends message as a record out of the json file to another handler.
      while(reader.hasNext) {
        self ! SendRecordToRedis(reader, unifiedJedis, aggregator)
      }

      reader.close()
      unifiedJedis.close()

    case SendRecordToRedis(reader, unifiedJedis, aggregator) =>
      val workerIndex = Random.nextInt((workers -- pendingRemoval.keys).size)
      val worker: ActorRef = (workers -- pendingRemoval.keys).values.toSeq(workerIndex)
      worker ! SendRecordToRedis(reader, unifiedJedis, aggregator)
      Thread.sleep(10)
}
}
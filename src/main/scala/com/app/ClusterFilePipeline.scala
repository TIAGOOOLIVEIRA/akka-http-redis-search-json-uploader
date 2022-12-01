package com.app
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster._
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import com.actor.{JsonPipelineActor, MasterJsonPipeline}
import com.commons._
import com.typesafe.config.ConfigFactory

//https://github.com/rockthejvm/akka-remoting-clustering/blob/master/src/main/scala/part3_clustering/ClusteringExample.scala

object ClusterFilePipeline extends App {
  import com.commons._

  def createNode(port: Int, role: String, props: Props, actorName: String): ActorRef = {
    val config = ConfigFactory.parseString(
      s"""
         |akka.cluster.roles = ["$role"]
         |akka.remote.artery.canonical.port = $port
       """.stripMargin)
      .withFallback(ConfigFactory.load("clusteringExample.conf"))

    val system = ActorSystem("JsonPipelineCluster", config)
    system.actorOf(props, actorName)
  }

  val master = createNode(2551, "master", Props[MasterJsonPipeline], "master")
  //registering worker actors
  //must trigger handler handleWorkerRegistration
  //missing the Address object for matching match case "case pair: (Address, ActorRef)"
  master ! createNode(2552, "worker", Props[JsonPipelineActor], "worker")
  master ! createNode(2554, "worker", Props[JsonPipelineActor], "worker")

  Thread.sleep(10000)
  //master ! LoadJsonToRedis("test.json")
}

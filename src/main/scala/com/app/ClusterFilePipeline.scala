package com.app
import akka.cluster._
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import com.actor.JsonPipelineActor
import com.commons._

object ClusterFilePipeline extends App {
  import com.commons._

  JsonPipelineActor.initiate(2551)
  JsonPipelineActor.initiate(2552)

  //JsonPipelineActor.getJsonPipelineActor ! LoadJsonToRedis("test")
}

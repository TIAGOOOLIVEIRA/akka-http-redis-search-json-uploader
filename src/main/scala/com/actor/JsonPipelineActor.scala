package com.actor

import akka.actor.{Actor, ActorSystem, Props}

object  JsonPipelineActor {
  def props = Props[JsonPipelineActor]

  case class LoadJsonToRedis(pathFile: String)
  case class ExtractHash(pathFile: String)
  case class ResponseHashFile(hash: String)
}

class JsonPipelineActor  extends Actor{
  import com.actor.JsonPipelineActor._

  //val nerActor = ActorSystem("pdf-processor").actorOf(Props[JsonPipelineActor], "NER_pdf")


  override def receive: Receive = {
    case ExtractHash(filePath) =>
      sender() ! ResponseHashFile("")
    case LoadJsonToRedis(filePath) => ???
  }
}

package com.actor

import akka.actor.{Actor, ActorLogging, ReceiveTimeout}
import com.commons.ProcessRecordResult

import scala.concurrent.duration.{Duration, DurationInt}

class AggregatorActor extends Actor with ActorLogging {
  context.setReceiveTimeout(3 seconds)

  override def receive: Receive = online(0)

  def online(totalCount: Int): Receive = {
    case ProcessRecordResult(count) =>
      context.become(online(totalCount + count))
    case ReceiveTimeout =>
      log.info(s"TOTAL COUNT: $totalCount")
      context.setReceiveTimeout(Duration.Undefined)
  }
}
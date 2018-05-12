package controller

import controller.HelloWorldActor.HelloWorldRequest
import akka.actor.{Actor, ActorLogging, Props}

object HelloWorldActor {
  def props: Props = Props(new HelloWorldActor)

  final case class HelloWorldRequest(name: Option[String])
}

class HelloWorldActor extends Actor with ActorLogging {
  override def preStart(): Unit = log.info("HelloWorldActor started")
  override def postStop(): Unit = log.info("HelloWorldActor stopped")

  override def receive: Receive = {
    case HelloWorldRequest(name) => log.info(name.toString)
  }
}

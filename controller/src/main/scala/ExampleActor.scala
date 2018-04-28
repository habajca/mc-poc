package controller

import akka.actor.{Actor, ActorLogging, Props}

object ExampleActor {
  def props: Props = Props(new ExampleActor)
}

class ExampleActor extends Actor with ActorLogging {
  override def preStart(): Unit = log.info("ExampleActor started")
  override def postStop(): Unit = log.info("ExampleActor stopped")

  override def receive = Actor.emptyBehavior
}

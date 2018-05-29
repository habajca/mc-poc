package controller

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object HelloWorldActor {
  final case class HelloWorldRequest(name: Option[String])

  val requestHandler: Behavior[HelloWorldRequest] = Behaviors.receive {
    case (ctx, HelloWorldRequest(name)) =>
      ctx.log.info(name.toString)
      Behaviors.same
  }
}

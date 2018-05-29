package controller

import controller.HelloWorldActor.HelloWorldRequest

import akka.actor.{ActorSystem => UntypedActorSystem}
import akka.actor.typed.{ActorSystem, DispatcherSelector}
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.{jackson, DefaultFormats}

object HttpApi extends App with Directives with Json4sSupport {
  val system: ActorSystem[HelloWorldRequest] =
    ActorSystem(HelloWorldActor.requestHandler, "mc-poc-controller")
  implicit val untypedSystem: UntypedActorSystem = system.toUntyped
  implicit val executionContext =
    system.dispatchers.lookup(DispatcherSelector.default)
  implicit val materializer = ActorMaterializer()

  implicit val serialization = jackson.Serialization
  implicit val formats = DefaultFormats

  lazy val route =
    path("hello") {
      get {
        system ! HelloWorldRequest(None)
        complete(HttpResponse("Hello, world!"))
      } ~
        (put & entity(as[Option[HelloUser]])) {
          case r @ Some(HelloUser(Some(name))) =>
            system ! HelloWorldRequest(Option(name))
            complete(
              HttpResponse(
                content = s"Hello, $name!",
                HttpResponseMetadata(
                  request = Some(HttpRequest(r))
                )
              ))
          case r =>
            system ! HelloWorldRequest(None)
            complete(
              HttpResponse(
                content = "Hello, world!",
                HttpResponseMetadata(
                  request = Some(HttpRequest(r)),
                  warnings = Seq("Name expected but not provided")
                )
              ))
        }
    }

  {
    Http().bindAndHandle(route, "localhost", 8080)
    println("Controller API started at http://localhost:8080/")
  }
}

case class HttpResponse[T](content: T,
                           metadata: HttpResponseMetadata =
                             HttpResponseMetadata())
case class HttpResponseMetadata(request: Option[HttpRequest[_]] = None,
                                warnings: Seq[String] = Seq.empty)
case class HttpRequest[R](translation: R)
case class HelloUser(name: Option[String])

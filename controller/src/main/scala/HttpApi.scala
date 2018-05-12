package controller

import controller.HelloWorldActor.HelloWorldRequest

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.{jackson, DefaultFormats}

object HttpApi extends App with Directives with Json4sSupport {
  implicit val system = ActorSystem("mc-poc-controller")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  implicit val serialization = jackson.Serialization
  implicit val formats = DefaultFormats

  def route(example: ActorRef) =
    path("hello") {
      get {
        example ! HelloWorldRequest(None)
        complete(HttpResponse("Hello, world!"))
      } ~
        (put & entity(as[Option[HelloUser]])) {
          case r @ Some(HelloUser(Some(name))) =>
            example ! HelloWorldRequest(Option(name))
            complete(
              HttpResponse(
                content = s"Hello, $name!",
                HttpResponseMetadata(
                  request = Some(HttpRequest(r))
                )
              ))
          case r =>
            example ! HelloWorldRequest(None)
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
    val example = system.actorOf(HelloWorldActor.props, "example-actor")
    Http().bindAndHandle(route(example), "localhost", 8080)
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

package controller

import akka.actor.ActorSystem
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

  val route =
    path("hello") {
      get {
        complete(HttpResponse("Hello, world!"))
      } ~
        (put & entity(as[Option[HelloUser]])) {
          case r @ Some(HelloUser(Some(name))) =>
            complete(
              HttpResponse(
                content = s"Hello, $name!",
                HttpResponseMetadata(
                  request = Some(HttpRequest(r))
                )
              ))
          case r =>
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

    system.actorOf(ExampleActor.props, "example-actor")
  }
}

case class HttpResponse[T](content: T,
                           metadata: HttpResponseMetadata =
                             HttpResponseMetadata())
case class HttpResponseMetadata(request: Option[HttpRequest[_]] = None,
                                warnings: Seq[String] = Seq.empty)
case class HttpRequest[R](translation: R)
case class HelloUser(name: Option[String])

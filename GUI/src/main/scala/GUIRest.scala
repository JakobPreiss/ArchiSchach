package GUI

import JsonProtocols._
import Requests.MoveRequest.jsonFormat2
import SharedResources.{ChessContext, GenericHttpClient, JsonResult}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import spray.json.DefaultJsonProtocol
import spray.json.RootJsonFormat
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global


// Import JavaFX’s own Platform so we can startup and runLater on the real FX thread:
import javafx.application.{Platform => JfxPlatform}

object JsonProtocols extends DefaultJsonProtocol {
    case class InputPayload(input: String)
    implicit val inputFormat: RootJsonFormat[InputPayload] = jsonFormat1(InputPayload)
}

class GuiRoutes()(implicit system: ActorSystem) {

    private def runOnFxThreadBlocking[T](fxCode: => T): Future[T] = {
        val p = Promise[T]()
        JfxPlatform.runLater(new Runnable {


            override def run(): Unit = {

                try {
                    p.success(fxCode) // return type is Promise[T], but we ignore it

                } catch {

                    case e: Throwable =>
                        p.failure(e) // same here

                }

            }


        })
        p.future
    }

    val routes: Route = pathPrefix("gui") {
        concat(
            path("update") {
                post {
                    onComplete(runOnFxThreadBlocking {
                        GuiMain.board.foreach(_.update)
                        GuiMain.menu.foreach(_.update)
                        "Updated"
                    }) {
                        case Success(msg) => complete(msg)
                        case Failure(e)   => complete(StatusCodes.InternalServerError, s"FX error: ${e.getMessage}")
                    }
                }
            },
            path("special") {
                post {
                    onComplete(runOnFxThreadBlocking {
                        GuiMain.board.foreach(_.specialCase)
                        "Special case triggered"
                    }) {
                        case Success(msg) => complete(msg)
                        case Failure(e)   => complete(StatusCodes.InternalServerError, s"FX error: ${e.getMessage}")
                    }
                }
            },
            path("reverse") {
                post {
                    onComplete(runOnFxThreadBlocking {
                        GuiMain.board.foreach(_.reverseSpecialCase)
                        GuiMain.promoWindow.foreach(_.reverseSpecialCase)
                        "Reverse special case triggered"
                    }) {
                        case Success(msg) => complete(msg)
                        case Failure(e)   => complete(StatusCodes.InternalServerError, s"FX error: ${e.getMessage}")
                    }
                }
            },
            path("error") {
                post {
                    onComplete(runOnFxThreadBlocking {
                        GuiMain.promoWindow.foreach(_.errorDisplay)
                        "Error displayed"
                    }) {
                        case Success(msg) => complete(msg)
                        case Failure(e)   => complete(StatusCodes.InternalServerError, s"FX error: ${e.getMessage}")
                    }
                }
            }
        )
    }
}

object GuiServer extends App {
    // 1) Initialize the JavaFX toolkit once, up front.
    //    This blocks until FX is ready, and from now on
    //    Platform.runLater truly enqueues on the FX thread.
    JfxPlatform.startup(() => {})

    // 2) Set up Akka
    implicit val system: ActorSystem = ActorSystem("ControllerSystem")
    val routes = new GuiRoutes().routes

    // 3) Bind HTTP server (non-blocking)
    Http()
      .newServerAt("0.0.0.0", 8080)
      .bind(routes)
      .onComplete {
          case Success(binding) =>
              println(s"Controller REST API running at http://${binding.localAddress.getHostString}:${binding.localAddress.getPort}/")
          case Failure(ex) =>
              println(s"Failed to bind HTTP server: ${ex.getMessage}")
      }

    // 4) Register with controller (non-blocking)
    val registerNotifier = GenericHttpClient.get[JsonResult[String]](
        baseUrl     = "http://controller:8080",
        route       = "/controller/register",
        queryParams = Map("url" -> "http://gui:8080/gui")
    )
    registerNotifier.onComplete {
        case Success(_)     => println("Registered successfully")
        case Failure(error) => println(s"Failed to register: ${error.getMessage}")
    }

    // 5) Launch your ScalaFX GUI—this may block, so we do it on a dedicated thread.
    new Thread("JavaFX-Launcher") {
        override def run(): Unit =
            GuiMain.main(args)
    }.start()
}

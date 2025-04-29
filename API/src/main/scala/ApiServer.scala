package API

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.util.matching.Regex
import SharedResources.GenericHttpClient
import SharedResources.JsonResult
import SharedResources.Requests.{PlayRequest, PromotePawnRequest}

// JSON protocols for requests and responses
object JsonProtocols extends DefaultJsonProtocol {
  case class InputPayload(input: String)
  implicit val inputFormat: RootJsonFormat[InputPayload] = jsonFormat1(InputPayload)

  case class PromotePayload(pieceKind: String)
  implicit val promoteFormat: RootJsonFormat[PromotePayload] = jsonFormat1(PromotePayload)

  case class ApiResponse(output: String)
  implicit val responseFormat: RootJsonFormat[ApiResponse] = jsonFormat1(ApiResponse)
}

class Api()(implicit system: ActorSystem) {
  import JsonProtocols._
  import system.dispatcher

  // Regex to detect move commands like "a1c3"
  private val movePattern: Regex = "[a-h][1-8][a-h][1-8]".r

  val routes: Route = concat(
    path("input") {
      post {
        entity(as[InputPayload]) { payload =>
          val input = payload.input
          // Determine which controller call to make
          val resultFut: Future[JsonResult[String]] = input match {
            case "undo" =>
              GenericHttpClient.post[Unit, JsonResult[String]](
                baseUrl = "http://controller:8080",
                route = "/controller/undo",
                payload = {}
              )
            case "redo" =>
              GenericHttpClient.post[Unit, JsonResult[String]](
                baseUrl = "http://controller:8080",
                route = "/controller/redo",
                payload = {}
              )
            case "reset" =>
              GenericHttpClient.post[Unit, JsonResult[String]](
                baseUrl = "http://controller:8080",
                route = "/controller/resetBoard",
                payload = {}
              )
            case movePattern() =>
              // Sequence: get FEN, translate move, play move, then get updated board
              for {
                fenJson <- GenericHttpClient.get[JsonResult[String]](
                  baseUrl = "http://controller:8080",
                  route = "/controller/fen",
                  queryParams = Map()
                )
                transJson <- GenericHttpClient.get[JsonResult[(Int, Int)]](
                  baseUrl = "http://controller:8080",
                  route = "/controller/translateMoveStringToInt",
                  queryParams = Map(
                    "fen"  -> fenJson.result,
                    "move" -> input
                  )
                )
                _ <- GenericHttpClient.post[PlayRequest, Unit](
                  baseUrl = "http://controller:8080",
                  route = "/controller/squareClicked",
                  payload = PlayRequest(move = transJson.result)
                )
                boardJson <- GenericHttpClient.get[JsonResult[String]](
                  baseUrl = "http://controller:8080",
                  route = "/controller/createOutput",
                  queryParams = Map()
                )
              } yield boardJson
            case _ =>
              // Invalid input
              Future.successful(JsonResult("Denk nochmal nach Bro"))
          }
          onComplete(resultFut) {
            case Success(json) =>
              complete(StatusCodes.OK, ApiResponse(json.result))
            case Failure(ex) =>
              complete(StatusCodes.InternalServerError, ApiResponse(ex.getMessage))
          }
        }
      }
    },
    path("promote") {
      post {
        entity(as[PromotePayload]) { payload =>
          // Promotion: send piece kind, then fetch updated board
          val resultFut = for {
            _ <- GenericHttpClient.post[PromotePawnRequest, JsonResult[Int]](
              baseUrl = "http://controller:8080",
              route = "/controller/promotePawn",
              payload = PromotePawnRequest(pieceKind = payload.pieceKind)
            )
            boardJson <- GenericHttpClient.get[JsonResult[String]](
              baseUrl = "http://controller:8080",
              route = "/controller/createOutput",
              queryParams = Map()
            )
          } yield boardJson

          onComplete(resultFut) {
            case Success(json) =>
              complete(StatusCodes.OK, ApiResponse(json.result))
            case Failure(ex) =>
              complete(StatusCodes.InternalServerError, ApiResponse(ex.getMessage))
          }
        }
      }
    },
    path("error") {
      get {
        // Return the latest error message
        onComplete(
          GenericHttpClient.get[JsonResult[String]](
            baseUrl = "http://controller:8080",
            route = "/controller/errorMessage",
            queryParams = Map()
          )
        ) {
          case Success(json) =>
            complete(StatusCodes.OK, ApiResponse(json.result))
          case Failure(ex) =>
            complete(StatusCodes.InternalServerError, ApiResponse(ex.getMessage))
        }
      }
    }
  )
}

object ApiServer extends App {
  implicit val system: ActorSystem = ActorSystem("ApiSystem")
  import system.dispatcher

  val api = new Api()
  val bindingFut = Http().newServerAt("0.0.0.0", 8080).bind(api.routes)

  bindingFut.onComplete {
    case Success(binding) =>
      println(s"API server running at http://${binding.localAddress.getHostString}:${binding.localAddress.getPort}")
    case Failure(ex) =>
      println(s"Failed to bind API server: ${ex.getMessage}")
  }
}

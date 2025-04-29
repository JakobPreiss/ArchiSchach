package SharedResources

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.model.Uri.Query
import akka.stream.ActorMaterializer
import spray.json._
import scala.concurrent.{ExecutionContext, Future}

/**
 * A generic HTTP client using Akka HTTP and Spray JSON, capable of GET and POST requests.
 */
object GenericHttpClient extends DefaultJsonProtocol {
  implicit val system: ActorSystem             = ActorSystem("GenericClientSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()(system)
  implicit val ec: ExecutionContext            = system.dispatcher

  private val http = Http(system)

  /**
   * Perform a GET request to the specified URL and route with query parameters.
   * @param baseUrl the base URL, e.g., "http://localhost:8080"
   * @param route the API route, e.g., "/getBoardString"
   * @param queryParams map of query parameters
   * @tparam T the type to unmarshal the JSON response into (requires JsonFormat)
   * @return Future[T]
   */
  def get[T: JsonFormat](baseUrl: String, route: String, queryParams: Map[String, String]): Future[T] = {
    val uri     = Uri(baseUrl).withPath(Uri.Path(route)).withQuery(Query(queryParams))
    val request = HttpRequest(method = HttpMethods.GET, uri = uri)

    http.singleRequest(request).flatMap {
      case HttpResponse(StatusCodes.OK, _, entity, _) =>
        Unmarshal(entity).to[String].map { jsonString =>
          jsonString.parseJson.convertTo[T]
        }
      case HttpResponse(code, _, entity, _) =>
        Unmarshal(entity).to[String].flatMap { body =>
          Future.failed(new RuntimeException(s"GET request failed with status $code and body $body"))
        }
    }
  }

  /**
   * Perform a POST request to the specified URL and route with a JSON payload.
   * @param baseUrl the base URL, e.g., "http://localhost:8080"
   * @param route the API route, e.g., "/updateEnpassant"
   * @param payload the object to marshal into JSON
   * @tparam Req the request payload type (requires JsonFormat)
   * @tparam Res the response type to unmarshal into (requires JsonFormat)
   * @return Future[Res]
   */
  def post[Req: JsonFormat, Res: JsonFormat](baseUrl: String, route: String, payload: Req): Future[Res] = {
    val uri       = Uri(baseUrl).withPath(Uri.Path(route))
    // Manually convert payload to JSON string and wrap in HttpEntity
    val jsonString = payload.toJson.compactPrint
    val entity     = HttpEntity(ContentTypes.`application/json`, jsonString)
    val request    = HttpRequest(method = HttpMethods.POST, uri = uri, entity = entity)

    http.singleRequest(request).flatMap {
      case HttpResponse(StatusCodes.OK | StatusCodes.Created, _, responseEntity, _) =>
        Unmarshal(responseEntity).to[String].map { respJson =>
          respJson.parseJson.convertTo[Res]
        }
      case HttpResponse(code, _, responseEntity, _) =>
        Unmarshal(responseEntity).to[String].flatMap { body =>
          Future.failed(new RuntimeException(s"POST request failed with status $code and body $body"))
        }
    }
  }

  /**
   * Shutdown the actor system when done.
   */
  def shutdown(): Future[Unit] =
    http
      .shutdownAllConnectionPools()
      .flatMap(_ => system.terminate())
      .map(_ => ())
}
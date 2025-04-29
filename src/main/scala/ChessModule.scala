import SharedResources.{GenericHttpClient, JsonResult}

import SharedResources.GenericHttpClient.ec

import SharedResources.ChessJsonProtocol.StringJsonFormat
import scala.concurrent.Future
import scala.util.{Failure, Success}

object ChessModule {
    def provideDuoChessXML() = {
        val loadedData: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
            baseUrl = "http://xml:8080",
            route = "/apifile/from",
            queryParams = Map()
        )
        loadedData.onComplete {
            case Success(arg1) =>
                val setController: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
                    baseUrl = "http://controller:8080",
                    route = "/controller/init/duo",
                    queryParams = Map(
                        "gameMode" -> "http://real-chess:8080",
                        "api" -> "http://xml:8080",
                        "fen" -> arg1.result,
                    )
                )
                setController.onComplete {
                    case Success(value) =>
                    case Failure(err) =>
                        println(s"Error: ${err.getMessage}")
                }
            case Failure(err) =>
                println(s"Error: ${err.getMessage}")
        }
    }

    def provideDuoChessJSON() = {
        val loadedData: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
            baseUrl = "http://json:8080",
            route = "/apifile/from",
            queryParams = Map()
        )
        loadedData.onComplete {
            case Success(arg1) =>
                val setController: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
                    baseUrl = "http://controller:8080",
                    route = "/controller/init/duo",
                    queryParams = Map(
                        "gameMode" -> "http://real-chess:8080",
                        "api" -> "http://json:8080",
                        "fen" -> arg1.result,
                    )
                )
                setController.onComplete {
                    case Success(value) =>
                    case Failure(err) =>
                        println(s"Error: ${err.getMessage}")
                }
            case Failure(err) =>
                println(s"Error: ${err.getMessage}")
        }
    }

    def provideEngineChessXML() = {
        val loadedData: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
            baseUrl = "http://xml:8080",
            route = "/apifile/from",
            queryParams = Map()
        )
        loadedData.onComplete {
            case Success(arg1) =>
                val setController: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
                    baseUrl = "http://controller:8080",
                    route = "/controller/init/duo",
                    queryParams = Map(
                        "gameMode" -> "http://devour-chess:8080",
                        "api" -> "http://xml:8080",
                        "fen" -> arg1.result,
                        "depth" -> "10"
                    )
                )
                setController.onComplete {
                    case Success(value) =>
                    case Failure(err) =>
                        println(s"Error: ${err.getMessage}")
                }
            case Failure(err) =>
                println(s"Error: ${err.getMessage}")
        }
    }

    def provideEngineChessJSON() = {
        val loadedData: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
            baseUrl = "http://json:8080",
            route = "/apifile/from",
            queryParams = Map()
        )
        loadedData.onComplete {
            case Success(arg1) =>
                val setController: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
                    baseUrl = "http://controller:8080",
                    route = "/controller/init/duo",
                    queryParams = Map(
                        "gameMode" -> "http://devour-chess:8080",
                        "api" -> "http://json:8080",
                        "fen" -> arg1.result,
                        "depth" -> "15"
                    )
                )
                setController.onComplete {
                    case Success(value) =>
                    case Failure(err) =>
                        println(s"Error: ${err.getMessage}")
                }
            case Failure(err) =>
                println(s"Error: ${err.getMessage}")
        }
    }
}
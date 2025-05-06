import SharedResources.{GenericHttpClient, JsonResult}
import SharedResources.GenericHttpClient.ec
import SharedResources.ChessJsonProtocol.StringJsonFormat
import SharedResources.Requests.{InitDuoRequest, InitEngineRequest}

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
                val payload = InitDuoRequest(
                    gameMode = "http://real-chess:8080",
                    api = "http://xml:8080",
                    fen = arg1.result
                )

                val setController: Future[JsonResult[String]] = GenericHttpClient.post[InitDuoRequest, JsonResult[String]](
                    baseUrl = "http://controller:8080",
                    route = "/controller/init/duo",
                    payload=payload
                )
                setController.onComplete {
                    case Success(value) =>
                        println(s"${value.result}")
                    case Failure(err) =>
                        println(s"Error in provideDuoChessXML (http://controller:8080/controller/init/duo): ${err.getMessage}")
                }
            case Failure(err) =>
                println(s"Error in provideDuoChessXML (http://xml:8080/apifile/from): ${err.getMessage}")
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
                val payload = InitDuoRequest(
                    gameMode = "http://real-chess:8080",
                    api = "http://json:8080",
                    fen = arg1.result
                )

                val setController: Future[JsonResult[String]] = GenericHttpClient.post[InitDuoRequest, JsonResult[String]](
                    baseUrl = "http://controller:8080",
                    route = "/controller/init/duo",
                    payload = payload
                )
                setController.onComplete {
                    case Success(value) =>
                        println(s"${value.result}")
                    case Failure(err) =>
                        println(s"Error in provideDuoChessJSON (http://controller:8080/controller/init/duo): ${err.getMessage}")
                }
            case Failure(err) =>
                println(s"Error in provideDuoChessJSON (http://json:8080/apifile/from): ${err.getMessage}")
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
                val payload = InitEngineRequest(
                    gameMode = "http://devour-chess:8080",
                    api = "http://xml:8080",
                    fen = arg1.result,
                    depth = 10
                )

                val setController: Future[JsonResult[String]] = GenericHttpClient.post[InitEngineRequest, JsonResult[String]](
                    baseUrl = "http://controller:8080",
                    route = "/controller/init/engine",
                    payload = payload
                )
                setController.onComplete {
                    case Success(value) =>
                        println(s"${value.result}")
                    case Failure(err) =>
                        println(s"Error in provideEngineChessXML (http://controller:8080/controller/init/engine): ${err.getMessage}")
                }
            case Failure(err) =>
                println(s"Error in provideEngineChessXML (http://xml:8080/apifile/from): ${err.getMessage}")
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
                val payload = InitEngineRequest(
                    gameMode = "http://devour-chess:8080",
                    api = "http://json:8080",
                    fen = arg1.result,
                    depth = 15
                )

                val setController: Future[JsonResult[String]] = GenericHttpClient.post[InitEngineRequest, JsonResult[String]](
                    baseUrl = "http://controller:8080",
                    route = "/controller/init/engine",
                    payload = payload
                )
                setController.onComplete {
                    case Success(value) =>
                        println(s"${value.result}")
                    case Failure(err) =>
                        println(s"Error in provideEngineChessJSON (http://controller:8080/controller/init/engine): ${err.getMessage}")
                }
            case Failure(err) =>
                println(s"Error in provideEngineChessJSON (http://json:8080/apifile/from): ${err.getMessage}")
        }
    }

    def provideDuoChessDatabase() = {
        val loadedData: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
            baseUrl = "http://database:8080",
            route = "/apifile/from",
            queryParams = Map()
        )
        loadedData.onComplete {
            case Success(arg1) =>
                val payload = InitDuoRequest(
                    gameMode = "http://real-chess:8080",
                    api = "http://database:8080",
                    fen = arg1.result
                )

                val setController: Future[JsonResult[String]] = GenericHttpClient.post[InitDuoRequest, JsonResult[String]](
                    baseUrl = "http://controller:8080",
                    route = "/controller/init/duo",
                    payload = payload
                )
                setController.onComplete {
                    case Success(value) =>
                        println(s"${value.result}")
                    case Failure(err) =>
                        println(s"Error in provideDuoChessDatabase (http://controller:8080/controller/init/duo): ${err.getMessage}")
                }
            case Failure(err) =>
                println(s"Error in provideDuoChessDatabase (http://database:8080/apifile/from): ${err.getMessage}")
        }
    }

    def provideEngineChessDatabase() = {
        val loadedData: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
            baseUrl = "http://database:8080",
            route = "/apifile/from",
            queryParams = Map()
        )
        loadedData.onComplete {
            case Success(arg1) =>
                val payload = InitEngineRequest(
                    gameMode = "http://devour-chess:8080",
                    api = "http://database:8080",
                    fen = arg1.result,
                    depth = 10
                )

                val setController: Future[JsonResult[String]] = GenericHttpClient.post[InitEngineRequest, JsonResult[String]](
                    baseUrl = "http://controller:8080",
                    route = "/controller/init/engine",
                    payload = payload
                )
                setController.onComplete {
                    case Success(value) =>
                        println(s"${value.result}")
                    case Failure(err) =>
                        println(s"Error in provideEngineChessDatabase (http://controller:8080/controller/init/engine): ${err.getMessage}")
                }
            case Failure(err) =>
                println(s"Error in provideEngineChessDatabase (http://database:8080/apifile/from): ${err.getMessage}")
        }
    }
}
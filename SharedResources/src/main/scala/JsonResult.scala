package SharedResources

import spray.json._

/**
 * Generic wrapper for JSON responses holding a single "result" field.
 */
case class JsonResult[T](result: T)

object JsonResult extends DefaultJsonProtocol {
  /**
   * Format for (de)serializing JsonResult[T] as {"result": ...}
   */
  implicit def jsonResultFormat[T: JsonFormat]: RootJsonFormat[JsonResult[T]] = new RootJsonFormat[JsonResult[T]] {
    override def write(obj: JsonResult[T]): JsValue =
      JsObject("result" -> obj.result.toJson)

    override def read(json: JsValue): JsonResult[T] = json.asJsObject.getFields("result") match {
      case Seq(jsValue) =>
        JsonResult(jsValue.convertTo[T])
      case _ =>
        throw DeserializationException("JsonResult expected JSON object with single 'result' field")
    }
  }
}

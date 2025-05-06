package SharedResources

import scala.concurrent.Future

trait ApiFileTrait {
    /**
     * converts the given paramters to the predefined language (xml or json) and saves it in a DataWrapper for flexibility
     * @param context current ChessContext with it's state
     * @param fen current fen Board state
     * @return DataWrapper with xml or Json game state inside
     */
    def to(contextStateOrdinal: Int, fen: String): DataWrapper

    /**
     * reads from a Data Wrapper and returns the fen-String and the current Game State (from ChessContext)
     * @param data DataWrapper with json or xml inside
     * @return fen-String and the current Game State
     */
    def from: Future[String]

    /**
     * writes the given context.state and the given fen to the xml or json file
     * @param context current ChessContext with it's state
     * @param fen current fen Board state
     */
    def printTo(contextStateOrdinal: Int, fen : String) : Unit
}

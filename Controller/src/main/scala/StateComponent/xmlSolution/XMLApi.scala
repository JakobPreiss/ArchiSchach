package Controller.StateComponent.xmlSolution

import Controller.ControllerTrait
import Controller.Extra.{ChessContext, State}
import Controller.StateComponent.{ApiFileTrait, DataWrapper}

import java.io.*

class XMLApi extends ApiFileTrait {
    def to(context: ChessContext, fen: String): DataWrapper = {
        val xmldata = <box><fen>{fen}</fen><state>{context.state.ordinal}</state></box>
        DataWrapper(Some(xmldata), None)
    }

    def from(data: DataWrapper) : (String, State) = {
        val node : scala.xml.Node = data.getNode()
        ((node \ "fen").text, State.fromOrdinal((node \ "state").text.toInt))
    }

    def printTo(context : ChessContext, fen : String) = {
        val writer = new PrintWriter(new File("src/main/resources/GameState.xml"))
        val data = to(context, fen).getNode()
        writer.write(data.toString())
        writer.close()
    }
}

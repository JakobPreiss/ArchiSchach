package SharedResources.util

// TODO: THAT SHOULD BE REMOVED
//import GUI.{GuiBoard, GuiMenu}

trait Observer {
    def update: Unit
    
    def specialCase : Unit
    
    def reverseSpecialCase : Unit
    
    def errorDisplay : Unit
}

class Observable {
    var subscribers: Vector[Observer] = Vector()

    def add(s: Observer): Unit = subscribers = subscribers :+ s

    def remove(s: Observer): Unit = subscribers = subscribers.filterNot(o => o == s)

    def notifyObservers: Unit = subscribers.foreach(o =>
      // Importing something from GUI should not be allowed in the first place
        /*if (o.isInstanceOf[GuiBoard] || o.isInstanceOf[GuiMenu]) {
            Platform.runLater(() => {
                o.update
            })
        } else {
            o.update
        }*/
        o.update
    )
    
    def ringObservers : Unit = subscribers.foreach(o => o.specialCase)

    def deRingObservers : Unit = subscribers.foreach(o => o.reverseSpecialCase)
    
    def tellErrorToObservers : Unit = subscribers.foreach(o => o.errorDisplay)
}

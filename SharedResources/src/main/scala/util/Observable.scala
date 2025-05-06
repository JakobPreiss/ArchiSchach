package SharedResources.util

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
        o.update
    )
    
    def ringObservers : Unit = subscribers.foreach(o => o.specialCase)

    def deRingObservers : Unit = subscribers.foreach(o => o.reverseSpecialCase)
    
    def tellErrorToObservers : Unit = subscribers.foreach(o => o.errorDisplay)
}

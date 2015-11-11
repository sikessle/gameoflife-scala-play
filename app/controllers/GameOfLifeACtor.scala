package controllers

import java.util.{Observable, Observer}

import akka.actor.{Actor, ActorRef}
import org.sikessle.gameoflife.controller.impl.GridControllerImpl
import org.sikessle.gameoflife.view.text.TextView
import play.api.libs.json.JsValue

class GameOfLifeActor extends Actor with Observer {

  val controller = new GridControllerImpl()
  val textView = new TextView(controller)
  val outSockets = new scala.collection.mutable.HashSet[ActorRef]

  controller.addObserver(this)

  def receive = {
    case commandObject: JsValue =>
      val cmd = commandObject \ "command"
      textView.readAndInterpretFromArgument(cmd.as[String])
    case  addOutSocket: AddOutSocket =>
      outSockets += addOutSocket.outSocket
      update(null, null)
  }

  override def update(o: Observable, arg: Object): Unit = {
    for (socket <- outSockets) {
      socket ! GridToJson.getGridAsJson(controller)
    }
  }
}

package controllers

import java.util.{Observable, Observer}

import akka.actor.{ActorRef, Actor}
import org.sikessle.gameoflife.controller.impl.GridControllerImpl
import org.sikessle.gameoflife.view.text.TextView
import play.api.libs.json.JsValue

class WebSocketActor(game: ActorRef) extends Actor {

  def receive = {
    case msg:Any => game ! msg
  }

}

package controllers

import java.util.UUID

import akka.actor.{Props, ActorRef, ActorSystem}
import akka.util.Timeout
import play.api.Play.current
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.json._
import play.api.libs.ws.WS
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.{Await, duration}

object Application extends Controller {

  private val HighscoreServer = "http://de-htwg-sa-highscores.herokuapp.com"
  private val runningGames: mutable.Set[String] = new mutable.HashSet[String]
  private val actorSystem = ActorSystem.create("GameOfLife")

  def index = Action {
    Ok(views.html.index.render(runningGames))
  }

  def createGame() = Action { implicit request =>
    val gameId = UUID.randomUUID().toString

    actorSystem.actorOf(Props[GameOfLifeActor], gameId)
    runningGames.add(gameId)

    Ok(Json.obj("gameUrl" -> routes.Application.playGame(gameId).absoluteURL()))
  }

  def playGame(gameId: String) = Action {
    if (gameNotExists(gameId)) {
      Redirect(routes.Application.createGame())
    }
    Ok(views.html.game.render(gameId))
  }

  def gameNotExists(gameId: String): Boolean = {
    getGameActorByGameId(gameId).isEmpty
  }

  def createHighscore() = Action(parse.json) { request =>
    WS.url(HighscoreServer).post(request.body)
    Ok(Json.obj())
  }

  def getGameActorByGameId(gameId: String): Option[ActorRef] = {
    val selection = actorSystem.actorSelection("/user/" + gameId)
    val future = selection.resolveOne(Timeout(2.seconds).duration)

    try {
      Some(Await.result(future, Duration(5, duration.SECONDS)))
    }
    catch {
      case _: Exception => None
    }
  }

  def connectWebSocket(gameId: String) = WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
    if (gameNotExists(gameId)) {
      Redirect(routes.Application.createGame())
    }
    val game = getGameActorByGameId(gameId).get
    game ! AddOutSocket(out)
    Props(new WebSocketActor(game))
}

}
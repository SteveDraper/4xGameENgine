package server

import org.http4s.{Request, Response}

import scalaz.concurrent.Task


object Maps {
  val testGame = Game.buildTestGame

  def getMapData(req: Request): Task[Response] = {
    //  TODO - validate API key and infer identity
    //  TODO - find game
    //  TODO - find specific map
    ???
  }
}

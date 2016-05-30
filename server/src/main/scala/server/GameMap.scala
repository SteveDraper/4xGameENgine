package server

import map.CartesianSpaceMap
import model.map.MapId
import state.test.TestCellState
import ApiHelper._

import scalaz.syntax.either._

final case class GameMap(id: MapId, mapData: CartesianSpaceMap[Double])

object GameMap {
  val testMapId = MapId("test")
  val testMap = CartesianSpaceMap(10,10,true)(TestCellState.testCellStateOps)

  def buildTestMap =
    GameMap(testMapId, testMap)

  def getGame(id: MapId) =
    if ( id == testMapId ) successM(testMap)
    else wrapM(notFound(s"Map '$id' not found").left)
}
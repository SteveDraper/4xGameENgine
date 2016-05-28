package server

import map.CartesianSpaceMap
import state.test.TestCellState

final case class MapId(value: String) extends AnyVal
final case class GameMap(id: MapId, mapData: CartesianSpaceMap[Double])

object GameMap {
  val testMapId = MapId("test")
  val testMap = CartesianSpaceMap(10,10,true)(TestCellState.testCellStateOps)

  def buildTestMap =
    GameMap(testMapId, testMap)
}
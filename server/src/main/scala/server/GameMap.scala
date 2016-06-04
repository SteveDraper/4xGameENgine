package server

import map.CartesianSpaceMap
import model.map.MapId
import state.test.TestCellState
import ApiHelper._
import server.properties.GamePropertyRegistry
import state.CellState
import state.property.DoubleProperty.DoubleProperty
import state.property.{BasicSparseVectorProperty, PropertyMapState, SimpleCompositePropertyCellState}
import topology.space.CartesianCell

import scalaz.syntax.either._

final case class GameMap(id: MapId, mapData: CartesianSpaceMap[SimpleCompositePropertyCellState])

object GameMap {
  val testMapId = MapId("test")
  val testMapSpace =
    CartesianSpaceMap(10,10,true)(SimpleCompositePropertyCellState.cellStateOps[CartesianCell](1,0))
  lazy val testMap =
    testMapSpace
      .buildFrom(initializeProperties)

  def buildTestMap =
    GameMap(testMapId, testMap)

  def getGame(id: MapId) =
    if ( id == testMapId ) successM(testMap)
    else wrapM(notFound(s"Map '$id' not found").left)

  private def initializeProperties(cell: CartesianCell): SimpleCompositePropertyCellState = {
    val cellLocation = testMapSpace.topology.cellCoordinates(cell).toPoint
    val distanceFrom5_5 =
      Math.sqrt((cellLocation.x - 5.0)*(cellLocation.x - 5.0) + (cellLocation.y - 5.0)*(cellLocation.y - 5.0))

    SimpleCompositePropertyCellState(
      PropertyMapState.buildFrom[DoubleProperty](GamePropertyRegistry.scalarUpdaters,_=> distanceFrom5_5),
      PropertyMapState.buildFrom[BasicSparseVectorProperty[DoubleProperty]](GamePropertyRegistry.vectorUpdaters,_=> null))
  }
}
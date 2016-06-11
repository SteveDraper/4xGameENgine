package server.map

import map.CartesianSpaceMap
import model.map.MapId
import server.ApiHelper._
import server.properties.GamePropertyRegistry
import state.property.DoubleProperty.DoubleProperty
import state.property.{BasicSparseVectorProperty, PropertyMapState, SimpleCompositePropertyCellState}
import topology.{CartesianProjection, Space}
import topology.space.CartesianCell

import scalaz.syntax.either._

final case class GameMap(id: MapId,
                         description: String,
                         mapData: CartesianSpaceMap[SimpleCompositePropertyCellState])

object GameMap {
  def buildTestMap(n: Int, id: MapId) = {
    val mapSpace =
      CartesianSpaceMap(n,n,true)(SimpleCompositePropertyCellState.cellStateOps[CartesianCell](1,0))
    GameMap(
      id,
      s"A $n X $n test map",
      mapSpace.buildFrom(initializeProperties(mapSpace.topology)))
  }

  private def initializeProperties(topology: CartesianProjection[CartesianCell])(cell: CartesianCell): SimpleCompositePropertyCellState = {
    val cellLocation = topology.cellCoordinates(cell).toPoint
    val distanceFrom5_5 =
      Math.sqrt((cellLocation.x - 5.0)*(cellLocation.x - 5.0) + (cellLocation.y - 5.0)*(cellLocation.y - 5.0))

    SimpleCompositePropertyCellState(
      PropertyMapState.buildFrom[DoubleProperty](GamePropertyRegistry.scalarUpdaters,_=> Math.min(GamePropertyRegistry.maxHeight,distanceFrom5_5)),
      PropertyMapState.buildFrom[BasicSparseVectorProperty[DoubleProperty]](GamePropertyRegistry.vectorUpdaters,_=> null))
  }
}
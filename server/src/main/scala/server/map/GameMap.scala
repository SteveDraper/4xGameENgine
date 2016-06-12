package server.map

import map.CartesianSpaceMap
import model.map.MapId
import server.properties.GamePropertyRegistry
import state.property.{BasicSparseVectorProperty, PropertyMapState, SimpleCompositePropertyCellState}
import topology.CartesianProjection
import topology.space.CartesianCell
import server.util.SpanOps._
import state.property.DoubleProperty.DoubleProperty


final case class GameMap(id: MapId,
                         description: String,
                         mapData: CartesianSpaceMap[SimpleCompositePropertyCellState]) {
  def update =
    GameMap(id, description, mapData.run)
}

object GameMap {
  def buildTestMap(n: Int, id: MapId) = {
    val legalN = 2*(n/2)  //  Must be even
    val mapSpace =
      CartesianSpaceMap(legalN,n,true)(SimpleCompositePropertyCellState.cellStateOps[CartesianCell](1,0))
    GameMap(
      id,
      s"A $legalN X $n test map",
      mapSpace.buildFrom(initializeProperties(mapSpace.topology)))
  }

  private def initializeProperties(topology: CartesianProjection[CartesianCell])(cell: CartesianCell): SimpleCompositePropertyCellState = {
    val cellLocation = topology.cellCoordinates(cell).toPoint
    val distanceFrom5_5 =
      Math.sqrt((cellLocation.x - 5.0)*(cellLocation.x - 5.0) + (cellLocation.y - 5.0)*(cellLocation.y - 5.0))
    val height = GamePropertyRegistry.heightSpan.clamp(10.0 - distanceFrom5_5)

    SimpleCompositePropertyCellState(
      PropertyMapState.buildFrom[DoubleProperty](GamePropertyRegistry.scalarUpdaters,_=> height),
      PropertyMapState.buildFrom[BasicSparseVectorProperty[DoubleProperty]](GamePropertyRegistry.vectorUpdaters,_=> null))
  }
}
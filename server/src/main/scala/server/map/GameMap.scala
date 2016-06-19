package server.map

import map.CartesianSpaceMap
import model.map.{Hex, MapId, RectWithDiagonals, TopologyFamily}
import server.ApiHelper.TaskFailureOr
import server.map.builders.{MapBuilder, SimpleMapBuilder}
import server.properties.GamePropertyRegistry
import state.property.{BasicSparseVectorProperty, PropertyMapState, SimpleCompositePropertyCellState}
import topology.CartesianProjection
import topology.space.CartesianCell
import server.util.SpanOps._
import server.ApiHelper._

import scalaz.syntax.either._


final case class GameMap(id: MapId,
                         description: String,
                         mapData: CartesianSpaceMap[SimpleCompositePropertyCellState]) {
  def update =
    GameMap(id, description, mapData.run)
}

object GameMap {
  def  buildMap(id: MapId,
                description: String,
                baseTopology: TopologyFamily,
                widthInCells: Int,
                heightInCells: Int,
                wrapX: Boolean,
                wrapY:Boolean,
                initializer: MapBuilder) = {
    val emptyMapSpace = baseTopology match {
      case Hex => CartesianSpaceMap(widthInCells,heightInCells,true,wrapX,wrapY)(SimpleCompositePropertyCellState.cellStateOps[CartesianCell](1,0))
      case RectWithDiagonals => CartesianSpaceMap(widthInCells,heightInCells,false,wrapX,wrapY)(SimpleCompositePropertyCellState.cellStateOps[CartesianCell](1,0))
    }

    for {
      initialized <- initializer.initialize(emptyMapSpace)
    } yield GameMap(
      id,
      description,
      initialized
    )
  }

  def buildTestMap(n: Int, id: MapId): TaskFailureOr[GameMap] = {
    val initializer = SimpleMapBuilder(0.01)

    val legalN = 2*(n/2)  //  Must be even
    buildMap(
      id,
      s"A $legalN X $n test map",
      Hex,
      legalN,
      n,
      true,
      false,
      initializer)
  }
}
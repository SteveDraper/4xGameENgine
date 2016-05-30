package topology.space.hex

import model.map.{Hex, MapTopology, MapTopologyProvider}
import topology.space.CartesianCell
import topology.{CartesianCoordinates, CartesianProjection, Neighbourhood, Space}

case class HexSpace(width: Int, height: Int, wrapX: Boolean, wrapY: Boolean)
  extends Space[CartesianCell]
    with CartesianProjection[CartesianCell]
    with MapTopologyProvider {
  private val cells: Array[CartesianCell] = Array.tabulate(width*height)(index => CartesianCell(index%width,index/width,index))

  def cellAt(x: Int, y: Int) = cells(x + y*width)

  def neighbourhoods: Traversable[Neighbourhood[CartesianCell]] = cells.map(c => DynamicHexNeighbourhood(c,this))

  def cellCoordinates(c: CartesianCell): CartesianCoordinates = {
    val yAdjust = if (c.x%2 == 0) 0.0 else -HexSpace.root3/2.0
    CartesianCoordinates(
      c.x.toDouble*3.0/2.0,
      c.y.toDouble*HexSpace.root3 + yAdjust)
  }

  def projectionWidth =
    (3*width).toDouble/2.0

  def projectionHeight =
    HexSpace.root3*height

  def getMapTopology: MapTopology =
    MapTopology(Hex, wrapX, wrapY, HexSpace.root3)
}

object HexSpace {
  val root3 = Math.sqrt(3.0)
}

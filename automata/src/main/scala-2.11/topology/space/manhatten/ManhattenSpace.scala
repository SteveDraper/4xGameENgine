package topology.space.manhatten

import model.map.{MapTopology, RectWithDiagonals}
import topology.space.CartesianCell
import topology.{CartesianCoordinates, CartesianProjection, Neighbourhood, Space}

case class ManhattenSpace(width: Int, height: Int, wrapX: Boolean, wrapY: Boolean)
  extends Space[CartesianCell]
  with CartesianProjection[CartesianCell] {
  private val cells: Array[CartesianCell] = Array.tabulate(width*height)(index => CartesianCell(index%width,index/width,index))

  def cellAt(x: Int, y: Int) = cells(x + y*width)

  def neighbourhoods: Traversable[Neighbourhood[CartesianCell]] = cells.map(c => DynamicManhattenNeighbourhood(c,this))

  def projectionWidth =
    width.toDouble

  def projectionHeight =
    height.toDouble

  def getMapTopology: MapTopology =
    MapTopology(RectWithDiagonals, wrapX, wrapY, 1.0)

  def cellCoordinates(c: CartesianCell): CartesianCoordinates =
    CartesianCoordinates(c.x,c.y)
}

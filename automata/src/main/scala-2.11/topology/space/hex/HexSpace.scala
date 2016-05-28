package topology.space.hex

import topology.space.CartesianCell
import topology.{Neighbourhood, Space}

case class HexSpace(width: Int, height: Int, wrapX: Boolean, wrapY: Boolean) extends Space[CartesianCell] {
  private val cells: Array[CartesianCell] = Array.tabulate(width*height)(index => CartesianCell(index%width,index/width,index))

  def cellAt(x: Int, y: Int) = cells(x + y*width)

  def neighbourhoods: Traversable[Neighbourhood[CartesianCell]] = cells.map(c => DynamicHexNeighbourhood(c,this))
}

package map

import topology.Cell

trait SpaceMap[C <: Cell,S <: CellState[_]] {
  def cells: Traversable[C]
  def cellStateValue(cell: C): S
}

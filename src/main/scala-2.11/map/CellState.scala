package map

import topology.Cell


trait CellState[S <: CellState[_]] {
  def update[C <: Cell](map: SpaceMap[C,S], neighbours: Traversable[C]): S
}

trait CellStateOps[C,S] {
  def initialize(cell: C): S
}

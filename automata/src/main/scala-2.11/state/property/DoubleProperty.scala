package state.property

import map.SpaceMap
import topology.Cell
import DoubleProperty._
import state.CellState

trait PropertyUpdater[P] {
  def update[C <: Cell,R](initialValue: P,
                        id: PropertyId,
                        cellState: C => R,
                        parentLens: R => PropertyMapState[P],
                        neighbours: Traversable[C]): P
}

trait DoublePropertyUpdater extends PropertyUpdater[DoubleProperty] {
  def update[C <: Cell,R](initialValue: DoubleProperty,
                        id: PropertyId,
                        cellState: C => R,
                        parentLens: R => PropertyMapState[DoubleProperty],
                        neighbours: Traversable[C]): DoubleProperty
}

object DoubleProperty {
  type DoubleProperty = Double
}

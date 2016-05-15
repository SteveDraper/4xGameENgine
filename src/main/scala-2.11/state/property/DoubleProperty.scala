package state.property

import map.SpaceMap
import topology.Cell
import DoubleProperty._

trait PropertyUpdater[P] {
  def update[C <: Cell](initialValue: P,
                        id: PropertyId,
                        map: SpaceMap[C, PropertyMapState[P]],
                        neighbours: Traversable[C],
                        parent: PropertyMapState[P]): P
}

trait DoublePropertyUpdater extends PropertyUpdater[DoubleProperty] {
  def update[C <: Cell](initialValue: DoubleProperty,
                        id: PropertyId,
                        map: SpaceMap[C, PropertyMapState[DoubleProperty]],
                        neighbours: Traversable[C],
                        parent: PropertyMapState[DoubleProperty]): DoubleProperty
}

object DoubleProperty {
  type DoubleProperty = Double
}

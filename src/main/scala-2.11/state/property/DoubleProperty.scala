package state.property

import map.SpaceMap
import topology.Cell


final case class DoubleProperty(value: Double) extends AnyVal

trait DoublePropertyUpdater {
  def update[C <: Cell](initialValue: DoubleProperty,
                        id: PropertyId,
                        map: SpaceMap[C, PropertyMapState],
                        neighbours: Traversable[C],
                        parent: PropertyMapState): DoubleProperty
}

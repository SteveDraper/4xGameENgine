package state.test

import map.SpaceMap
import state.property.{DoublePropertyUpdater, PropertyId, PropertyMapState, DoubleProperty}
import topology.Cell
import DoubleProperty._

object TestPropertyUpdater extends DoublePropertyUpdater {
  def update[C <: Cell](initialValue: DoubleProperty,
                        id: PropertyId,
                        map: SpaceMap[C, PropertyMapState[DoubleProperty]],
                        neighbours: Traversable[C],
                        parent: PropertyMapState[DoubleProperty]): DoubleProperty = {
    var count = 0;
    var total = 0.0
    neighbours.foreach(c => {
      count = count+1
      total = total + map.cellStateValue(c).properties(id.value) - initialValue
    })

    //  Add average difference of neighbour values from own value / 4
    initialValue + total/(4*count)
  }
}
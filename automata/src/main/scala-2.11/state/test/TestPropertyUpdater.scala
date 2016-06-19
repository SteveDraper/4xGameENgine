package state.test

import state.property.{DoublePropertyUpdater, PropertyId, PropertyMapState}
import topology.{Cell, Neighbourhood}
import state.property.PropertyUpdater.DoubleProperty

object TestPropertyUpdater extends DoublePropertyUpdater {
  def update[C <: Cell,R](initialValue: DoubleProperty,
                          id: PropertyId,
                          cellState: C => R,
                          parentLens: R => PropertyMapState[DoubleProperty],
                          neighbourhood: Neighbourhood[C]): DoubleProperty = {
    var count = 0;
    var total = 0.0
    neighbourhood.neighbours.foreach(c => {
      count = count+1
      total = total + parentLens(cellState(c)).properties(id.value) - initialValue
    })

    //  Add average difference of neighbour values from own value / 4
    initialValue + total/(4*count)
  }
}
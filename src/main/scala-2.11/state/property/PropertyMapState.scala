package state.property

import map.SpaceMap
import state.CellState
import state.property.PropertyMapState.DoublePropertyUpdaterMap
import topology.Cell

import scala.collection.mutable

final case class PropertyId(value: Int) extends AnyVal

final case class PropertyMapState(properties: Array[DoubleProperty], updaters: DoublePropertyUpdaterMap) extends CellState[PropertyMapState] {
  def update[C <: Cell](map: SpaceMap[C, PropertyMapState], neighbours: Traversable[C]): PropertyMapState = {
    //  TODO - optimize identity mapping case to minimize GC
    val numProperties = properties.size
    val builder = new mutable.ArraySeq[DoubleProperty](numProperties)
    var i = 0
    while(i < numProperties) {
      builder(i) = updaters(i).update(properties(i),PropertyId(i),map, neighbours, this)
      i += 1
    }
    PropertyMapState(builder.toArray, updaters)
  }

  override def toString =
    if (properties.size == 1) properties(0).toString
    else properties.toString
}

object PropertyMapState {
  type DoublePropertyUpdaterMap = Array[DoublePropertyUpdater]
}

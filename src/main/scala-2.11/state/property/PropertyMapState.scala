package state.property

import map.SpaceMap
import state.CellState
import state.property.PropertyMapState.PropertyUpdaterMap
import topology.Cell

import scala.collection.mutable
import scala.reflect.ClassTag

final case class PropertyId(value: Int) extends AnyVal

final case class PropertyMapState[P](properties: Array[P], updaters: PropertyUpdaterMap[P])(implicit tag: ClassTag[P]) extends CellState[PropertyMapState[P]] {
  def update[C <: Cell](map: SpaceMap[C, PropertyMapState[P]], neighbours: Traversable[C]): PropertyMapState[P] = {
    //  TODO - optimize identity mapping case to minimize GC
    val numProperties = properties.size
    val builder = new Array[P](numProperties)
    var i = 0
    while(i < numProperties) {
      builder(i) = updaters(i).update(properties(i),PropertyId(i),map, neighbours, this)
      i += 1
    }
    PropertyMapState(builder, updaters)
  }

  override def toString =
    if (properties.size == 1) properties(0).toString
    else properties.toString
}

object PropertyMapState {
  type PropertyUpdaterMap[P] = Array[PropertyUpdater[P]]
}

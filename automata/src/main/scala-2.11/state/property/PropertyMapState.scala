package state.property

import map.SpaceMap
import state.CellState
import state.property.PropertyMapState.PropertyUpdaterMap
import topology.Cell

import scala.collection.mutable
import scala.reflect.ClassTag

final case class PropertyId(value: Int) extends AnyVal

sealed trait PropertyCollection[P] extends Traversable[(PropertyId,P)] {
  def find(id: PropertyId): Option[P]
}

final case class PropertyMapState[P](properties: Array[P], updaters: PropertyUpdaterMap[P])(implicit tag: ClassTag[P])
  extends CellState[PropertyMapState[P]]
  with PropertyCollection[P] {
  def get = this
  def update[C <: Cell,R](cellState: C => R, selfLens: R => PropertyMapState[P], neighbours: Traversable[C]): PropertyMapState[P] = {
    //  TODO - optimize identity mapping case to minimize GC
    val numProperties = properties.size
    val builder = new Array[P](numProperties)
    var i = 0
    while(i < numProperties) {
      builder(i) = updaters(i).update(properties(i),PropertyId(i),cellState,selfLens, neighbours)
      i += 1
    }
    PropertyMapState(builder, updaters)
  }

  override def toString =
    if (properties.size == 1) properties(0).toString
    else properties.toString

  def find(id: PropertyId): Option[P] = {
    if (id.value < properties.length) Some(properties(id.value))
    else None
  }

  def foreach[U](f: ((PropertyId, P)) => U): Unit = {
    var i = 0
    while(i < properties.length) {
      f((PropertyId(i),properties(i)))
      i += 1
    }
  }
}

object PropertyMapState {
  type PropertyUpdaterMap[P] = Array[PropertyUpdater[P]]

  def buildFrom[P](updaters: Array[PropertyUpdater[P]], f: PropertyId => P)(implicit tag: ClassTag[P]): PropertyMapState[P] = {
    val numProperties = updaters.length
    val builder = new Array[P](numProperties)
    var i = 0
    while(i < numProperties) {
      builder(i) = f(PropertyId(i))
      i += 1
    }
    PropertyMapState(builder, updaters)
  }
}

package state.property

import map.SpaceMap
import topology.{Cell, Neighbourhood}
import state.CellState

import PropertyUpdater._

trait PropertyUpdater[P] {
  def update[C <: Cell,R](initialValue: P,
                          id: PropertyId,
                          cellState: C => R,
                          parentLens: R => PropertyMapState[P],
                          neighbourhood: Neighbourhood[C]): P
}

trait DoublePropertyUpdater extends PropertyUpdater[DoubleProperty] {
  def update[C <: Cell,R](initialValue: DoubleProperty,
                          id: PropertyId,
                          cellState: C => R,
                          parentLens: R => PropertyMapState[DoubleProperty],
                          neighbourhood: Neighbourhood[C]): DoubleProperty
}

object PropertyUpdater {
  type DoubleProperty = Double

  def identityUpdater[P] = new PropertyUpdater[P] {
    def update[C <: Cell, R](initialValue: P, id: PropertyId, cellState: (C) => R, parentLens: (R) => PropertyMapState[P], neighbourhood: Neighbourhood[C]): P =
      initialValue
  }
}

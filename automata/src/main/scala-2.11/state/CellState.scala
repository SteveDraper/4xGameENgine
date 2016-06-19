package state

import map.SpaceMap
import topology.{Cell, Neighbourhood}


trait CellState[S] {
  def get: S
  def update[C <: Cell,R](cellState: C => R, selfLens: R => S, neighbourhood: Neighbourhood[C]): CellState[S]
}

trait CellStateOps[C,S] {
  def initialize(cell: C): CellState[S]
}

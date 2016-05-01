package automata

import topology.{Cell, Neighbourhood}


case class AutomataCell[T,C <: Cell](state: T, localTopology: Neighbourhood[C])

package automata

import topology.Neighbourhood


case class AutomataCell[T,C](state: T, localTopology: Neighbourhood[C])

package automata

trait AutomataTopology[R[_,_],C,T] {
  def cellMap(r :R[C,T]): C => T

  def map[T2](r: R[C,T])(f: T => T2): R[C,T2]
}

case class SimpleAutomataTopologyRep[C,T](m: Map[C,T]) {
  def cellMap = (c:C) => m.get(c).get

  def map[T2](f: T => T2): SimpleAutomataTopologyRep[C,T2] =
    SimpleAutomataTopologyRep(m.transform((c,t)=>f(t)))
}

object SimpleAutomataTopology {
  def simpleAutomataTopology[C,T] = new AutomataTopology[SimpleAutomataTopologyRep,C,T] {
    def cellMap(r: SimpleAutomataTopologyRep[C, T]) = c => r.cellMap(c)

    def map[T2](r: SimpleAutomataTopologyRep[C, T])(f: (T) => T2) = r.map(f)
  }
}

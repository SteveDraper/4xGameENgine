package automata

import scala.reflect.ClassTag

trait AutomataTopology[C,T] {
  def cellMap: C => T

  def map[T2](f: T => T2)(implicit tag: ClassTag[T2]): AutomataTopology[C,T2]
}

case class SimpleAutomataTopologyRep[C,T](m: Map[C,T]) extends AutomataTopology[C,T] {
  def cellMap = (c:C) => m.get(c).get

  def map[T2](f: T => T2)(implicit tag: ClassTag[T2]): SimpleAutomataTopologyRep[C,T2] =
    SimpleAutomataTopologyRep(m.transform((c,t)=>f(t)))
}

object SimpleAutomataTopology {
  /*def simpleAutomataTopology[C,T] = new AutomataTopology[SimpleAutomataTopologyRep,C,T] {
    def cellMap(r: SimpleAutomataTopologyRep[C, T]) = c => r.cellMap(c)

    def map[T2](r: SimpleAutomataTopologyRep[C, T])(f: (T) => T2)(implicit tag: ClassTag[T2]) = r.map(f)
  }*/
}

/*trait AutomataTopology[R[_ <: C,_],C,T] {
  def cellMap(r :R[C,T]): C => T

  def map[T2](r: R[C,T])(f: T => T2)(implicit tag: ClassTag[T2]): R[C,T2]
}

case class SimpleAutomataTopologyRep[C,T](m: Map[C,T]) {
  def cellMap = (c:C) => m.get(c).get

  def map[T2](f: T => T2)(implicit tag: ClassTag[T2]): SimpleAutomataTopologyRep[C,T2] =
    SimpleAutomataTopologyRep(m.transform((c,t)=>f(t)))
}

object SimpleAutomataTopology {
  def simpleAutomataTopology[C,T] = new AutomataTopology[SimpleAutomataTopologyRep,C,T] {
    def cellMap(r: SimpleAutomataTopologyRep[C, T]) = c => r.cellMap(c)

    def map[T2](r: SimpleAutomataTopologyRep[C, T])(f: (T) => T2)(implicit tag: ClassTag[T2]) = r.map(f)
  }
}*/

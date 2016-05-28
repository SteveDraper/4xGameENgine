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

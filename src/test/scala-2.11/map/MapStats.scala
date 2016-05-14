package map

import state.CellState
import state.test.DoubleValued
import topology.Cell

import scalaz.Monoid
import scalaz.std.AllInstances._


case class MapStats(cellAverage: Double, cellStdDev: Double)

object MapStats {
  implicit val doubleMonoid = new Monoid[Double] {
    def zero: Double = 0.0

    def append(f1: Double, f2: => Double): Double = f1 + f2
  }

  def sumGeneric[A](l: Traversable[A])(implicit A: Monoid[A]): A =
    l.foldLeft(A.zero)((x, y) => A.append(x, y))

  def apply[C <: Cell, S <: CellState[S]](m: SpaceMap[C,S])(implicit getDouble: DoubleValued[S]): MapStats = {
    val sums = sumGeneric(
      for {
        state <- m.cells map m.cellStateValue
      } yield {
        val value = getDouble(state)
        (value, value*value, 1)
      })

    val mean = sums._1/sums._3
    MapStats(mean, math.sqrt(sums._2 - mean*mean*sums._3)/sums._3)
  }
}
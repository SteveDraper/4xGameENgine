package util

import scala.collection.parallel.mutable.ParArray


class DefaultPArray[A] private (a: ParArray[A]) extends PArray[A] {
  def map[B](f: (A) => B): PArray[B] = new DefaultPArray(a.map(f))

  def get(index: Int): A = a(index)
}

object DefaultPArray {
  def apply[A](from: Array[A]) = new DefaultPArray(from.par)
}
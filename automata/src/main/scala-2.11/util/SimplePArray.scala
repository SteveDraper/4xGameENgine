package util

import scala.collection.mutable
import scala.reflect.ClassTag


class SimplePArray[A] private (a: mutable.ArraySeq[A]) extends PArray[A] {
  def map[B](f: (A) => B): PArray[B] = new SimplePArray(a.map(f))
  def get(index: Int) = a(index)
}

object SimplePArray {
  def apply[A](from: Array[A]) = new SimplePArray[A](from.map(identity))
}
package util

import scala.collection.mutable
import scala.reflect.ClassTag
import scalaz.concurrent.Task

import scalaz.syntax.std.boolean._

class TaskPArray[A] private (a: mutable.ArraySeq[A]) extends PArray[A] {
  def map[B](f: (A) => B): PArray[B] = {
    val s = a.size
    val numSubTasks = 2
    val subLen = s/numSubTasks

    def mapSubArray(b: mutable.ArraySeq[B], r: (Int,Int)) = Task.delay {
      for(i <- r._1 to r._2) b(i) = f(a(i))
    }

    val newArray = new mutable.ArraySeq[B](s)

    val subTasks = (1 to numSubTasks)
      .map {n => ((n-1)*subLen, (n == numSubTasks) ? (a.size-1) | n*subLen-1)}
      .map(r => Task.fork(mapSubArray(newArray,r)))

    Task.gatherUnordered(subTasks).unsafePerformSync

    new TaskPArray(newArray)
  }
  def get(index: Int) = a(index)
}

object TaskPArray {
  def apply[A](from: Array[A]) = new TaskPArray[A](from.map(identity))
}
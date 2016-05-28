package state.test

import state.CellState
import state.property.{BasicSparseVectorProperty, ElementId, SparseVectorPropertyUpdater}


object TestSparseVectorPropertyUpdater extends SparseVectorPropertyUpdater[Double, BasicSparseVectorProperty[Double]] {
  val zero = 0.0

  def elementUpdate(initialValue: Double, neighbourValues: Traversable[Double]): Double = {
    var count = 0
    var total = 0.0
    neighbourValues.foreach(c => {
      count = count+1
      total = total + c - initialValue
    })

    //  Add average difference of neighbour values from own value / 4
    if (count == 0) initialValue - initialValue/4
    else initialValue + total/(4*count)
  }

  def build(from: IndexedSeq[(ElementId, Double)]): BasicSparseVectorProperty[Double] = {
    BasicSparseVectorProperty(from.toArray)
  }
}
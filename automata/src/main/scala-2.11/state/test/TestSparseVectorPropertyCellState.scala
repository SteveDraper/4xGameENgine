package state.test

import state.{CellStateOps}
import state.property._
import topology.space.CartesianCell

import scalaz.Show


object TestSparseVectorPropertyCellState {
  type TestSparseVectorPropertyCellState = PropertyMapState[BasicSparseVectorProperty[Double]]

  val TEST_PROPERTY_ID1 = PropertyId(0)

  def stateVal(x: Double, numElements: Int) =
    PropertyMapState(
      Array.tabulate[BasicSparseVectorProperty[Double]](1)(n => {
        if (x == 0.0) TestSparseVectorPropertyUpdater.build(Array.empty[(ElementId,Double)])
        else TestSparseVectorPropertyUpdater.build((0 until numElements).map(i => (ElementId(i),x)))
      }),
      Array.fill[PropertyUpdater[BasicSparseVectorProperty[Double]]](1)(TestSparseVectorPropertyUpdater))

  def initialize(forCell: CartesianCell, numProperties: Int): TestSparseVectorPropertyCellState =
    if ( forCell.x == 5 && forCell.y == 5 ) stateVal(100.0, numProperties)
    else stateVal(0.0, numProperties)

  implicit def stateDoubleValued = new DoubleValued[TestSparseVectorPropertyCellState] {
    def apply(a: TestSparseVectorPropertyCellState): Double =
      a.properties(TEST_PROPERTY_ID1.value).get(ElementId(0)).getOrElse(0.0)
  }

  implicit def stateShow = new Show[TestSparseVectorPropertyCellState] {
    override def shows(s: TestSparseVectorPropertyCellState) = {
      s.properties(TEST_PROPERTY_ID1.value).get(ElementId(1)).getOrElse(0.0).toString
    }
  }

  implicit def testCellStateOps[C <: CartesianCell](numProperties: Int) = new CellStateOps[C,TestSparseVectorPropertyCellState] {
    def initialize(cell: C): TestSparseVectorPropertyCellState = TestSparseVectorPropertyCellState.initialize(cell, numProperties)
  }
}

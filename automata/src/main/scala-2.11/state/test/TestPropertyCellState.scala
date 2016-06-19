package state.test

import state.property._
import state.{CellStateOps}
import topology.space.CartesianCell
import state.property.PropertyUpdater.DoubleProperty


object TestPropertyCellState {
  type TestPropertyCellState = PropertyMapState[DoubleProperty]

  val TEST_PROPERTY_ID1 = PropertyId(0)
  val TEST_PROPERTY_ID2 = PropertyId(1)

  def stateVal(x: Double, numProperties: Int) =
    PropertyMapState(
      Array.tabulate[DoubleProperty](numProperties)(n => x),
      Array.fill[PropertyUpdater[DoubleProperty]](numProperties)(TestPropertyUpdater))

  def initialize(forCell: CartesianCell, numProperties: Int): TestPropertyCellState =
    if ( forCell.x == 5 && forCell.y == 5 ) stateVal(100.0, numProperties)
    else stateVal(0.0, numProperties)

  implicit def stateDoubleValued = new DoubleValued[TestPropertyCellState] {
    def apply(a: TestPropertyCellState): Double = a.properties(TEST_PROPERTY_ID1.value)
  }

  implicit def testCellStateOps[C <: CartesianCell](numProperties: Int) = new CellStateOps[C,TestPropertyCellState] {
    def initialize(cell: C): TestPropertyCellState = TestPropertyCellState.initialize(cell, numProperties)
  }
}


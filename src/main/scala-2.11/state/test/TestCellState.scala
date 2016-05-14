package state.test

import map.SpaceMap
import state.{CellStateOps, CellState}
import topology.Cell
import topology.space.CartesianCell

case class TestCellState(value: Double) extends CellState[TestCellState] {
  def update[A <: Cell](map: SpaceMap[A,TestCellState], neighbours: Traversable[A]): TestCellState = {
    var count = 0;
    var total = 0.0
    neighbours.foreach(c => {
      count = count+1
      total = total + map.cellStateValue(c).value - value
    })

    //  Add average difference of neighbour values from own value / 4
    TestCellState(value + total/(4*count))
  }
}

object TestCellState {
  def initialize(forCell: CartesianCell): TestCellState =
    if ( forCell.x == 5 && forCell.y == 5 ) TestCellState(100.0)
    else TestCellState(0.0)

  implicit val stateDoubleValued = new DoubleValued[TestCellState] {
    def apply(a: TestCellState): Double = a.value
  }

  implicit def testCellStateOps[C <: CartesianCell] = new CellStateOps[C,TestCellState] {
    def initialize(cell: C): TestCellState = TestCellState.initialize(cell)
  }
}


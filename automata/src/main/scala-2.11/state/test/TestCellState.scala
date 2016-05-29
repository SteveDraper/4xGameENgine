package state.test

import map.SpaceMap
import state.{CellState, CellStateOps}
import topology.Cell
import topology.space.CartesianCell

import scalaz.Show

case class TestCellState(value: Double) extends CellState[Double] {
  def get = value
  def update[C <: Cell,R](cellState: C => R, selfLens: R => Double, neighbours: Traversable[C]): TestCellState = {
    var count = 0
    var total = 0.0
    neighbours.foreach(c => {
      count = count+1
      total = total + selfLens(cellState(c)) - value
    })

    //  Add average difference of neighbour values from own value / 4
    TestCellState(value + total/(4*count))
  }
}

object TestCellState {
  def initialize(forCell: CartesianCell): TestCellState =
    if ( forCell.x == 5 && forCell.y == 5 ) TestCellState(100.0)
    else TestCellState(0.0)

  implicit def stateDoubleValued = new DoubleValued[Double] {
    def apply(a: Double): Double = a
  }

  implicit def testCellStateOps[C <: CartesianCell] = new CellStateOps[C,Double] {
    def initialize(cell: C): TestCellState = TestCellState.initialize(cell)
  }

  implicit def stateShow = new Show[TestCellState] {
    override def shows(s: TestCellState) = {
      s.value.toString
    }
  }
}


package state.test

import topology.space.CartesianCell

case class TestCellState(value: Double) extends AnyVal

object TestCellState {
  def initialize(forCell: CartesianCell): TestCellState =
    if ( forCell.x == 5 && forCell.y == 5 ) TestCellState(100.0)
    else TestCellState(0.0)
}

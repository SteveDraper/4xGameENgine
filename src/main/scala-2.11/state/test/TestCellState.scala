package state.test

import topology.space.manhatten.ManhattenCell

case class TestCellState(value: Double) extends AnyRef with DoubleValued {
  def getDouble: Double = value
}

object TestCellState {
  def initialize(forCell: ManhattenCell): TestCellState =
    if ( forCell.x == 5 && forCell.y == 5 ) TestCellState(100.0)
    else TestCellState(0.0)
}

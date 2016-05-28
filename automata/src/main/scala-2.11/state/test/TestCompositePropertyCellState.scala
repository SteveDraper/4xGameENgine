package state.test

import map.SpaceMap
import state.{CellState, CellStateOps}
import state.property.DoubleProperty._
import state.property.{DoubleProperty => _, _}
import topology.Cell
import topology.space.CartesianCell

import scalaz.Show

final case class TestComposite(scalarProperties: PropertyMapState[DoubleProperty],
                                  vectorProperties: PropertyMapState[BasicSparseVectorProperty[Double]])

final case class TestCompositePropertyCellState(value: TestComposite) extends CellState[TestComposite] {
  def get = value
  def update[C <: Cell,R](cellState: C => R, selfLens: R => TestComposite, neighbours: Traversable[C]): TestCompositePropertyCellState = {
    TestCompositePropertyCellState(
      TestComposite(
        value.scalarProperties.update(cellState, scalarLens(selfLens), neighbours),
        value.vectorProperties.update(cellState, vectorLens(selfLens), neighbours))
    )
  }

  private def scalarLens[R](selfLens: R => TestComposite) = { (r: R) =>
    selfLens(r).scalarProperties
  }

  private def vectorLens[R](selfLens: R => TestComposite) = { (r: R) =>
    selfLens(r).vectorProperties
  }
}

object TestCompositePropertyCellState {
  def stateVal(x: Double, numProperties: Int) =
    TestCompositePropertyCellState(
      TestComposite(
        PropertyMapState(
          Array.tabulate[DoubleProperty](numProperties)(n => x),
          Array.fill[PropertyUpdater[DoubleProperty]](numProperties)(TestPropertyUpdater)),
        PropertyMapState(
          Array.tabulate[BasicSparseVectorProperty[Double]](numProperties)(n => {
            if (x == 0.0) TestSparseVectorPropertyUpdater.build(Array.empty[(ElementId,Double)])
            else TestSparseVectorPropertyUpdater.build((0 until numProperties).map(i => (ElementId(i),x)))
          }),
          Array.fill[PropertyUpdater[BasicSparseVectorProperty[Double]]](numProperties)(TestSparseVectorPropertyUpdater)))
    )

  def initialize(forCell: CartesianCell, numProperties: Int): TestCompositePropertyCellState =
    if ( forCell.x == 5 && forCell.y == 5 ) stateVal(100.0, numProperties)
    else stateVal(0.0, numProperties)

  implicit def stateDoubleValued = new DoubleValued[TestComposite] {
    def apply(a: TestComposite): Double = a.scalarProperties.properties(0)
  }

  implicit def stateShow = new Show[TestComposite] {
    override def shows(s: TestComposite) = {
      s.scalarProperties.properties(0).toString
    }
  }
  implicit def testCellStateOps[C <: CartesianCell](numProperties: Int) = new CellStateOps[C,TestComposite] {
    def initialize(cell: C): TestCompositePropertyCellState = TestCompositePropertyCellState.initialize(cell, numProperties)
  }
}

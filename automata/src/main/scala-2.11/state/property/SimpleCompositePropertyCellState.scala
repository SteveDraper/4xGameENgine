package state.property

import state.property.PropertyUpdater.DoubleProperty
import state.test._
import state.{CellState, CellStateOps}
import topology.{Cell, Neighbourhood}
import topology.space.CartesianCell

import scalaz.Show

/*final case class SimpleComposite(scalarProperties: PropertyMapState[DoubleProperty],
                                 vectorProperties: PropertyMapState[BasicSparseVectorProperty[Double]])

final case class SimpleCompositePropertyCellState(value: SimpleComposite) extends CellState[SimpleComposite] {
  def get = value
  def update[C <: Cell,R](cellState: C => R, selfLens: R => SimpleComposite, neighbours: Traversable[C]): SimpleCompositePropertyCellState = {
    SimpleCompositePropertyCellState(
      SimpleComposite(
        value.scalarProperties.update(cellState, scalarLens(selfLens), neighbours),
        value.vectorProperties.update(cellState, vectorLens(selfLens), neighbours))
    )
  }

  private def scalarLens[R](selfLens: R => SimpleComposite) = { (r: R) =>
    selfLens(r).scalarProperties
  }

  private def vectorLens[R](selfLens: R => SimpleComposite) = { (r: R) =>
    selfLens(r).vectorProperties
  }
}*/
final case class SimpleCompositePropertyCellState(scalarProperties: PropertyMapState[DoubleProperty],
                                 vectorProperties: PropertyMapState[BasicSparseVectorProperty[Double]]) extends CellState[SimpleCompositePropertyCellState] {
  def get = this
  def update[C <: Cell,R](cellState: C => R, selfLens: R => SimpleCompositePropertyCellState, neighbourhood: Neighbourhood[C]): SimpleCompositePropertyCellState = {
    SimpleCompositePropertyCellState(
      scalarProperties.update(cellState, scalarLens(selfLens), neighbourhood),
      vectorProperties.update(cellState, vectorLens(selfLens), neighbourhood))
  }

  private def scalarLens[R](selfLens: R => SimpleCompositePropertyCellState) = { (r: R) =>
    selfLens(r).scalarProperties
  }

  private def vectorLens[R](selfLens: R => SimpleCompositePropertyCellState) = { (r: R) =>
    selfLens(r).vectorProperties
  }
}


object SimpleCompositePropertyCellState {
  def stateVal(x: Double, numScalarProperties: Int, numVectorProperties: Int) =
    SimpleCompositePropertyCellState(
      PropertyMapState(
        Array.tabulate[DoubleProperty](numScalarProperties)(n => x),
        Array.fill[PropertyUpdater[DoubleProperty]](numScalarProperties)(TestPropertyUpdater)),
      PropertyMapState(
        Array.tabulate[BasicSparseVectorProperty[Double]](numVectorProperties)(n => {
          if (x == 0.0) TestSparseVectorPropertyUpdater.build(Array.empty[(ElementId,Double)])
          else TestSparseVectorPropertyUpdater.build((0 until numVectorProperties).map(i => (ElementId(i),x)))
        }),
        Array.fill[PropertyUpdater[BasicSparseVectorProperty[Double]]](numVectorProperties)(TestSparseVectorPropertyUpdater)))

  def initialize(forCell: CartesianCell, numProperties: Int, numVectorProperties: Int): SimpleCompositePropertyCellState =
    if ( forCell.x == 5 && forCell.y == 5 ) stateVal(100.0, numProperties, numVectorProperties)
    else stateVal(0.0, numProperties, numVectorProperties)

  implicit def stateDoubleValued = new DoubleValued[SimpleCompositePropertyCellState] {
    def apply(a: SimpleCompositePropertyCellState): Double = a.scalarProperties.properties(0)
  }

  implicit def stateShow = new Show[SimpleCompositePropertyCellState] {
    override def shows(s: SimpleCompositePropertyCellState) = {
      s.scalarProperties.properties(0).toString
    }
  }
  implicit def cellStateOps[C <: CartesianCell](numScalarProperties: Int, numVectorProperties: Int) = new CellStateOps[C,SimpleCompositePropertyCellState] {
    def initialize(cell: C): SimpleCompositePropertyCellState = SimpleCompositePropertyCellState.initialize(cell, numScalarProperties, numVectorProperties)
  }
}

package map

import automata.{AutomataCell, AutomataTopology}
import state.test.{DoubleValued, TestCellState}
import topology.space.CartesianCell
import topology.space.hex.HexSpace
import topology.space.manhatten.ManhattenSpace
import topology.{Cell, Neighbourhood, Space}

import scala.collection.parallel.mutable.ParArray
import scala.reflect.ClassTag

case class TestSpaceMap[A <: CellState[A]]
    (topology: Space[CartesianCell],
     at: AutomataTopology[CartesianCell,AutomataCell[A,CartesianCell]],
     width: Int,
     height: Int) extends SpaceMap[CartesianCell, A] {

  def cells = topology.neighbourhoods map(_.center)
  def cellStateValue(cell: CartesianCell) =
    at.cellMap(cell).state

  def run = {
    def updateCell(mcell: AutomataCell[A,CartesianCell]) = {
      AutomataCell(mcell.state.update(this, mcell.localTopology.neighbours), mcell.localTopology)
    }

    val newAutoTopology = at.map(updateCell)
    val result = new TestSpaceMap(topology, newAutoTopology, width, height)
    result
  }

  def render = {
    println
    val coordsMap: Map[(Int,Int),A] = topology.neighbourhoods.map(n => (n.center.x, n.center.y) -> at.cellMap(n.center).state).toMap
    for(y <- 0 to height-1) {
      val colVals = for {
        x <- 0 to width - 1
      } yield coordsMap.get((x,y)).getOrElse(0.0).toString

      println(colVals.mkString(""," ",""))
    }
    println
  }
}

object TestSpaceMap {
  implicit val stateDoubleValued = new DoubleValued[TestCellState] {
    def apply(a: TestCellState): Double = a.value
  }
  def apply[S <: CellState[S]](width: Int, height: Int, useHex: Boolean)(implicit cellStateOps: CellStateOps[CartesianCell,S]): TestSpaceMap[S] = {
    val topology: Space[CartesianCell] = if (useHex) new HexSpace(width, height, true, false) else new ManhattenSpace(width, height, true, false)
    val autoTopology: AutomataTopology[CartesianCell,AutomataCell[S,CartesianCell]] =
      IndexedAutomataTopologyRep[CartesianCell,AutomataCell[S,CartesianCell]](
        width,
        topology.neighbourhoods.map { n: Neighbourhood[CartesianCell] =>
          n.center.index -> AutomataCell(cellStateOps.initialize(n.center), n)
        }.toSeq.sortWith(_._1 < _._1).map(_._2).toParArray)
    new TestSpaceMap[S](topology, autoTopology, width, height)
  }
}

final case class IndexedAutomataTopologyRep[C <: Cell,T](width: Int, autoCells:ParArray[T]) extends AutomataTopology[C,T] {
  val cellMap = (cell:C) => autoCells(cell.index)

  def map[T2](f: (T) => T2)(implicit tag: ClassTag[T2]): AutomataTopology[C, T2] = {
    new IndexedAutomataTopologyRep(width, autoCells.map(f))
  }
}

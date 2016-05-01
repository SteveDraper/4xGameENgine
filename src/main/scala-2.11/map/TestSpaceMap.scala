package map

import automata.{AutomataCell, AutomataTopology}
import state.test.{DoubleValued, TestCellState}
import topology.space.CartesianCell
import topology.space.hex.HexSpace
import topology.space.manhatten.ManhattenSpace
import topology.{Cell, Neighbourhood, Space}

import scala.collection.parallel.mutable.ParArray
import scala.reflect.ClassTag

case class TestSpaceMap
    (topology: Space[CartesianCell],
     at: AutomataTopology[CartesianCell,AutomataCell[TestCellState,CartesianCell]],
     width: Int,
     height: Int) extends SpaceMap[CartesianCell, TestCellState] {

  def cells = topology.neighbourhoods map(_.center)
  def cellStateValue(cell: CartesianCell) =
    at.cellMap(cell).state

  def run = {
    def updateCell(mcell: AutomataCell[TestCellState,CartesianCell]) = {
      val startStateVal = mcell.state.value
      val neighbours =  mcell.localTopology.neighbours
      var count = 0;
      var total = 0.0
      neighbours.foreach(c => {
        count = count+1
        total = total + cellStateValue(c).value - startStateVal
      })

      //  Add average difference of neighbour values from own value / 4
      val newValue = startStateVal + total/(4*count)
      AutomataCell(TestCellState(newValue), mcell.localTopology)
    }

    val newAutoTopology = at.map(updateCell)
    val result = new TestSpaceMap(topology, newAutoTopology, width, height)
    result
  }

  def render = {
    println
    val coordsMap: Map[(Int,Int),Double] = topology.neighbourhoods.map(n => (n.center.x, n.center.y) -> at.cellMap(n.center).state.value).toMap
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
  def apply(width: Int, height: Int, useHex: Boolean): TestSpaceMap = {
    val topology: Space[CartesianCell] = if (useHex) new HexSpace(width, height, true, false) else new ManhattenSpace(width, height, true, false)
    val autoTopology: AutomataTopology[CartesianCell,AutomataCell[TestCellState,CartesianCell]] =
      IndexedAutomataTopologyRep[CartesianCell,AutomataCell[TestCellState,CartesianCell]](
        width,
        topology.neighbourhoods.map { n: Neighbourhood[CartesianCell] =>
          n.center.index -> AutomataCell(TestCellState.initialize(n.center), n)
        }.toSeq.sortWith(_._1 < _._1).map(_._2).toParArray)
    new TestSpaceMap(topology, autoTopology, width, height)//IndexedAutomataTopologyRep.automataTopology)
  }
}

final case class IndexedAutomataTopologyRep[C <: Cell,T](width: Int, autoCells:ParArray[T]) extends AutomataTopology[C,T] {
  val cellMap = (cell:C) => autoCells(cell.index)

  def map[T2](f: (T) => T2)(implicit tag: ClassTag[T2]): AutomataTopology[C, T2] = {
    new IndexedAutomataTopologyRep(width, autoCells.map(f))
  }
}

package map

import automata.{SimpleAutomataTopologyRep, SimpleAutomataTopology, AutomataCell, AutomataTopology}
import state.test.TestCellState
import topology.space.manhatten.{ManhattenCell, ManhattenSpace}
import topology.{Neighbourhood, Space}

import scala.collection.mutable

case class TestSpaceMap[AT[_,_]]
    (topology: Space[ManhattenCell],
     at: AT[ManhattenCell,AutomataCell[TestCellState,ManhattenCell]],
     width: Int,
     height: Int)
    (implicit autoTopology: AutomataTopology[AT, ManhattenCell,AutomataCell[TestCellState,ManhattenCell]]) extends SpaceMap[ManhattenCell, TestCellState] {

  def cells = topology.neighbourhoods map(_.center)
  def cellStateValue(cell: ManhattenCell) = autoTopology.cellMap(at)(cell).state

  def run = {
    def updateCell(mcell: AutomataCell[TestCellState,ManhattenCell]) = {
      val startState: TestCellState = mcell.state
      val neighbours =  mcell.localTopology.neighbours

      //  Add average difference of neighbour values from own value / 4
      val newValue = startState.value + (neighbours.map(cellStateValue(_).value - startState.value).sum)/(4*neighbours.size)
      AutomataCell(TestCellState(newValue), mcell.localTopology)
    }

    //val newAutoTopology = SimpleAutomataTopologyRep(topology.neighbourhoods.map(n => n.center -> updateCell(autoTopology.cellMap(at)(n.center))).toMap)
    val newAutoTopology = autoTopology.map(at)(updateCell)
    val result = new TestSpaceMap(topology, newAutoTopology, width, height)
    result
  }

  def render = {
    println
    val coordsMap: Map[(Int,Int),Double] = topology.neighbourhoods.map(n => (n.center.x, n.center.y) -> autoTopology.cellMap(at)(n.center).state.value).toMap
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
  def apply(width: Int, height: Int): TestSpaceMap[IndexedAutomataTopologyRep] = {
    val topology = new ManhattenSpace(width, height, true, false)
    val autoTopology =
      IndexedAutomataTopologyRep(
        width,
        topology.neighbourhoods.map { n: Neighbourhood[ManhattenCell] =>
          n.center -> AutomataCell(TestCellState.initialize(n.center), n)
        }.toMap)
    new TestSpaceMap(topology, autoTopology, width, height)(IndexedAutomataTopologyRep.automataTopology)
  }
}

class IndexedAutomataTopologyRep[C,T](width: Int, autoCells: mutable.ArraySeq[T])(implicit cellIndex: C => Int) {
  private def lookupCell(cell: C) = autoCells(cellIndex(cell))

  def map[T2](f: T => T2): IndexedAutomataTopologyRep[C,T2] = {
    new IndexedAutomataTopologyRep(width, autoCells.map(f))
  }
}

object IndexedAutomataTopologyRep {
  def automataTopology[C,T] = new AutomataTopology[IndexedAutomataTopologyRep,C,T] {
    def cellMap(r: IndexedAutomataTopologyRep[C, T]) = c => r.lookupCell(c)

    def map[T2](r: IndexedAutomataTopologyRep[C, T])(f: (T) => T2): IndexedAutomataTopologyRep[C, T2] =
      r.map(f)
  }

  def apply[T](width: Int, m: Map[ManhattenCell, T]) = {
    val coordLookup = m.toList.map(k_v => (k_v._1.x, k_v._1.y) -> k_v._2).toMap
    val array: mutable.ArraySeq[T] = mutable.ArraySeq.tabulate(m.size)(i=>coordLookup((i/width,i%width)))
    implicit val cellIndexer = (c:ManhattenCell) => c.x%width + c.y*width

    new IndexedAutomataTopologyRep[ManhattenCell,T](width, array)
  }
}

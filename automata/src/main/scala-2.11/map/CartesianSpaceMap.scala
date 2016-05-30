package map

import automata.{AutomataCell, AutomataTopology}
import model.map.{MapTopology, MapTopologyProvider}
import state.{CellState, CellStateOps}
import state.test.{DoubleValued, TestCellState}
import topology.space.CartesianCell
import topology.space.hex.HexSpace
import topology.space.manhatten.ManhattenSpace
import topology.{CartesianProjection, Cell, Neighbourhood, Space}
import util.{DefaultPArray, PArray, SimplePArray, TaskPArray}

import scala.reflect.ClassTag
import scalaz.Show

case class CartesianSpaceMap[S]
    (topology: Space[CartesianCell] with CartesianProjection[CartesianCell],
     at: AutomataTopology[CartesianCell,AutomataCell[CellState[S],CartesianCell]],
     width: Int,
     height: Int) extends SpaceMap[CartesianCell, S] with MapTopologyProvider {

  def cells = topology.neighbourhoods map(_.center)
  def cellStateValue(cell: CartesianCell) =
    at.cellMap(cell).state.get

  def run = {
    def updateCell(mcell: AutomataCell[CellState[S],CartesianCell]) = {
      AutomataCell(mcell.state.update(cellStateValue, identity[S], mcell.localTopology.neighbours), mcell.localTopology)
    }

    val newAutoTopology = at.map(updateCell)
    val result = new CartesianSpaceMap[S](topology, newAutoTopology, width, height)
    result
  }

  def render(implicit s: Show[S]) = {
    println
    val coordsMap: Map[(Int,Int),S] = topology.neighbourhoods.map(n => (n.center.x, n.center.y) -> at.cellMap(n.center).state.get).toMap
    for(y <- 0 to height-1) {
      val colVals = for {
        x <- 0 to width - 1
      } yield coordsMap.get((x,y)).map(s.shows).getOrElse("")

      println(colVals.mkString(""," ",""))
    }
    println
  }

  def getMapTopology: MapTopology =
    topology.getMapTopology
}

object CartesianSpaceMap {
  def apply[S](width: Int,
                height: Int,
                useHex: Boolean)
               (implicit cellStateOps: CellStateOps[CartesianCell,S]): CartesianSpaceMap[S] = {
    val topology: Space[CartesianCell] with CartesianProjection[CartesianCell] = if (useHex) new HexSpace(width, height, true, false) else new ManhattenSpace(width, height, true, false)
    val autoTopology: AutomataTopology[CartesianCell,AutomataCell[CellState[S],CartesianCell]] =
      IndexedAutomataTopologyRep[CartesianCell,AutomataCell[CellState[S],CartesianCell]](
        width,
        DefaultPArray(topology.neighbourhoods.map { n: Neighbourhood[CartesianCell] =>
            n.center.index -> AutomataCell(cellStateOps.initialize(n.center), n)
          }.toSeq.sortWith(_._1 < _._1).map(_._2).toArray))
    new CartesianSpaceMap[S](topology, autoTopology, width, height)
  }
}

final case class IndexedAutomataTopologyRep[C <: Cell,T](width: Int, autoCells:PArray[T]) extends AutomataTopology[C,T] {
  val cellMap = (cell:C) => autoCells.get(cell.index)

  def map[T2](f: (T) => T2)(implicit tag: ClassTag[T2]): AutomataTopology[C, T2] = {
    new IndexedAutomataTopologyRep(width, autoCells.map(f))
  }
}

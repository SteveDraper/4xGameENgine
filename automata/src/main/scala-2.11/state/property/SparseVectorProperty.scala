package state.property

import map.SpaceMap
import state.CellState
import topology.{Cell, Neighbourhood}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

final case class ElementId(value: Int) extends AnyVal

trait SparseVectorProperty[T] extends Traversable[(ElementId,T)] {
  def get(index: ElementId): Option[T]
  def getOrElse(index: ElementId, default: T): T
  def assertedGet(index: ElementId): T
}

trait SparseVectorPropertyBuilder[T,S <: SparseVectorProperty[T]] {
  def build(from: IndexedSeq[(ElementId,T)]): S
}

abstract class SparseVectorPropertyUpdater[T,S <: SparseVectorProperty[T]] extends PropertyUpdater[S] with SparseVectorPropertyBuilder[T,S] {
  def elementUpdate(initialValue: T, neighbourValues: Traversable[T]): T
  def zero: T

  def update[C <: Cell,R](initialValue: S,
                          id: PropertyId,
                          cellState: C => R,
                          parentLens: R => PropertyMapState[S],
                          neighbourhood: Neighbourhood[C]): S = {
    val builder = new mutable.ArrayBuffer[(ElementId,T)]
    def addToBuilder(el: (ElementId,T)) = builder += el

    neighbourhood.neighbours.foreach(c => {
      parentLens(cellState(c)).properties(id.value).foreach(addToBuilder)
    })

    val builderSize = builder.size

    def builderTraversable(id: ElementId, startHint: Int) = new Traversable[T] {
      def foreach[U](f: (T) => U): Unit = {
        def recursiveIterate(index: Int): Unit = {
          if (index < builderSize) {
            val el = builder(index)
            if (el != null && el._1 == id) {
              f(el._2)
              builder(index) = null
            }

            recursiveIterate(index+1)
          }
        }

        recursiveIterate(0)

      }
    }

    val resultBuilder = new ArrayBuffer[(ElementId,T)]

    def recursiveProcess(index: Int): Unit = {
      if ( index < builder.size ) {
        val el = builder(index)
        if ( el != null ) {
          resultBuilder += ((el._1,elementUpdate(initialValue.getOrElse(el._1,zero),builderTraversable(el._1, index))))
        }
        recursiveProcess(index+1)
      }
    }

    recursiveProcess(0)

    def findInResultBuilder(id: ElementId) = resultBuilder.exists(el => el._1 == id)

    //  Add in any elementIds already present but not represented in neighbours
    for(el <- initialValue) {
      if (!findInResultBuilder(el._1)) resultBuilder += ((el._1,elementUpdate(el._2,mutable.Traversable.empty[T])))
    }

    build(resultBuilder)
  }
}
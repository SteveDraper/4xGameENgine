package server.properties

import java.util.concurrent.ThreadLocalRandom

import model.Span
import state.property._
import topology.{Cell, Neighbourhood}
import server.util.SpanOps._
import state.property.PropertyUpdater.DoubleProperty

object GamePropertyRegistry extends PropertyRegistry[DoubleProperty, BasicSparseVectorProperty[DoubleProperty]] {
  val heightSpan = Span(-10.0,10.0)
  val scalarProperties = Map(
    PropertyId(0) -> PropertyInfo("Height", "Height above sea level", Some(heightSpan), heightUpdater),
    PropertyId(1) -> PropertyInfo("HeightVelocity", "Lifting rate", Some(heightSpan), PropertyUpdater.identityUpdater[DoubleProperty])
  )
  val vectorProperties = Map[PropertyId,PropertyInfo[BasicSparseVectorProperty[Double]]]()

  def scalarUpdaters = scalarProperties.toList.sortBy(_._1.value).map(_._2.updater).toArray

  def vectorUpdaters = vectorProperties.toList.sortBy(_._1.value).map(_._2.updater).toArray

  private def heightUpdater: PropertyUpdater[DoubleProperty] = new PropertyUpdater[DoubleProperty] {
    def update[C <: Cell, R](initialValue: DoubleProperty,
                             id: PropertyId,
                             cellState: (C) => R,
                             parentLens: (R) => PropertyMapState[DoubleProperty],
                             neighbourhood: Neighbourhood[C]): DoubleProperty = {
      //  For now height update is just a small random perturbation
      heightSpan.clamp(initialValue + ThreadLocalRandom.current.nextDouble(1.0) - 0.5)
    }
  }
}

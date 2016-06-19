package server.map.builders
import java.util.concurrent.ThreadLocalRandom

import map.CartesianSpaceMap
import model.Span
import model.map.Point
import server.properties.{GamePropertyRegistry, PropertyInfo}
import state.property._
import topology.{CartesianProjection, Cell, Neighbourhood}
import topology.space.CartesianCell
import server.util.SpanOps._
import server.ApiHelper._
import state.property.PropertyUpdater.DoubleProperty

import scala.util.Random
import scalaz.syntax.either._
import scalaz.syntax.std.boolean._


final class SimpleMapBuilder(seedDensity: Double) extends MapBuilder {
  val rand = new Random

  //  Build-time property updaters
  val heightSpan = GamePropertyRegistry.heightSpan
  val heightVelocitySpan = Span(-1.0,1.0)
  val heightPropertyId = PropertyId(0)
  val heightVelocityPropertyId = PropertyId(1)
  val scalarProperties = Map(
    heightPropertyId -> PropertyInfo("Height", "Height above sea level", Some(heightSpan), heightUpdater),
    heightVelocityPropertyId -> PropertyInfo("Height velocity", "Lifting rate", Some(heightVelocitySpan), heightVelocityUpdater)
  )
  val vectorProperties = Map[PropertyId,PropertyInfo[BasicSparseVectorProperty[Double]]]()

  def scalarUpdaters = scalarProperties.toList.sortBy(_._1.value).map(_._2.updater).toArray
  def vectorUpdaters = vectorProperties.toList.sortBy(_._1.value).map(_._2.updater).toArray

  def initialize(s: CartesianSpaceMap[SimpleCompositePropertyCellState]) = {
    for {
      landSeedList <- generateSeeds(s)
      oceanSeedList <- generateSeeds(s)
      seededMap = s.buildFrom(initializeProperties(s.topology, landSeedList, oceanSeedList))
      finalMap <- evolve(seededMap)
    } yield  finalMap
  }

  private def generateSeeds(s: CartesianSpaceMap[_]) = {
    val numCells = Math.max(1,(0.5+s.width*s.height*Math.max(0.0,Math.min(1.0,seedDensity))).toInt)

    wrapM((1 until numCells).map(_=>Point(rand.nextInt(s.width),rand.nextInt(s.height))).right)
  }

  private def initializeProperties(topology: CartesianProjection[CartesianCell],
                                   landSeedList: Seq[Point],
                                   oceanSeedList: Seq[Point])(cell: CartesianCell): SimpleCompositePropertyCellState = {
    val heightVelocity =
      landSeedList.contains(Point(cell.x,cell.y)) ?
        GamePropertyRegistry.heightSpan.clamp(1.0) |
        (oceanSeedList.contains(Point(cell.x,cell.y)) ?
          GamePropertyRegistry.heightSpan.clamp(-1.0) |
        GamePropertyRegistry.heightSpan.clamp(0.0))
    val height = 0.0

    def initialValue(id: PropertyId): Double = id match {
      case `heightPropertyId` => height
      case `heightVelocityPropertyId` => heightVelocity
      case _ => 0.0
    }

    SimpleCompositePropertyCellState(
      PropertyMapState.buildFrom[DoubleProperty](scalarUpdaters,initialValue),
      PropertyMapState.buildFrom[BasicSparseVectorProperty[DoubleProperty]](vectorUpdaters,_=> null))
  }

  private def evolve(s: CartesianSpaceMap[SimpleCompositePropertyCellState]) =
  {
    def recursiveEvolve(remaining: Int,
                        s: CartesianSpaceMap[SimpleCompositePropertyCellState]): CartesianSpaceMap[SimpleCompositePropertyCellState] =
      if (remaining == 0) s
      else recursiveEvolve(remaining-1, s.run)

    wrapM(recursiveEvolve(50,s).right)
  }

  private def heightUpdater: PropertyUpdater[DoubleProperty] = new PropertyUpdater[DoubleProperty] {
    def update[C <: Cell, R](initialValue: DoubleProperty,
                             id: PropertyId,
                             cellState: (C) => R,
                             parentLens: (R) => PropertyMapState[DoubleProperty],
                             neighbourhood: Neighbourhood[C]): DoubleProperty = {
      heightSpan.clamp(initialValue + parentLens(cellState(neighbourhood.center)).find(heightVelocityPropertyId).getOrElse(0.0))
    }
  }

  private def heightVelocityUpdater: PropertyUpdater[DoubleProperty] = new PropertyUpdater[DoubleProperty] {
    def update[C <: Cell, R](initialValue: DoubleProperty,
                             id: PropertyId,
                             cellState: (C) => R,
                             parentLens: (R) => PropertyMapState[DoubleProperty],
                             neighbourhood: Neighbourhood[C]): DoubleProperty = {
      //  Average with neighbour cells and perturb slightly
      var count = 0;
      var total = 0.0
      neighbourhood.neighbours.foreach(c => {
        val neighbourValue = parentLens(cellState(c)).find(id).getOrElse(0.0)

        count = count+1
        total = total + neighbourValue*neighbourValue*Math.signum(neighbourValue)
      })

      val averageOfSquares = (initialValue*initialValue*Math.signum(initialValue) + total)/(count+1)
      val average = Math.sqrt(Math.abs(averageOfSquares))*Math.signum(averageOfSquares)
      heightSpan.clamp(average + ThreadLocalRandom.current.nextDouble(0.1) - 0.05)
    }
  }
}

object SimpleMapBuilder {
  def apply(seedDensity: Double) = new SimpleMapBuilder(seedDensity)
}
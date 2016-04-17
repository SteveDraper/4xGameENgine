package topology.space.manhatten

import topology.{StaticNeighbourhood, Neighbourhood, Space}

import scala.collection.mutable

case class ManhattenSpace(width: Int, height: Int, wrapX: Boolean, wrapY: Boolean) extends Space[ManhattenCell] {
  private val cells: Array[ManhattenCell] = Array.tabulate(width*height)(index => ManhattenCell(index%width,index/width))

  def cellAt(x: Int, y: Int) = cells(x + y*width)

  private lazy val staticCellNeighbourhoods = {
    for {
      x <- 0 to width-1
      y <- 0 to height-1
    } yield {
      val neighbours = for {
        deltaX <- -1 to 1
        deltaY <- -1 to 1
        if (deltaX != 0 || deltaY != 0)
        neighbourX = x + deltaX if (wrapX || (neighbourX >= 0 && neighbourX < width))
        neighbourY = y + deltaY if (wrapY || (neighbourY >= 0 && neighbourY < height))
      } yield cellAt((neighbourX + width)%width, (neighbourY + height)%height)

      StaticNeighbourhood(cellAt(x,y), mutable.WrappedArray.make(neighbours.toList.toArray))
    }
  }

  def neighbourhoods: Traversable[Neighbourhood[ManhattenCell]] = cells.map(c => DynamicManhattenNeighbourhood(c,this))
}

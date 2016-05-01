package topology.space.manhatten

import topology.Neighbourhood
import topology.space.CartesianCell


case class DynamicManhattenNeighbourhood(center: CartesianCell, space: ManhattenSpace) extends Neighbourhood[CartesianCell] {
  def neighbours = new Traversable[CartesianCell] {
    def foreach[U](f: (CartesianCell) => U) = {
      val allowUp = ( center.y > 0 || space.wrapY )
      val allowDown = ( center.y < space.height-1 || space.wrapY )
      val allowLeft = ( center.x > 0 || space.wrapX )
      val allowRight = ( center.x < space.width-1 || space.wrapX )

      if ( allowUp ) {
        val upY = (center.y+space.height-1)%space.height
        if ( allowLeft ) f(space.cellAt((center.x+space.width-1)%space.width, upY))
        f(space.cellAt(center.x,upY))
        if ( allowRight ) f(space.cellAt((center.x+space.width+1)%space.width, upY))
      }
      if ( allowLeft ) f(space.cellAt((center.x+space.width-1)%space.width, center.y))
      if ( allowRight ) f(space.cellAt((center.x+space.width+1)%space.width, center.y))
      if ( allowDown ) {
        val downY = (center.y+space.height+1)%space.height
        if ( allowLeft ) f(space.cellAt((center.x+space.width-1)%space.width, downY))
        f(space.cellAt(center.x,downY))
        if ( allowRight ) f(space.cellAt((center.x+space.width+1)%space.width, downY))
      }
    }
  }

  /*def neighboursOld: Traversable[CartesianCell] = {
    for {
      deltaX <- -1 to 1
      deltaY <- -1 to 1
      if (deltaX != 0 || deltaY != 0)
      neighbourX = center.x + deltaX if (space.wrapX || (neighbourX >= 0 && neighbourX < space.width))
      neighbourY = center.y + deltaY if (space.wrapY || (neighbourY >= 0 && neighbourY < space.height))
    } yield space.cellAt((neighbourX + space.width)%space.width, (neighbourY + space.height)%space.height)
  }*/
}

package state.property


final case class BasicSparseVectorProperty[T](l: Array[(ElementId,T)]) extends SparseVectorProperty[T] {
  //  TODO - this would be more efficient split into 2 arrays, especially optimizing for
  //  the case where the ids remain constant over an update
  def get(index: ElementId): Option[T] = {
    val len = l.size

    def recursiveFind(i: Int): Option[T] = {
      if ( i == len ) None
      else {
        val el = l(i)
        if ( el._1 == index ) Some(el._2)
        else recursiveFind(i+1)
      }
    }

    recursiveFind(0)
  }

  def getOrElse(index: ElementId, default: T): T = {
    val len = l.size

    def recursiveFind(i: Int): T = {
      if ( i == len ) default
      else {
        val el = l(i)
        if ( el._1 == index ) el._2
        else recursiveFind(i+1)
      }
    }

    recursiveFind(0)
  }

  def assertedGet(index: ElementId): T = {
    val len = l.size

    def recursiveFind(i: Int): T = {
      if ( i == len ) {
        throw new RuntimeException("Asserted get failure")
      }
      else {
        val el = l(i)
        if ( el._1 == index ) el._2
        else recursiveFind(i+1)
      }
    }

    recursiveFind(0)
  }

  def foreach[U](f: ((ElementId, T)) => U): Unit =
    for(el <- l) f(el)

  override def toString = get(ElementId(0)).map(_.toString).getOrElse("")
}

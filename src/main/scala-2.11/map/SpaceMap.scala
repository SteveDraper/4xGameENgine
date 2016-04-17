package map


trait SpaceMap[C,S] {
  def cells: Traversable[C]
  def cellStateValue(cell: C): S
}

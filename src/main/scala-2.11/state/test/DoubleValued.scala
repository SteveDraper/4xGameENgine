package state.test

trait DoubleValued[A] {
  def apply(a: A): Double
}

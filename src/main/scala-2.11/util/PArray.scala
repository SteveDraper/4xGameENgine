package util


trait PArray[A] {
  def map[B](f: A => B): PArray[B]
  def get(index: Int): A
}

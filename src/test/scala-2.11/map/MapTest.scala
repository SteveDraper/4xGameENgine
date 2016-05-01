package map

object MapTest extends App {
  val startTime = System.currentTimeMillis()
  var testMap = TestSpaceMap(1000,1000,true)
  val mapCreatedTime = System.currentTimeMillis()
  implicit val stateDoubleValued = TestSpaceMap.stateDoubleValued
  val testMapStats = MapStats(testMap)
  println(s"Initial map mean and std dev,: ${testMapStats.cellAverage}, ${testMapStats.cellStdDev}")
  //testMap.render
  val genStartTime = System.currentTimeMillis()
  val numGens = 100
  for(i <- 1 to numGens) {
    testMap = testMap.run

    //val genMapStats = MapStats(testMap)
    //println(s"Gen $i map mean and std dev,: ${genMapStats.cellAverage}, ${genMapStats.cellStdDev}")
    //println(s"Generation $i:")
    //testMap.render
  }
  val endTime = System.currentTimeMillis()
  val finalMapStats = MapStats(testMap)

  println(s"Map generated in ${mapCreatedTime-startTime}ms, $numGens gens in ${endTime-genStartTime}ms")

  println(s"Final map mean and std dev,: ${finalMapStats.cellAverage}, ${finalMapStats.cellStdDev}")
}

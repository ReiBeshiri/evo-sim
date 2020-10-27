package evo_sim.dsl

object WorldCreation {
  implicit class IntWithDegrees(temperature:Int){
    def °<() = temperature
  }

  implicit class IntWithMeters(lengthInMeters: Int){
    def km() = 10 * hm
    def hm(): Int = 100 * lengthInMeters
    def m(): Int = lengthInMeters
  }
}

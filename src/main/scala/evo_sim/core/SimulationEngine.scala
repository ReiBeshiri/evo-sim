package evo_sim.core

import evo_sim.model.EntityBehaviour.SimulableEntity
import evo_sim.model.Intersection.intersected
import evo_sim.model.World
import evo_sim.model.World._
import evo_sim.view.ViewModule

object SimulationEngine {

  def worldUpdated(world: World): World = {
    World(
      world.width,
      world.height,
      world.currentIteration + 1,
      world.entities.foldLeft(Set[SimulableEntity]())((updatedEntities, entity) => updatedEntities ++ entity.updated(world)),
      world.totalIterations
    )
  }


  def collisionsHandled(world: World): World = {
    def collisions = for {
      i <- world.entities
      j <- world.entities
      if i != j && intersected(i.boundingBox, j.boundingBox)
    } yield (i, j)

    def entitiesAfterCollision =
      collisions.foldLeft(Set.empty[SimulableEntity])((entitiesAfterCollision, collision) => entitiesAfterCollision ++ collision._1.collided(collision._2))

    World(
      world.width,
      world.height,
      world.currentIteration,
      world.entities ++ entitiesAfterCollision,
      world.totalIterations
    )
  }

  def started(): Unit = {
    ViewModule.GUIBuilt()
    val environment = ViewModule.inputReadFromUser()
    val world = worldCreated(environment)
    ViewModule.simulationGUIBuilt()
    val startingTime = System.currentTimeMillis()
    ViewModule.rendered(world)
    val endingTime = System.currentTimeMillis() //val endingTime = System.nanoTime();
    val elapsed = endingTime - startingTime
    waitUntil(elapsed, 1000) //period in milliseconds
    simulationLoop(world)

    @scala.annotation.tailrec
    def simulationLoop(world: World): Unit = {
      val startingTime = System.currentTimeMillis()
      val updatedWorld = worldUpdated(world)
      val worldAfterCollisions = collisionsHandled(updatedWorld)
      ViewModule.rendered(worldAfterCollisions)

      val endingTime = System.currentTimeMillis() //val endingTime = System.nanoTime();
      val elapsed = endingTime - startingTime

      waitUntil(elapsed, 1000) //period in milliseconds

      if (worldAfterCollisions.currentIteration < worldAfterCollisions.totalIterations)
        simulationLoop(worldAfterCollisions)
    }

    def waitUntil(from: Long, period: Long): Unit = {
      if (from < period)
        try
          Thread.sleep(period - from)
        catch {
          case _: InterruptedException =>
        }
    }
  }
}

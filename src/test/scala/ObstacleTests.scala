import evo_sim.model.Entities.{BaseBlob, BaseObstacle}
import evo_sim.model.EntityStructure.Blob
import evo_sim.model._
import org.scalatest.FunSuite

class ObstacleTests extends FunSuite {

  val blob: BaseBlob = BaseBlob(
    boundingBox = BoundingBox.Rectangle.apply(point = Point2D(100, 100), width = 10, height = 10),
    life = 100,
    velocity = 3,
    degradationEffect = DegradationEffect.standardDegradation,
    fieldOfViewRadius = 10,
    movementStrategy = MovingStrategies.baseMovement)
  val mud: BaseObstacle = BaseObstacle(
    boundingBox = BoundingBox.Triangle.apply(point = Point2D(100, 100), height = 50),
    effect = Effect.mudEffect)

  test("BlobInMud") {
    val updatedBlob = blob.collided(mud).toVector(0).asInstanceOf[Blob]
    assert(updatedBlob.velocity == 2)
  }
  /*
  test("StoppedMobInMud") {
    val updatedBlob = doTimesRecursively(blob, 10, blob.collided(mud)).asInstanceOf[BaseBlob]
    assert(updatedBlob.velocity == 0)
  }

  def doTimesRecursively[A](x: A, times: Int, f: A => A): A = times match {
    case _ if times > 0 => doTimesRecursively(f(x), times - 1, f)
    case _ => f(x)
  }
  */
}

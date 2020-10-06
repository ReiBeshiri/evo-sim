package evo_sim.view

import evo_sim.model.BoundingBoxShape.{Circle, Rectangle, Triangle, triangleVertices}
import evo_sim.model.{Environment, World}
import javafx.stage.Stage
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.scene.chart.{LineChart, NumberAxis, XYChart}
import scalafx.scene.control.Label
import scalafx.scene.layout._
import scalafx.scene.paint.Color._
import scalafx.scene.{Parent, Scene}
import scalafxml.core.{FXMLView, NoDependencyResolver}

import scala.collection.mutable
import scala.concurrent.Promise

trait View {
  def inputGUIBuilt(): Unit

  def inputReadFromUser(): Promise[Environment]

  def simulationGUIBuilt(): Unit

  def rendered(world: World): Unit

  def showResultGUI(world: World): Unit

}

object View {

  // "1-bounded" multiton pattern: grants apparent immutability and singleton
  val instance = mutable.Map.empty[Int, View]
  def apply(stage: Stage): View = instance.getOrElseUpdate(0, new FXView(stage))

}

private class FXView(stage: Stage) extends View {

  private var primaryStage: Stage = stage
  private val inputView: Parent = FXMLView(getClass.getResource("/InputSelector.fxml"),
    NoDependencyResolver)
  private val simulatorView: BorderPane = new BorderPane
  private val entityPane = new BorderPane
  private val barPane = new BorderPane

  override def inputGUIBuilt(): Unit = {
    primaryStage = stage
    primaryStage.title = "evo-sim"
    barPane.setBorder(new Border(new BorderStroke(Black,
      BorderStrokeStyle.Solid, CornerRadii.Empty, BorderWidths.Default)))
    barPane.top = new Label("Info")
    simulatorView.top = barPane
    simulatorView.center = entityPane
  }

  override def inputReadFromUser(): Promise[Environment] = {
    primaryStage.scene = new Scene(inputView)
    primaryStage.resizable = false
    primaryStage.show()
    userInput.environment
  }

  override def simulationGUIBuilt(): Unit = {
    primaryStage.scene = new Scene(simulatorView, 600, 450)
  }

  override def rendered(world: World): Unit = {
    entityPane.children = world.entities.map(e =>
      e.structure.boundingBox match {
        case Circle((x, y), r) => new scalafx.scene.shape.Circle {
          centerX = x
          centerY = y
          radius = r
          fill = Yellow
        }
        case Rectangle((xCord, yCord), w, h) => new scalafx.scene.shape.Rectangle {
          x = xCord - (w / 2)
          y = yCord - (h / 2)
          width = w
          height = h
          fill = Red
        }
        case Triangle((xCord, yCord), h, a) => new scalafx.scene.shape.Polygon {
          private val vertices = triangleVertices(Triangle((xCord, yCord), h, a))
          points.addAll(vertices._1, vertices._2, vertices._3, vertices._4, vertices._5, vertices._6)
          fill = Green
        }
      })
  }

  override def showResultGUI(world: World): Unit = {
    val linechart = {
      val xAxis = NumberAxis("", 0, 3, 1)
      val yAxis = NumberAxis("Values for Y-Axis", 0, 3, 1)

      val toChartData = (xy: (Int, Int)) => XYChart.Data[Number, Number](xy._1, xy._2)

      val series1 = new XYChart.Series[Number, Number] {
        name = "Death"
        data = Seq(
          (0, 0),
          (1, 2),
          (2, 3),
          (3, 1)).map(toChartData)
      }

      val series2 = new XYChart.Series[Number, Number] {
        name = "Birth"
        data = Seq(
          (0, 0),
          (1, 1),
          (2, 1),
          (3, 0)).map(toChartData)
      }

      val lineChart = new LineChart[Number, Number](xAxis, yAxis, ObservableBuffer(series1, series2))
      lineChart.setAnimated(true)

      /*def savePng: Unit = {
        val img = lineChart.snapshot(null, new WritableImage(500, 250))
        val file = new File("/tmp/chart.png")
        ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", file)
      }*/

      lineChart
    }
    val resultView = new BorderPane
    resultView.setCenter(linechart)
    primaryStage.scene = new Scene(resultView, 600, 450)
  }

}

private[view] object userInput {
  val environment: Promise[Environment] = Promise[Environment]()
}
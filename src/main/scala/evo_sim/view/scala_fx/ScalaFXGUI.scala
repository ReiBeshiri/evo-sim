package evo_sim.view.scala_fx

import evo_sim.model.BoundingBox.{Circle, Rectangle, Triangle, triangleVertices}
import evo_sim.model.{Environment, World}
import evo_sim.view.GUI
import javafx.scene.Parent
import javafx.stage.Stage
import scalafx.Includes._
import scalafx.beans.binding.Bindings
import scalafx.collections.ObservableBuffer
import scalafx.event.Event
import scalafx.scene.Scene
import scalafx.scene.chart.{LineChart, NumberAxis, XYChart}
import scalafx.scene.control.{Button, Label, TextField, TextFormatter}
import scalafx.scene.layout._
import scalafx.scene.paint.Color.{Black, Green, Red, Yellow}
import scalafx.stage.Screen
import scalafx.util.converter.NumberStringConverter
import scalafxml.core.macros.sfxml
import scalafxml.core.{FXMLView, NoDependencyResolver}

import scala.concurrent.{Future, Promise}

case class ScalaFXGUI(stage: Stage) extends GUI {

  private val inputView: Parent = FXMLView(getClass.getResource("/InputSelector.fxml"),
    NoDependencyResolver)
  private val entityPane = new BorderPane
  private val barPane = new BorderPane
  private val simulatorView: BorderPane = new BorderPane

  override def inputGUIBuilt(): Unit = {
    stage.title = "evo-sim"
    stage.resizable = false
    barPane.setBorder(new Border(new BorderStroke(Black,
      BorderStrokeStyle.Solid, CornerRadii.Empty, BorderWidths.Default)))
    barPane.top = new Label("Info")
    simulatorView.top = barPane
    simulatorView.center = entityPane
    stage.scene = new Scene(inputView)
    stage.show()
  }

  override def inputReadFromUser(): Future[Environment] = {
    userInput.environment.future
  }

  override def simulationGUIBuilt(): Unit = {
    stage.scene = new Scene(simulatorView, Screen.primary.visualBounds.width, Screen.primary.visualBounds.height)
    stage.maximized = true
  }

  override def rendered(world: World): Unit = {
    entityPane.children = world.entities.map(e =>
      e.boundingBox match {
        case Circle(point2D, r) => new scalafx.scene.shape.Ellipse {
          centerX = point2D.x
          centerY = point2D.y
          radiusX = r
          radiusY = r
          fill = Yellow
        }
        case Rectangle(point2D, w, h) => new scalafx.scene.shape.Rectangle {
          x = point2D.x
          y = point2D.y
          x = point2D.x - w / 2
          y = point2D.y - h / 2
          fill = Red
        }
        case Triangle(point2D, h, a) => new scalafx.scene.shape.Polygon {
          private val vertices = triangleVertices(Triangle(point2D, h, a))
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
    stage.scene = new Scene(resultView, 600, 450)
    stage.maximized = false
  }
}

@sfxml
class ScalaFXInputPresenter(blobTextField: TextField,
                            foodTextField: TextField,
                            obstacleTextField: TextField,
                            temperatureTextField: TextField,
                            luminosityTextField: TextField,
                            startButton: Button) {

  blobTextField.setTextFormatter(new TextFormatter(new NumberStringConverter()))
  foodTextField.setTextFormatter(new TextFormatter(new NumberStringConverter()))
  obstacleTextField.setTextFormatter(new TextFormatter(new NumberStringConverter()))
  temperatureTextField.setTextFormatter(new TextFormatter(new NumberStringConverter()))
  luminosityTextField.setTextFormatter(new TextFormatter(new NumberStringConverter()))

  startButton.disableProperty().bind(
    Bindings.createBooleanBinding(
      () => blobTextField.text.value.trim.isEmpty,
      blobTextField.text))

  def onStart(event: Event): Unit = {
    userInput.environment.success(Environment(
      temperature = temperatureTextField.text.value.toInt,
      luminosity = luminosityTextField.text.value.toInt,
      initialBlobNumber = blobTextField.text.value.toInt,
      initialFoodNumber = foodTextField.text.value.toInt,
      initialObstacleNumber = obstacleTextField.text.value.toInt))
  }
}

private[scala_fx] object userInput {
  val environment: Promise[Environment] = Promise[Environment]()
}
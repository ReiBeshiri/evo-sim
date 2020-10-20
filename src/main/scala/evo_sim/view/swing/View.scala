package evo_sim.view.swing

import java.awt.event.ActionEvent
import java.awt.{BorderLayout, Dimension, Toolkit}

import cats.effect.IO
import evo_sim.model.{Constants, Environment, World}
import evo_sim.view.View
import evo_sim.view.swing.SwingEffects._
import javax.swing._
import javax.swing.event.ChangeEvent

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Promise}

object View extends View {

  private val frame = new JFrame("evo-sim")
  private val barPanel = new JPanel
  private val entityPanel = new JPanel

  private val userInput: Promise[Environment] = Promise[Environment]()

  override def inputViewBuiltAndShowed(): Unit = {

    def inputViewShowedInFrame(frame: JFrame, inputPanel: JPanel, startButton: JButton): IO[Unit] = IO {
      SwingUtilities.invokeAndWait(() => {
        frame.removeAll()
        frame.getContentPane.add(inputPanel, BorderLayout.CENTER)
        frame.getContentPane.add(startButton, BorderLayout.SOUTH)
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
        frame.pack()
        frame.setResizable(false)
        frame.setVisible(true)
      })
    }

    def sliderChangeUpdatesLabelAdded(slider: JSlider, label: JLabel): IO[Unit] = IO {
      slider.addChangeListener((event: ChangeEvent) => {
        val source = event.getSource.asInstanceOf[JSlider]
        label.setText(source.getValue.toString)
      })
    }

    def buttonEffectUpdatesSliderAdded(button: JButton, slider: JSlider, checkCondition: Int => Boolean,
                                       updateFunction: Int => Int): IO[Unit] = IO {
      button.addActionListener((_: ActionEvent) => {
        if (checkCondition(slider.getValue))
          slider.setValue(updateFunction(slider.getValue))
      })
    }

    def sliderGraphicsUpdated(slider: JSlider): IO[Unit] = IO {
      slider.setMajorTickSpacing(slider.getMaximum / 5)
      slider.setMinorTickSpacing(1)
      slider.setPaintTicks(true)
      slider.setPaintLabels(true)
    }

    def createDataInputRow(mainPanel: JPanel, text: String, minValue: Int, maxValue: Int, defaultValue: Int): IO[JSlider] = {
      for {
        rowPanel <- panelCreated
        _ <- componentBorderSet(rowPanel, 10, 10, 10, 10)
        _ <- panelLayoutSet(rowPanel.asInstanceOf[JPanel])
        description <- labelCreated(text + ":")
        counter <- labelCreated(defaultValue.toString)
        slider <- sliderCreated(minValue, maxValue, defaultValue)
        _ <- sliderChangeUpdatesLabelAdded(slider, counter)
        _ <- componentBorderSet(slider, 5, 0, 5, 0)
        _ <- sliderGraphicsUpdated(slider)
        increment <- buttonCreated("+")
        _ <- buttonEffectUpdatesSliderAdded(increment, slider, _ < maxValue, _ + 1)
        decrement <- buttonCreated("-")
        _ <- buttonEffectUpdatesSliderAdded(decrement, slider, _ > minValue, _ - 1)
        infoPanel <- panelCreated
        _ <- panelComponentsAdded(infoPanel, description, counter)
        _ <- componentBorderSet(infoPanel, (1.5 * counter.getFont.getSize).toInt, 0, 0, 0)
        commandPanel <- panelCreated
        _ <- panelComponentsAdded(commandPanel, decrement, slider, increment)
        _ <- panelComponentAdded(rowPanel, infoPanel, BorderLayout.WEST)
        _ <- panelComponentAdded(rowPanel, commandPanel, BorderLayout.EAST)
        _ <- panelComponentAdded(mainPanel, rowPanel)
      } yield slider
    }

    def buttonEffectCompletesEnvironment(button: JButton, promise: Promise[Environment], temperature: JSlider,
                                         luminosity: JSlider, initialBlobNumber: JSlider, initialFoodNumber: JSlider,
                                         initialObstacleNumber: JSlider, daysNumber: JSlider): IO[Unit] =
      IO {
        button.addActionListener((_: ActionEvent) =>
          promise.success(Environment(temperature.getValue, luminosity.getValue, initialBlobNumber.getValue,
            initialFoodNumber.getValue, initialObstacleNumber.getValue, daysNumber.getValue)))
      }

    val buildAndShowView = {
      for {
        inputPanel <- panelCreated
        initialBlobNumber <- createDataInputRow(inputPanel, "#Blob", Constants.MIN_BLOBS, Constants.MAX_BLOBS,
          Constants.DEF_BLOBS)
        initialFoodNumber <- createDataInputRow(inputPanel, "#Food", Constants.MIN_FOODS, Constants.MAX_FOODS,
          Constants.DEF_FOODS)
        initialObstacleNumber <- createDataInputRow(inputPanel, "#Obstacle", Constants.MIN_OBSTACLES,
          Constants.MAX_OBSTACLES, Constants.DEF_OBSTACLES)
        luminosity <- createDataInputRow(inputPanel, "Luminosity (cd)", Constants.SELECTABLE_MIN_LUMINOSITY,
          Constants.SELECTABLE_MAX_LUMINOSITY, Constants.DEFAULT_LUMINOSITY)
        temperature <- createDataInputRow(inputPanel, "Temperature (°C)", Constants.SELECTABLE_MIN_TEMPERATURE,
          Constants.SELECTABLE_MAX_TEMPERATURE, Constants.DEF_TEMPERATURE)
        days <- createDataInputRow(inputPanel, "#Days", Constants.MIN_DAYS, Constants.MAX_DAYS, Constants.DEF_DAYS)
        start <- buttonCreated("Start")
        _ <- buttonEffectCompletesEnvironment(start, userInput, temperature, luminosity, initialBlobNumber,
          initialFoodNumber, initialObstacleNumber, days)
        _ <- panelComponentAdded(inputPanel, start)
        _ <- inputViewShowedInFrame(frame, inputPanel, start)
      } yield ()
    }

    buildAndShowView.unsafeRunSync()
  }

  override def inputReadFromUser(): Environment =
    Await.result(userInput.future, Duration.Inf)

  override def simulationViewBuiltAndShowed(): Unit = {
    SwingUtilities.invokeAndWait(() => {
      frame.getContentPane.removeAll()
      frame.getContentPane.add(barPanel, BorderLayout.NORTH)
      frame.getContentPane.add(entityPanel, BorderLayout.CENTER)
      frame.setPreferredSize(new Dimension(
        Toolkit.getDefaultToolkit.getScreenSize.width,
        Toolkit.getDefaultToolkit.getScreenSize.height))
      frame.pack()
      frame.setVisible(true)
    })
  }

  override def rendered(world: World): Unit = {
    SwingUtilities.invokeAndWait(() => {
      entityPanel.removeAll()
      entityPanel.add(new ShapesPanel(world))
      frame.pack()
    })
  }

  override def resultViewBuiltAndShowed(world: World): Unit = {
    SwingUtilities.invokeAndWait(() => {
      frame.getContentPane.removeAll()
      // TODO
      frame.setSize(800, 800)
      frame.pack()
    })
  }
}

package evo_sim.view.chart

object JFreeChart extends App with scalax.chart.module.Charting {

  /*val data = for (i <- 1 to 5) yield (i,i)
  val chart = XYLineChart(data)
  chart.show()*/

  val born = List.iterate(0, 5)(_ + 3)
  val death = List.iterate(0, 5)(_ + 1)
  val prova = new XYSeries("Prova")
  val prova2 = new XYSeries("Prova2")
  for(elem <- 0 to 4){
    prova.add(elem, born(elem))
    prova2.add(elem, death(elem))
  }

  val dataset = new XYSeriesCollection
  dataset.addSeries(prova)
  dataset.addSeries(prova2)

  val chart = XYLineChart(dataset)
  chart.show()


  /*val series = new XYSeries("f(x) = sin(x)")
  val chart = XYLineChart(series)
  chart.show()
  for (x <- -4.0 to 4 by 0.1) {
    swing.Swing onEDT {
      series.add(x,math.sin(x))
    }
    Thread.sleep(50)
  }*/

}



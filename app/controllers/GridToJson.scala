package controllers

import org.sikessle.gameoflife.controller.Controller
import org.sikessle.gameoflife.model.Grid
import play.api.libs.json._

object GridToJson {

  def getGridAsJson(controller: Controller): JsObject = {
    val grid = controller.grid

    Json.obj(
      "cells" -> getCellsJsArray(grid),
      "generationStrategy" -> controller.stepperName,
      "numberOfSteppedGenerations" -> controller.steppedGenerations
    )
  }

  private def getCellsJsArray(grid: Grid): JsArray = {
    var cells = Json.arr()

    for (i <- 0 until grid.rows) {
      var row = Json.arr()
      for (j <- 0 until grid.columns) {
        row = row :+ JsBoolean(grid(i)(j))
      }
      cells = cells :+ row
    }

    cells
  }

}

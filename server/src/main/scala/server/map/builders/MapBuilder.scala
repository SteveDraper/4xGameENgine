package server.map.builders

import map.CartesianSpaceMap
import server.ApiHelper.TaskFailureOr
import state.property.SimpleCompositePropertyCellState

trait MapBuilder {
  def initialize(s: CartesianSpaceMap[SimpleCompositePropertyCellState]): TaskFailureOr[CartesianSpaceMap[SimpleCompositePropertyCellState]]
}


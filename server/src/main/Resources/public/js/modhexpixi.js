/*
* Wrapper extending/modifying HexPixi library for our use. Note we have had to make some direct modifications
* to the library to enable this. Will probably end up rewriting for our specific purpose.
*
* TODO: modify the underlying library to use prototypes for efficiency, when we need to scale.
*/
define([ 'pixi', 'hexPixi', 'underscore'
], function(pixi, hexPixi, _) {
  'use strict';

  /*
  * Store game cell data, and override default center of (0, 0)
  */
  hexPixi.GameCell = function(rowNo, columnNo, center, data){
    this.data = _.defaults(data, { color: 0xffffff });
    var cell = hexPixi.Cell.call(this, rowNo, columnNo, data.terrainIndex);
    this.center = center;
    return cell;
  };

  hexPixi.GameCell.prototype = _.create(hexPixi.Cell.prototype);
  hexPixi.GameCell.prototype.toJSON = function(){
    var json = hexPixi.Cell.prototype.toJSON.call(this);
    json.data = this.data;
  };

  // override to display game coordinates, not cell indexes
  hexPixi.GameCell.prototype.coordinateText = function(){
    var loc = this.locationForDisplay();
    return loc.x + ', ' + loc.y + ': ' + loc.z;
  };

  hexPixi.GameCell.prototype.locationForDisplay = function(precision){
    function toP(n){ return typeof n === 'number' ? n.toPrecision(p) : ''; }
    var loc = this.data.location;
    var p = precision || 2;
    return {
      x: toP(loc.x),
      y: toP(loc.y),
      z: toP(loc.z)
    };
  };

  hexPixi.GameMap = function(stage, options) {
    return hexPixi.Map.apply(this, arguments);
  };

  hexPixi.GameMap.prototype = _.create(hexPixi.Map.prototype);

  hexPixi.GameMap.prototype.generateMap = function(cellArray, scalingFactor){
    // Generate from cell data as provided by server
    // TODO make sensitive to mapWidth mapHeight. Also add a mapCenter.
    if (cellArray.length === 0) { return; }

    var scale = scalingFactor || 1.0;
    // We map game cell centers into view cell centers based on the left-hand edge
    // We look at the first two rows to find the left-most.
    var firstLoc = cellArray[0][0].location;
    var secondLoc = cellArray[1] ? cellArray[1][0].location : cellArray[0][0].location;
    this.xOffset = this.options.hexSize * (1 - scale * Math.min(firstLoc.x, secondLoc.x));
    this.yOffset = this.options.hexSize * (1 - scale * firstLoc.y);

    var self = this;
    this.cells = _.map(cellArray, function(row, rowIndex){
      return _.map(row, function(cell, colIndex){
        var cellCenter = {
          x: cell.location.x * scale * self.options.hexSize + self.xOffset,
          y: cell.location.y * scale * self.options.hexSize + self.yOffset
        };

        return new hexPixi.GameCell(rowIndex, colIndex, cellCenter, cell);
      });
    });
    this.createSceneGraph();
  };

  hexPixi.GameMap.prototype.getCellColor = function(cell){
    if (cell.data && cell.data.color) {
      return cell.data.color;
    }
    return hexPixi.Map.prototype.getCellColor.apply(this, arguments);
  };

  /*
  * Merge specified options into current options and reset the map
  */
  hexPixi.GameMap.prototype.updateOptions = function(options){
    var opts = _.extend({}, this.options, options);
    return this.reset(opts);
  };

  return hexPixi;
});

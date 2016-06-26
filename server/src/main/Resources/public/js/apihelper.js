/*
* Useful functions that may end up somewhere else
*/

define([ 'underscore' ], function(_) {
  'use strict';

  var colorMap = {
    lowlands: 0x70B45A,
    hills: 0x799779,
    lowMountain: 0x807515,
    highMountain: 0xD4CA6A,
    snow: 0xebebfa,
    sand:  0xdBd588,
    water: 0x4B688B,
    deepWater: 0x143153,
    abyss: 0x051C38,
    offMap: 0xffffff
  };

  /*
  * relativeElevation is -100 (lowest) to +100 (highest)
  * TODO: Make color a sliding scale rather than discrete
  */
  function defaultElevationColor(relativeElevation){
    if(relativeElevation < -80) { return colorMap.abyss; }
    if(relativeElevation < -40) { return colorMap.deepWater; }
    if(relativeElevation < 0) { return colorMap.water; }
    if(relativeElevation < 40) { return colorMap.lowlands; }
    if(relativeElevation < 60) { return colorMap.hills; }
    if(relativeElevation < 80) { return colorMap.lowMountain; }
    if(relativeElevation <= 90) { return colorMap.highMountain; }
    if(relativeElevation <= 100) { return colorMap.snow; }
    return colorMap.offMap;
  }

  var helper = {
    indexCells : function(cells){
      // group into rows and sort the cells in each row
      var rowHash = _.mapObject(
        _.groupBy(cells, function(c){
          return c.location.y;
        }),
        function(row){
          return _.sortBy(row, function(c){
            return c.location.x;
          });
        }
      );

      // sort rows and return
      return _.map(_.sortBy(_.pairs(rowHash), _.first), _.last);
    },

    // scale server dimensions to one cell's width
    scalingFactor: function(spacing){
      return (spacing / Math.sqrt(3));
    },

    // return a color based on elevation, this game's elevation bounds, and a set of
    // options which might influence the choice (e.g. explicit statement of type like water)
    terrainColor: function(cell, elevationBounds, options){
      var elevation = helper.elevation(cell);
      var relativeElevation = 0;
      if(elevation < 0 && elevationBounds.min < 0) {
        relativeElevation = -100 * (elevation / elevationBounds.min);
      }

      if(elevation > 0 && elevationBounds.max > 0) {
        relativeElevation = 100 * (elevation / elevationBounds.max);
      }
      return defaultElevationColor(relativeElevation);
    },

    elevation: function(cell){
      return (_.findWhere(cell.scalars, { name: 'Height'}) || {}).value;
    }

    // TODO add texture function
  };

  return helper;

});

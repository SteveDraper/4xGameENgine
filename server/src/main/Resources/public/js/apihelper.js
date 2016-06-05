define([ 'underscore' ], function(_) {
	"use strict";

    return {
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

        scalingFactor: function(spacing){
            return (spacing / Math.sqrt(3));
        }
    }

});
define([ 'pixi', 'modhexpixi', 'jquery', 'underscore'
], function(pixi, hexPixi, $, _) {
  'use strict';

  /*
  * Holds properties of the world
  */
  function World(){
    this.uri = 'http://localhost:9600/properties';
    this.queryString = _.template('');
  }

  /**
  * done - function to call after fetched
  */
  World.prototype.fetchProperties = function(done){
    $.get(
      this.uri + '?' + this.queryString(),
      {},
      function(props){
        var height = _.findWhere(props.scalars, { name: 'Height' });
        if (height) {
          this.elevationBounds = {
            max: height.valueRange.max,
            min: height.valueRange.min
          };
        }
        if (done) { done(); }
      }.bind(this),
      'json'
    );
    return this;
  };

  return World;
});

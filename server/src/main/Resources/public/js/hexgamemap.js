define([ 'pixi', 'modhexpixi', 'jquery', 'underscore', 'apihelper'
], function(pixi, hexPixi, $, _, helper) {
  'use strict';

  /*
  * TODO: Decouple views of map from model.
  */
  function GameMap(options){
    var opts = options || {};
    // model initialization
    this.gameId = opts.gameId || 'test';
    this.mapId = opts.mapId || 'testLarge';
    this.mapCells = [];
    this.map = null;
    this.world = opts.world;
    this.uri = 'http://localhost:9600/games/' + this.gameId +'/maps/' + this.mapId;
    this.queryString = _.template('leftX=<%= leftX %>&rightX=<%= rightX %>&topY=<%= topY %>&bottomY=<%= bottomY %>');
    this.stage = new pixi.Container();
    this.renderer = new pixi.autoDetectRenderer(
      1800,
      1024,
      {
        antialiasing: false,
        transparent: false,
        resolution: 1
      }
    );
    this.renderer.backgroundColor = 0xFFFFFF;
  }

  /*
  * Deals with view setup.
  */
  GameMap.prototype.initialize = function(options){
    var opts = _.defaults(options, { mapWidth: 10, mapHeight: 8, hexSize: 40 });
    var el = opts.el || $('<div>');
    this.$el = el instanceof $ ? el :  $(el);

    var renderer = this.renderer;
    var stage = this.stage;

    function animate() {
      window.requestAnimationFrame(animate);
      renderer.render(stage);
    }

    var self = this;
    var getOptions = function() {
      return {
        mapWidth: opts.mapWidth,
        mapHeight: opts.mapHeight,
        // Our coord system is always 'flat top' but we don't know if it is odd-q or
        // even-q until we get multiple rows of cells.  Setting the system to 'even-q' makes sure
        // the hexes are drawn as flat top. We don't need odd-q/even-q for offset because the game server
        // calculates cell centers.
        coordinateSystem: 1,
        hexLineWidth: 2,
        hexSize: opts.hexSize,
        showCoordinates: true,
        cellColor: function(){
          // 'this' is cell, 'self' is map!!!
          return helper.terrainColor(this.data, self.world.elevationBounds, {});
        },
        textures: ['images/texture/grassTexture.jpg', 'images/texture/waterTexture.jpg'],
        // TODO replace terrain types functions for determining color, texture based on game cell properties
        terrainTypes: [
          { name: 'dirt', color: 0x9B5523 },
          { name: 'sand', color: 0xdBd588 },
          { name: 'snow', color: 0xebebfa },
          { name: 'water', textureIndex: 1, color: 0x4060fa },
          { name: 'grass', textureIndex: 0, color: 0x10fa10 }
        ],
        onAssetsLoaded: function () {
          try{
            renderer.render(stage);
            animate();
          }
          catch (e){
            console.error(e);
          }
        }
      };
    }.bind(this);

    this.map = new hexPixi.GameMap(stage, getOptions());
    return this;
  };

  /*
  * Set topology info from 'topology' element in response
  */
  GameMap.prototype.setTopology = function(topology){
    if (!(topology && topology.cellSpacing)) { return; }
    this.scalingFactor = helper.scalingFactor(topology.cellSpacing);
  };

  /**
  * bounds - { leftX: nn, rightX: nn, topY: nn, bottomY: nn }
  * done - function to call after fetched and rendered
  */
  GameMap.prototype.fetchMapArea = function(bounds, done){
    var b = _.defaults(bounds || {}, { leftX: -10, rightX: 10, topY: -10, bottomY: 10 });
    var world = this.world;
    $.get(
      this.uri + '?' + this.queryString(b),
      {},
      function(data){
        var cells = data.cells.map(function(c){
          return {
            location: {
              x: c.location.x,
              y: c.location.y,
              z: helper.elevation(c)
            },
            color: helper.terrainColor(c, world.elevationBounds, {})
          };
        });
        this.mapCells = helper.indexCells(cells);
        this.setTopology(data.topology);
        this.render();
        if (done) { done(); }
      }.bind(this),
      'json'
    ).fail(function(jqXHR, status, err){
      alert(status);
    });
    return this;
  };

  /*
  * Currently renders all map cells. TODO: render just a subarea
  */
  GameMap.prototype.render = function(){
    this.map.generateMap(this.mapCells, this.scalingFactor);
    this.$el.empty().append(this.renderer.view);
    return this;
  };

  /*
  * Currently renders all map cells. TODO: render just a subarea
  */
  GameMap.prototype.updateMapOptions = function(opts){
    this.map.updateOptions(opts);
    return this;
  };

  return GameMap;
});

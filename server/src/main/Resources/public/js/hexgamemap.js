define([ 'pixi', 'hexPixi', 'jquery'
    ], function(pixi, hexPixi, $) {
	"use strict";

    function GameMap(){
        this.map = null;
        this.stage = new pixi.Container();
        this.renderer = new pixi.autoDetectRenderer(
            800,
            600,
            {
                antialiasing: false,
                transparent: false,
                resolution: 1
            }
        );
        this.renderer.backgroundColor = 0xFFFFFF;
    }

    GameMap.prototype.initialize = function(options){

        var opts = options || {};
        var el = opts.el || $('<div>');
        this.$el = el instanceof $ ? el :  $(el);
        var renderer = this.renderer;
        var stage = this.stage;

        function animate() {
            window.requestAnimationFrame(animate);
            // render the stage
            renderer.render(stage);
        }

        function getOptions() {
            return {
                mapWidth: opts.mapWidth || 10,
                mapHeight: opts.mapHeight || 8,
                coordinateSystem: 2,
                hexLineWidth: 2,
                hexSize: opts.hexSize || 40,
                showCoordinates: true,
                textures: ["images/texture/grassTexture.jpg", "images/texture/waterTexture.jpg"],
                terrainTypes: [
                    { name: "dirt", color: 0x9B5523 },
                    { name: "sand", color: 0xdBd588 },
                    { name: "snow", color: 0xebebfa },
                    { name: "water", textureIndex: 1, color: 0x4060fa },
                    { name: "grass", textureIndex: 0, color: 0x10fa10 }
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
        }

        this.map = new hexPixi.Map(stage, getOptions());
        this.map.generateRandomMap();
        this.$el.empty().append(this.renderer.view);

        return this;
    }

    return GameMap;
});
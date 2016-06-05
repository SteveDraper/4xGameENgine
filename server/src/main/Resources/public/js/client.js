// client.js
/*
 * Wrapper that sets up require.config and calls clientMain.
 */
require.config({
	baseUrl: '/resources',
	paths : {
		underscore : 'lib/underscore',
		react: 'lib/react-with-addons',
		jquery: 'lib/jquery',
		JSXTransformer: 'lib/jsxtransformer',
		jsx: 'lib/require-jsx',
		text: 'lib/require-text',
		pixi: 'lib/pixi_current',
//		pixi: 'https://cdnjs.cloudflare.com/ajax/libs/pixi.js/3.0.7/pixi.min',
		hexPixi: 'lib/hexPixi'

    },
    jsx: {
        fileExtension: '.jsx'
    }
});

// define(['jquery', 'react', 'jsx!js/app'], function($, React, App){
define(['jquery', 'pixi', 'hexPixi'], function($, pixi, hexPixi){

/*    var AppElement = React.createElement(App);
    React.render(AppElement, document.body);
*/

    var map = null;
    var stage = new pixi.Container();
    var renderer = new pixi.autoDetectRenderer(800, 600,
     {
        antialiasing: false,
        transparent: false,
        resolution: 1
    });

    renderer.backgroundColor = 0xFFFFFF;

    function animate() {
        window.requestAnimationFrame(animate);
        // render the stage
        renderer.render(stage);
    }

    function getOptions() {
        return {
            mapWidth: 10,
            mapHeight: 8,
            coordinateSystem: 2,
            hexLineWidth: 2,
            hexSize: 40,
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

    var map = new hexPixi.Map(stage, getOptions());
    map.generateRandomMap();
    $('#mainmap').append(renderer.view);

});


// https://github.com/kittykatattack/learningPixi
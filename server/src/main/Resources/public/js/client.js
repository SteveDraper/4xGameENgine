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
		pixi: 'lib/pixi.min',
		modhexpixi: 'js/modhexpixi',
		hexPixi: 'lib/hexPixi',
		gamemap: 'js/hexgamemap',
		apihelper: 'js/apihelper',
		world: 'js/world'

    },
    jsx: {
        fileExtension: '.jsx'
    }
});

// define(['jquery', 'react', 'jsx!js/app'], function($, React, App){
define(['jquery', 'gamemap', 'apihelper', 'world'], function($, HexGameMap, helper, World){

/*    var AppElement = React.createElement(App);
    React.render(AppElement, document.body);
*/
    var world = new World();
    var gameMap =  new HexGameMap({ world: world }).initialize({
      el: $('#mainmap'),
      hexSize: 20, // px
    });

    world.fetchProperties(function(){
        gameMap.fetchMapArea({ leftX: -0, rightX: 40, topY: 0, bottomY: 40});
    });
});


// https://github.com/kittykatattack/learningPixi
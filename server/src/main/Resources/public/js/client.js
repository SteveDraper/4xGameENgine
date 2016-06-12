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
		apihelper: 'js/apihelper'

    },
    jsx: {
        fileExtension: '.jsx'
    }
});

// define(['jquery', 'react', 'jsx!js/app'], function($, React, App){
define(['jquery', 'gamemap', 'apihelper'], function($, HexGameMap, helper){

/*    var AppElement = React.createElement(App);
    React.render(AppElement, document.body);
*/

    new HexGameMap().initialize({
        el: $('#mainmap'),
        hexSize: 40, // px
    }).fetch({ leftX: 0, rightX: 10, topY: 0, bottomY: 10});
});


// https://github.com/kittykatattack/learningPixi
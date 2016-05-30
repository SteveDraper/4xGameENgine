// client.js
/*
 * Wrapper that sets up require.config and calls clientMain.
 */
require.config({
	baseUrl: '/resources',
	paths : {
		underscore : 'lib/underscore',
		react: 'lib/react-with-addons',
		reactdom: 'lib/react-dom',
		reactart: 'lib/react-art',
		jquery: 'lib/jquery',
		JSXTransformer: 'lib/jsxtransformer',
		jsx: 'lib/require-jsx',
		text: 'lib/require-text',
		hexgrid: 'js/hexgrid',
		hextile: 'js/hextile',
		gameboard: 'js/gameboard',
		displayhelper: 'js/displayhelper'

    },
    jsx: {
        fileExtension: '.jsx'
    }
});

//define(['react', 'jsx!./js/app', 'jquery'], function(React, App, $){
define(['jquery', 'react', 'jsx!js/app'], function($, React, App){

    var AppElement = React.createElement(App);
    React.render(AppElement, document.body);

    $('body').append($('<p>Hello world</p>'));
});



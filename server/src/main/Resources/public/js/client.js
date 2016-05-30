// client.js
/*
 * Wrapper that sets up require.config and calls clientMain.
 */
require.config({
	// baseUrl is Resources
	paths : {
		jquery : '/resources/lib/jquery',
		underscore : '/resources/lib/underscore',
		react: '/resources/lib/react-with-addons',
		reactdom: '/resources/lib/react-dom',
		reactart: '/resources/lib/react-art',
		JSXTransformer: '/resources/lib/jsxtransformer',
		jsx: '/resources/lib/require-jsx',
		text: '/resources/lib/require-text',
		hexgrid: 'jsx!/js/hexgrid',
		hextile: 'jsx!/js/hextile',
		gameboard: 'jsx!/js/gameboard',
		displayhelper: '/js/displayhelper'

    },
    jsx: {
        fileExtension: '.jsx'
    }
});

define(['react', 'js/app.jsx', 'jquery'], function(React, App, $){

    var AppElement = React.createElement(App);
    React.render(AppElement, document.body);

    $('body').append($('<p>Hello world</p>'));
});



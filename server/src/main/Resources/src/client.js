// client.js
/*
 * Wrapper that sets up require.config and calls clientMain.
 */
require.config({
	// baseUrl is Resources
	paths : {
		jquery : 'lib/jquery',
		underscore : 'lib/underscore'
	}
});

define(['jquery'], function($){
    $('body').append($('<p>Hello world</p>'));
});

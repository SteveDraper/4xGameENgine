// client.js
/*
 * Wrapper that sets up require.config and calls clientMain.
 */
require.config({
	// baseUrl is Resources
	paths : {
		jquery : '/resources/lib/jquery',
		underscore : '/resources/lib/underscore'
	}
});

define(['jquery'], function($){
    $('body').append($('<p>Hello world</p>'));
});

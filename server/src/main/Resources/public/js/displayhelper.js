define([ 'underscore',/* 'eventemitter' */], function(_, /*eventEmitter*/) {
	"use strict";

	var displayHelper = function() {

        function onResize() {
 /*           eventEmitter.emit('resize', {
                width: window.innerWidth,
                height: window.innerHeight
  */          });
        }
        window.addEventListener('resize', _.debounce(onResize, 200));

        return {
            subscribeResize: function(onResize) {
 //               eventEmitter.addListener('resize', onResize);
            },
            unsubscribeResize: function(onResize) {
 //               eventEmitter.removeListener('resize', onResize);
            },
            getDimensions: function() {
                return {
                    width: window.innerWidth,
                    height: window.innerHeight
                };
            }
        };
    };

    return displayHelper();
});
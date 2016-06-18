define([ 'react', gamemap, 'jsx!controlpanel'
    ], function(React, HexGameMap, ControlPanel) {
	"use strict";

    var App = function() {
        return React.createClass({
            displayName: 'app',
            render: function() {
                return (
                    <ControlPanel />
                );
            }
        });
    }

    return App;
});
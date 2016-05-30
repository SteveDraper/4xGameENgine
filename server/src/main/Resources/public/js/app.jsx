define([ 'react', 'jsx!gameboard'
    ], function(React, GameBoard) {
	"use strict";

    var App = function() {
        return React.createClass({
            displayName: 'app',
            mixins: [React.addons.PureRenderMixin],
            render: function() {
                return (
                    <GameBoard />
                );
            }
        });
    }

    return App;
});
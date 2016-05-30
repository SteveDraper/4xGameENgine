define([ 'react', 'gameboard', 'purerendermixin'
    ], function(React, GameBoard, PureRenderMixin) {
	"use strict";

    var App = function() {
        return React.createClass({
            displayName: 'app',
            mixins: [PureRenderMixin],
            render: function() {
                return (
                    <GameBoard />
                );
            }
        });
    }

    return App;
);
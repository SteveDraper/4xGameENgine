define([ 'react', 'reactart', 'jsx!hexgrid', 'displayhelper'
    ], function(React, ReactArt, HexGrid, displayHelper) {
	"use strict";

	var GameBoard = function() {
        return React.createClass({
            displayName: 'GameBoard',
            mixins: [React.addons.PureRenderMixin],
            getInitialState: function() {
                return {
                    displayDimensions: displayHelper.getDimensions()
                }
            },
            setDisplayDimensions: function(dimensions) {
                this.setState({
                    displayDimensions: displayHelper.getDimensions()
                });
            },
            componentWillMount: function() {
//                displayHelper.subscribeResize(this.setDisplayDimensions);
            },
            componentWillUnmount: function() {
//                displayHelper.unsubscribeResize(this.setDisplayDimensions);
            },
            render: function() {
                var width = this.state.displayDimensions.width;
                var height = this.state.displayDimensions.height;

                return (
                    <ReactArt.Surface width={ width } height={ height }>
                        <HexGrid width={ width } height={ height } hexCountHorizontal='25' hexCountVertical='13' />
                    </ReactArt.Surface>
                );
            }
        });
    }

	return GameBoard;
});
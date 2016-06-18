define(['jquery', 'underscore', 'react'], function($, _, React) {
    "use strict";

    var ControlPanel = React.createClass({
      getInitialState: function() {
        return { bounds: { leftX: -10, rightX: 20, topY: 0, bottomY: 20 }, hexSize: 40 };
      },
      handleHexSizeChange: function(e) {
        var size = parseInt(e.target.value);
         if(!isNaN(size) && size > 0){
            this.setState({hexSize: size});
        }
      },
      handleBoundsChange: function(e) {
          var bounds = _.extend({}, this.state.bounds);
          var field = e.target.id;
          bounds[field] = parseInt(e.target.value);
          if (isNaN(bounds[field])){
            bounds[field] = this.state.bounds[field];
          }
          this.setState({ bounds: bounds });
      },
      handleSubmit: function(e) {
        e.preventDefault();
        this.props.onBoundsChange(this.state.bounds);
        this.props.onHexSizeChange({ hexSize: this.state.hexSize });
      },
      render: function() {
        return (
        	React.createElement('form', 
        		{ className: 'boundsForm', onSubmit: this.handleSubmit},
         		React.createElement('label', {
         			htmlFor: 'hexsize',
         		}, "Size of hexagons: "),
         		React.createElement('input', {
        			type: 'text',
        			id: "hexsize",
        			value: this.state.hexSize,
        			onChange: this.handleHexSizeChange
        		}),
                React.createElement('label', {
                    htmlFor: 'leftX'
                }, "Left X: "),
         		React.createElement('input', {
        			type: 'text',
        			id: "leftX",
        			value: this.state.bounds.leftX,
        			onChange: this.handleBoundsChange
        		}),
                React.createElement('label', {
                    htmlFor: 'rightX'
                }, "Right X: "),
        		React.createElement('input', {
        			type: 'text',
        			id: "rightX",
        			value: this.state.bounds.rightX,
        			onChange: this.handleBoundsChange
        		}),
                React.createElement('label', {
                    htmlFor: 'topY'
                }, "Top Y: "),
                React.createElement('input', {
                    type: 'text',
        			id: "topY",
                    value: this.state.bounds.topY,
                    onChange: this.handleBoundsChange
                }),
                React.createElement('label', {
                    htmlFor: 'bottomY'
                }, "Bottom Y: "),
                React.createElement('input', {
                    type: 'text',
                    id: "bottomY",
                    value: this.state.bounds.bottomY,
                    onChange: this.handleBoundsChange
                }),
        		React.createElement('input', {
        			type: 'submit',
        			value: "Update"
        		})
        	)
        );
      }
    });

	return ControlPanel;
});
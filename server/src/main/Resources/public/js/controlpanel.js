define([ 'jquery', 'underscore', 'react' ], function($, _, React) {
  'use strict';

  var ControlPanel = React.createClass({
    propTypes: {
      initialBounds: React.PropTypes.shape({
        leftX: React.PropTypes.number.isRequired,
        rightX: React.PropTypes.number.isRequired,
        topY: React.PropTypes.number.isRequired,
        bottomY: React.PropTypes.number.isRequired
      }),
      initialHexSize: React.PropTypes.number.isRequired,
      onHexSizeChange: React.PropTypes.func.isRequired,
      onBoundsChange: React.PropTypes.func.isRequired
    },
    getInitialState : function() {
      return {
        bounds : _.mapObject(
          this.props.initialBounds,
          function(s) { return s.toString(); }
        ),
        hexSize : this.props.initialHexSize.toString(),
        error : ''
      };
    },
    handleHexSizeChange : function(e) {
      this.setState({
        hexSize : e.target.value.trim()
      });
    },
    handleBoundsChange : function(e) {
      var bounds = _.extend({}, this.state.bounds);
      var field = e.target.id;
      bounds[field] = e.target.value.trim();
      this.setState({
        bounds : bounds
      });
    },
    validateBounds : function(errArray) {
      var bounds = _.mapObject(this.state.bounds, parseInt);
      Object.keys(bounds).forEach(function(k) {
        if (isNaN(bounds[k])) {
          errArray.push(k + ' must be a number.');
        }
      });
      return bounds;
    },
    validateSize : function(errArray) {
      var size = parseInt(this.state.hexSize);
      if (isNaN(size) || size < 1) {
        errArray.push('Hex size must be a number greater than 0.');
      }
      return size;
    },
    handleSubmit : function(e) {
      e.preventDefault();
      var errArray = [];
      var b = this.validateBounds(errArray);
      var s = this.validateSize(errArray);
      if (errArray.length > 0) {
        this.setState({
          error : errArray.join(' ')
        });
        return;
      }

      // Order is important - onBoundsChange causes async fetch
      // and must be last
      this.props.onHexSizeChange({ hexSize: s });
      this.props.onBoundsChange(b);
    },
    render : function() {
      return (React.createElement('form', {
        className : 'boundsForm',
        onSubmit : this.handleSubmit
      }, React.createElement('label', {
        htmlFor : 'hexsize'
      }, 'Size of hexagons: '), React.createElement('input', {
        type : 'text',
        id : 'hexsize',
        value : this.state.hexSize,
        onChange : this.handleHexSizeChange
      }), React.createElement('label', {
        htmlFor : 'leftX'
      }, 'Left X: '), React.createElement('input', {
        type : 'text',
        id : 'leftX',
        value : this.state.bounds.leftX,
        onChange : this.handleBoundsChange
      }), React.createElement('label', {
        htmlFor : 'rightX'
      }, 'Right X: '), React.createElement('input', {
        type : 'text',
        id : 'rightX',
        value : this.state.bounds.rightX,
        onChange : this.handleBoundsChange
      }), React.createElement('label', {
        htmlFor : 'topY'
      }, 'Top Y: '), React.createElement('input', {
        type : 'text',
        id : 'topY',
        value : this.state.bounds.topY,
        onChange : this.handleBoundsChange
      }), React.createElement('label', {
        htmlFor : 'bottomY'
      }, 'Bottom Y: '), React.createElement('input', {
        type : 'text',
        id : 'bottomY',
        value : this.state.bounds.bottomY,
        onChange : this.handleBoundsChange
      }), React.createElement('input', {
        type : 'submit',
        value : 'Update'
      }), React.createElement('p', {}, this.state.error)));
    }
  });

  return ControlPanel;
});

// client.js
/*
* Wrapper that sets up require.config and calls clientMain.
*/
require.config({
  baseUrl: '/resources',
  paths : {
    underscore : 'lib/underscore',
    react: 'lib/react-with-addons',
    reactdom: 'lib/react-dom',
    jquery: 'lib/jquery',
    //    JSXTransformer: 'lib/jsxtransformer',
    //    jsx: 'lib/require-jsx',
    //    text: 'lib/require-text',
    pixi: 'lib/pixi.min',
    modhexpixi: 'js/modhexpixi',
    hexPixi: 'lib/hexPixi',
    gamemap: 'js/hexgamemap',
    apihelper: 'js/apihelper',
    world: 'js/world'

  },
  jsx: {
    fileExtension: '.jsx'
  }
});

define(['jquery', 'gamemap', 'apihelper', 'world', 'js/controlpanel', 'react', 'reactdom'],
function($, HexGameMap, helper, World, ControlPanel, React, ReactDOM){

  var world = new World();
  var gameMap =  new HexGameMap({ world: world }).initialize({
    el: $('#mainmap'),
    hexSize: 20 // px
  });

  var initialBounds = {
    leftX : -10,
    rightX : 20,
    topY : 0,
    bottomY : 20
  };

  var controlPanelElement = React.createElement(
    ControlPanel,
    {
      initialBounds: initialBounds,
      initialHexSize: gameMap.map.options.hexSize,
      onBoundsChange: gameMap.fetchMapArea.bind(gameMap),
      onHexSizeChange: function(opts){ gameMap.updateMapOptions(opts); }
    }
  );
  ReactDOM.render(controlPanelElement, document.getElementById('controlpanel'));

  world.fetchProperties(function(){
    gameMap.fetchMapArea(initialBounds);
  });
});


// https://github.com/kittykatattack/learningPixi

﻿/// <reference path="../examples/shared/js/dep/pixi.dev.js" />
/* 
    HexPixi (alpha)
    Version 0.41
    by Mark Harmon 2015
    A free hex game library for pixijs.
    Released under MIT License.
    Please let me know about any games released using this library or derivative work.
*/

/*
    Modified by Christine Draper to allow customization of Cell.
 */

define(['pixi', 'underscore'], function(pixi) {
    'use strict';

    var hp = {};

    // There are four basic coordinate systems based on http://www.redblobgames.com/grids/hexagons/
    hp.CoordinateSystems = [
        { name: "odd-q", isFlatTop: true, isOdd: true },
        { name: "even-q", isFlatTop: true, isOdd: false },
        { name: "odd-r", isFlatTop: false, isOdd: true },
        { name: "even-r", isFlatTop: false, isOdd: false }];

    hp.Camera = function (amap) {
        var position = { x: 0, y: 0 },
            map = amap;

        function updateSceneGraph() {
            map.container.position.x = position.x;
            map.container.position.y = position.y;
        }

        this.position = function (x, y) {
            var result = position;

            if (x && y) {
                position.x = x;
                position.y = y;
                updateSceneGraph();
            }

            return position;
        };
    };

    hp.GameObject = function (type, name, properties, onUpdate) {
        this.type = type;
        this.name = name;
        this.onUpdate = onUpdate;
        this.cell = null;
        this.isVisible = false;
        this.properties = properties;
    };

    // The hexPixi.Cell object represents one map hex cell.
    hp.Cell = function (rowNo, columnNo, terrainIndex) {
        this.row = rowNo;
        this.column = columnNo;
        this.center = { x: 0, y: 0 };
        this.terrainIndex = terrainIndex ? terrainIndex : 0;
        this.poly = null; // The cell's poly that is used as a hit area.
        this.outline = null; // The PIXI.Graphics outline of the cell's hex.
        this.inner = null; // If a non-textured cell then this is the PIXI.Graphics of the hex inner, otherwise a PIXI.Sprite.
        this.hex = null; // The parent container of the hex's graphics objects.
        this.isEmpty = null; // The cell is empty if set to true.
        this.gameObjects = []; // A variable for attaching GameObjects you want (items, people, descriptions).
    };

    hp.Cell.prototype.resetGraphics = function () {
        this.terrainIndex = terrainIndex ? terrainIndex : 0;
        this.poly = null; // The cell's poly that is used as a hit area.
        this.outline = null; // The PIXI.Graphics outline of the cell's hex.
        this.inner = null; // If a non-textured cell then this is the PIXI.Graphics of the hex inner.
        this.hex = null; // The parent container of the hex's graphics objects.
    };

    hp.Cell.prototype.toJSON = function () {
        return {
            row: this.row,
            column: this.column,
            terrainIndex: this.terrainIndex
        };
    };

    // CD: Allow override of coordinate text
    hp.Cell.prototype.coordinateText = function(){
        return this.column.toString() + ", " + this.row.toString();
    }

    // Scene graph heirarchy = pixiState -> container -> hexes
    hp.Map = function (pixiStage, options) {
        this.defaultOptions = {
            // The hexPixi.CoordinateSystems index to use for the map.
            coordinateSystem: 1,
            // The map's number of cells across (cell column count).
            mapWidth: 10,
            // The map's number of cells high (cell row count).
            mapHeight: 10,
            // The radius of the hex.
            hexSize: 40,
            // The color to use when drawing hex outlines.
            hexLineColor: 0x909090,
            // The width in pixels of the hex outline.
            hexLineWidth: 2,
            // If true then the hex's coordinates will be visible on the hex.
            showCoordinates: false,
            // Callback function (cell) that handles a hex being clicked on or tapped.
            onHexClick: null,
            // Specify the types of terrain available on the map. Map cells reference these terrain
            // types by index. Add custom properties to extend functionality.
            terrainTypes: [{ name: "empty", color: 0xffffff, isEmpty: true }],
            // Array of strings that specify the url of a texture. Can be referenced by index in terrainType.
            textures: [],
            // An array of GameObjects. These are the players, nps, items, everything other than the map and ui.
            gameObjects: [],
            // This is the pixel height specifying an area of overlap for hex cells. Necessary when
            // working with isometric view art systems.
            hexBottomPad: 0,
            onAssetsLoaded: function () { }
        };
        this.options = {};
        this.textures = [];
        this.hexes = new pixi.Graphics();
        this.container = new pixi.Container();
        this.pixiStage = pixiStage;
        this.cells = [];
        this.camera = new hp.Camera(this);
        this.cellHighlighter = null;
        this.inCellCount = 0;
        this.hexAxis = { x: 0, y: 0 };
        this.aspectRatio = 1;

        this._init(options, pixi);
    };

    // Calculates and returns the width of a hex cell.
    hp.Map.prototype.getHexWidth = function() {
        var result = null,
            cs = hp.CoordinateSystems[this.options.coordinateSystem];
        result = this.options.hexSize * 2;
        if (cs.isFlatTop == false) {
            result = Math.sqrt(3) / 2 * result;
        }

        return result;
    }

    // Calculates and returns the height of a hex cell.
    hp.Map.prototype.getHexHeight = function() {
        var result = null,
            cs = hp.CoordinateSystems[this.options.coordinateSystem];
        result = this.options.hexSize * 2;
        if (cs.isFlatTop == true) {
            result = Math.sqrt(3) / 2 * result;
        }

        return result;
    }

    // Loads all the textures specified in options.
    hp.Map.prototype.loadTextures = function() {
        this.textures = [];

        if (this.options.textures.length) {
            var loader = PIXI.loader;
            loader = new PIXI.loaders.Loader();

            loader.add(this.options.textures);

            // use callback
            loader.once('complete', this.options.onAssetsLoaded);

            //begin load
            loader.load();

            for (var i = 0; i < this.options.textures.length; i++) {
                this.textures.push(new pixi.Texture.fromImage(this.options.textures[i]));
            }
        } else {
            // No assets to load so just call onAssetsLoaded function to notify game that we are done.
            this.options.onAssetsLoaded && this.options.onAssetsLoaded();
        }
    }


    hp.Map.prototype._init = function(options, pixi) {

       this.options = _.extend({}, this.defaultOptions, options);

       hp.init(pixi);

       this.aspectRatio = 1;
       this.options.hexWidth = this.getHexWidth();
       this.options.hexHeight = this.getHexHeight();
       this.hexAxis.x = this.options.hexSize * 2;
       this.hexAxis.y = this.options.hexSize * 2;

       this.container.addChild(this.hexes);
       this.pixiStage.addChild(this.container);
       this.hexes.clear();
       this.loadTextures();

       // Setup cell hilighter
       var cell = new hp.Cell(0, 0, 0);
       cell.poly = this.createHexPoly();
       var chg = this.createDrawHex_internal(cell, true, false);
       if (chg) {
           chg.updateLineStyle(3, 0xff5521);
           this.cellHighlighter = new pixi.Container();
           this.cellHighlighter.addChild(chg);
       } else {
           console.log("Error creating cell hilighter");
       }
    }

    hp.Map.prototype.getCellColor = function(cell){
        var color = this.options.terrainTypes[cell.terrainIndex].color;
        return color || 0xffffff;
    }

    hp.Map.prototype.setCellTerrainType = function (cell, terrainIndex) {
        cell.terrainIndex = terrainIndex;
        this.createSceneGraph();
    };

    // Clears out all objects from this.hexes.children.
    hp.Map.prototype.clearHexes = function() {
        while (this.hexes.children.length) {
            this.hexes.removeChild(this.hexes.children[0]);
        }
    }

    // Used for manually drawing a hex cell. Creates the filled in hex, creates the outline (if there is one)
    // and then wraps them in a PIXI.Container.
    hp.Map.prototype.createDrawnHex = function (cell) {
        var parentContainer = new pixi.Container();

        cell.inner = this.createDrawHex_internal(cell, false, true);
        parentContainer.addChild(cell.inner);

        if (this.options.hexLineWidth > 0) {
            cell.outline = this.createDrawHex_internal(cell, true, false);
            parentContainer.addChild(cell.outline);
        }

        parentContainer.position.x = cell.center.x;
        parentContainer.position.y = cell.center.y;

        return parentContainer;
    };

    // Creates a drawn hex while ignoring the cell's position. A new PIXI.Graphics object is created
    // and used to draw and (possibly) fill in the hex. The PIXI.Graphics is returned to the caller.
    hp.Map.prototype.createDrawHex_internal = function(cell, hasOutline, hasFill) {
        var graphics = new pixi.Graphics(),
            i = 0,
            cs = hp.CoordinateSystems[this.options.coordinateSystem],
            color = this.getCellColor(cell);

        if (cell.poly == null) {
            console.log("Cell's poly must first be defined by calling createHexPoly");
            return null;
        }

        if (hasOutline === false) {
            // If this is for masking then we don't need the line itself. Just the poly filled.
            graphics.lineStyle(0, 0, 1);
        } else {
            graphics.lineStyle(this.options.hexLineWidth, this.options.hexLineColor, 1);
        }

        if (hasFill !== false) {
            graphics.beginFill(color, 1);
        }

        graphics.moveTo(cell.poly.points[i], cell.poly.points[i + 1]);

        for (i = 2; i < cell.poly.points.length; i += 2) {
            graphics.lineTo(cell.poly.points[i], cell.poly.points[i + 1]);
        }

        if (hasFill !== false) {
            graphics.endFill();
        }

        return graphics;
    }

    // Creates a hex shaped polygon that is used for the hex's hit area.
    hp.Map.prototype.createHexPoly = function() {
        var i = 0,
            cs = hp.CoordinateSystems[this.options.coordinateSystem],
            offset = cs.isFlatTop ? 0 : 0.5,
            angle = 2 * Math.PI / 6 * offset,
            center = { x: this.hexAxis.x / 2, y: this.hexAxis.y / 2 },
            x = center.x * Math.cos(angle),
            y = center.y * Math.sin(angle),
            points = [];

        points.push(new pixi.Point(x, y));

        for (i = 1; i < 7; i++) {
            angle = 2 * Math.PI / 6 * (i + offset);
            x = center.x * Math.cos(angle);
            y = center.y * Math.sin(angle);

            points.push(new pixi.Point(x, y));
        }

        //console.log(points);

        return new pixi.Polygon(points);
    }




    // Resets the entire map without destroying the hexPixi.Map instance.
    hp.Map.prototype.reset = function (options) {
        while (this.cells.length > 0) {
            while (this.cells[0].length > 0) {
                this.cells[0].splice(0, 1);
            }
            this.cells.splice(0, 1);
        }

        this.clearHexes();

        while (this.container.children.length > 0) {
            this.container.removeChildAt(0);
        }

        this.pixiStage.removeChild(this.container);

        if (this.cellHighlighter) {
            this.cellHighlighter = null;
        }

        this._init(options, pixi);
    };

    // Clears the scene graph and recreates it from this.cells.
    // TODO Make this a standalone class
    hp.Map.prototype.createSceneGraph = function() {
        var self = this;

        function onHexMouseOver(data) {
            var cell = data.target.p_cell;
            self.cellHighlighter.position.x = cell.center.x;
            self.cellHighlighter.position.y = cell.center.y;

            if (self.inCellCount == 0) {
                self.hexes.addChild(self.cellHighlighter);
            }

            if (cell.isOver !== true) {
                cell.isOver = true;
                self.inCellCount++;
            }
        };

        function onHexMouseOut(data) {
            var cell = data.target.p_cell;
            if (cell.isOver === true) {
                self.inCellCount--;

                if (self.inCellCount == 0) {
                    self.hexes.removeChild(self.cellHighlighter);
                }

                cell.isOver = false;
            }
        };

        // Use for creating a hex cell with a textured background. First creates a PIXI.Graphics of the hex shape.
        // Next creates a PIXI.Sprite and uses the PIXI.Graphics hex as a mask. Masked PIXI.Sprite is added to parent
        // PIXI.Container. Hex outline (if there is one) is created and added to parent container.
        // Parent container is returned.
        function createTexturedHex(cell) {
            var sprite = new pixi.Sprite(self.textures[self.options.terrainTypes[cell.terrainIndex].textureIndex]),
                cs = hp.CoordinateSystems[self.options.coordinateSystem],
                parentContainer = new pixi.Container(),
                mask = null;

            // Get the display object for the hex shape
            mask = self.createDrawHex_internal(cell, false, true);

            sprite.anchor.x = 0.5;
            sprite.anchor.y = 0.5;
            sprite.width = self.options.hexWidth;
            sprite.height = self.options.hexHeight;
            parentContainer.addChild(mask);
            sprite.mask = mask;
            parentContainer.addChild(sprite);

            cell.inner = sprite;

            if (self.options.hexLineWidth > 0) {
                cell.outline = self.createDrawHex_internal(cell, true, false);
                parentContainer.addChild(cell.outline);
            }

            parentContainer.position.x = cell.center.x;
            parentContainer.position.y = cell.center.y;

            return parentContainer;
        };

        // Use for creating a hex cell with a textured background that stands on it's own. The hex outline will
        // bee added if options.hexLineWidth is greater than 0. Parent container is returned.
        function createTileHex(cell) {
            var sprite = new pixi.Sprite(self.textures[self.options.terrainTypes[cell.terrainIndex].tileIndex]),
                cs = hp.CoordinateSystems[self.options.coordinateSystem],
                parentContainer = new pixi.Container(),
                mask = null,
                topPercent = 0.5;

            sprite.width = self.options.hexWidth;
            sprite.height = self.options.hexHeight + self.options.hexBottomPad;

            topPercent = self.options.hexHeight / sprite.height;
            sprite.anchor.x = 0.5;
            sprite.anchor.y = topPercent / 2;

            parentContainer.addChild(sprite);

            cell.inner = sprite;

            if (self.options.hexLineWidth > 0) {
                cell.outline = self.createDrawHex_internal(cell, true, false);
                parentContainer.addChild(cell.outline);
            }

            parentContainer.position.x = cell.center.x;
            parentContainer.position.y = cell.center.y;

            return parentContainer;
        };

        function createEmptyHex(cell) {
            var parentContainer = new pixi.Container();

            cell.inner = null;

            if (self.options.hexLineWidth > 0) {
                cell.outline = self.createDrawHex_internal(cell, true, false);
                parentContainer.addChild(cell.outline);
            }

            parentContainer.position.x = cell.center.x;
            parentContainer.position.y = cell.center.y;

            return parentContainer;
        };

        // Takes a cell and creates all the graphics to display it.
        function createCell(cell) {
            // Calculate the center of a cell based on column, row and coordinate system.
            function getCellCenter(column, row, coordinateSystem) {
                var incX = 0.75 * self.options.hexWidth,
                    incY = self.options.hexHeight,
                    cs = hp.CoordinateSystems[coordinateSystem],
                    center = { x: 0, y: 0 },
                    offset = (cs.isOdd) ? 0 : 1;

                if (cs.isFlatTop) {
                    center.x = (column * incX) + (self.options.hexWidth / 2);
                    if ((column + offset) % 2) {
                        // even
                        center.y = (row * incY) + (incY / 2);
                    } else {
                        // odd
                        center.y = (row * incY) + incY;
                    }
                } else {
                    incX = self.options.hexWidth;
                    incY = (0.75 * self.options.hexHeight);
                    center.y = (row * incY) + (self.options.hexHeight / 2);
                    offset = (cs.isOdd) ? 1 : 0;
                    if ((row + offset) % 2) {
                        // even
                        center.x = (column * incX) + (self.options.hexWidth / 2);
                    } else {
                        // odd
                        center.x = (column * incX) + self.options.hexWidth;
                    }
                }

                //center.y -= self.options.hexBottomPad;

                return center;
            };

            // CD: Use pre-calculated cell center
            cell.center = cell.center || getCellCenter(cell.column, cell.row, self.options.coordinateSystem);

            // Generate poly first then use poly to draw hex and create masks and all that.
            cell.poly = self.createHexPoly();

            if (self.options.showCoordinates) {
                cell.text = new pixi.Text("1", { font: "10px Arial", fill: "black", dropShadow: "true", dropShadowDistance: 1, dropShadowColor: "white" });
                // CD: Use overrideable method for coordinate text
                cell.text.text = cell.coordinateText();
                cell.text.position.x = -Math.round((cell.text.width / 2));
                cell.text.position.y = 8 - Math.round(self.options.hexHeight / 2);
            }

            // Create the hex or textured hex
            var hex = null;

            /* TODO replace use of terrain types cleanly
            if (self.options.terrainTypes[cell.terrainIndex].isEmpty === true) {
                hex = createEmptyHex(cell);
            } else if (self.options.terrainTypes[cell.terrainIndex].textureIndex >= 0) {
                hex = createTexturedHex(cell);
            } else if (self.options.terrainTypes[cell.terrainIndex].tileIndex >= 0) {
                hex = createTileHex(cell);
            } else {
                hex = self.createDrawnHex(cell);
            } */
            hex = self.createDrawnHex(cell);

            // Text is a child of the display object container containing the hex.
            if (self.options.showCoordinates) {
                hex.addChild(cell.text);
            }

            // Set a property on the hex that references the cell.
            hex.p_cell = cell;
            hex.p_cell.hex = hex;

            return hex;
        };

        // A wrapper for createCell that adds interactivity to the individual cells.
        function createInteractiveCell(cell) {
            var hex = createCell(cell);
            hex.hitArea = cell.poly;
            hex.interactive = true;

            // set the mouseover callback..
            hex.mouseover = onHexMouseOver;

            // set the mouseout callback..
            hex.mouseout = onHexMouseOut;

            hex.click = function (data) {
                if (self.options.onHexClick) {
                    self.options.onHexClick(data.target.p_cell, data);
                }
            }.bind(self);

            hex.tap = function (data) {
                if (self.options.onHexClick) {
                    self.options.onHexClick(data.target.p_cell, data);
                }
            }.bind(self);

            return hex;
        };

        var cell = null,
            row = null,
            rowIndex = 0,
            colIndex = 0;

        self.clearHexes();
        while (rowIndex < self.cells.length) {
            row = self.cells[rowIndex];
            colIndex = 0;
            while (colIndex < row.length) {
                cell = row[colIndex];
                self.hexes.addChild(createInteractiveCell.call(self, cell));
                colIndex++;
            }
            rowIndex++;
        }
    }

    hp.Map.prototype.importMap = function (exportedMap) {
        var newOptions = {
            onHexClick: this.options.onHexClick, // Preserve the hex click callback
            mapHeight: exportedMap.mapHeight,
            mapWidth: exportedMap.mapWidth,
            coordinateSystem: exportedMap.coordinateSystem,
            hexLineWidth: exportedMap.hexLineWidth,
            hexLineColor: exportedMap.hexLineColor,
            hexWidth: exportedMap.hexWidth,
            hexHeight: exportedMap.hexHeight,
            hexBottomPad: exportedMap.hexBottomPad,
            showCoordinates: exportedMap.showCoordinates,
            textures: JSON.parse(exportedMap.textures),
            terrainTypes: JSON.parse(exportedMap.terrainTypes)
        };

        this.reset(newOptions);

        exportedMap.cells = JSON.parse(exportedMap.cells);

        for (var row = 0; row < this.options.mapHeight; row++) {
            this.cells.push([]);
            for (var column = 0; column < this.options.mapWidth; column += 2) {
                var exportedCell = exportedMap.cells[row][column];
                var cell = new hp.Cell(exportedCell.row, exportedCell.column, exportedCell.terrainIndex);
                this.cells[cell.row].push(cell);
            }
            for (var column = 1; column < this.options.mapWidth; column += 2) {
                var exportedCell = exportedMap.cells[row][column];
                var cell = new hp.Cell(exportedCell.row, exportedCell.column, exportedCell.terrainIndex);
                this.cells[cell.row].push(cell);
            }
        }

        this.createSceneGraph();
    };

    hp.Map.prototype.exportMap = function () {
        var result = {
            mapHeight: this.options.mapHeight,
            mapWidth: this.options.mapWidth,
            coordinateSystem: this.options.coordinateSystem,
            hexLineWidth: this.options.hexLineWidth,
            hexLineColor: this.options.hexLineColor,
            hexWidth: this.options.hexWidth,
            hexHeight: this.options.hexHeight,
            hexBottomPad: this.options.hexBottomPad,
            showCoordinates: this.options.showCoordinates,
            textures: JSON.stringify(this.options.textures),
            terrainTypes: JSON.stringify(this.options.terrainTypes),
            cells: JSON.stringify(this.cells)
        };
        return result;
    };

    hp.Map.prototype.generateRandomMap = function () {
        for (var row = 0; row < this.options.mapHeight; row++) {
            this.cells.push([]);
            for (var column = 0; column < this.options.mapWidth; column += 2) {
                var rnd = Math.floor((Math.random() * this.options.terrainTypes.length));
                var cell = new hp.Cell(row, column, rnd);
                this.cells[cell.row].push(cell);
            }
            for (var column = 1; column < this.options.mapWidth; column += 2) {
                var rnd = Math.floor((Math.random() * this.options.terrainTypes.length));
                var cell = new hp.Cell(row, column, rnd);
                this.cells[cell.row].push(cell);
            }
        }
        this.createSceneGraph();
    };

    hp.Map.prototype.generateBlankMap = function () {
        for (var row = 0; row < this.options.mapHeight; row++) {
            this.cells.push([]);
            for (var column = 0; column < this.options.mapWidth; column += 2) {
                var cell = new hp.Cell(row, column, 0);
                this.cells[cell.row].push(cell);
            }
            for (var column = 1; column < this.options.mapWidth; column += 2) {
                var cell = new hp.Cell(row, column, 0);
                this.cells[cell.row].push(cell);
            }
        }
        this.createSceneGraph();
    };

    hp.init = function (pixi) {
        pixi.Graphics.prototype.updateLineStyle = function (lineWidth, color, alpha) {
            var len = this.graphicsData.length;
            for (var i = 0; i < len; i++) {
                var data = this.graphicsData[i];
                if (data.lineWidth && lineWidth) {
                    data.lineWidth = lineWidth;
                }
                if (data.lineColor && color) {
                    data.lineColor = color;
                }
                if (data.alpha && alpha) {
                    data.alpha = alpha;
                }
                this.dirty = true;
                this.clearDirty = true;
            }
        };

        pixi.Graphics.prototype.updateFillColor = function (fillColor, alpha) {
            var len = this.graphicsData.length;
            for (var i = 0; i < len; i++) {
                var data = this.graphicsData[i];
                if (data.fillColor && fillColor) {
                    data.fillColor = fillColor;
                }
                if (data.alpha && alpha) {
                    data.alpha = alpha;
                }
                this.dirty = true;
                this.clearDirty = true;
            }
        };
    };

    return hp;
});

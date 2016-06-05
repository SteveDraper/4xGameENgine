﻿/// <reference path="../examples/shared/js/dep/pixi.dev.js" />
/* 
    HexPixi (alpha)
    Version 0.41
    by Mark Harmon 2015
    A free hex game library for pixijs.
    Released under MIT License.
    Please let me know about any games released using this library or derivative work.
*/

(function(root, factory) {
    if (typeof define == 'function' && define.amd) {
        define(['pixi'], factory);
    } else if (typeof exports == 'object') {
        module.exports = factory(require('pixi'));
    } else {
        root.hexPixi = factory(root.PIXI);
    }
}(this, function(pixi) {
    'use strict';

    var hp = {};

    // There are four basic coordinate systems based on http://www.redblobgames.com/grids/hexagons/
    hp.CoordinateSystems = [
        { name: "odd-q", isFlatTop: true, isOdd: true },
        { name: "even-q", isFlatTop: true, isOdd: false },
        { name: "odd-r", isFlatTop: false, isOdd: true },
        { name: "even-r", isFlatTop: false, isOdd: false }];

    hp.Camera = function (amap) {
        var self = this,
            position = { x: 0, y: 0 },
            map = amap;

        function updateSceneGraph() {
            map.container.position.x = position.x;
            map.container.position.y = position.y;
        }

        self.position = function (x, y) {
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
        var self = this;
        self.type = type;
        self.name = name;
        self.onUpdate = onUpdate;
        self.cell = null;
        self.isVisible = false;
        self.properties = properties;
    };

    // The hexPixi.Cell object represents one map hex cell.
    hp.Cell = function (rowNo, columnNo, terrainIndex) {
        var self = this;
        self.row = rowNo;
        self.column = columnNo;
        self.center = { x: 0, y: 0 };
        self.terrainIndex = terrainIndex ? terrainIndex : 0;
        self.poly = null; // The cell's poly that is used as a hit area.
        self.outline = null; // The PIXI.Graphics outline of the cell's hex.
        self.inner = null; // If a non-textured cell then this is the PIXI.Graphics of the hex inner, otherwise a PIXI.Sprite.
        self.hex = null; // The parent container of the hex's graphics objects.
        self.isEmpty = null; // The cell is empty if set to true.
        self.gameObjects = []; // A variable for attaching GameObjects you want (items, people, descriptions).

        self.resetGraphics = function () {
            self.terrainIndex = terrainIndex ? terrainIndex : 0;
            self.poly = null; // The cell's poly that is used as a hit area.
            self.outline = null; // The PIXI.Graphics outline of the cell's hex.
            self.inner = null; // If a non-textured cell then this is the PIXI.Graphics of the hex inner.
            self.hex = null; // The parent container of the hex's graphics objects.
        };

        self.toJSON = function () {
            return {
                row: self.row,
                column: self.column,
                terrainIndex: self.terrainIndex
            };
        };
    };

    // Scene graph heirarchy = pixiState -> container -> hexes
    hp.Map = function (pixiStage, options) {
        var self = this,
            defaultOptions = {
                // The hexPixi.CoordinateSystems index to use for the map.
                coordinateSystem: 1,
                // The map's number of cells across (cell column count).
                mapWidth: 10,
                // The map's number of cells high (cell row count).
                mapHeight: 10,
                // The radius of the hex. Ignored if hexWidth and hexHeight are set to non-null.
                hexSize: 40,
                // The pixel width of a hex.
                hexWidth: null,
                // The pixel height of a hex.
                hexHeight: null,
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

        self.textures = [];
        self.hexes = new pixi.Graphics();
        self.container = new pixi.Container();
        self.pixiStage = null;
        self.options = null;
        self.cells = [];
        self.camera = new hp.Camera(self);
        self.cellHighlighter = null;
        self.inCellCount = 0;
        self.hexAxis = { x: 0, y: 0 };
        self.aspectRatio = 1;


        self.setCellTerrainType = function (cell, terrainIndex) {
            cell.terrainIndex = terrainIndex;
            createSceneGraph();
        };

        // Creates a hex shaped polygon that is used for the hex's hit area.
        function createHexPoly() {
            var i = 0,
                cs = hp.CoordinateSystems[self.options.coordinateSystem],
                offset = cs.isFlatTop ? 0 : 0.5,
                angle = 2 * Math.PI / 6 * offset,
                center = { x: self.hexAxis.x / 2, y: self.hexAxis.y / 2 },
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

        // Creates a drawn hex while ignoring the cell's position. A new PIXI.Graphics object is created
        // and used to draw and (possibly) fill in the hex. The PIXI.Graphics is returned to the caller.
        function createDrawHex_internal(cell, hasOutline, hasFill) {
            var graphics = new pixi.Graphics(),
                i = 0,
                cs = hp.CoordinateSystems[self.options.coordinateSystem],
                color = self.options.terrainTypes[cell.terrainIndex].color ? self.options.terrainTypes[cell.terrainIndex].color : 0xffffff;

            if (cell.poly == null) {
                console.log("Cell's poly must first be defined by calling createHexPoly");
                return null;
            }

            if (hasOutline === false) {
                // If this is for masking then we don't need the line itself. Just the poly filled.
                graphics.lineStyle(0, 0, 1);
            } else {
                graphics.lineStyle(self.options.hexLineWidth, self.options.hexLineColor, 1);
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

        // Used for manually drawing a hex cell. Creates the filled in hex, creates the outline (if there is one)
        // and then wraps them in a PIXI.Container.
        self.createDrawnHex = function (cell) {
            var parentContainer = new pixi.Container();

            cell.inner = createDrawHex_internal(cell, false, true);
            parentContainer.addChild(cell.inner);

            if (self.options.hexLineWidth > 0) {
                cell.outline = createDrawHex_internal(cell, true, false);
                parentContainer.addChild(cell.outline);
            }

            parentContainer.position.x = cell.center.x;
            parentContainer.position.y = cell.center.y;

            return parentContainer;
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
            mask = createDrawHex_internal(cell, false, true);

            sprite.anchor.x = 0.5;
            sprite.anchor.y = 0.5;
            sprite.width = self.options.hexWidth;
            sprite.height = self.options.hexHeight;
            parentContainer.addChild(mask);
            sprite.mask = mask;
            parentContainer.addChild(sprite);

            cell.inner = sprite;

            if (self.options.hexLineWidth > 0) {
                cell.outline = createDrawHex_internal(cell, true, false);
                parentContainer.addChild(cell.outline);
            }

            parentContainer.position.x = cell.center.x;
            parentContainer.position.y = cell.center.y;

            return parentContainer;
        }

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
                cell.outline = createDrawHex_internal(cell, true, false);
                parentContainer.addChild(cell.outline);
            }

            parentContainer.position.x = cell.center.x;
            parentContainer.position.y = cell.center.y;

            return parentContainer;
        }

        function createEmptyHex(cell) {
            var parentContainer = new pixi.Container();

            cell.inner = null;

            if (self.options.hexLineWidth > 0) {
                cell.outline = createDrawHex_internal(cell, true, false);
                parentContainer.addChild(cell.outline);
            }

            parentContainer.position.x = cell.center.x;
            parentContainer.position.y = cell.center.y;

            return parentContainer;
        }

        // Calculates and returns the width of a hex cell.
        function getHexWidth() {
            var result = null,
                cs = hp.CoordinateSystems[self.options.coordinateSystem];
            result = self.options.hexSize * 2;
            if (cs.isFlatTop == false) {
                result = Math.sqrt(3) / 2 * result;
            }

            return result;
        }

        // Calculates and returns the height of a hex cell.
        function getHexHeight() {
            var result = null,
                cs = hp.CoordinateSystems[self.options.coordinateSystem];
            result = self.options.hexSize * 2;
            if (cs.isFlatTop == true) {
                result = Math.sqrt(3) / 2 * result;
            }

            return result;
        }

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
        }

        // Takes a cell and creates all the graphics to display it.
        function createCell(cell) {
            cell.center = getCellCenter(cell.column, cell.row, self.options.coordinateSystem);

            // Generate poly first then use poly to draw hex and create masks and all that.
            cell.poly = createHexPoly();

            if (self.options.showCoordinates) {
                cell.text = new pixi.Text("1", { font: "10px Arial", fill: "black", dropShadow: "true", dropShadowDistance: 1, dropShadowColor: "white" });
                cell.text.text = cell.column.toString() + ", " + cell.row.toString();
                cell.text.position.x = -Math.round((cell.text.width / 2));
                cell.text.position.y = 8 - Math.round(self.options.hexHeight / 2);
            }

            // Create the hex or textured hex
            var hex = null;
            if (self.options.terrainTypes[cell.terrainIndex].isEmpty === true) {
                hex = createEmptyHex(cell);
            } else if (self.options.terrainTypes[cell.terrainIndex].textureIndex >= 0) {
                hex = createTexturedHex(cell);
            } else if (self.options.terrainTypes[cell.terrainIndex].tileIndex >= 0) {
                hex = createTileHex(cell);
            } else {
                hex = self.createDrawnHex(cell);
            }

            // Text is a child of the display object container containing the hex.
            if (self.options.showCoordinates) {
                hex.addChild(cell.text);
            }

            // Set a property on the hex that references the cell.
            hex.p_cell = cell;
            hex.p_cell.hex = hex;

            return hex;
        }

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
        }

        function onHexMouseOut(data) {
            var cell = data.target.p_cell;
            if (cell.isOver === true) {
                self.inCellCount--;

                if (self.inCellCount == 0) {
                    self.hexes.removeChild(self.cellHighlighter);
                }

                cell.isOver = false;
            }
        }

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
            }

            hex.tap = function (data) {
                if (self.options.onHexClick) {
                    self.options.onHexClick(data.target.p_cell, data);
                }
            }

            return hex;
        }

        // Loads all the textures specified in options.
        function loadTextures() {
            self.textures = [];

            if (self.options.textures.length) {
                var loader = PIXI.loader;
                loader = new PIXI.loaders.Loader();

        loader.add(self.options.textures);

                // use callback
                loader.once('complete', self.options.onAssetsLoaded);

                //begin load
                loader.load();

                for (var i = 0; i < self.options.textures.length; i++) {
                    self.textures.push(new pixi.Texture.fromImage(self.options.textures[i]));
                }
            } else {
                // No assets to load so just call onAssetsLoaded function to notify game that we are done.
                self.options.onAssetsLoaded && self.options.onAssetsLoaded();
            }
        }

        // Clears out all objects from self.hexes.children.
        function clearHexes() {
            while (self.hexes.children.length) {
                self.hexes.removeChild(self.hexes.children[0]);
            }
        }

        // Resets the entire map without destroying the hexPixi.Map instance.
        self.reset = function (options) {
            while (self.cells.length > 0) {
                while (self.cells[0].length > 0) {
                    self.cells[0].splice(0, 1);
                }
                self.cells.splice(0, 1);
            }

            clearHexes();

            while (self.container.children.length > 0) {
                self.container.removeChildAt(0);
            }

            self.pixiStage.removeChild(self.container);

            if (self.cellHighlighter) {
                self.cellHighlighter = null;
            }

            _init(options, pixi);
        };

        // Clears the scene graph and recreates it from self.cells.
        function createSceneGraph() {
            var cell = null,
                row = null,
                rowIndex = 0,
                colIndex = 0;

            clearHexes();
            while (rowIndex < self.cells.length) {
                row = self.cells[rowIndex];
                colIndex = 0;
                while (colIndex < row.length) {
                    cell = row[colIndex];
                    self.hexes.addChild(createInteractiveCell(cell));
                    colIndex++;
                }
                rowIndex++;
            }
        }

        self.importMap = function (exportedMap) {
            var newOptions = {
                onHexClick: self.options.onHexClick, // Preserve the hex click callback
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

            self.reset(newOptions);

            exportedMap.cells = JSON.parse(exportedMap.cells);

            for (var row = 0; row < self.options.mapHeight; row++) {
                self.cells.push([]);
                for (var column = 0; column < self.options.mapWidth; column += 2) {
                    var exportedCell = exportedMap.cells[row][column];
                    var cell = new hp.Cell(exportedCell.row, exportedCell.column, exportedCell.terrainIndex);
                    self.cells[cell.row].push(cell);
                }
                for (var column = 1; column < self.options.mapWidth; column += 2) {
                    var exportedCell = exportedMap.cells[row][column];
                    var cell = new hp.Cell(exportedCell.row, exportedCell.column, exportedCell.terrainIndex);
                    self.cells[cell.row].push(cell);
                }
            }

            createSceneGraph();
        };

        self.exportMap = function () {
            var result = {
                mapHeight: self.options.mapHeight,
                mapWidth: self.options.mapWidth,
                coordinateSystem: self.options.coordinateSystem,
                hexLineWidth: self.options.hexLineWidth,
                hexLineColor: self.options.hexLineColor,
                hexWidth: self.options.hexWidth,
                hexHeight: self.options.hexHeight,
                hexBottomPad: self.options.hexBottomPad,
                showCoordinates: self.options.showCoordinates,
                textures: JSON.stringify(self.options.textures),
                terrainTypes: JSON.stringify(self.options.terrainTypes),
                cells: JSON.stringify(self.cells)
            };
            return result;
        };

        self.generateRandomMap = function () {
            for (var row = 0; row < self.options.mapHeight; row++) {
                self.cells.push([]);
                for (var column = 0; column < self.options.mapWidth; column += 2) {
                    var rnd = Math.floor((Math.random() * self.options.terrainTypes.length));
                    var cell = new hp.Cell(row, column, rnd);
                    self.cells[cell.row].push(cell);
                }
                for (var column = 1; column < self.options.mapWidth; column += 2) {
                    var rnd = Math.floor((Math.random() * self.options.terrainTypes.length));
                    var cell = new hp.Cell(row, column, rnd);
                    self.cells[cell.row].push(cell);
                }
            }
            createSceneGraph();
        };

        self.generateBlankMap = function () {
            for (var row = 0; row < self.options.mapHeight; row++) {
                self.cells.push([]);
                for (var column = 0; column < self.options.mapWidth; column += 2) {
                    var cell = new hp.Cell(row, column, 0);
                    self.cells[cell.row].push(cell);
                }
                for (var column = 1; column < self.options.mapWidth; column += 2) {
                    var cell = new hp.Cell(row, column, 0);
                    self.cells[cell.row].push(cell);
                }
            }
            createSceneGraph();
        };

        function extend(obj) {
            Array.prototype.slice.call(arguments, 1).forEach(function (source) {
                if (source) {
                    for (var prop in source) {
                        if (source[prop].constructor === Object) {
                            if (!obj[prop] || obj[prop].constructor === Object) {
                                obj[prop] = obj[prop] || {};
                                extend(obj[prop], source[prop]);
                            } else {
                                obj[prop] = source[prop];
                            }
                        } else {
                            obj[prop] = source[prop];
                        }
                    }
                }
            });
            return obj;
        }

        function _init(options, pixi) {
            self.options = extend(defaultOptions, options);

            hp.init(pixi);

            // If we are overiding the top-down view method then need to force some settings
            if (self.options.hexWidth && self.options.hexHeight) {
                var cs = hp.CoordinateSystems[self.options.coordinateSystem];
                self.options.hexSize = self.options.hexWidth / 2;
                self.aspectRatio = self.options.hexHeight / self.options.hexWidth;
                self.hexAxis.x = cs.isFlatTop ? self.options.hexWidth : ((1 - (Math.sqrt(3) / 2)) * self.options.hexWidth) + self.options.hexWidth;
                self.hexAxis.y = cs.isFlatTop ? ((1 - (Math.sqrt(3) / 2)) * self.options.hexHeight) + self.options.hexHeight : self.options.hexHeight;
            } else {
                self.aspectRatio = 1;
                self.options.hexWidth = getHexWidth();
                self.options.hexHeight = getHexHeight();
                self.hexAxis.x = self.options.hexSize * 2;
                self.hexAxis.y = self.options.hexSize * 2;
            }

            if (self.pixiStage == null) {
                self.pixiStage = pixiStage;
            }

            self.container.addChild(self.hexes);
            self.pixiStage.addChild(self.container);
            self.hexes.clear();
            loadTextures();

            // Setup cell hilighter
            var cell = new hp.Cell(0, 0, 0);
            cell.poly = createHexPoly();
            var chg = createDrawHex_internal(cell, true, false);
            if (chg) {
                chg.updateLineStyle(3, 0xff5521);
                self.cellHighlighter = new pixi.Container();
                self.cellHighlighter.addChild(chg);
            } else {
                console.log("Error creating cell hilighter");
            }
        }

        _init(options, pixi);
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
}));

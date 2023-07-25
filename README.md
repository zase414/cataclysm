# cataclysm
a work-in-progress OpenGL pseudo-3D raycasting-based game (engine) written in Java

## version history
#### v0.1.0:
rendering of individual rectangles assigned to rays\
![](https://github.com/zase414/assets/blob/main/1.gif)
#### v0.2.0:
minimap scene added\
![](https://github.com/zase414/assets/blob/main/2.gif)
#### v0.3.0:
improved minimap and optimized rendering that only renders one trapezoid per wall segment\
![](https://github.com/zase414/assets/blob/main/3.gif)
#### v0.3.1
minimap only renders the parts which have been revealed by the player\
![](https://github.com/zase414/assets/blob/main/5.gif)
#### v0.4.0
enabled movement along the z-axis, improved the engine to allow multi-layer rendering\
![](https://github.com/zase414/assets/blob/main/6.gif)
#### v0.4.1
added the ability to walk on top of walls\
![](https://github.com/zase414/assets/blob/main/7.gif)
#### v0.5.0
added option to convert PGF/TikZ files (GeoGebra export option) to in-game maps
#### v0.5.1 
menu screen improved, here you can select a map to play from the "maps" folder

## dependencies
LWJGL 3.3.2\
JOML 1.10.5\
GSON 2.10.1

## planned features
+ texture support
+ in-game map editor

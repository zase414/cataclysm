<h1><code>BETA</code> Cataclysm</h1>
<b>a work-in-progress OpenGL pseudo-3D raycasting-based game (engine) written in Java</b>
 



<h2>Controls</h2>
  <ul>
    <li><code>LAlt</code> to show cursor</li>
  </ul>
  
  <h3>Menu</h3>
  <ul>
    <li><code>Tab</code> to cycle maps</li>
    <li><code>Enter</code> to convert a PGF/TikZ into a map (will be saved into assets/maps)</li>
    <li><code>Spacebar</code> to load the map</li>
  </ul>
  <h3>Movement</h3>
  <ul>
    <li><code>W</code><code>A</code><code>S</code><code>D</code> to walk</li>
    <li><code>Shift</code> to sprint</li>
    <li><code>Spacebar</code> to jump</li>
  </ul>
  <h3>Map</h3>
  <ul>
    <li><code>Tab</code> to toggle</li>
    <li><code>LAlt</code> to interrupt player tracking</li>
    <li><code>LAlt</code> + <code>MOUSE2</code> drag to draw a wall on the map</li>
    <li><code>MWheel</code> to zoom</li>
  </ul>




<h2>Dependencies</h2><ul>
  <li><a href="https://www.lwjgl.org/download">LWJGL 3.3.2</a></li>
  <li><a href="https://github.com/JOML-CI/JOML">JOML 1.10.5</a></li>
  <li><a href="https://github.com/google/gson">GSON 2.10.1</a></li>
</ul>
  


<h2>Planned features</h2>
<ul>
  <li>texture support</li>
  <li>procedural map generation</li>
</ul>



 <h2>Version history</h2>
  
  <h3>v0.1.0:</h3>
    <ul><li>rendering of individual rectangles assigned to rays</li></ul>
    <p><img src="https://github.com/zase414/assets/blob/main/1.gif" style="width:360px;height:200px;"></p>
  
  
  <h3>v0.2.0:</h3>
    <ul><li>minimap scene added</li></ul>
    <img src="https://github.com/zase414/assets/blob/main/2.gif" style="width:360px;height:200px;">
  
  
  <h3>v0.3.0:</h3>
    <ul><li>improved minimap and optimized rendering that only renders one trapezoid per wall segment</li></ul>
    <img src="https://github.com/zase414/assets/blob/main/3.gif" style="width:360px;height:200px;">
  
  
  <h3>v0.3.1:</h3>
    <ul><li>minimap only renders the parts which have been revealed by the player</li></ul>
    <img src="https://github.com/zase414/assets/blob/main/4.gif" style="width:360px;height:200px;">
  
  
 <h3>v0.4.0:</h3>
    <ul><li>enabled movement along the z-axis, improved the engine to allow multi-layer rendering</li></ul>
    <img src="https://github.com/zase414/assets/blob/main/6.gif" style="width:360px;height:200px;">
  
  
  <h3>v0.4.1:</h3>
    <ul><li>added the ability to walk on top of walls</li></ul>
    <img src="https://github.com/zase414/assets/blob/main/7.gif" style="width:360px;height:200px;">
  
  
  <h3>v0.5.0:</h3>
    <ul><li>added option to convert PGF/TikZ files (GeoGebra export option) to in-game maps</li></ul>
  
  
   <h3>v0.5.1:</h3>
    <ul><li>menu screen improved, here you can select a map to play from the "maps" folder</li></ul>
  

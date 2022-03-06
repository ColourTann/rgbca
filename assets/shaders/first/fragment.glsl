#ifdef GL_ES
precision mediump float;
#endif 
 
// varying input variables from our vertex shader
varying vec4 v_color;
varying vec2 v_texCoords;

uniform float u_t;

uniform vec2 u_mloc;
uniform vec2 u_screen;
 
// a special uniform for textures 
uniform sampler2D u_texture;
 
void main()
{
  // set the colour for this fragment|pixel
  vec2 locPos = gl_FragCoord/u_screen;
  vec2 dist = 1.-abs(locPos - u_mloc);
  float totalDist = length(dist);
  vec4 col = vec4(locPos.x,locPos.y,totalDist*.5,1);
  gl_FragColor = col 
  / sqrt(dist.x * dist.y) 
  * pow(totalDist, 5.)
  * texture2D(u_texture, v_texCoords);
}
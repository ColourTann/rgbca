#ifdef GL_ES
precision mediump float;
#endif 
 
// varying input variables from our vertex shader
varying vec4 v_color;
varying vec2 v_texCoords;

uniform float u_t;

uniform float u_ml;
uniform float u_mr;
uniform vec2 u_mloc;
uniform vec2 u_screen;
 
// a special uniform for textures 
uniform sampler2D u_texture;
 
void main()
{

  vec2 locPos = gl_FragCoord/u_screen;
  vec2 dist = locPos - u_mloc;
  float totalDist = length(dist);
  vec3 col = texture2D(u_texture, v_texCoords).xyz;
  if(totalDist<.02) {
    col.r += .4 * u_ml;
  }
  gl_FragColor = vec4(col,1.);
  if(u_mr > 0) {
    gl_FragColor = vec4(0.,0.,0.,1.);
  }
}
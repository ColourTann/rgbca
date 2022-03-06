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
  vec2 caLoc = vec2(floor(gl_FragCoord.x), floor(gl_FragCoord.y));
  caLoc = v_texCoords;
  vec2 onePixel = vec2(1.0, 1.0) / u_screen;
  int me = 0;
  int total = 0;
  for(int y=-1;y<=1;y++) {
    for(int x=-1;x<=1;x++) {
    float val = texture2D(u_texture, caLoc+onePixel*vec2(x,y));
      if(x==0&&y==0) {
        me = val > 0 ? 1 : 0;
        continue;
      }
      if(val > .5) {
        total++;
      }
    }
  }

  float newVal = 0.;
  if(me == 1) {
    if(total == 2 || total == 3) {
      newVal = 1.;
    } else {
      newVal = 0.;
    }
  } else {
   if(total == 3) {
      newVal = 1.;
    } else {
      newVal = 0.;
    }
  }


  vec3 col = vec3(newVal, newVal, newVal);

  vec2 locPos = gl_FragCoord/u_screen;
  vec2 dist = locPos - u_mloc;
  float totalDist = length(dist);
  if(totalDist<.01 && u_ml>0) {
    col.rgb = 1.;
  }
  if(u_mr > 0) {
    col = vec3(0.,0.,0.);
  }
  gl_FragColor = vec4(col, 1.);
}
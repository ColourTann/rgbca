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
 
bool isClick() {
  vec2 locPos = gl_FragCoord/u_screen;
  vec2 dist = locPos - u_mloc;
  float totalDist = length(dist);
  return totalDist<.01 && u_ml>0;
}

bool isClear() {
  return u_mr > 0;
}

int getTotal() {
  int total = 0;
  vec2 onePixel = vec2(1.0, 1.0) / u_screen;
  for(int y=-1;y<=1;y++) {
    for(int x=-1;x<=1;x++) {
    float val = texture2D(u_texture, v_texCoords+onePixel*vec2(x,y));
    if(x==0&&y==0) {
      continue;
    }
    if(val > .5) {
      total++;
    }
    }
  }
  return total;
}

vec3 getAverage() {
  vec3 total = vec3(0.,0.,0.);
  vec2 onePixel = vec2(1.0, 1.0) / u_screen;
  for(int y=-1;y<=1;y++) {
    for(int x=-1;x<=1;x++) {
      vec3 val = texture2D(u_texture, v_texCoords+onePixel*vec2(x,y));
      if(x==0&&y==0) {
        continue;
      }
      total += val;
    }
  }
  return total/8.;
}

int getNumWithin(vec3 me, float epsilon) {
  int total = 0;
  vec2 onePixel = vec2(1.0, 1.0) / u_screen;
  for(int y=-1;y<=1;y++) {
    for(int x=-1;x<=1;x++) {
      vec3 val = texture2D(u_texture, v_texCoords+onePixel*vec2(x,y));
      if(x==0&&y==0) {
        continue;
      }
      if(length(val-me) < epsilon) {
        total++;
      }
    }
  }
  return total;
}

void main()
{
  vec3 me = texture2D(u_texture, v_texCoords).xyz;

  vec3 newVal = me;
  int within = getNumWithin(me, .5f);
  vec3 avg = getAverage();

  float avgLn = length(avg);

  if(length(me) < 0.7 && length(me) > 0.3 && false) {
    newVal.r = length(avg)-length(me);
    newVal.g = abs(avg.r-me.g);
    newVal.b = me.b/avg.b;
  } else {
    newVal.r = length(avg);
    newVal.g = abs(avg.r - me.g);
    newVal.b = avg.g-avg.r;
  }

  vec3 col = newVal;
  if(isClick()) {
    col.rgb = vec3(mod(u_t,1.),mod(u_t*1.1,1.),mod(u_t*1.2,1.));
  }
  if(isClear()) {
    col = vec3(0.,0.,0.);
  }
  gl_FragColor = vec4(col, 1.);
}


#ifdef GL_ES
precision mediump float;
#endif 
 
// varying input variables from our vertex shader
varying vec4 v_color;
varying vec2 v_texCoords;

uniform float u_t;

uniform float u_ml;
uniform float u_mr;
uniform float u_mm;
uniform vec2 u_mloc;
uniform vec2 u_screen;
 
// a special uniform for textures 
uniform sampler2D u_texture;
 
bool isClick() {
  vec2 locPos = gl_FragCoord/u_screen;
  vec2 dist = locPos - u_mloc;
  float totalDist = length(dist);
  return totalDist<.0135 && u_ml>0;
}

bool isClear() {
  return u_mr > 0;
}

vec2 onePixel() {
  return (vec2(1.0, 1.0) / u_screen);
}

int getTotal(float threshold) {
  int total = 0;
  vec2 onePixel = onePixel();
  for(int y=-1;y<=1;y++) {
    for(int x=-1;x<=1;x++) {
      if(x==0&&y==0) {
        continue;
      }
      vec3 val = texture2D(u_texture, v_texCoords+onePixel*vec2(x,y));
      if(length(val) > threshold) {
        total++;
      }
    }
  }
  return total;
}

vec3 getAverage(int dist) {
  vec3 total = vec3(0.,0.,0.);
  vec2 onePixel = onePixel();
  for(float y=-dist;y<=dist;y++) {
    for(float x=-dist;x<=dist;x++) {
      if(x==0&&y==0) {
        continue;
      }
      vec3 val = texture2D(u_texture, v_texCoords+onePixel*vec2(x,y));
      total += val;
    }
  }
  return total/8.;
}

int getNumWithin(vec3 me, float epsilon) {
  int total = 0;
  vec2 onePixel = onePixel();
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

bool isWithin(float val, float target, float epsilon) {
  return abs(val-target)<=epsilon;
}

vec3 rgb2hsv(vec3 c)
{
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

float rand(vec2 co){
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}

vec3 randC(vec2 co) {
  return vec3(rand(co), rand(co+1.), rand(co+2.));
}

void main() {
  vec3 me = texture2D(u_texture, v_texCoords).xyz;
  int total = getTotal(.5);
  bool living = length(me) > .5;
  bool newLiving = false;
  if(living) {
    newLiving = total == 2 || total == 3;
  } else {
    newLiving = total == 3;
  }

  float incDelta = .15;
  float decDelta = .3;
  float colValue = newLiving ? me.x+incDelta : me.x-decDelta;
  vec3 col = vec3(colValue);
  if(isClick()) {
    col = randC(v_texCoords);
  }
  if(isClear()) {
    col = vec3(0.,0.,0.);
  }
  if(u_mm) {
    vec2 relPos = gl_FragCoord/u_screen;
    if(isWithin(relPos.y, 0.5, 0.91)) {
      col = randC(v_texCoords);
    }
  }
  gl_FragColor = vec4(col, 1.);

}


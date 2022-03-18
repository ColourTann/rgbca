#ifdef GL_ES
precision mediump float;
#endif 
 
// varying input variables from our vertex shader
varying vec4 v_color;
varying vec2 v_texCoords;

uniform float u_t;
uniform int u_seed;
uniform int u_randomise;
uniform float u_ml;
uniform float u_mult;
uniform float u_mr;
uniform vec2 u_mloc;
uniform vec2 u_screen;
 
// a special uniform for textures 
uniform sampler2D u_texture;
 
vec2 onePixel() {
  return (vec2(1.00, 1.00) / u_screen);
}

vec3 getRelative(ivec2 delta) {
  return texelFetch(u_texture, ivec2(v_texCoords*u_screen)+delta, 0);
}

bool isClick() {
  vec2 locPos = gl_FragCoord/u_screen;
  vec2 dist = locPos - u_mloc;
  float totalDist = length(dist);
  return totalDist<.022 && u_ml>0;
}

bool isClear() {
  return u_mr > 0;
}

vec3 getAverage(int dist) {
  vec3 total = vec3(0.,0.,0.);
  for(float y=-dist;y<=dist;y++) {
    for(float x=-dist;x<=dist;x++) {
      if(x==0&&y==0) {
        continue;
      }
      vec3 val = getRelative(ivec2(x,y));
      total += val;
    }
  }
  return total/8.;
}

vec3 getAverageExactDist(int dist) {
  vec3 total = vec3(0.,0.,0.);
  for(float y=-dist;y<=dist;y++) {
    for(float x=-dist;x<=dist;x++) {
      if(abs(x)+abs(y) != dist) {
        continue;
      }
      vec3 val = getRelative(ivec2(x,y));
      total += val;
    }
  }
  return total/8.;
}

vec3 getMax(int dist) {
  vec3 maxes = vec3(0.,0.,0.);
  for(float y=-dist;y<=dist;y++) {
    for(float x=-dist;x<=dist;x++) {
      if(x==0&&y==0) {
        continue;
      }
      vec3 val = getRelative(ivec2(x,y));
      maxes = max(maxes, val);
    }
  }
  return maxes;
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

int hexDist(int dx, int dy) {
  return max(abs(dx), max(abs(dy), abs(dx+                                                                                                                                             dy)));
}

vec3 avgExactDistHex(int dist) {
  int amt = 0;
  vec3 total = vec3(0.);
  for(int y=-dist;y<=dist;y++) {
    for(int x=-dist;x<=dist;x++) {
      if(hexDist(x,y)!=dist) {
        continue;
      }
      total += getRelative(ivec2(x,y));
      amt++;
    }
  }
  return total/(float)amt;
}

vec3 avgDistHex(int dist) {
  int amt = 0;
  vec3 total = vec3(0.);
  for(int y=-dist;y<=dist;y++) {
    for(int x=-dist;x<=dist;x++) {
      if(hexDist(x,y)>dist) { continue;}
      total += getRelative(vec2(x,y));
      amt++;
    }
  }
  return total/(float)amt;
}

float rand(vec2 co){
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}

vec3 randC(vec2 co) {
  return vec3(rand(co), rand(co+1.), rand(co+2.));
}

int magpie_gen(in int n)
{
  return n*997;
  // n = (n << 13) ^ n; 
  // return (n * (n*n*15731+789221) + 1376312589) & 0x7fffffff;
}

int seed;
int rng(int bound) {
  seed = magpie_gen(seed)+u_seed;
  return seed%bound;
}

const int NUM_SUMS = 12;
float sum(float a, float b) {
  int sumIndex = rng(NUM_SUMS);
  switch(sumIndex) {
    case 0: return a+b;
    case 1: return sin(a*6.28);
    case 2: return pow(a, b);
    case 3: return a-b;
    case 4: return a*b;
    case 5: return a>b?1.:0.;
    case 6: return abs(a-b)<.1 ? 1. : 0.;
    case 7: return abs(a-b)<.2 ? 1. : 0.;
    case 8: return abs(a-.3)<.1 ? 1. : 0.;
    case 9: return abs(a-.7)<.1 ? 1. : 0.;
    case 10: return max(a,b);
    case 11: return min(a,b);
    default: return 0;
  }
}

const int VAL_LN = 6;

float[VAL_LN] getPossibleValues() {

  vec3 avg1 = avgDistHex(1);
  vec3 avg2 = avgDistHex(2);

  float[VAL_LN] result;

  result[0] = avg2.x;
  result[1] = avg2.y;
  result[2] = avg2.z;

  result[3] = avg1.x;
  result[4] = avg1.y;
  result[5] = avg1.z;

  return result;
}

float getVal(float[VAL_LN] possibles) {
  return mod(abs(sum(possibles[rng(VAL_LN)], possibles[rng(VAL_LN)])),1.000001);
}

void main() {

  float across = 1., down = 1.;
  seed = u_seed + 
    int(v_texCoords.x*across+17.)*238 
  + int(v_texCoords.y*down+15)*92183;
  // seed = u_seed + 17*238 + 15 * 92138;

  vec3 me = texelFetch(u_texture, ivec2(v_texCoords), 0).xyz;

  float[VAL_LN] pVals = getPossibleValues();

  vec3 result = vec3(getVal(pVals), getVal(pVals), getVal(pVals));

  vec3 col = mix(me, result, u_mult);

  if(isClick()) {
    col = randC(v_texCoords);
    // col = vec3(.5,.5,.5);
  }
  if(isClear()) {
    col = vec3(0.,0.,0.);
  }
  if(u_randomise>0) {
    vec2 relPos = gl_FragCoord/u_screen;
    if(isWithin(relPos.y, 0.5, 0.91)) {
      col = randC(v_texCoords);
    }
  }
  gl_FragColor = vec4(col, 1.);

}


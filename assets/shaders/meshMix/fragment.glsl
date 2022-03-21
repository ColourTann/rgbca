#version 150

#ifdef GL_ES
precision highp float;
#endif 
 
// varying input variables from our vertex shader
varying vec4 v_color;
uniform float u_t;
uniform int u_seed;
uniform int u_randomise;
uniform float u_ml;
uniform float u_mult;
uniform float u_mix;
uniform float u_mr;
uniform vec2 u_mloc;
uniform ivec2 u_screen;
 
// a special uniform for textures 
uniform sampler2D u_texture;
 
vec2 onePixel() {
  return (vec2(1.00, 1.00) / u_screen);
}

vec3 getRelative(ivec2 delta) {
    // vec2 onePixel = vec2(1.0/u_screen.x, 1.0/u_screen.y);
    // return texture2D(u_texture, v_texCoords+onePixel*delta);
  
  ivec2 tmp = ivec2(gl_FragCoord.xy)+delta;
  tmp = (tmp+u_screen) % u_screen;
  return texelFetch(u_texture, tmp, 0).xyz;
}

bool isClick() {
  // return true;

  vec2 locPos = gl_FragCoord.xy/u_screen;
  vec2 dist = locPos - u_mloc;
  float totalDist = length(dist);
  return totalDist<.022 && u_ml>0;
}

bool isClear() {
  return u_mr > 0;
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
  return max(abs(dx), max(abs(dy), abs(dx+dy)));
}

float circDist(float dx, float dy) {
  return float(sqrt(dx*dx+dy*dy));
}

int squareDist(int dx, int dy) {
  return max(abs(dx),  abs(dy));
}


vec3 avgExactDistHex(int dist) {
  float amt = 0;
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
  return total/amt;
}

vec3 avgExactDistSq(int dist) {
  float amt = 0;
  vec3 total = vec3(0.);
  for(int y=-dist;y<=dist;y++) {
    for(int x=-dist;x<=dist;x++) {
      if(squareDist(x,y)!=dist) {
        continue;
      }
      total += getRelative(ivec2(x,y));
      amt++;
    }
  }
  return total/amt;
}

vec3 avgDistHex(int dist) {
  float amt = 0;
  vec3 total = vec3(0.);
  for(int y=-dist;y<=dist;y++) {
    for(int x=-dist;x<=dist;x++) {
      if(hexDist(x,y)>dist) { continue;}
      total += getRelative(ivec2(x,y));
      amt++;
    }
  }
  return total/amt;
}

vec3 avgDistCirc(float dist) {
  float amt = 0;
  vec3 total = vec3(0.);
  for(float y=-dist;y<=dist;y++) {
    for(float x=-dist;x<=dist;x++) {
      if(circDist(x,y)>dist) { continue;}
      total += getRelative(ivec2(x,y));
      amt++;
    }
  }
  return total/amt;
}

vec3 exactDistCirc(float dist) {
  float amt = 0;
  vec3 total = vec3(0.);
  for(float y=-dist;y<=dist;y++) {
    for(float x=-dist;x<=dist;x++) {
      if(circDist(x,y)!=dist) { continue;}
      total += getRelative(ivec2(x,y));
      amt++;
    }
  }
  return total/amt;
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

  vec3 avg1 = exactDistCirc(5);
  vec3 avg2 = avgDistCirc(2);
  vec3 avg3 = avgExactDistSq(0);
  vec3 avg4 = avgExactDistSq(3);



  float[VAL_LN] result;

  result[0] = avg1.x;
  result[1] = avg1.y;
  result[2] = avg1.z;

  result[3] = avg2.x;
  result[4] = avg2.y;
  result[5] = avg2.z;

  // result[6] = avg3.x;
  // result[7] = avg3.y;
  // result[8] = avg3.z;

  // result[9] = avg4.x;
  // result[10] = avg4.y;
  // result[11] = avg4.z;

  return result;
}

float getVal(float[VAL_LN] possibles) {
  float val1 = possibles[rng(VAL_LN)];
  float val2 = possibles[rng(VAL_LN)];
  float val3 = possibles[rng(VAL_LN)];
  float val4 = possibles[rng(VAL_LN)];
  float result = sum(sum(val1, val2), sum(val3, val4));

  return mod(abs(result), 1.000001);
}

void main() {

  float across = 1., down = 1.;
  seed = u_seed + 
    int(gl_FragCoord.x/u_screen.x*across+38)*238 
  + int(gl_FragCoord.y/u_screen.y*down+33)*92183;

  vec3 me = getRelative(ivec2(0,0));

  float[VAL_LN] pVals = getPossibleValues();

  vec3 result1 = vec3(getVal(pVals), getVal(pVals), getVal(pVals));
  vec3 result2 = vec3(getVal(pVals), getVal(pVals), getVal(pVals));

  float mixx = u_mix;
  float multt = u_mult;
  if(false) {
    mixx = gl_FragCoord.x/u_screen.x;
    multt = gl_FragCoord.y/u_screen.y;
  }

  vec3 result = mix(result1, result2, mixx);


  //result = normalize(result);

  vec3 col = mix(me, result, multt);
  col = normalize(col);
  if(isClick()) {
    // col = randC(gl_FragCoord.xy);
    col = vec3(.5,.5,.5);
  }
  if(isClear()) {
    col = vec3(0.,0.,0.);
  }
  if(u_randomise>0) {
    col = randC(gl_FragCoord.xy);
  }
  gl_FragColor = vec4(col, 1.);

}


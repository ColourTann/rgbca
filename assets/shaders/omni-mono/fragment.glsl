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

const int NUM_WEIGHTS = 16;
const float prec = 9999;

uniform float[NUM_WEIGHTS] u_weights;
uniform float[NUM_WEIGHTS] u_reseeds;
uniform int u_weightIndex;
uniform int u_showMore;
uniform int u_middle;
 
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
  return totalDist<.052 && u_ml>0;
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

vec3 avgDistSq(int dist, bool exact) {
  float amt = 0;
  vec3 total = ivec3(0,0,0);
  for(int y=-dist;y<=dist;y++) {
    for(int x=-dist;x<=dist;x++) {
      if(!exact && squareDist(x,y)>dist) {
        continue;
      }
      if(exact && squareDist(x,y)!=dist) {
        continue;
      }
      vec3 raw = getRelative(ivec2(x,y))*prec;
      total.x += int(raw.x);
      total.y += int(raw.y);
      total.z += int(raw.z);

      amt++;
    }
  }
  return total/(amt*prec);
}

vec3 avgDistHex(int dist, bool exact) {
  float amt = 0;
  vec3 total = ivec3(0,0,0);
  for(int y=-dist;y<=dist;y++) {
    for(int x=-dist;x<=dist;x++) {
      if(!exact && hexDist(x,y)>dist) {
        continue;
      }
      if(exact && hexDist(x,y)!=dist) {
        continue;
      }
      vec3 raw = getRelative(ivec2(x,y))*prec;
      total.x += int(raw.x);
      total.y += int(raw.y);
      total.z += int(raw.z);
      amt++;
    }
  }
  return total/(amt*prec);
}

vec3 avgDistCirc(float dist, bool exact) {
  float amt = 0;
  vec3 total = vec3(0.);
  for(float y=-dist;y<=dist;y++) {
    for(float x=-dist;x<=dist;x++) {
      if(!exact && circDist(x,y)>dist) {
        continue;
      }
      if(exact && circDist(x,y)!=dist) {
        continue;
      }
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


int seed;
int rng(int bound) {
  seed = seed * 12357 % 0x10001;
  return bound * (seed - 1) >> 16;
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

  // vec3 avg1 = avgDistCirc(rng(5)+1);
  // vec3 avg2 = avgExactDistSq (rng(2)+1);
  // vec3 avg3 = exactDistCirc (rng(3)) ;
  // vec3 avg4 = avgExactDistSq(3);

  // vec3 avg0 = avgDistSq(0, true);
  vec3 avg1 = avgDistHex(5, true);
  vec3 avg2 = avgDistHex(2, false); 

  float[VAL_LN] result;

  result[0]=avg1.x;
  result[1]=avg1.y;
  result[2]=avg1.z;
  result[3]=avg2.x;
  result[4]=avg2.y;
  result[5]=avg2.z;

    // vec3 hsv = rgb2hsv(getRelative(ivec2(0,0)));
  // float dist = hsv.g*rng(5);
  // float angle = hsv.r * 6.28;
  // vec3 valueOfHueNeighbour = getRelative(
  //   ivec2(int(sin(angle)*dist),int(cos(angle)*dist))
  // );
  // result[6]=valueOfHueNeighbour.x;
  // result[7]=valueOfHueNeighbour.y;
  // result[8]=valueOfHueNeighbour.z;


  // result[3] = avg2.x;
  // result[4] = avg2.y;
  // result[5] = avg2.z;

  // result[6] = avg3.x;
  // result[7] = avg3.y;
  // result[8] = avg3.z;

  // result[9] = avg4.x;
  // result[10] = avg4.y;
  // result[11] = avg4.z;

  return result;
}

float computeSingle(float[VAL_LN] possibles) {
  float val1 = possibles[rng(VAL_LN)];
  float val2 = possibles[rng(VAL_LN)];
  float val3 = possibles[rng(VAL_LN)];
  float val4 = possibles[rng(VAL_LN)];
  // float val5 = possibles[rng(VAL_LN)];
  // float val6 = possibles[rng(VAL_LN)];
  // float val7 = possibles[rng(VAL_LN)];
  // float val8 = possibles[rng(VAL_LN)];
  // float result1 = sum(sum(val1, val2), sum(val3, val4));
  // float result2 = sum(sum(val5, val6), sum(val7, val8));
  // float result = sum(result1, result2);
  // float result = sum(val1, val2); 

  float result = sum(sum(val1, val2), sum(val3, val4));

  return mod(abs(result), 1.000001);
}

vec3 compute(float[VAL_LN] possibles) {
  return vec3(
    computeSingle(possibles), 
    computeSingle(possibles), 
    computeSingle(possibles)
  );
}


void main() {

  float across = 1., down = 1.;
  seed = u_seed + 
    int(gl_FragCoord.x/u_screen.x*across+38)*238 
  + int(gl_FragCoord.y/u_screen.y*down+33)*92183;

  vec3 me = getRelative(ivec2(0,0));
  vec3 col = me;
  float offset = -.5;
  float mult = .2;
  float[VAL_LN] pVals;
  for(int i=0;i<u_weights.length();i++) {
    seed += int(u_reseeds[i]);
    // for(float rs=0.;rs<u_reseeds[i];rs++) {
    //   rng(10);
    // }
    if(i%6==0) {
      pVals = getPossibleValues();
    }
    if(u_weights[i]==-1) {
        if(u_showMore>0) {
          
          vec2 relPos = gl_FragCoord.xy/u_screen;
          col = mix(col, compute(pVals), (relPos.x+offset)*mult);
          col = mix(col, compute(pVals), (relPos.y+offset)*mult); 
        }
        break;
    } else {
        col = mix(col, compute(pVals), ((u_weights[i])+offset)*mult); 
    }
  }

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
  if(u_middle>0) {
    vec2 diff = gl_FragCoord.xy - u_screen/2. - vec2(.5,.5); 
    float cutoff = .6f;
    if(abs(diff.x) < cutoff && abs(diff.y) < cutoff) {
      col = vec3(1.,1.,1.);
    }
  }
  gl_FragColor = vec4(col, 1.);

}


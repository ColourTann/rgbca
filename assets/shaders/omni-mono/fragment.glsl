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
const int NUM_NH = 3;
const int NUM_WEIGHTS = 16;
const float prec = 9999;

uniform float[NUM_WEIGHTS] u_weights;
uniform float[NUM_WEIGHTS] u_reseeds;
uniform int u_weightIndex;
uniform int u_showMore;
uniform int u_middle;
 
// a special uniform for textures 
uniform sampler2D u_texture;
 
int triangular(int i) {
  return (i*(i+1))/2;
}

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

int nDist(int nType, int dx, int dy){
  switch(nType) {
   case 0: return squareDist(dx,dy);
   case 1: return int(circDist(dx,dy));
   case 2: return hexDist(dx,dy);
   default: return -1;
  }
}

vec3 avgDist(int nType, int dist) {
  float amt = 0;
  vec3 total = ivec3(0,0,0);
  for(int y=-dist;y<=dist;y++) {
    for(int x=-dist;x<=dist;x++) {
      int aDist = nDist(nType, x, y);
      if(aDist>dist) {
        continue;
      }
      // int max = max(abs(x),abs(y));
      // int min = min(abs(x),abs(y));
      // int maskIndex = triangular(max)+min;
      // if((mask & (1 << maskIndex)) == 0) {
      //   continue;
      // }
      vec3 raw = getRelative(ivec2(x,y))*prec;
      total.x += int(raw.x);
      total.y += int(raw.y);
      total.z += int(raw.z);
      amt++;
    }
  }
  return total/(amt*prec);
}

int getHashIndex(int dx, int dy, int dist) {
  dx += dist;
  dy += dist;
  return dy*dist+dx;
}

int getSymmIndex(int dx, int dy) {
  int max = max(abs(dx),abs(dy));
  int min = min(abs(dx),abs(dy));
  return triangular(max)+min;
}

int getRotIndexBad(int dx, int dy) {
  int symm = getSymmIndex(dx, dy);
  symm *= 2;
  if(abs(dx) != abs(dy)) {
    int thing = 0;
    if(sign(dx) == sign(dy)) {
      thing = 1-thing;  
    }
    if(abs(dx)>abs(dy)) {
      thing = 1-thing;
    }

    if(thing == 1) {
      symm++; 
    }
  }
  return symm;
}

vec3 avgDistMasked(int dist, int mask) {
  float amt = 0;
  vec3 total = ivec3(0,0,0);
  for(int y=-dist;y<=dist;y++) {
    for(int x=-dist;x<=dist;x++) {
      int aDist = squareDist(x, y);
      if(aDist>dist) {
        continue;
      }
      int max = max(abs(x),abs(y));
      int min = min(abs(x),abs(y));
      int maskIndex = triangular(max)+min;
      if((mask & (1 << getHashIndex(x, y, dist))) == 0) {
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

const int VAL_LN = 3;

float[VAL_LN] getPossibleValues() {

  // vec3 avg1 = avgDistCirc(rng(5)+1);
  // vec3 avg2 = avgExactDistSq (rng(2)+1);
  // vec3 avg3 = exactDistCirc (rng(3)) ;
  // vec3 avg4 = avgExactDistSq(3);

  // vec3 avg0 = avgDistSq(0, true);
  vec3 avg1 = avgDistMasked(1 , rng(99999999));
  // vec3 avg2 = avg1; 
  // vec3 avg1 = avgDist(1, 1, rng(99999999));
  // vec3 avg2 = avgDist(1, 1,  rng(99999999)); 

  float[VAL_LN] result;

  result[0]=avg1.x;
  result[1]=avg1.y;
  result[2]=avg1.z;
  // result[3]=avg2.x;
  // result[4]=avg2.y;
  // result[5]=avg2.z;


  return result;
}

float computeSingle(float[VAL_LN] possibles) {
  float val1 = possibles[rng(VAL_LN)];
  float val2 = possibles[rng(VAL_LN)];
  float val3 = possibles[rng(VAL_LN)];
  float val4 = possibles[rng(VAL_LN)];
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
  float mult = 0.3;
  float[VAL_LN] pVals;
  for(int i=0;i<u_weights.length();i++) {
    seed += int(u_reseeds[i]);
    if(i%2==0) {
      pVals = getPossibleValues();
    }
    int numExtras = 0;
    if(u_weights[i]==-1) {
        if(u_showMore>0) {
          if(numExtras == 1) {
            continue;
          }          
          numExtras ++;
          vec2 relPos = gl_FragCoord.xy/u_screen;
          col = mix(col, compute(pVals), (relPos.x+offset)*mult);
          // col = mix(col, compute(pVals), (relPos.y+offset)*mult); 
          break;
        } else {
          break;
        }

    } else {
        col = mix(col, compute(pVals), ((u_weights[i])+offset)*mult); 
    }
  }

  // if(true) {
  //   int dist = 4;
  //   int size = dist*2+1;
  //   int row = u_screen.x/size;
  //   int neighbX = int(gl_FragCoord.x/size) ;
  //   int neighbY = int(gl_FragCoord.y/size) +int(u_t*0.);
  //   int neighb = neighbX + neighbY * (u_screen.x/size);

  //   int x = (int(gl_FragCoord.x)%size)-dist;
  //   int y = (int(gl_FragCoord.y)%size)-dist;
  //   if((neighb & (1 << getRotIndexBad(x, y))) > 0) {
  //     col = vec3(1.,1.,1.);
  //   } else {
  //      col = vec3(0.,0.,0.);
  //   }
  // }

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


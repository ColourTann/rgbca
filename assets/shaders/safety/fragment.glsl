#version 150

#ifdef GL_ES
precision highp float;
#endif 
 

// a special uniform for textures 
uniform sampler2D u_texture0;
uniform sampler2D u_texture1;
 
const int protectionType =2;
// 0 - fade flashies to checkerboard
// 1 - blur through time 

void main() {



  ivec2 v = ivec2(gl_FragCoord.xy);
  vec4 c0 = texelFetch(u_texture0 , v, 0);
  vec4 c1 = texelFetch(u_texture1, v, 0);
  // if(true) {
  //   gl_FragColor = vec4(c0);
  //   return;
  // }
  
  // float alphaFactor = 0.1-l;
  // float newAlpha = c1.a + alphaFactor * .01;
  //    float newAlpha = c1.a+alphaFactor;
  if(protectionType == 1) {
    vec4 diff = abs(c0-c1);
    float l = length(diff.xyz);
    float newAlpha = c1.a;
    if(l < 0.08) {
      newAlpha += 0.03;
    } else {
      newAlpha = max(0., c1.a-.3);
      // newAlpha = 0.;
    }
    gl_FragColor = vec4(c0.rgb , newAlpha);
  } else if(protectionType == 2) {
    gl_FragColor = mix(c1, c0, .1);
  } else {
    gl_FragColor = vec4(c0);
  }

  // resultCol = c0;

  // gl_FragColor = resultCol;
  // gl_FragColor.a = max(0., min(1., c0.a-l));
  // gl_FragColor.a = 0.;
  // gl_FragColor.r = 0.0;
  // gl_FragColor = vec4(1.,0.,1., 1.);

}


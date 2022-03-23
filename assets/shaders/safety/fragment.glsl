#version 150

#ifdef GL_ES
precision highp float;
#endif 
 

// a special uniform for textures 
uniform sampler2D u_texture0;
uniform sampler2D u_texture1;
 

void main() {
  ivec2 v = ivec2(gl_FragCoord.xy);
  vec4 c0 = texelFetch(u_texture0 , v, 0);
  vec4 c1 = texelFetch(u_texture1, v, 0);
  vec4 diff = abs(c0-c1);
  float l = length(diff.xyz);
  float newAlpha = c1.a;
  if(l < 0.08) {
    newAlpha += 0.05  ;
  } else {
    newAlpha = max(0., c1.a-.3);
    // newAlpha = 0.;
  }
  // float alphaFactor = 0.1-l;
  // float newAlpha = c1.a + alphaFactor * .01;
  //    float newAlpha = c1.a+alphaFactor;

  gl_FragColor = vec4(c0.rgb , newAlpha);
  // gl_FragColor.a = max(0., min(1., c0.a-l));
  // gl_FragColor.a = 0.;
  // gl_FragColor.r = 0.0;
  // gl_FragColor = vec4(1.,0.,1., 1.);

}


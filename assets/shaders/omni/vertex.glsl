//Vertex
attribute vec4 a_position;
varying vec2 v_pos;
uniform mat4 u_projTrans;

void main() {
    gl_Position =  a_position;
    v_pos = gl_Position;
}
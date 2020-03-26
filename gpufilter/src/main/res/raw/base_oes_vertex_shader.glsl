attribute vec4 position;
attribute vec4 inputTextureCoordinate;
varying vec2 textureCoordinate;
uniform mat4 uTextureMatrix;
void main()
{
  textureCoordinate = (uTextureMatrix * inputTextureCoordinate).xy;
  gl_Position = position;
}
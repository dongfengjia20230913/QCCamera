attribute vec4 position;
uniform mat4 uTextureMatrix;
attribute vec4 inputTextureCoordinate;
varying vec2 textureCoordinate;
void main()
{
  textureCoordinate = (uTextureMatrix * inputTextureCoordinate).xy;
  gl_Position = position;
}
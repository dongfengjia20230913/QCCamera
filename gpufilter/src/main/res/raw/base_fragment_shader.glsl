precision mediump float;
uniform sampler2D inputImageTexture;
varying vec2 textureCoordinate;
void main()
{
  gl_FragColor = texture2D(inputImageTexture, textureCoordinate);
}

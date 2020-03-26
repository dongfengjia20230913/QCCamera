attribute vec4 position;//顶点坐标
attribute vec4 inputTextureCoordinate;//纹理坐标

varying vec2 textureCoordinate;//传递到片元的纹理坐标

void main()
{
    gl_Position = position;
    textureCoordinate = inputTextureCoordinate.xy;
}
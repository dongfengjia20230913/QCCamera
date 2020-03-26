#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES inputImageTexture;
varying vec2 textureCoordinate;
const highp vec3 W = vec3(0.2125, 0.7154, 0.0721);

void main()
{
   lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
	float luminance = dot(textureColor.rgb, W);
	gl_FragColor = vec4(vec3(luminance), textureColor.w);
}

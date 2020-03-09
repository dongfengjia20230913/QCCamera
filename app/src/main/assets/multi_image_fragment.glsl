precision mediump float;

varying vec2 textureCoordinate;//从顶点作色器传递过来

uniform sampler2D inputImageTexture;//应用层传递的纹理内容
uniform sampler2D blowoutTexture; //blowout;
uniform sampler2D overlayTexture; //overlay;
uniform sampler2D mapTexture; //map

uniform float strength;//混合强度，这里默认设置成了1，实际场景应当从应用层传递过来

void main()
{

     //从采样器中进程纹理采样
    vec4 originColor = texture2D(inputImageTexture, textureCoordinate.xy);
    vec4 texel = texture2D(inputImageTexture, textureCoordinate.xy);
    vec3 bbTexel = texture2D(blowoutTexture, textureCoordinate.xy).rgb;

    texel.r = texture2D(overlayTexture, vec2(bbTexel.r, texel.r)).r;
    texel.g = texture2D(overlayTexture, vec2(bbTexel.g, texel.g)).g;
    texel.b = texture2D(overlayTexture, vec2(bbTexel.b, texel.b)).b;

     //按比例分别混合RGB
    vec4 mapped;
    mapped.r = texture2D(mapTexture, vec2(texel.r, 0.16666)).r;
    mapped.g = texture2D(mapTexture, vec2(texel.g, 0.5)).g;
    mapped.b = texture2D(mapTexture, vec2(texel.b, 0.83333)).b;
    mapped.a = 1.0;

     //mix(x, y, a): x, y的线性混叠， x(1-a) + y*a;
    mapped.rgb = mix(originColor.rgb, mapped.rgb, 1.0);

    gl_FragColor = mapped;
 }
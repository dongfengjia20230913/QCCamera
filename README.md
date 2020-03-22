# 效果预览 #

![主界面](https://upload-images.jianshu.io/upload_images/6338004-1ffde32b4da7ef40.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![预览界面](https://upload-images.jianshu.io/upload_images/6338004-896daec1c1933ff6.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


## 相机API的选择 ##
为简单起见，后续相机所有例子，使用Camera API2。我们主要的焦点集中在Opengl图像帧处理上，不对API的差异做过多适配

## 显示View的选择 ##
网上大部分的相机例子的预览都是使用GlSurfaceView或者TextureView来显示数据，两者有什么区别？



### TextureView ###
同样继承自View，必须在开启硬件加速的设备中使用（保守估计目前百分之九十的Android设备都开启了），TextureView通过setSurfaceTextureListener的回调在子线程中进行更新UI. 

**TextureView显示预览数据步骤：**

1. 在xml文件中设置TextureView
2. 实现SurfaceTextureListener的回调。
3. 步骤2回调onSurfaceTextureAvailable()时，请求打开摄像头Camera.open(0);
设置摄像头相关参数；
4. 获取TextureView的SurfaceTexture，根据获取的SurfaceTexture创建Surface
5. 将Surface传递给底层相机驱动程序，
6. 开启预览，底层有数据时，将摄像头数据设置到Surface中，并显示到TextureView中

### GlSurfaceView ###
GlSurfaceView继承自SurfaceView类，专门用来显示OpenGL渲染的，简单理解可以显示视频，图像及3D场景这些的。

如果你在学习自定义相机，而且你的相机想要实现美颜，滤镜，人脸识别等场景，你就必须要学习如何使用GlsurfaView

**GlSurfaceView 显示预览步骤：**

1. 在xml中添加GlSurfaceView
2. 创建渲染器类实现GlSurfaceView.Renderer
3. 清除画布，并创建一个纹理并绑定到。
4. 创建一个用来最后显示的SurfaceTexture来显示处理后的数据。
5. 创建Opengl ES程序并添加着色器到该程序中，创建openGl程序的可执行文件，并释放shader资源。
6. 打开摄像头，并配置相关属性。设置预览视图，并开启预览。
7. 添加程序到ES环境中，并设置及启用各类句柄。
8. 在onDrawFrame中进行画布的清理及绘制最新的数据到纹理图形中。
9. 设置一个SurfaceTexture.OnFrameAvailableListener的回调来通知GlSurfaceview渲染新的帧数据

**因此，后续我们相机的相关实现都是技术GlSurfaceView来处理的**

## OPENGL ES 版本选择##

反编译了市场上几家大的第三方app，都是使用OPENGL ES 2.0版本，因此，我们使用2.0版本。

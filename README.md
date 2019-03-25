# ZVideoPlayer视频播放库

    dependencies {
        implementation 'com.github.ctiao:DanmakuFlameMaster:0.9.25'
        implementation 'com.github.ctiao:ndkbitmap-armv7a:0.9.21'
        implementation 'com.github.z-pivot:ZVideoPlayer:1.0.0'
    }

## 基本功能：
* 单个视频播放和列表视频播放
* 网络、直播、本地视频播放切换
* 浮窗播放模式，可当前页面浮窗和系统页面浮窗
* 可显示、发送弹幕
* 清晰度切换
* 播放倍速切换：X0.5 X1.0 X1.5 X2.0
* 播放视图拉伸切换：原尺寸 自适应 16:9 4:3
* 视频分享：微信 QQ 微博
* 屏幕手势功能：左侧上下滑动调节清晰度 右侧上下滑动调节音量 左右滑动执行前进或后退（显示时间跨度）

### 1.视频清晰度切换
这个功能主要是给出包含三个不同清晰度的同一个视频的不同地址集合，用户点击哪一个清晰度按钮就初始化播放哪一个地址的视频
其实就是简单的按钮点击响应更换视频播放方法的url参数
![清晰度](https://github.com/z-pivot/ZVideoPlayer/blob/master/images/qxd.png)

### 2.关于分享
分享功能是引入的是mob的ShareSDK，这个网站上有很多的开源免费的API，上面有很详细的教程，这里的微信分享比较麻烦，
先要获取本app的签名再从微信开发者平台上获取id和密码，有兴趣的可以尝试一下，挺折磨人的
![分享](https://github.com/z-pivot/ZVideoPlayer/blob/master/images/fenxiang.png)

### 3.THVideoView
这个类是大部分功能的实现类，里面有部分必须初始化的成员变量需要说明一下：
* private String title; 视频的标题，一般在初始化播放的方法startVideo中初始化设置
* private Class<? extends BaseMedia> decodeMediaStyle = IjkMedia.class; 视频解码方式，在初始化播放的方法startVideo中初始化设置，默认为IjkMedia（一般就用这个解码器）
* private String danMuJson; 弹幕json数据，这个数据格式在本地有示例：

        {
          "p": "205.2259979248,6,18,6830715,1484219280,0,47f24a19,2839373425",
          "t": "厉害了我的弹幕君！"
        },
        
    主要是p标签下的几个字段的含义：第一个是出现的时间点（秒）、第二个是弹幕类型、第三个是弹幕字体大小、第四个是字体颜色，后面的是自定义参数时间戳,用户id等
    弹幕类型有：1从右往左 4固定在底部 5固定在顶部 6从左往右 7特殊弹幕，解析类是QSDanmakuParser：
    
        JSONObject object = jsonArray.getJSONObject(i);
        String p = object.getString("p");
        String t = object.getString("t");
        String[] values = p.split(",");
        long time = (long) (parseFloat(values[0]) * 1000); // 出现时间
        int type = parseInteger(values[1]); // 弹幕类型
        float textSize = parseFloat(values[2]); // 字体大小
        int color = (int) ((0xff000000 | parseLong(values[3])) & 0xffffffff); // 颜色
        item = buildDanmaku(t, type, time, textSize, color);
![弹幕](https://github.com/z-pivot/ZVideoPlayer/blob/master/images/danmu.png)
      
* private List<String> sharpnessUrlList = new ArrayList<>();//不同清晰度视频的url集合 默认有标清、高清、超清，需要自行设置不可为空，若没有该清晰度的地址就用同一地址
* private boolean isShowWifiDialog = true;//是否显示移动网络提示框，一般就是true
* private String shareTitle = "百度一下";//分享的标题
* private String shareUrl = "http://blog.csdn.net/qq_31390699";//分享到的链接，一般为视频所在的网址
* private String shareImg = "http://img.zcool.cn/community/0183b855420c990000019ae98b9ce8.jpg@900w_1l_2o_100sh.jpg";//分享的封面图片
* private String shareDesc = "不懂你就百度啊";//分享的描述文字
  
### 4.MainActivity
主Demo界面中除了对THVideoView的初始化设置，还包括了视频切换、视频列表、浮窗模式功能
可以在MainActivity的基础上对主界面进行布局修改
![当前浮窗](https://github.com/z-pivot/ZVideoPlayer/blob/master/images/dangqianfuchuang.png)
![系统浮窗](https://github.com/z-pivot/ZVideoPlayer/blob/master/images/xitongfuchuang.png)
![视频列表](https://github.com/z-pivot/ZVideoPlayer/blob/master/images/liebiao.png)
![视频直播](https://github.com/z-pivot/ZVideoPlayer/blob/master/images/zhibo.png)

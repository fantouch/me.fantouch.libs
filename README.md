me.fantouch.libs
================

**若干个帮助Android开发的模块.**    

>已包含 [afinal.jar](https://github.com/yangfuhai/afinal) 和 `android-support-v4.jar`,  
如果你的工程引用了这个库,请不需要重复包含

================  

##CrashHandler崩溃处理模块
* 程序崩溃了  
![](https://www.evernote.com/shard/s25/sh/4d01bbd4-c5df-4d90-a617-29e5ead4bfc2/e18af5ee47804638bcf9c4251b9639a9/res/6e307ff6-15bc-40ea-a3de-c0ebb05733af.jpg?resizeSmall&width=832)  

* 崩溃报告存储在私有目录(/data/data/com.xxx)  
![](https://www.evernote.com/shard/s25/sh/4d01bbd4-c5df-4d90-a617-29e5ead4bfc2/e18af5ee47804638bcf9c4251b9639a9/res/6cd49d87-8abb-4fa9-9fe4-e33920ef7bb9.jpg?resizeSmall&width=832)  

* 服务器收到崩溃报告  
![](https://www.evernote.com/shard/s25/sh/4d01bbd4-c5df-4d90-a617-29e5ead4bfc2/e18af5ee47804638bcf9c4251b9639a9/res/214cb2a3-eb44-41c0-8527-6536c7c302e9.jpg?resizeSmall&width=832)  

###需要权限  
```xml  
<!-- 不依赖Activity的Context弹出Dialog -->
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
   
<!-- 检查是否wifi网络 (如果需要上传日志) -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- 使用网络上传日志 (如果需要上传日志) -->
   <uses-permission android:name="android.permission.INTERNET" />
```
###如何使用  
* 在自定义的MyApplication内注册CrashHandler   

```java
    public class MyApplication extends Application {
        @Override
        public void onCreate() {
            super.onCreate();
    
            // 注册crashHandler,保存日志但不上传到服务器
            CrashHandler.getInstance().init(getApplicationContext(), null);
            
            // 注册crashHandler,保存日志并自动上传到服务器
            //SendService extends AbsSendReportsService
            CrashHandler.getInstance().init(getApplicationContext(), SendService.class);
        }
    }
```  

* 在 `AndroidManifest.xml` 内注册你的MyApplication   

```xml 
<application
  android:name="me.fantouch.libs.crash.MyApplication"
  … >
    <activity> … </activity>
</application>  
```

* 好了,你可以坐等错误报告了.  

####如何能自动上传日志到服务器?
* 根据你与服务器的协议,实现 `SendService`  

>推荐使用[FinalHttp](https://github.com/yangfuhai/afinal)(me.fantouch.libs已包含FinalHttp)

```java
    public class SendService extends AbsSendReportsService {
        @Override
        public void sendZipReportsToServer(File reportsZip, NotificationHelper notificationHelper) {
            
            AjaxParams params = new AjaxParams();
            try {
                params.put("reportsZip", reportsZip);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            
            FinalHttp fh = new FinalHttp();
            fh.post("http://192.168.0.163:8888/upload.php", params, new AjaxCallBack<String>() {
                
                @Override
                public void onSuccess(String t) {
                    stopSelf();
                }
                
                @Override
                public void onFailure(Throwable t, String strMsg) {
                    stopSelf();
            }
            });
            
        } 
    }
```


================  

##日志模块ELog
* `ELog.d("Hello~~");`  
![](https://www.evernote.com/shard/s25/sh/4d01bbd4-c5df-4d90-a617-29e5ead4bfc2/e18af5ee47804638bcf9c4251b9639a9/res/39fdd19e-c607-4ad9-b80b-d169f5a979d7.png?resizeSmall&width=832)  

* Eclipse Logcat输出  
![](https://www.evernote.com/shard/s25/sh/4d01bbd4-c5df-4d90-a617-29e5ead4bfc2/e18af5ee47804638bcf9c4251b9639a9/res/e8f2016e-e8e8-46e1-8b23-3a21442fa75b.jpg?resizeSmall&width=832)  
 * 简单,你只需关心要Log的内容
 * 自动用类名填充TAG
 * 自动Log方法名
 * 自动Log文件名,行号
 * Eclipse的LogCat里面双击,自动转跳到Java文件相应行  
 (过段时间再来调试,有没有感觉看不懂log,找不到log语句在哪里?)
 * 可以把日志保存到文件
 * 可以上传日志到服务器  
 
###需要权限  

```xml  
<!-- 检查是否wifi网络  (如果需要上传日志)-->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- 使用网络上传日志  (如果需要上传日志)-->
   <uses-permission android:name="android.permission.INTERNET" />
```
###如何使用  

* 开启ELog  
 
>建议在`Application`里面执行,或者做一个菜单让用户在需要的时候自行开启  
如果不启用,ELog将什么都不做,不会耗费系统资源  

```java
ELog.setEnableLogCat(true);// 启用Logcat输出
```
* 使用    

```java
ELog.d("Hello~~");
```

####如何保存日志?  
```java
// 启用保存日志功能
// 日志文件在/data/data/com.xxx
ELog.setEnableLogToFile(true, getApplicationContext());
```
####如何上传日志到服务器?  
* 启用保存日志功能
* 然后  

```java
// SendService extends AbsSendReportsService
ELog.sendReportFiles(getApplicationContext(), SendService.class);
```

* 注意,你需要根据你与服务器的协议,实现[SendService](https://github.com/fantouch/me.fantouch.libs/edit/master/README.md#-3)


================  

##UpdateHelper自动更新模块
* 发现新版本  
![](https://www.evernote.com/shard/s25/sh/4d01bbd4-c5df-4d90-a617-29e5ead4bfc2/e18af5ee47804638bcf9c4251b9639a9/res/a7ccd1f6-fdaf-4b40-a2d0-bcbdff2b3a41.png?resizeSmall&width=832)  

* 后台下载  
![](https://www.evernote.com/shard/s25/sh/4d01bbd4-c5df-4d90-a617-29e5ead4bfc2/e18af5ee47804638bcf9c4251b9639a9/res/62c5e4a8-4836-4a04-9912-e58c02210c86.png?resizeSmall&width=832)  

* 下载完成  
![](https://www.evernote.com/shard/s25/sh/4d01bbd4-c5df-4d90-a617-29e5ead4bfc2/e18af5ee47804638bcf9c4251b9639a9/res/90d142ba-0d85-4371-b8f6-e418fac3e806.png?resizeSmall&width=832)  

###需要权限  

```xml  
   <!-- 使用网络 -->
   <uses-permission android:name="android.permission.INTERNET" />
   
   <!-- 更新apk文件会下载到sd卡 /mnt/sdcard/com.xxx -->  
   <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

###如何使用
```java
// 需要实现 parser 和 normalUpdateListener
 new UpdateHelper(this, parser, normalUpdateListener).check("http://192.168.1.100/checkUpdate.php");
```
* 根据与服务器的交互协议,实现 `parser`.示例:  

```java
        /** 解析服务器信息,并把信息存入UpdateInfoBean */
        AbsUpdateInfoParser parser = new AbsUpdateInfoParser() {
            @Override
            public UpdateInfoBean parse(String info) {
                UpdateInfoBean infoBean = new UpdateInfoBean();
                // 这里使用Android自带的Json解析服务器返回信息,如果数据复杂,推荐使用Gson
                try {
                    JSONObject infoJson = new JSONObject(info).getJSONObject("version");
                    infoBean.setVersionCode(infoJson.getString("build"));
                    infoBean.setVersionName(infoJson.getString("version"));
                    infoBean.setWhatsNew(infoJson.getString("content"));
                    infoBean.setDownUrl(URL_HOST + "filedownload?showname="
                            + infoJson.getString("build") + "&filename="
                            + infoJson.getString("path"));
                } catch (Exception e) {
                    e.printStackTrace();
                    infoBean = null;
                }
                return infoBean;
            }
        };
```

* 根据你的业务逻辑,实现 `normalUpdateListener`.示例:   

```java
        NormalUpdateListener normalUpdateListener = new NormalUpdateListener() {
            @Override
            public void onCheckStart() {
                // 可以在这提示用户正在检查更新
            }

            @Override
            public void onDownloadStart() {
                // 如果用户选择下载更新,这个方法会被调用
            }

            @Override
            public void onCheckFinish() {
                // 检查完成(没有可用更新,或者检查更新中途出现异常)
                // 例如你是在程序Loading界面进行检查更新,那么现在可以跳过Loading进入程序首页了
            }

            @Override
            public void onCancel() {
                // 用户不想更新,选择了"下次再说"
                // 例如你是在程序Loading界面进行检查更新,那么现在可以跳过Loading进入程序首页了
            }
        };
```

me.fantouch.libs
================

**若干个帮助Android开发的模块.**    

>已包含 [afinal.jar](https://github.com/yangfuhai/afinal) 和 `android-support-v4.jar`,  
如果你的工程引用了这个库,请不需要重复包含

================  

##CrashHandler模块
###简介
1. 捕获程序意外崩溃
2. 弹出崩溃提示对话框,同时保存崩溃日志
3. 退出程序
4. 程序重新开启,自动上传崩溃日志到服务器  
  
  ![](https://www.evernote.com/shard/s25/sh/4d01bbd4-c5df-4d90-a617-29e5ead4bfc2/e18af5ee47804638bcf9c4251b9639a9/res/6e307ff6-15bc-40ea-a3de-c0ebb05733af.jpg?resizeSmall&width=832)

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

* 你需要根据你与服务器的协议,扩展 `AbsSendReportsService` ,例如 `SendService extends AbsSendReportsService`:
 
>*推荐使用[FinalHttp](https://github.com/yangfuhai/afinal)(已包含在本库中)*  

```java
    public class SendService extends AbsSendReportsService {
        private static final String TAG = SendService.class.getSimpleName();
    
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
                    Log.i(TAG, t);
                    stopSelf();
                }
            });
        }
    }
```
* 自定义Application  
```java
    public class MyApplication extends Application {
        private final static String TAG = MyApplication.class.getSimpleName();
    
        @Override
        public void onCreate() {
            Log.i(TAG, "onCreate");
            super.onCreate();
    
            // 注册crashHandler
            CrashHandler.getInstance().init(getApplicationContext(), SendService.class);
        }
    }
```  

* 在 `AndroidManifest.xml` 内注册你的Application
 ```xml 
<application
       android:name="me.fantouch.libs.crash.MyApplication"
       … >
       <activity> … </activity>
</application>  
```

* 好了,如果程序崩溃,下次程序开启的时候,如果有WIFI可用,那么服务器就能收到错误报告了.

================  

##Log模块
###简介
 * 自动用当前类名填充Tag
 * 显示当前方法名
 * 显示当前文件名,行号
 * 双击LogCat的输出,能转跳到Java文件相应行
 * 可以把日志保存到文件
 * 可以上传日志到服务器
###需要权限  
```xml  
<!-- 不依赖Activity的Context弹出Dialog -->
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
   
<!-- 检查是否wifi网络  (如果需要上传日志)-->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- 使用网络上传日志  (如果需要上传日志)-->
   <uses-permission android:name="android.permission.INTERNET" />
```
###如何使用  

* 启用ELog(*全局开关,建议在`Application`里面执行*)  
如果不启用,ELog将什么都不做,不会耗费系统资源
```java
ELog.setEnableLogCat(true);// 启用Logcat输出
ELog.setEnableLogToFile(true, getApplicationContext());// 启用保存到文件
```
* 使用  
```java
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ELog.d("Hello~~");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }
```
* 效果  

![](https://www.evernote.com/shard/s25/sh/4d01bbd4-c5df-4d90-a617-29e5ead4bfc2/e18af5ee47804638bcf9c4251b9639a9/res/e8f2016e-e8e8-46e1-8b23-3a21442fa75b.jpg?resizeSmall&width=832)

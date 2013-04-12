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
   
<!-- 检查是否wifi网络 -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- 使用网络上传日志 -->
   <uses-permission android:name="android.permission.INTERNET" />
```
###如何使用
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
* 你需要根据你与服务器的协议,实现 `AbsSendReportsService` 的 `sendZipReportsToServer()`方法,例如:
 
*推荐使用[FinalHttp](https://github.com/yangfuhai/afinal)(已包含在本库中)*
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
            public void onStart() {
                Log.i(TAG, "start");
            }

            @Override
            public void onSuccess(String t) {
                Log.i(TAG, t);
                stopSelf();
            }

            @Override
            public void onFailure(Throwable t, String strMsg) {
                Log.i(TAG, "send fail");
                stopSelf();
            }
        });
    }
}
```

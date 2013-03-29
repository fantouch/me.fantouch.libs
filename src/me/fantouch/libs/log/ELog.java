
package me.fantouch.libs.log;

import android.util.Log;

/**
 * (ExtremLog=>ELog)<br>
 * 可以记录行号,类名,方法名,可是实现双击转跳的Android Log增强工具
 * <p>
 * 这里使用+拼接String,由于编译器自动优化,<br>
 * 与StringBuilder几乎没差别,<br>
 * 直观起见,不用{@link StringBuilder}了
 * <p>
 * 性能方面,耗时是{@link Log}的20倍,<br>
 * 因此在App发布前务必关闭DEBUG开关: {@link ELog#disable()}
 * <p>
 * 示例:<br>
 * 
 * <pre>
 * ELog.v(&quot;Hello World&tilde;&quot;);<p>
 * LogCat输出:
 * 03-29 16:06:53.657: V/TestActivity(2244): onCreate(): Hello World~    at (TestActivity.java:41)<p>
 * 双击LogCat的输出能转跳到Java文件相应行.
 * </pre>
 * <p>
 * 
 * @author Fantouch
 */
public class ELog {
    private static boolean DEBUG = true;

    /**
     * 开启调试输出
     */
    public static void enable() {
        DEBUG = true;
    }

    /**
     * 关闭调试输出
     */
    public static void disable() {
        DEBUG = false;
    }

    public static boolean isEnable() {
        return DEBUG;
    }

    public static void e(String message) {
        if (DEBUG) {
            String[] infos = getTagAndAutoJumpFunctionText();
            Log.e(infos[0], infos[1] + message + infos[2]);
        }
    }

    public static void d(String message) {
        if (DEBUG) {
            String[] infos = getTagAndAutoJumpFunctionText();
            Log.d(infos[0], infos[1] + message + infos[2]);
        }
    }

    public static void i(String message) {
        if (DEBUG) {
            String[] infos = getTagAndAutoJumpFunctionText();
            Log.i(infos[0], infos[1] + message + infos[2]);
        }
    }

    public static void w(String message) {
        if (DEBUG) {
            String[] infos = getTagAndAutoJumpFunctionText();
            Log.w(infos[0], infos[1] + message + infos[2]);
        }
    }

    public static void v(String message) {
        if (DEBUG) {
            String[] infos = getTagAndAutoJumpFunctionText();
            Log.v(infos[0], infos[1] + message + infos[2]);
        }
    }

    /**
     * @return String[0]:<br>
     *         LogCat的Tag标签,当前类名
     *         <p>
     *         String[1]:<br>
     *         LogCat的Text标签前缀,当前方法名
     *         <p>
     *         String[2]:<br>
     *         能使Eclipse的LogCat窗口实现双击自动转跳到相应行的功能.
     */
    private static String[] getTagAndAutoJumpFunctionText() {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        if (elements.length < 5) {
            Log.e(ELog.class.getSimpleName(), "Stack too shallow!!");
            return new String[] {
                    "", "", ""
            };
        } else {
            String[] s = new String[3];
            s[0] = elements[4].getClassName().substring(
                    elements[4].getClassName().lastIndexOf(".") + 1);

            s[1] = elements[4].getMethodName() + "(): ";

            s[2] = "    at (" + elements[4].getFileName() + ":"
                    + elements[4].getLineNumber()
                    + ")";
            return s;
        }
    }
}

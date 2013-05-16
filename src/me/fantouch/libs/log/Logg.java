
package me.fantouch.libs.log;

import android.util.Log;

/**
 * 可以记录行号,类名,方法名,可是实现双击转跳的Android Log增强工具
 * <p>
 * 直观起见,本工具内部使用+拼接String,编译器会自动优化,<br>
 * 效率与{@link StringBuilder}几乎没差别<br>
 * <p>
 * 性能方面,耗时是{@link Log}的20倍,<br>
 * 因此在App发布前务必关闭DEBUG开关: {@link Logg#disable()}
 * <p>
 * 示例:<br>
 * 
 * <pre>
 * Logg.v(&quot;Hello World&tilde;&quot;);<p>
 * LogCat输出:
 * 03-29 16:06:53.657: V/TestActivity(2244): onCreate(): Hello World~    at (TestActivity.java:41)<p>
 * 
 * 效果:
 * 1.Tag自动用当前类名填充
 * 2.显示当前方法名,后面跟随你的输出信息
 * 3.结尾显示当前文件名,行号
 * 4.双击LogCat的输出,能转跳到Java文件相应行
 * </pre>
 * <p>
 * 
 * @author Fantouch
 */
public class Logg extends BasicLog {
    private Logg() {
    }

    public static void e() {
        // TODO 可设置格式化
        // TODO 抛弃数组形式
        e(null);// FIXME 栈信息加深了一层了
    }

    public static void e(String message) {
        if (ENABLE_LOGCAT || TO_FILE) {
            String[] infos = getTagAndAutoJumpFunctionText();
            if (ENABLE_LOGCAT) {
                Log.e(infos[0], infos[1] + message + infos[2]);
            }
            if (TO_FILE) {
                save(infos[0] + "." + infos[1] + infos[3] + " " + message);
            }
        }
    }

    public static void d() {
        d(null);
    }

    public static void d(String message) {
        if (ENABLE_LOGCAT || TO_FILE) {
            String[] infos = getTagAndAutoJumpFunctionText();
            if (ENABLE_LOGCAT) {
                Log.d(infos[0], infos[1] + message + infos[2]);
            }
            if (TO_FILE) {
                save(infos[0] + "." + infos[1] + infos[3] + " " + message);
            }
        }
    }

    public static void i(String message) {
        if (ENABLE_LOGCAT || TO_FILE) {
            String[] infos = getTagAndAutoJumpFunctionText();
            if (ENABLE_LOGCAT) {
                Log.i(infos[0], infos[1] + message + infos[2]);
            }
            if (TO_FILE) {
                save(infos[0] + "." + infos[1] + infos[3] + " " + message);
            }
        }
    }

    public static void w(String message) {
        if (ENABLE_LOGCAT || TO_FILE) {
            String[] infos = getTagAndAutoJumpFunctionText();
            if (ENABLE_LOGCAT) {
                Log.w(infos[0], infos[1] + message + infos[2]);
            }
            if (TO_FILE) {
                save(infos[0] + "." + infos[1] + infos[3] + " " + message);
            }
        }
    }

    public static void v(String message) {
        if (ENABLE_LOGCAT || TO_FILE) {
            String[] infos = getTagAndAutoJumpFunctionText();
            if (ENABLE_LOGCAT) {
                Log.v(infos[0], infos[1] + message + infos[2]);
            }
            if (TO_FILE) {
                save(infos[0] + "." + infos[1] + infos[3] + " " + message);
            }
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
     *         <p>
     *         String[3]:<br>
     *         行号,把log写入到文件的时候用到
     */
    private static String[] getTagAndAutoJumpFunctionText() {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        if (elements.length < 5) {
            Log.e(Logg.class.getSimpleName(), "Stack too shallow!!");
            return new String[] {
                    "", "", "", ""
            };
        } else {
            String[] s = new String[4];
            s[0] = elements[4].getClassName().substring(
                    elements[4].getClassName().lastIndexOf(".") + 1);

            s[1] = elements[4].getMethodName() + "():";

            s[2] = "    at (" + elements[4].getFileName() + ":"
                    + elements[4].getLineNumber()
                    + ")";

            s[3] = elements[4].getLineNumber() + "";
            return s;
        }
    }
}


package me.fantouch.libs.iostitlebar;

import android.view.View.OnClickListener;

import java.io.Serializable;

/**
 * 标题栏状态存储类,记录了哪些信息可参见类的成员变量
 * @author Fantouch
 *
 */
public class TitleStateBundle implements Serializable {
    private static final long serialVersionUID = 6912296335369510910L;
    /* 后退按钮 */
    private boolean backBtnVisible = false;
    /* 标题文字 */
    private String titleTxt;
    private OnClickListener titleTxtListener;
    /* 标题栏按钮 */
    private boolean titleBtnVisibility = false;
    private OnClickListener titleBtnListener;
    private String titleBtnTxt;

    /**
     * @param backBtnVisible
     * @param titleTxt can be not null,but "" is OK
     * @param titleTxtListener null is OK
     * @param titleBtnVisibility
     * @param titleBtnListener null is OK
     * @param titleBtnTxt can be not null,but "" is OK
     */
    public TitleStateBundle(boolean backBtnVisible, String titleTxt,
            OnClickListener titleTxtListener, boolean titleBtnVisibility,
            OnClickListener titleBtnListener, String titleBtnTxt) {
        super();
        this.backBtnVisible = backBtnVisible;
        this.titleTxt = titleTxt;
        this.titleTxtListener = titleTxtListener;
        this.titleBtnVisibility = titleBtnVisibility;
        this.titleBtnListener = titleBtnListener;
        this.titleBtnTxt = titleBtnTxt;
    }

    public OnClickListener getTitleTxtListener() {
        return titleTxtListener;
    }

    public void setTitleTxtListener(OnClickListener titleTxtListener) {
        this.titleTxtListener = titleTxtListener;
    }

    public boolean isBackBtnVisible() {
        return backBtnVisible;
    }

    public String getTitleTxt() {
        return titleTxt;
    }

    public boolean isTitleBtnVisibility() {
        return titleBtnVisibility;
    }

    public OnClickListener getTitleBtnListener() {
        return titleBtnListener;
    }

    public String getTitleBtnTxt() {
        return titleBtnTxt;
    }

    public void setTitleTxt(String titleTxt) {
        this.titleTxt = titleTxt;
    }

    public void setTitleBtnVisibility(boolean titleBtnVisibility) {
        this.titleBtnVisibility = titleBtnVisibility;
    }

    public void setTitleBtnListener(OnClickListener titleBtnListener) {
        this.titleBtnListener = titleBtnListener;
    }

    public void setTitleBtnTxt(String titleBtnTxt) {
        this.titleBtnTxt = titleBtnTxt;
    }

    @Override
    public String toString() {
        return "TitleStateBundle [backBtnVisible=" + backBtnVisible + ", titleTxt=" + titleTxt
                + ", titleTxtListener=" + titleTxtListener + ", titleBtnVisibility="
                + titleBtnVisibility + ", titleBtnListener=" + titleBtnListener + ", titleBtnTxt="
                + titleBtnTxt + "]";
    }

}

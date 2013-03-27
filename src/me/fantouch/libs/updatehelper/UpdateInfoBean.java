
package me.fantouch.libs.updatehelper;

public class UpdateInfoBean {

    private String versionCode = "0";
    private String versionName = "", whatsNew = "", downUrl = "";

    /**
     * @return 如果versionCode非法,返回0
     */
    public int getVersionCode() {
        int code = 0;
        try {
            code = Integer.valueOf(versionCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return code;
    }

    public String getVersionName() {
        return versionName;
    }

    public String getWhatsNew() {
        return whatsNew;
    }

    public String getDownUrl() {
        return downUrl;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public void setWhatsNew(String whatsNew) {
        this.whatsNew = whatsNew;
    }

    public void setDownUrl(String downUrl) {
        this.downUrl = downUrl;
    }

}

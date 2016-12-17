package name.caiyao.tencentsport;

import cn.bmob.v3.BmobObject;

/**
 * Created by 蔡小木 on 2016/1+6+-*-*-+2/17 0017.
 */

public class JdAd extends BmobObject{
    private String title;
    private String imgUrl;
    private String url;

    public JdAd() {
        this.setTableName("jdad");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

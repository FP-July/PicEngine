package bean;

import java.util.Date;

/**
 * Created by THU73 on 17/7/18.
 */
public class Task {

    public static final String picture = "图像渲染";
    public static final String video = "视频渲染";
    
    public static final String init = "初始化";
    public static final String ready = "就绪";
    public static final String stop = "停止";
    public static final String error = "错误";
    public static final String ongoing = "正在进行";
    public static final String finished = "已完成";

    private int id;
    private String name;
    private String type;
    private String state;
    private String fileLocation;
    private Date date;
    // these parameters stored in database.

    private int minutes;
    private int percent;
    // these parameters will be queried from hadoop platform,
    // manually set in the servlet function then passed to frontend.

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }
}

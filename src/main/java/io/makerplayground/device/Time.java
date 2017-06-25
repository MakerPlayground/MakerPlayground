package io.makerplayground.device;

/**
 * Created by tanyagorn on 6/23/2017.
 */
public class Time {
        private Integer minute;
        private Integer second;

    public Time(Integer minute, Integer second) {
        this.minute = minute;
        this.second = second;
    }

    public Integer getMinute() {
        return minute;
    }

    public void setMinute(Integer minute) {
        this.minute = minute;
    }

    public Integer getSecond() {
        return second;
    }

    public void setSecond(Integer second) {
        this.second = second;
    }

}

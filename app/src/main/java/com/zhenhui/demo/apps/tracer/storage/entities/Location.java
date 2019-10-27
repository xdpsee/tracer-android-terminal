package com.zhenhui.demo.apps.tracer.storage.entities;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Index;

import java.util.Date;

@Entity(
        indexes = {
                @Index(value = "status, timestamp")
        }
)
public class Location {

    @Id
    private Long id;

    private Double latitude;

    private Double longitude;

    private Double speed;

    private Double accuracy;

    private Date timestamp;

    private Integer status = 0;

    @Generated(hash = 1392241452)
    public Location(Long id, Double latitude, Double longitude, Double speed,
                    Double accuracy, Date timestamp, Integer status) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.accuracy = accuracy;
        this.timestamp = timestamp;
        this.status = status;
    }

    @Generated(hash = 375979639)
    public Location() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getSpeed() {
        return this.speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Double getAccuracy() {
        return this.accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public Date getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

}

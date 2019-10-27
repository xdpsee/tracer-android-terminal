package com.zhenhui.demo.apps.tracer.storage.entities;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;

import java.util.Date;

@Entity(
        indexes = {
                @Index(value = "status, timestamp")
        }
)
public class Location {

    @Id
    private Long id;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    @NotNull
    private Double speed;

    @NotNull
    private Double accuracy;

    @NotNull
    private Date timestamp;

    @NotNull
    private Integer status = 0;

    @Generated(hash = 520895325)
    public Location(Long id, @NotNull Double latitude, @NotNull Double longitude,
            @NotNull Double speed, @NotNull Double accuracy,
            @NotNull Date timestamp, @NotNull Integer status) {
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

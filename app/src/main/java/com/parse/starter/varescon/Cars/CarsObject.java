package com.parse.starter.varescon.Cars;

/**
 * Created by iSwear on 1/7/2018.
 */

public class CarsObject {
    private String plate_no;
    private String type;
    private String status;
    private String carId;

    public CarsObject(String plate_no, String type, String status, String carId) {
        this.plate_no = plate_no;
        this.type = type;
        this.status = status;
        this.carId = carId;
    }

    public String getPlate_no() {
        return plate_no;
    }

    public void setPlate_no(String plate_no) {
        this.plate_no = plate_no;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCarId() {
        return carId;
    }

    public void setCarId(String carId) {
        this.carId = carId;
    }
}

package io.github.amsatrio.vertx_benchmark_database.modules.conditions;

import io.vertx.sqlclient.Row;

public class Conditions {
    private String id;
    private String createdOn;
    private String location;
    private Double humidity;
    private Double temperature;

    

    public String getId() {
        return id;
    }



    public void setId(String id) {
        this.id = id;
    }



    public String getCreatedOn() {
        return createdOn;
    }



    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }



    public String getLocation() {
        return location;
    }



    public void setLocation(String location) {
        this.location = location;
    }



    public Double getHumidity() {
        return humidity;
    }



    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }



    public Double getTemperature() {
        return temperature;
    }



    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public static Conditions fromRow(Row row) {
        Conditions condition = new Conditions();
        condition.setId(row.getString("id"));
        condition.setCreatedOn(row.getTemporal("created_on").toString());
        condition.setLocation(row.getString("location"));
        condition.setHumidity(row.getDouble("humidity"));
        condition.setTemperature(row.getDouble("temperature"));
        return condition;   

    }
}

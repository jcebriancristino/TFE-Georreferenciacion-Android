package com.cebrian.tfg.geotactical.model;


public class TacticalElement extends GeoPoint {
    private final String type;


    //CONSTRUTOR DE OBJETO ELEMENTO TACTICO
    public TacticalElement(double imageCoordinateX, double imageCoordinateY, double realCoordinateX, double realCoordinateY, String type) {
        super(imageCoordinateX, imageCoordinateY, realCoordinateX, realCoordinateY);
        this.type = type;
    }

    //GETTER DEL TIPO DE ELEMENTO
    public String getType() {
        return type;
    }


}





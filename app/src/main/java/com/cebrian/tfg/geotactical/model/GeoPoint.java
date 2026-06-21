package com.cebrian.tfg.geotactical.model;

public class GeoPoint {
    protected double xImg;
    protected double yImg;
    protected double longitude;
    protected double latitude;

    //CONSTRUCTOR DE GEOPOINT
    public GeoPoint(double coordinateImageX, double coordinateImageY, double realCoordinateX, double realCoordinateY) {
        this.xImg = coordinateImageX;
        this.yImg = coordinateImageY;
        this.longitude = realCoordinateX;
        this.latitude = realCoordinateY;
    }

    //BLOQUE DE METODOS 1 ################################################################################

    //SETTERS

    //SETTER DE LAS COORDENADAS DEL PUNTO EN PIXELES CON RESPECTO A LA IMAGEN ---------------------------------
    public void setImageCoordinate(double xImg, double yImg) {
        this.xImg = xImg;
        this.yImg = yImg;
    }
    //SETTER DE LAS COORDENADAS DEL PUNTO EN FORMATO LATLON DENTRO DEL SISTEMA DE COORDENADAS ------------------
    public void setRealCoordinate(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    //BLOQUE DE METODOS 2 ################################################################################

    //GETTERS

    //GETTER DE COORDENADA X EN PIXELES CON RESPECTO A LA IMAGEN
    public double getXImg() {
        return xImg;
    }

    //GETTER DE COORDENADA Y EN PIXELES CON RESPECTO A LA IMAGEN
    public double getYImg() {
        return yImg;
    }

    //GETTER DE COORDENADA X (LONGITUD) EN SISTEMA DE REFERENCIA LATLON
    public double getLongitude() {
        return longitude;
    }

    //GETTER DE COORDENADA Y (LATITUD) EN SISTEMA DE REFERENCIA LATLON
    public double getLatitude() {
        return latitude;
    }

    //GETTER DE LAS COORDENADAS EN EL SISTEMA DE REFERENCIA REAL
    public double[] getRealCoordinate() {
        return new double[]{longitude, latitude};
    }
}
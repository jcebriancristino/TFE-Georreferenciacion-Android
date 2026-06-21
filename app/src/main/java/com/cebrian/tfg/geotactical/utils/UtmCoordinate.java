package com.cebrian.tfg.geotactical.utils;

public class UtmCoordinate {
    private final int zone;
    private final char band;
    private final char hemisphere;
    private final double easting;
    private final double northing;

    public UtmCoordinate(int zone, char band, char hemisphere, double easting, double northing) {
        this.zone = zone;
        this.band = band;
        this.hemisphere = hemisphere;
        this.easting = easting;
        this.northing = northing;
    }

    //GETTER DE LA ZONA DE LA COORDENADA
    public int getZone() {
        return zone;
    }

    //GETTER DE LA BANDA DE LA COORDENADA
    public char getBand() {
        return band;
    }

    //GETTER DEL HEMISFERIO DE LA COORDENADA
    public char getHemisphere() {
        return hemisphere;
    }

    //GETTER DE LA Y (NORTHING) DE LA COORDENADA
    public double getNorthing() {
        return northing;
    }
    //GETTER DE LA X (EASTING) DE LA COORDENADA
    public double getEasting() {
        return easting;
    }

}
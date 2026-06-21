package com.cebrian.tfg.geotactical.model;
import android.net.Uri;

public class GeoImage {
    private final Uri imageUri;
    private final int width;
    private final int height;
    public GeoImage(Uri imageUri, int width, int height) {
        this.imageUri = imageUri;
        this.height = height;
        this.width = width;
    }

    //CONSTRUCTOR DE OBJETO GEOIMAGE
    //GETTER DEL ANCHO DE LA IMAGEN
    public int getWidth(){
        return width;
    }
    //GETTER DEL ALTURA DE LA IMAGEN
    public int getHeight(){
        return height;
    }
    //GETTER DE LA IMAGEN
    public Uri getImageUri() { return imageUri; }
}
package com.cebrian.tfg.geotactical.model;


import com.cebrian.tfg.geotactical.utils.HomographyCalculator;

import org.opencv.core.Mat;

import java.util.ArrayList;


public class ProjectData {
    private GeoImage geoImage;
    private final ArrayList<GcpPoint> gcpPoints;
    private final ArrayList<TacticalElement> tacticalElements;
    private HomographyMatrix homographyMatrix;


    //CONSTRUCTOR DEL OBJETO PROJECT DATA
    public ProjectData() {
        this.tacticalElements = new ArrayList<>();
        this.gcpPoints = new ArrayList<>();
    }

    //BLOQUE DE METODOS 1 #####################################################################################

    //METODOS PARA GESTION DE GCP POINT Y SUS RESPECTIVOS CONJUNTOS

    //AÑADIR GCP POINT A PARTIR DE LAS 4 COORDENADAS-------------------------------------------------------GCP POINT
    public boolean addGcpPoint(double[] imageCoordinates, double[] realCoordinates){
        if(getGcpPoints().size()<4) {
            getGcpPoints().add(new GcpPoint(imageCoordinates[0], imageCoordinates[1],realCoordinates[0],realCoordinates[1]));
            return true;
        } else {
            return false;
        }
    }

    //AÑADIR UN OBJETO GCPPOINT AL CONJUNTO DE GCP POINTS --------------------------------------------- GCP POINT
    public void addGcpPoint(GcpPoint gcpPoint) {
        if (getGcpPoints().size() >= 4) {
            return;
        }
        getGcpPoints().add(gcpPoint);
    }

    //BLOQUE DE METODOS 2 #####################################################################################

    //METODOS PARA GESTION DE ELEMENTOS TACTICOS Y SUS RESPECTIVOS CONJUNTOS

    //AÑADIR UN OBJETO TACTICALELEMENT AL CONJUNTO DE ELEMENTOS TACTICOS -------------------------- TACTICAL ELEMENT
    public void addElement(TacticalElement tacticalElement){
        getTacticalElements().add(tacticalElement);
    }

    //ACTUALIZAR COORDENADAS DE UN ELEMENTO TACTICO ------------------------------------------------ TACTICAL ELEMENT
    public void updateTacticalElementPosition(TacticalElement tacticalElement, double[] imageCoordinate, HomographyCalculator homographyCalculator) {
        tacticalElement.setImageCoordinate(imageCoordinate[0], imageCoordinate[1]);
        double[] realCoordinate = homographyCalculator.calculateRealCoordinates(imageCoordinate, homographyMatrix.getHomographyMatrix());

        if (realCoordinate != null) {
            tacticalElement.setRealCoordinate(realCoordinate[0], realCoordinate[1]);
        }
    }

    //BUSQUEDA DE ELEMENTO CERCANO A UN TOQUE ------------------------------------------------------ TACTICAL ELEMENT
    public TacticalElement findElementNeartPoint(double[] lastTouchImage, double touchRadius) {
        for (TacticalElement tacticalElement : getTacticalElements()) {

            double dx = tacticalElement.getXImg() - lastTouchImage[0];
            double dy = tacticalElement.getYImg() - lastTouchImage[1];

            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance <= touchRadius) {
                return tacticalElement;
            }
        }
        return null;
    }

    //BLOQUE DE METODOS 3 #####################################################################################

    // GETTERS

    //GETTER DEL OBJETO GEOIMAGE (GUARDA LA IMAGEN) -------------------------------------------------- GEO IMAGE
    public GeoImage getGeoImage() {
        return geoImage;
    }
    //GETTER DE ARRAY TODOS LOS GCP POINTS ----------------------------------------------------------- GCP POINT
    public ArrayList<GcpPoint> getGcpPoints() {
        return gcpPoints;
    }

    //GETTER DE ARRAY TODOS LOS ELEMENTOS TACTICOS ---------------------------------------------------- ELEMENTO TACTICO
    public ArrayList<TacticalElement> getTacticalElements() {

        return tacticalElements;
    }

    //GETTER DE MATRIZ DE HOMOGRAAFIA ----------------------------------------------------------------- MATRIZ HOMOGRAFIA
    public Mat getHomographyMatrix() {
        return homographyMatrix.getHomographyMatrix();
    }

    //GETTER DE MATRIZ DE HOMOGRAAFIA INVERSA ------------------------------------------------------ MATRIZ HOMOGRAFIA INVERSA
    public Mat getInverseHomographyMatrix() {
        return homographyMatrix.getInverseHomographyMatrix();
    }

    // BLOQUE DE METODOS 4 ##############################################################################################

    // SETTERS

    //SETTER DE LA IMAGEN SELECCIONADA ------------------------------------------------------------------- IMAGEN
    public void setGeoImage(GeoImage geoImage) {
        this.geoImage = geoImage;
    }

    //SETTER DE LA MATRIZ DE HOMOGRAFIA -----------------------------------------------------------------MATRIZ DE HOMOGRAFIA
    public void setHomographyMatrix(HomographyMatrix homographyMatrix){
        this.homographyMatrix = homographyMatrix ;
    }


}

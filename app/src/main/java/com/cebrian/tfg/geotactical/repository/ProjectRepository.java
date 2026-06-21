package com.cebrian.tfg.geotactical.repository;

import com.cebrian.tfg.geotactical.model.HomographyMatrix;
import com.cebrian.tfg.geotactical.model.TacticalElement;
import com.cebrian.tfg.geotactical.model.GcpPoint;
import com.cebrian.tfg.geotactical.model.GeoImage;
import com.cebrian.tfg.geotactical.model.ProjectData;
import com.cebrian.tfg.geotactical.utils.HomographyCalculator;

import org.opencv.core.Mat;

import java.util.List;

public class ProjectRepository {
    private static final double TOUCH_RADIUS = 40;
    private ProjectData projectData = new ProjectData();



    //BLOQUE DE METODOS 1 #####################################################################################

    //METODOS PARA GESTION DE GCP POINT Y SUS RESPECTIVOS CONJUNTOS

    //AÑADIR GCP POINT A PARTIR DE LAS 4 COORDENADAS-------------------------------------------------------GCP POINT
    public boolean addGcpPoint(double[] imageCoordinates, double[]realCoordinates){
        return projectData.addGcpPoint(imageCoordinates, realCoordinates);
    }
    //AÑADIR UN OBJETO GCPPOINT AL CONJUNTO DE GCP POINTS --------------------------------------------- GCP POINT
    public void addGcpPoint(GcpPoint gcpPoint){
        projectData.addGcpPoint(gcpPoint);
    }

    //BLOQUE DE METODOS 2 #####################################################################################

    //METODOS PARA GESTION DE ELEMENTOS TACTICOS Y SUS RESPECTIVOS CONJUNTOS

    //AÑADIR UN OBJETO TACTICALELEMENT AL CONJUNTO DE ELEMENTOS TACTICOS -------------------------- TACTICAL ELEMENT
    public void addTacticalElement(TacticalElement tacticalElement){
        projectData.addElement(tacticalElement);
    }

    //AÑADIR ELEMENTO TACTICO MEDIANTE LA INTRODUCCION DE COORDENADAS  ------------------------------- TACTICAL ELEMENT
    public void addTacticalElement(double[] imageCoordinate, double[] realCoordinate, String type){
        projectData.getTacticalElements().add(new TacticalElement(
                imageCoordinate[0],
                imageCoordinate[1],
                realCoordinate[0],
                realCoordinate[1],type
        ));
    }

    //ELIMINA UN ELEMENTO TACTICO ------------------------------------------------------------------ TACTICAL ELEMENT
    public void deleteTacticalElement(TacticalElement element) {
        projectData.getTacticalElements().remove(element);
    }

    //ACTUALIZAR COORDENADAS DE UN ELEMENTO TACTICO ------------------------------------------------ TACTICAL ELEMENT
    public void updateTacticalElementPosition(TacticalElement tacticalElement, double[] imageCoordinate, HomographyCalculator homographyCalculator){
        projectData.updateTacticalElementPosition(tacticalElement, imageCoordinate, homographyCalculator);
    }

    //BUSQUEDA DE ELEMENTO CERCANO A UN TOQUE ------------------------------------------------------ TACTICAL ELEMENT
    public TacticalElement findElementNearPoint(double[] lastTouchImage) {
        return projectData.findElementNeartPoint(lastTouchImage, TOUCH_RADIUS);
    }

    // BLOQUE DE METODOS 3 ##############################################################################################

    // SETTERS

    //SETTER DE LA IMAGEN SELECCIONADA ------------------------------------------------------------------- IMAGEN
    public void setGeoImage(GeoImage geoImage) {
        projectData.setGeoImage(geoImage);
    }

    //SETTER DE LA CALCULADOR DE HOMOGRAFIA --------------------------------------------------------- CALCULADORA DE HOMOGRAFIA
    public void setHomographyMatrix(HomographyMatrix homographyMatrix) {
        projectData.setHomographyMatrix(homographyMatrix);
    }

    //BLOQUE DE METODOS 4 #####################################################################################

    // GETTERS

    //GETTER DEL OBJETO PROJECT DATA ------------------------------------------------------------------ PROJECT DATA
    public ProjectData getProjectData() {
        return projectData;
    }

    //GETTER DE ARRAY TODOS LOS GCP POINTS ----------------------------------------------------------- GCP POINT
    public List<GcpPoint> getGcpPoints() {
        return projectData.getGcpPoints();
    }
    //GETTER DE ARRAY TODOS LOS ELEMENTOS TACTICOS ---------------------------------------------------- ELEMENTO TACTICO
    public List<TacticalElement> getTacticalElements() {
        return projectData.getTacticalElements();
    }

    //GETTER DE la MATRIZ DE HOMOGRAFIA  --------------------------------------------------------------MATRIZ DE HOMOGRAFIA
    public Mat getHomographyMatrix(){
        return projectData.getHomographyMatrix();
    }

    //GETTER DE la MATRIZ DE HOMOGRAFIA -------------------------------------------------------------MATRIZ DE HOMOGRAFIA INVERSA
    public Mat getInverseHomographyMatrix(){
        return projectData.getInverseHomographyMatrix();
    }

    //GETTER DEL OBJETO GEOIMAGE (GUARDA LA IMAGEN) -------------------------------------------------- GEO IMAGE
    public GeoImage getGeoImage() {
        return projectData.getGeoImage();
    }

    //BLOQUE DE METODOS 5 #####################################################################################

    // RESET

    //RESET DEL PROYECTO COMPLETO A PROYECTO VACIO --------------------------------------------------------------
    public void resetProject() {
        projectData = new ProjectData();
    }
}


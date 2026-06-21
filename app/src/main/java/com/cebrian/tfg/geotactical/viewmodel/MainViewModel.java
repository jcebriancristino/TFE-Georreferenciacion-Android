package com.cebrian.tfg.geotactical.viewmodel;

import android.content.Context;
import android.net.Uri;


import androidx.lifecycle.ViewModel;

import com.cebrian.tfg.geotactical.model.GeoPoint;
import com.cebrian.tfg.geotactical.model.HomographyMatrix;
import com.cebrian.tfg.geotactical.model.TacticalElement;
import com.cebrian.tfg.geotactical.model.GcpPoint;
import com.cebrian.tfg.geotactical.model.GeoImage;
import com.cebrian.tfg.geotactical.repository.ProjectRepository;
import com.cebrian.tfg.geotactical.utils.CSVManager;
import com.cebrian.tfg.geotactical.utils.CoordinateConverter;
import com.cebrian.tfg.geotactical.utils.CoordinateFormatter;
import com.cebrian.tfg.geotactical.utils.HomographyCalculator;
import com.cebrian.tfg.geotactical.utils.OverlayPointMapper;


import org.opencv.core.Mat;

import java.util.List;

public class MainViewModel extends ViewModel {
    private double[] lastTouchImage;
    private final ProjectRepository repository = new ProjectRepository();
    private int imageContainerWidth;
    private int imageContainerHeight;
    private AppMode appMode = AppMode.NO_IMAGE;
    private CurrentFormat currentFormat = CurrentFormat.LATLON;
    private CoordinateConverter converter;
    private final CSVManager csvManager = new CSVManager();
    private final CoordinateFormatter coordinateFormatter = new CoordinateFormatter();
    private final OverlayPointMapper overlayPointMapper = new OverlayPointMapper();
    private final HomographyCalculator homographyCalculator = new HomographyCalculator();

    //BLOQUE 1 DE METODOS

    //CONFIGURACION INICIAL DEL SISTEMA (IMAGEN, CONTENEDOR,ETC)

    //SETTER DE LA IMAGEN INTRODUCIDA ----------------------------------------------------------IMAGEN
    public void setImage(Uri imageUri, int width, int height) throws Exception {
        //SE SETTEA LA IMAGEN
        if (imageUri == null) {
            throw new Exception("La imagen seleccionada no es válida.");
        }

        if (width <= 0 || height <= 0) {
            throw new Exception("Las dimensiones de la imagen no son válidas.");
        }

        if (imageContainerWidth <= 0 || imageContainerHeight <= 0) {
            throw new Exception("Las dimensiones del contenedor de imagen no son válidas.");
        }

        GeoImage geoImage = new GeoImage(imageUri, width, height);
        repository.setGeoImage(geoImage);
        converter = new CoordinateConverter(geoImage, imageContainerWidth, imageContainerHeight);
    }

    //SETTER DEL TAMAÑO DEL CONTENEDOR DE LA IMAGEN -----------------------------------------CONTENEDOR IMAGEN
    public void setImageContainerSize(int width, int height) throws Exception {
        if (width <= 0 || height <= 0) {
            throw new Exception("Las dimensiones del contenedor de imagen no son válidas.");
        }
        //SETTER PARA EL CONTENEDOR DE LA IMAGEN MOSTRADA
        this.imageContainerWidth = width;
        this.imageContainerHeight = height;
    }

    //GETTER DE DE LA IMAGEN CARGADA----------------------------------------------------------------------------
    public Uri getSelectedImageUri() {
        if (repository.getGeoImage() == null) {
            return null;
        }
        return repository.getGeoImage().getImageUri();
    }

    //BLOQUE 2 DE METODOS ################################################################################

    //GESTION DE TOQUE EN LA PANTALLA

    //SETTER DEL ULTIMO TOQUE EN LA PANTALLA EN COORDENADAS ABSOLUTAS (IMAGEN) -------------------------------
    public boolean setLastTouchOnImage(double lastTouchXImage, double lastTouchYImage){
        if (converter == null) {
            return false;
        }
        //COMPRUEBA QUE EL CONVERTER EXISTE Y SETEA EL ULTIMO TOQUE COMPROBANDO QUE SE HA ESTABLECIDO
        lastTouchImage = converter.convertContainerCoordinatesToImageCoordinates(lastTouchXImage, lastTouchYImage);
        return lastTouchImage != null;
    }

    //GETTER DEL ULTIMO TOQUE DE PANTALLA REALIZADO POR EL USUARIO EN COORDENADAS DE LA IMAGEN--------------------
    public double[] getLastTouchImage() {
        return lastTouchImage;
    }

    //BLOQUE 3 DE METODOS ################################################################################

    //GESTION DE LA CONVERSION DE COORDENADAS ENTRE DIFERENTES FORMATOS (CONTENEDOR-IMAGEN-UTM-LATLON)

    //GETTER DEL ULTIMO TOQUE EN PANTALLA EN EL SISTEMA DE COORDENADAS SELECCIONADO POR EL USUARIO--------------------
    public String[] getRealCoordinateFromTouchOnSelectedFormat(double [] touchOnImageCoordinates) throws Exception {
        if (converter == null) {
            throw new Exception("Primero debes cargar una imagen");
        }

        if (touchOnImageCoordinates == null || touchOnImageCoordinates.length < 2) {
            throw new Exception("No hay un punto seleccionado válido.");
        }

        double[] realCoordinates = homographyCalculator.calculateRealCoordinates(touchOnImageCoordinates, repository.getHomographyMatrix());
        return coordinateFormatter.formatRealCoordinates(realCoordinates, currentFormat, converter);
    }

    //NORMALIZAR Y CONVERTIR LAS COORDENADAS INTRODUCIDAS POR EL USUARIO EN CUALQUIER FORMATO AL FORMATO ESTANDAR
    //DE TRABAJO DEL SISTEMA-------------------------------------------------------------------------------------------
    private double[] convertInputToStandardFormatCoordinate(String xCoordinate, String yCoordinate, String zone) throws Exception {
        return coordinateFormatter.parseInputCoordinatesToLatLon(xCoordinate, yCoordinate, zone, currentFormat, converter);
    }

    //NORMALIZA Y CONVIERTE LAS COORDENADAS INTRODUCIDAS EN CUALQUIER FORMATO POR EL USUARIO EN EL FORMATO
    //ELEGIDO POR EL USUARIO----------------------------------------------------------------------------------------------
    public String[] convertInputCoordinatesToFormat(String xText, String yText, String zoneText) throws Exception {
        if (converter == null) {
            throw new Exception("Primero debes cargar una imagen");
        }
        return coordinateFormatter.convertInputCoordinatesToFormat(xText, yText, zoneText, currentFormat, converter);
    }

    //BLOQUE 4 DE METODOS ################################################################################

    //GESTION DE LOS GCP POINT

    //AÑADIR UN GCP A PARTIR DEL TOQUE EN PANTALLA Y LA COORDENADA REAL-------------------------------GCP POINT
    public void addGcpPoint( String xReal, String yReal, String zone) throws Exception {
        checkLastTouchAvailable();
        double[] latLonCoordinate = convertInputToStandardFormatCoordinate(xReal, yReal, zone);
        boolean added = repository.addGcpPoint(lastTouchImage, latLonCoordinate);

        if (added && getGcpCount() == 4) {
            appMode = AppMode.CONFIRM_GCP;
        }
    }

    //GETTER DEL NUMERO DE GCP POINT QUE TIENE EL PROYECTO------------------------------------------GCP
    public int getGcpCount() {
        return repository.getGcpPoints().size();
    }

    //GETTER DE LOS GCP POINT QUE TIENE EL PROYECTO ------------------------------------------------GCP
    public List<GcpPoint> getGcpPoints() {
        return repository.getGcpPoints();
    }

    //CONFIRMAR LOS GCP POINT CREADOS Y CREAR LA MATRIZ DE HOMOGRAFIA A PARTIR DE ELLOS ------------GCP
    public void confirmGcp(){
        Mat homographyMatrix = homographyCalculator.calculateHomographyMatrix(repository.getGcpPoints());
        repository.setHomographyMatrix(new HomographyMatrix(homographyMatrix));
        appMode = AppMode.ADD_ELEMENT;
    }

    //BLOQUE 5 DE METODOS ################################################################################

    //GESTION DE LOS ELEMENTOS TACTICOS

    //AÑADIR UN ELEMENTO TACTICO A PARTIR DEL TOQUE EN PANTALLA Y LA COORDENADA REAL   -----------ELEMENTO TACTICO
    public void addElement(String xReal, String yReal, String zone, String type) throws Exception {
        checkLastTouchAvailable();
        double[] latLonCoordinate = convertInputToStandardFormatCoordinate(xReal, yReal, zone);
        repository.addTacticalElement(lastTouchImage,
                latLonCoordinate,
                type);
    }

    //GETTER DE LOS ELEMENTOS TACTICOS QUE TIENE EL PROYECTO -------------------------------------ELEMENTO TACTICO
    public List<TacticalElement> getTacticalElements(){
        return repository.getTacticalElements();
    }

    //ACTUALIZAR LA POSICION DE UN ELEMENTO TACTICO-----------------------------------------------ELEMENTO TACTICO
    public void updateTacticalElementPosition(TacticalElement tacticalElement) throws Exception {
        checkLastTouchAvailable();
        repository.updateTacticalElementPosition(tacticalElement, lastTouchImage, homographyCalculator);
    }

    //ELIMINAR UN ELEMENTO TACTICO DENTRO DE LOS EXISTENTES EN EL PROYECTO------------------------ELEMENTO TACTICO
    public void deleteElement(TacticalElement element) {
        repository.deleteTacticalElement(element);
    }

    //BUSCAR ELEMENTO TACTICO CERCANO A UN TOQUE--------------------------------------------------ELEMENTO TACTICO
    public TacticalElement findElementNearTouch() {
        if (lastTouchImage == null) {
            return null;
        }
        return repository.findElementNearPoint(lastTouchImage);
    }

    //BLOQUE 6 DE METODOS ########################################################################################

    //PREPARACION DE OBJETOS A PINTAR EN EL OVERLAY

    //PREPARACION DE GCP POINTS PARA PINTAR EN EL OVERLAY---------------------------------------------GCP POINT
    public List<GcpPoint> getGcpPointsToPaint() {

        return overlayPointMapper.mapGcpPoints(
                getGcpPoints(),
                converter
        );
    }

    //PREPARACION DE ELEMENTOS TACTICOS PARA PINTAR EN EL OVERLAY------------------------------------ELEMENTO TACTICO
    public List<TacticalElement> getTacticalElementsToPaint() {


        return overlayPointMapper.mapTacticalElement(
                getTacticalElements(),
                converter,
                homographyCalculator,
                repository.getInverseHomographyMatrix()
        );
    }

    //BLOQUE 7 DE METODOS##################################################################################

    //GESTION DE LAS FUNCIONES DE IMPORTACION Y EXPORTACION

    //GESTION DEL EXPORTADOR
    public void exportCsv(Context context, Uri uri) throws Exception {
        csvManager.exportCsv(context, uri, repository.getProjectData());
    }

    //GESTION DEL IMPORTADOR
    public void importCsv(Context context, Uri uri) throws Exception {

        List<GeoPoint> points = csvManager.importCsv(context, uri, appMode);

        if (appMode == AppMode.ADD_GCP) {
            importGcpPoints(points);

        } else if (appMode == AppMode.ADD_ELEMENT) {
            importTacticalElements(points);
        }
    }

    //IMPORTACION DE LOS GCP POINT---------------------------------------------------------------IMPORTAR
    private void importGcpPoints(List<GeoPoint> points) {
        for (GeoPoint point : points) {
            if (!(point instanceof GcpPoint)) {
                continue;
            }

            if (getGcpCount() >= 4) {
                setAppMode(AppMode.CONFIRM_GCP);
                return;
            }

            repository.addGcpPoint((GcpPoint) point);

            if (getGcpCount() == 4) {
                setAppMode(AppMode.CONFIRM_GCP);
                return;
            }
        }
    }

    //IMPORTACION DE LOS ELEMENTOS TACTICOS-----------------------------------------------------IMPORTAR
    private void importTacticalElements(List<GeoPoint> points) {
        for (GeoPoint point : points) {
            if (point instanceof TacticalElement) {
                TacticalElement tacticalElement = (TacticalElement) point;
                repository.addTacticalElement(tacticalElement);
            }
        }
    }

    //BLOQUE 8 DE METODOS###############################################################################

    //GESTION DE ESTADOS Y FORMATOS DE LA APP

    //SETTER DEL ESTADO DE LA APLICACION----------------------------------------------------------APP MODE
    public void setAppMode(AppMode appMode) {
        this.appMode = appMode;
    }

    //GETTER DEL ESTADO DE LA APLICACION----------------------------------------------------------APP MODE
    public AppMode getAppMode() {
        return appMode;
    }

    //SETTER DEL FORMATO DE COORDENADAS----------------------------------------------------------CURRENT FORMAT
    public void setCurrentFormat(CurrentFormat currentFormat) {
        this.currentFormat = currentFormat;
    }

    //GETTER DEL FORMATO DE COORDENADAS----------------------------------------------------------CURRENT FORMAT
    public CurrentFormat getCurrentFormat() {
        return currentFormat;
    }

    //COMPROBADOR DEL CAMBIO DE FORMATO DE COORDENADAS ANTERIOR != ACTUAL -----------------------CURRENT FORMAT
    public boolean checkCoordinateFormatChanged(String selectedFormat) {
        if (selectedFormat == null) {
            return false;
        }
        CurrentFormat selectedCurrentFormat;
        if (selectedFormat.equals("LatLon")) {
            selectedCurrentFormat = CurrentFormat.LATLON;
        } else if (selectedFormat.equals("UTM")) {
            selectedCurrentFormat = CurrentFormat.UTM;
        } else {
            return false;
        }
        if (currentFormat == selectedCurrentFormat) {
            return false;
        }
        currentFormat = selectedCurrentFormat;
        return true;
    }

    //BLOQUE 8 DE METODOS ###############################################################################

    //CLASE AUXILIARES

    //COMPROBADOR DE QUE ULTIMO TOQUE NO ES NULO----------------------------------------------------ULTIMO TOQUE
    private void checkLastTouchAvailable() throws Exception {
        if (lastTouchImage == null) {
            throw new Exception("Primero selecciona un punto en la imagen");
        }
    }

    //RESET APP-------------------------------------------------------------------------------------------
    public void resetProject() {
        repository.resetProject();

        lastTouchImage = null;
        imageContainerWidth = 0;
        imageContainerHeight = 0;
        appMode = AppMode.NO_IMAGE;
        currentFormat = CurrentFormat.LATLON;
        converter = null;
    }
}
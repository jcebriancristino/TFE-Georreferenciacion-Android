package com.cebrian.tfg.geotactical.utils;
import androidx.annotation.NonNull;

import com.cebrian.tfg.geotactical.model.GeoImage;

import org.locationtech.proj4j.BasicCoordinateTransform;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.ProjCoordinate;


public class CoordinateConverter {
    private final int imageWidth;
    private final int imageHeight;
    private final int imageContainerWidth;
    private final int imageContainerHeight;
    private final double scale;
    private final double offsetX;
    private final double offsetY;
    private static final String WGS_84_PARAMS = "+proj=longlat " + "+datum=WGS84 " + "+no_defs";


    //BLOQUE 1 DE METODOS #############################################################################

    // CONSTRUCTOR--------------------------------------------------------------------------------------

    public CoordinateConverter(GeoImage image, int imageContainerWidth, int imageContainerHeight){
        validateConstructorParameters(image, imageContainerWidth, imageContainerHeight);

        this.imageWidth = image.getWidth();
        this.imageHeight = image.getHeight();
        this.imageContainerWidth = imageContainerWidth;
        this.imageContainerHeight = imageContainerHeight;
        scale = calculateScale();
        offsetX = calculateOffset(scale,imageWidth, imageContainerWidth);
        offsetY = calculateOffset(scale, imageHeight, imageContainerHeight);

    }

    //BLOQUE 2 DE METODOS #############################################################################

    //CONVERSIONES ENTRE COORDENADAS DEL CONTENEDOR DE LA IMAGEN Y LA PROPIA IMAGEN

    // METODO DE CONVERSION DE COORDENADAS DEL CONTENEDOR A COORDENADAS DE LA IMAGEN--------------------COORDENADAS DE IMAGEN
    public double[] convertContainerCoordinatesToImageCoordinates(double coordinateX, double coordinateY) {

        //COMPROBAR QUE EL TOQUE NO HA SIDO FUERA DE LA IMAGEN
        if(checkTouchOutOfRange(offsetX, offsetY, scale, imageWidth, imageHeight, coordinateX, coordinateY)){
            return null;
        }

        //CALCULO DE LAS COORDENADAS DESDE EL CONTENEDOR A LA IMAGEN ORIGINAL
        double imageX = (coordinateX - offsetX) / scale;
        double imageY = (coordinateY - offsetY) / scale;

        return new double[]{imageX, imageY};
    }

    // METODO DE CONVERSION DE COORDENADAS DE LA IMAGEN A COORDENADAS DEL CONENEDOR-------------------------COORDENADAS DE CONTENEDOR
    public double[] convertImageCoordinatesToContainer(double coordinateX, double coordinateY) {

        //CALCULO DE LAS COORDENADAS DESDE LA IMAGEN ORIGINAL AL CONTENEDOR
        double imageX = coordinateX * scale + offsetX;
        double imageY = coordinateY * scale + offsetY;

        return new double[]{imageX, imageY};
    }

    //BLOQUE 3 DE METODOS #######################################################################################

    //CALCULOS MATEMATICOS ENTRE FORAMTOS DE COORDENADAS (UTILIZANDOS PROJ4J)

    //METODO DE CONVERSION DE UTM A LATLON ----------------------------------------------------COORDENADAS LATLON
    public double[] convertUtmToLatLon(UtmCoordinate utmCoordinate)  {

        if (utmCoordinate == null) {
            throw new IllegalArgumentException("La coordenada UTM no puede ser nula.");
        }

        BasicCoordinateTransform transform = getBasicCoordinateTransform(utmCoordinate);

        ProjCoordinate source = new ProjCoordinate(utmCoordinate.getEasting(), utmCoordinate.getNorthing());

        ProjCoordinate target = new ProjCoordinate();

        transform.transform(source, target);

        double longitude = target.x;
        double latitude = target.y;

        return new double[]{longitude, latitude};
    }

    @NonNull
    private static BasicCoordinateTransform getBasicCoordinateTransform(UtmCoordinate utmCoordinate) {
        CRSFactory crsFactory = new CRSFactory();

        String utmParams = "+proj=utm " + "+zone=" + utmCoordinate.getZone() + " " + (utmCoordinate.getHemisphere() == 'S' ? "+south " : "")
                + "+datum=WGS84 " + "+units=m " + "+no_defs";

        CoordinateReferenceSystem utmCrs = crsFactory.createFromParameters("UTM", utmParams);

        CoordinateReferenceSystem wgs84Crs = crsFactory.createFromParameters("WGS84", WGS_84_PARAMS);

        return new BasicCoordinateTransform(utmCrs, wgs84Crs);
    }


    //METODO DE CONVERSION DE LATLON A UTM --------------------------------------------------------COORDENADAS UTM
    public UtmCoordinate convertLatLonToUtm(double longitude, double latitude) {

        int zone = calculateUtmZone(longitude);
        char band = calculateUtmBand(latitude);
        char hemisphere = latitude >= 0 ? 'N' : 'S';

        CRSFactory crsFactory = new CRSFactory();

        String utmParams = "+proj=utm " + "+zone=" + zone + " " + (hemisphere == 'S' ? "+south " : "") + "+datum=WGS84 "
                + "+units=m " + "+no_defs";

        CoordinateReferenceSystem wgs84Crs = crsFactory.createFromParameters("WGS84", WGS_84_PARAMS);
        CoordinateReferenceSystem utmCrs = crsFactory.createFromParameters("UTM", utmParams);
        BasicCoordinateTransform transform = new BasicCoordinateTransform(wgs84Crs, utmCrs);

        ProjCoordinate source = new ProjCoordinate(longitude, latitude);
        ProjCoordinate target = new ProjCoordinate();

        transform.transform(source, target);

        return new UtmCoordinate(zone, band, hemisphere, target.x, target.y);
    }

    //CALCULO DE LA ZONA UTM A PARTIR DE LONGITUD DE COORDENADA LATLON --------------------------COORDENADA UTM
    private int calculateUtmZone(double longitude) {
        if (longitude == 180) {
            return 60;
        }
        return (int) Math.floor((longitude + 180) / 6) + 1;
    }

    //CALCULO DE LA BANDA UTM A PARTIR DE LATITUD DE COORDENADA LATLON --------------------------COORDENADA UTM
    private char calculateUtmBand(double latitude) {

        String bands = "CDEFGHJKLMNPQRSTUVWX";
        int index = (int) Math.floor((latitude + 80) / 8);

        if (index < 0) {
            index = 0;
        }

        if (index >= bands.length()) {
            index = bands.length() - 1;
        }

        return bands.charAt(index);
    }



    // BLOQUE 4 DE METODOS ########################################################################################

    // METODOS AUXILIARES

    //METODO DE CALCULO DE LA ESCALA UTILIZADA PARA CONTENER LA IMAGEN EN EL CONTENEDOR---------------------------
    private double calculateScale(){
        return Math.min(
                (double) imageContainerWidth / imageWidth,
                (double) imageContainerHeight / imageHeight
        );
    }

    //METODO DE CALCULO DEL OFFSET PRODUCIDO ENTRE LA IMAGEN ORIGINAL Y LA IMAGEN REDIMENSIONADA-------------------
    private double calculateOffset(double scale,double sizeOriginal, double  imageViewSize){
        double sizeRescaled = scale * sizeOriginal;
        return (imageViewSize - sizeRescaled )/2.0;
    }

    //COMPROBACION TOQUE FUERA DE RANGO DE LA IMAGEN (PERO DENTRO DEL CONTENEDOR)
    private boolean checkTouchOutOfRange(double offsetX, double offsetY, double scale, int imageWidth, int imageHeight, double touchX, double touchY){
        return touchX < offsetX ||
                touchX > offsetX + scale * imageWidth ||
                touchY < offsetY ||
                touchY > offsetY + scale * imageHeight;
    }

    private void validateConstructorParameters(GeoImage image, int imageContainerWidth, int imageContainerHeight)  {

        if (image == null) {
            throw new IllegalArgumentException("La imagen no puede ser nula.");
        }

        if (image.getWidth() <= 0 || image.getHeight() <= 0) {
            throw new IllegalArgumentException("Las dimensiones de la imagen no son válidas.");
        }

        if (imageContainerWidth <= 0 || imageContainerHeight <= 0) {
            throw new IllegalArgumentException("Las dimensiones del contenedor de la imagen no son válidas.");
        }
    }
}
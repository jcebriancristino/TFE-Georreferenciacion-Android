package com.cebrian.tfg.geotactical.utils;


import com.cebrian.tfg.geotactical.viewmodel.CurrentFormat;

public class CoordinateFormatter {

    //BLOQUE 1 DE METODOS #############################################################################

    //METODOS DE VALIDACION Y EXTRACCION DE LAS COORDENADAS

    //EXTRAE LA ZONA UTM (NUMERO) DE UNA ENTRADA DE TEXTO ------------------------------COORDENADAS UTM
    public int extractUtmZone(String zoneText) throws Exception {
        if (zoneText == null || zoneText.trim().isEmpty()) {
            throw new Exception("Introduce una zona UTM válida, por ejemplo 30T");
        }

        String zoneNumberText = zoneText.replaceAll("[^0-9]", "");

        if (zoneNumberText.isEmpty()) {
            throw new Exception("Introduce una zona UTM válida, por ejemplo 30T");
        }

        int zone = Integer.parseInt(zoneNumberText);

        if (zone < 1 || zone > 60) {
            throw new Exception("La zona UTM debe estar entre 1 y 60");
        }

        return zone;
    }

    //EXTRAE LA BANDA UTM (LETRA) DE UNA ENTRADA DE TEXTO------------------------------- COORDENADAS UTM
    public char extractUtmBand(String zoneText) throws Exception {
        if (zoneText == null || zoneText.trim().isEmpty()) {
            throw new Exception("Introduce una zona UTM válida, por ejemplo 30T");
        }

        String bandText = zoneText.replaceAll("[0-9]", "").trim().toUpperCase();

        if (bandText.length() != 1) {
            throw new Exception("La zona UTM debe tener una única letra de banda. Ejemplo: 30T.");
        }

        char band = bandText.charAt(0);

        if (band < 'C' || band > 'X' || band == 'I' || band == 'O') {
            throw new Exception("La banda UTM debe estar entre C y X, sin I ni O.");
        }

        return band;
    }

    //EXTRAE EL HEMISFERIO DE LA COORDENADA A PARTIR DE LA BANDA UTM--------------------COORDENADAS LATLON
    public char getHemisphereFromBand(char band) throws Exception {
        band = Character.toUpperCase(band);

        if (band < 'C' || band > 'X' || band == 'I' || band == 'O') {
            throw new Exception("La banda UTM debe estar entre C y X, sin I ni O.");
        }

        return band >= 'N' ? 'N' : 'S';
    }
    //VALIDACION DE COORDENADAS X e Y DE COORDENADAS UTM--------------------------------COORDENADAS UTM
    private void validateXYUtmCoordinate(double easting, double northing) throws Exception {

        if (easting < 100000 || easting > 900000) {
            throw new Exception("La coordenada X UTM debe estar entre 100000 y 900000.");
        }

        if (northing < 0 || northing > 10000000) {
            throw new Exception("La coordenada Y UTM debe estar entre 0 y 10000000.");
        }
    }

    //VALIDACION DE COORDENADAS X e Y DE COORDENADAS LATLON-------------------------------------COORDENADAS LATLON
    private void validateLatLonCoordinates(double longitude, double latitude) throws Exception {
        if (longitude < -180 || longitude > 180) {
            throw new Exception("La longitud debe estar entre -180 y 180.");
        }

        if (latitude < -90 || latitude > 90) {
            throw new Exception("La latitud debe estar entre -90 y 90.");
        }
    }

    //BLOQUE 2 DE METODOS ####################################################################################
    //CONVERSION DE UNA ENTRADA DE TEXTO EN CUALQUIER FORMATO A LATLON -------------------------COORDENADAS LATLON
    public double[] parseInputCoordinatesToLatLon(String xText, String yText, String zoneText, CurrentFormat inputFormat, CoordinateConverter converter) throws Exception {

        checkConverterAvailable(converter);
        if (inputFormat == CurrentFormat.LATLON) {
            return parseLatLonCoordinate(xText, yText);
        }

        if (inputFormat == CurrentFormat.UTM) {
            UtmCoordinate utmCoordinate = parseUtmCoordinate(
                    xText,
                    yText,
                    zoneText
            );

            return converter.convertUtmToLatLon(utmCoordinate);
        }

        throw new Exception("Formato de coordenadas no reconocido.");
    }


    //CONVERSION DE TEXTO DE USUARIO A COORDENADA UTM (OBJETO AUXILIAR)
    public UtmCoordinate parseUtmCoordinate(String xText, String yText, String zoneText) throws Exception {
        int zone = extractUtmZone(zoneText);
        char band = extractUtmBand(zoneText);
        char hemisphere = getHemisphereFromBand(band);

        double easting = parseRequiredDouble(xText, "Introduce la coordenada X UTM.");
        double northing = parseRequiredDouble(yText,"Introduce la coordenada Y UTM.");
        validateXYUtmCoordinate(easting, northing);


        return new UtmCoordinate(zone, band, hemisphere, easting, northing);
    }

    //CONVERSION DE UNA ENTRADA DE TEXTO A UN DOUBLE EN LATLON --------------------------------COORDENADAS LATLON
    private double[] parseLatLonCoordinate(
            String xText,
            String yText
    ) throws Exception {

        double longitude = parseRequiredDouble(xText, "Introduce la coordenada X.");
        double latitude = parseRequiredDouble(yText, "Introduce la coordenada Y.");

        validateLatLonCoordinates(longitude, latitude);

        return new double[]{longitude, latitude};
    }

    //BLOQUE 3 DE METODOS #############################################################################
    //CONVERSION ENTRE FORMATOS DE COORDENADAS A PARTIR DE ENTRADAS DE TEXTO

    //CONVIERTE ENTRADA DE TEXTO A FORMATO SELECCIONADO------------------------------------------COORDENADAS LATLON/UTM
    public String[] convertInputCoordinatesToFormat(String xText, String yText, String zoneText,
            CurrentFormat targetFormat, CoordinateConverter converter) throws Exception {

        checkConverterAvailable(converter);

        if (targetFormat == null) {
            throw new Exception("Formato de coordenadas no reconocido.");
        }

        if (xText == null || yText == null || xText.trim().isEmpty() || yText.trim().isEmpty()) {
            return null;
        }

        if (targetFormat == CurrentFormat.LATLON) {
            return convertUtmTextToLatLonText(xText, yText, zoneText, converter);
        }

        if (targetFormat == CurrentFormat.UTM) {
            return convertLatLonTextToUtmText(xText, yText, converter);
        }

        throw new Exception("Formato de coordenadas no reconocido.");
    }

    //CONVIERTE ENTRADA DE TEXTO LATLON(DELEGA EN CONVERTER) EN COORDENADAS UTM FORMATEADAS------------- COORDENADAS UTM
    private String[] convertLatLonTextToUtmText(String xText, String yText, CoordinateConverter converter) throws Exception {
        double longitude = parseRequiredDouble(xText, "Introduce la coordenada X.");
        double latitude = parseRequiredDouble(yText, "Introduce la coordenada Y.");
        validateLatLonCoordinates(longitude,latitude);

        UtmCoordinate utmCoordinates = converter.convertLatLonToUtm(longitude, latitude);

        return formatUtmCoordinate(utmCoordinates);
    }

    //CONVIERTE ENTRADA DE TEXTO UTM (DELEGA EN CONVERTER) EN COORDENADAS LATLON FORMATEADAS------------COORDENADAS LATLON
    private String[] convertUtmTextToLatLonText(String xText, String yText, String zoneText, CoordinateConverter converter) throws Exception {

        UtmCoordinate utmCoordinates = parseUtmCoordinate(xText, yText, zoneText);
        double[] latLonCoordinates = converter.convertUtmToLatLon(utmCoordinates);

        return formatLatLonCoordinates(latLonCoordinates);

    }


    //BLOQUE 4 DE METODOS #############################################################################
    //METODOS DE PREPARACION DE COORDENADAS PARA MOSTRAR EN PANTALLA

    //FOORMATEA COORDENADAS LATLON AL FORMATO SELECCIONADO -------------------------------COORDENADAS LATLON/UTM
    public String[] formatRealCoordinates(double[] realCoordinates, CurrentFormat currentFormat, CoordinateConverter converter) throws Exception {

        checkConverterAvailable(converter);

        if (realCoordinates == null || realCoordinates.length < 2) {
            throw new Exception("Las coordenadas reales no son válidas.");
        }

        if (currentFormat == null) {
            throw new Exception("Formato de coordenadas no reconocido.");
        }

        if (currentFormat == CurrentFormat.LATLON) {
            return formatLatLonCoordinates(realCoordinates);
        }
        if (currentFormat == CurrentFormat.UTM) {
            UtmCoordinate realUTM = converter.convertLatLonToUtm(realCoordinates[0], realCoordinates[1]);
            return formatUtmCoordinate(realUTM);
        }
        throw new Exception("Formato de coordenadas no reconocido.");
    }

    //PREPARA COORDENADAS EN TEXTO A PARTIR DE COORDENADAS UTM (OBJETO AUXILIAR)---------------------COORDENADAS UTM
    private String[] formatUtmCoordinate(UtmCoordinate utmCoordinate){
        return new String[]{
                String.valueOf(Math.round(utmCoordinate.getEasting())),
                String.valueOf(Math.round(utmCoordinate.getNorthing())),
                utmCoordinate.getZone() + String.valueOf(utmCoordinate.getBand())
        };
    }

    //PREPARA COORDENADAS EN TEXTO A PARTIR DE COORDENADAS LATLON------------------------------------COORDENADAS LATLON
    private String[] formatLatLonCoordinates(double[] latLonCoordinates) throws Exception {

        if (latLonCoordinates == null || latLonCoordinates.length < 2) {
            throw new Exception("Las coordenadas LatLon no son válidas.");
        }
        return new String[]{
                String.valueOf(latLonCoordinates[0]),
                String.valueOf(latLonCoordinates[1]),
                ""
        };
    }

    //BLOQUE 5 DE METODOS #############################################################################
    //METODOS DE COMPROBACIONES AUXILIARES

    //COMPRUEBA QUE EL CONVERTER NO SEA NULO
    private void checkConverterAvailable(CoordinateConverter converter) throws Exception {
        if (converter == null) {
            throw new Exception("Primero debes cargar una imagen");
        }
    }

    //PARSEO A DOUBLE DESDE ENTRADA DE TEXTO (USADO PARA LAS COORDENADAS X E Y)
    private double parseRequiredDouble(
            String text,
            String emptyMessage
    ) throws Exception {

        if (text == null || text.trim().isEmpty()) {
            throw new Exception(emptyMessage);
        }

        try {
            return Double.parseDouble(text.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new Exception("Introduce valores numéricos válidos.");
        }
    }
}

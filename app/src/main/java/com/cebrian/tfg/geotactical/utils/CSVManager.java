package com.cebrian.tfg.geotactical.utils;

import android.content.Context;
import android.net.Uri;

import com.cebrian.tfg.geotactical.model.GeoPoint;
import com.cebrian.tfg.geotactical.model.TacticalElement;
import com.cebrian.tfg.geotactical.model.GcpPoint;
import com.cebrian.tfg.geotactical.model.ProjectData;
import com.cebrian.tfg.geotactical.viewmodel.AppMode;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CSVManager {

    private static final String CSV_HEADER = "Tipo;xImg;yImg;xReal;yReal;tipoElemento";

    // BLOQUE DE METODOS 1 ############################################################################

    // METODOS DE EXPORTACION

    //METODO DE GENERAL DE EXPORTACION----------------------------------------------------------------
    public void exportCsv(Context context, Uri uri, ProjectData projectData) throws Exception {
        validateExportParameters(context, uri, projectData);

        try  (BufferedWriter writer = createCsvWriter(context, uri)){

            writer.write(CSV_HEADER);
            writer.newLine();

            exportGcpPoints(writer, projectData);
            exportTacticalElements(writer, projectData);

            writer.flush();
        }
    }

    //METODO DE VALIDACION DE PARAMETROS INICIALES DE EXPORTACION  -----------------------------------------
    private void validateExportParameters(Context context, Uri uri, ProjectData projectData) throws Exception {
        if (context == null) {
            throw new Exception("El contexto no puede ser nulo.");
        }

        if (uri == null) {
            throw new Exception("El archivo de destino no es válido.");
        }

        if (projectData == null) {
            throw new Exception("No existen datos del proyecto para exportar.");
        }
    }

    //CREACION DEL OBJETO DE ESCRITURA DE LINEAS DE CSV -----------------------------------------------------
    private BufferedWriter createCsvWriter(Context context, Uri uri) throws Exception {
        OutputStream outputStream = context.getContentResolver().openOutputStream(uri);

        if (outputStream == null) {
            throw new Exception("No se ha podido abrir el archivo CSV.");
        }

        return new BufferedWriter(new OutputStreamWriter(outputStream));
    }

    //REDACTOR DE LINEA MODELO DE GCP POINT EXPORTADOS ---------------------------------------------------
    private String createLineGcp(GcpPoint gcpPoint) {
        return String.format(
                Locale.US,
                "GCP;%.6f;%.6f;%.6f;%.6f;-",
                gcpPoint.getXImg(),
                gcpPoint.getYImg(),
                gcpPoint.getLongitude(),
                gcpPoint.getLatitude()
        );
    }

    //REDACTOR DE LINEA MODELO DE ELEMENTOS TACTICOS EXPORTADOS ------------------------------------------
    private String createLineTacticalElement(TacticalElement tacticalElement) {
        return String.format(
                Locale.US,
                "TACTICALELEMENT;-;-;%.6f;%.6f;%s",
                tacticalElement.getLongitude(),
                tacticalElement.getLatitude(),
                tacticalElement.getType()
        );
    }

    //METODO DE EXPORTACION DE GCP POINTS ------------------------------------------------------------ GCP POINT
    private void exportGcpPoints(BufferedWriter writer, ProjectData projectData) throws Exception {
        for (GcpPoint gcp : projectData.getGcpPoints()) {
            if (gcp == null) {
                throw new Exception("Existe un GCP no válido en los datos del proyecto.");
            }
            writer.write(createLineGcp(gcp));
            writer.newLine();
        }
    }

    //METODO DE EXPORTACION DE ELEMENTOS TACTICOS  ---------------------------------------------------ELEMENTO TACTICOS
    private void exportTacticalElements(BufferedWriter writer, ProjectData projectData) throws Exception {
        for (TacticalElement tacticalElement : projectData.getTacticalElements()) {
            if (tacticalElement == null) {
                throw new Exception("Existe un Elemento Tactico no válido en los datos del proyecto.");
            }
            writer.write(createLineTacticalElement(tacticalElement));
            writer.newLine();
        }
    }

    //BLOQUE DE METODOS 2 ##########################################################################################

    //METODOS DE IMPORTACION DE DATOS

    //METODO PRINCIPAL DE IMPORTACION -----------------------------------------------------------------------------
    public List<GeoPoint> importCsv(Context context, Uri uri, AppMode appMode) throws Exception {

        validateImportParameters(context, uri, appMode);

        List<GeoPoint> points = new ArrayList<>();

        try (BufferedReader reader = createCsvReader(context, uri)) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                GeoPoint point = processCsvLine(line, lineNumber, appMode);
                if (point != null) {
                    points.add(point);
                }
            }
        }

        if (points.isEmpty()) {
            throw new Exception("El CSV no contiene datos compatibles con el modo seleccionado.");
        }
        return points;
    }

    //VALIDA LOS PARAMETROS INICIALES PARA LA IMPORTACION DE DATOS ----------------------------------------
    private void validateImportParameters(Context context, Uri uri, AppMode appMode) throws Exception {

        if (context == null) {
            throw new Exception("El contexto no puede ser nulo.");
        }

        if (uri == null) {
            throw new Exception("El archivo seleccionado no es válido.");
        }

        if (appMode == null) {
            throw new Exception("El modo de importación no puede ser nulo.");
        }
    }

    //CREACION DE READER ------------------------------------------------------------------------------ READER
    private BufferedReader createCsvReader(Context context, Uri uri) throws Exception {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);

        if (inputStream == null) {
            throw new Exception("No se ha podido abrir el archivo CSV.");
        }

        return new BufferedReader(new InputStreamReader(inputStream));
    }

    // PROCESADO DE CADA UNA DE LAS LINEAS DEL CSV ---------------------------------------------------
    private GeoPoint processCsvLine(String line, int lineNumber, AppMode appMode) throws Exception {

        line = line.trim();
        if (line.isEmpty()) {
            return null;
        }
        if (lineNumber == 1 && isHeaderLine(line)) {
            return null;
        }
        String[] fields = line.split(";",-1);
        validateCsvFields(fields, lineNumber);
        String tipo = fields[0].trim();
        if (tipo.equalsIgnoreCase("GCP")) {
            if (appMode != AppMode.ADD_GCP) {
                return null;
            }
            return parseGcpPoint(fields);
        }
        if (tipo.equalsIgnoreCase("TACTICALELEMENT")) {
            if (appMode != AppMode.ADD_ELEMENT) {
                return null;
            }
            return parseTacticalElement(fields);
        }
        throw new Exception("Tipo de registro desconocido en la línea " + lineNumber + ": " + tipo);
    }

    //DETECTA SI SE TRATA DE UNA LINEA DE CABECERA --------------------------------------------------------
    private boolean isHeaderLine(String line) {
        return line.toLowerCase().contains("tipo");
    }

    // VALIDA SI EL CSV TIENE LOS CAMPOS QUE DEBERIA -----------------------------------------------------
    private void validateCsvFields(String[] fields, int lineNumber) throws Exception {
        if (fields.length != 6) {
            throw new Exception("Formato incorrecto en la línea " + lineNumber);
        }
    }

    //PARSEO DE VARIOS STRING A GCP POINTS ---------------------------------------------------------------
    private GcpPoint parseGcpPoint(String[] fields) throws Exception {
        double xImg = parseCsvDouble(fields[1]);
        double yImg = parseCsvDouble(fields[2]);
        double xReal = parseCsvDouble(fields[3]);
        double yReal = parseCsvDouble(fields[4]);

        return new GcpPoint(xImg, yImg, xReal, yReal);
    }

    //PARSEO DE VARIOS STRING A ELEMENTO TACTICO -----------------------------------------------------------
    private TacticalElement parseTacticalElement(String[] fields) throws Exception {
        double xReal = parseCsvDouble(fields[3]);
        double yReal = parseCsvDouble(fields[4]);
        String tacticalElementType = fields[5].trim();

        if (tacticalElementType.isEmpty() || tacticalElementType.equals("-")) {
            throw new Exception("El elemento táctico no tiene tipo definido.");
        }


        return new TacticalElement(0, 0, xReal, yReal, tacticalElementType);
    }

    //PARSEO DE STRING A DOUBLE ---------------------------------------------------------------------------
    private double parseCsvDouble(String value) throws Exception {
        if (value == null || value.trim().isEmpty() || value.trim().equals("-")) {
            throw new Exception("Falta un valor numérico en el CSV.");
        }

        try {
            return Double.parseDouble(value.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new Exception("Valor numérico no válido: " + value);
        }
    }
}
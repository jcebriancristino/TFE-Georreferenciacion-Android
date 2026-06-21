package com.cebrian.tfg.geotactical.utils;

import com.cebrian.tfg.geotactical.model.GcpPoint;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

import java.util.List;

public class HomographyCalculator {
    //METODO DE CALCULO DE LA MATRIZ DE HOMOGRAFIA ----------------------------------------------------------------
    public Mat calculateHomographyMatrix(List<GcpPoint> gcpPoints) {

        if (gcpPoints == null || gcpPoints.size() < 4) {
            throw new IllegalArgumentException("Se necesitan al menos 4 puntos GCP para calcular la homografía.");
        }
        Point[] imagePointsArray = new Point[gcpPoints.size()];
        Point[] realPointsArray = new Point[gcpPoints.size()];

        for (int i = 0; i < gcpPoints.size(); i++) {

            GcpPoint gcp = gcpPoints.get(i);

            if (gcp == null) {
                throw new IllegalArgumentException("Existe un punto GCP no válido.");
            }

            imagePointsArray[i] = new Point(gcp.getXImg(), gcp.getYImg());
            realPointsArray[i] = new Point(gcp.getLongitude(), gcp.getLatitude());
        }

        MatOfPoint2f imagePoints = new MatOfPoint2f(imagePointsArray);
        MatOfPoint2f realPoints = new MatOfPoint2f(realPointsArray);

        Mat homographyMatrix;

        try {
            homographyMatrix = Calib3d.findHomography(imagePoints, realPoints);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "No se ha podido calcular la homografía. Revisa que los puntos GCP no estén repetidos ni alineados.",
                    e
            );
        }
        if (homographyMatrix.empty()) {
            throw new IllegalStateException("No se ha podido calcular la matriz de homografía.");
        }
        return homographyMatrix;
    }

    //METODO DE CALCULO DE LAS COORDENADAS REALES A TRAVES DE LA MATRIZ DE HOMOGRAFIA Y DE LAS COORDENADAS DE
    //LA IMAGEN --------------------------------------------------------------------------------------------------------
    public double[] calculateRealCoordinates(double[] imgCoordinates, Mat homographyMatrix){

        if (imgCoordinates == null || imgCoordinates.length < 2) {
            throw new IllegalArgumentException("Las coordenadas de imagen no son válidas.");
        }

        if (homographyMatrix == null || homographyMatrix.empty()) {
            throw new IllegalStateException("La matriz de homografía no está disponible.");
        }

        MatOfPoint2f imagePoint = new MatOfPoint2f(new Point(imgCoordinates[0], imgCoordinates[1]));

        MatOfPoint2f realPoint = new MatOfPoint2f();

        Core.perspectiveTransform(imagePoint, realPoint, homographyMatrix);

        Point result = realPoint.toArray()[0];

        return new double[]{result.x, result.y};
    }

    //METODO DE CALCULO DE LAS COORDENADAS REALES A TRAVES DE LA MATRIZ INVERSA DE LA MATRIZ DE  HOMOGRAFIA Y DE
    //LAS COORDENADAS DE LA IMAGEN --------------------------------------------------------------------------------------
    public double[] calculateImageCoordinates(double[] realCoordinates, Mat  inverseHomographyMatrix) {

        if (realCoordinates == null || realCoordinates.length < 2) {
            throw new IllegalArgumentException("Las coordenadas reales no son válidas.");
        }

        if (inverseHomographyMatrix == null || inverseHomographyMatrix.empty()) {
            throw new IllegalStateException("La matriz inversa de homografía no está disponible.");
        }

        MatOfPoint2f realPoint = new MatOfPoint2f(
                new Point(realCoordinates[0], realCoordinates[1])
        );

        MatOfPoint2f imagePoint = new MatOfPoint2f();

        Core.perspectiveTransform(realPoint, imagePoint, inverseHomographyMatrix );

        Point result = imagePoint.toArray()[0];

        return new double[]{result.x, result.y};
    }
}

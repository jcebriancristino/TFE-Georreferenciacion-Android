package com.cebrian.tfg.geotactical.utils;

import com.cebrian.tfg.geotactical.model.GcpPoint;
import com.cebrian.tfg.geotactical.model.TacticalElement;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

public class OverlayPointMapper {

    //PREPARACION DE LOS GCP POINT PARA PINTAR EN LA CAPA VIEW EN EL OVERLAY
    public List<GcpPoint> mapGcpPoints(List<GcpPoint> gcpPoints, CoordinateConverter converter) {

        validateGcpParameters(gcpPoints, converter);

        List<GcpPoint> gcpPointsToPaint = new ArrayList<>();

        for (GcpPoint gcpPoint : gcpPoints) {

            if (gcpPoint == null) {
                throw new IllegalArgumentException("Existe un punto GCP no válido.");
            }

            double[] point = converter.convertImageCoordinatesToContainer(gcpPoint.getXImg(), gcpPoint.getYImg());

            if (point != null && point.length >= 2) {
                gcpPointsToPaint.add(new GcpPoint(point[0], point[1], 0.0, 0.0));
            }
        }
        return gcpPointsToPaint;
    }

    //PREPARACION DE LOS ELEMENTOS TACTICOS PARA PINTAR EN LA CAPA VIEW EN EL OVERLAY
    public List<TacticalElement> mapTacticalElement(List<TacticalElement> tacticalElements, CoordinateConverter converter,
            HomographyCalculator homographyCalculator, Mat inverseHomographyMatrix) {

        validateTacticalElementsParameters(tacticalElements, converter, homographyCalculator, inverseHomographyMatrix);
        List<TacticalElement> elementsContainer = new ArrayList<>();
        for (TacticalElement tacticalElement : tacticalElements) {
            if (tacticalElement == null) {
                throw new IllegalArgumentException("Existe un elemento táctico no válido.");
            }
            double[] imageCoordinates = homographyCalculator.calculateImageCoordinates(tacticalElement.getRealCoordinate(),
                    inverseHomographyMatrix);
            if (imageCoordinates == null || imageCoordinates.length < 2) {
                throw new IllegalStateException("No se han podido calcular las coordenadas de imagen del elemento táctico.");
            }

            double[] containerPoint = converter.convertImageCoordinatesToContainer(imageCoordinates[0], imageCoordinates[1]);

            if (containerPoint != null && containerPoint.length >= 2) {
                TacticalElement tacticalElementContainer = new TacticalElement(containerPoint[0], containerPoint[1],
                        tacticalElement.getLongitude(),
                        tacticalElement.getLatitude(),
                        tacticalElement.getType()
                );
                elementsContainer.add(tacticalElementContainer);
            }
        }
        return elementsContainer;
    }

    //CONTROLA ERRORES DE LA APP DE PARAMETROS INTERNOS DE CALCULO (NO INITRODUCIDOS POR EL USUARIO
    private void validateGcpParameters(List<GcpPoint> gcpPoints, CoordinateConverter converter) {

        if (gcpPoints == null) {
            throw new IllegalArgumentException("La lista de puntos GCP no puede ser nula.");
        }

        if (converter == null) {
            throw new IllegalStateException("El conversor de coordenadas no está disponible.");
        }
    }


    //CONTROLA ERRORES DE LA APP DE PARAMETROS INTERNOS DE CALCULO (NO INITRODUCIDOS POR EL USUARIO
    private void validateTacticalElementsParameters(List<TacticalElement> tacticalElements,
                                                   CoordinateConverter converter,
                                                   HomographyCalculator homographyCalculator, Mat inverseHomographyMatrix) {

        if (tacticalElements == null) {
            throw new IllegalArgumentException("La lista de elementos tácticos no puede ser nula.");
        }

        if (converter == null) {
            throw new IllegalStateException("El conversor de coordenadas no está disponible.");
        }

        if (homographyCalculator == null) {
            throw new IllegalStateException("El calculador de homografía no está disponible.");
        }

        if (inverseHomographyMatrix == null || inverseHomographyMatrix.empty()) {
            throw new IllegalStateException("La matriz inversa de homografía no está disponible.");
        }
    }

}


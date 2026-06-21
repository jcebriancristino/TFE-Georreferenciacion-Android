package com.cebrian.tfg.geotactical.model;

import org.opencv.core.Mat;

public class HomographyMatrix {
    private final Mat homographyMatrix;
    private final Mat inverseHomographyMatrix;

    //CONSTRUCTOR
    public HomographyMatrix(Mat homographyMatrix) {
        this.homographyMatrix = homographyMatrix;
        inverseHomographyMatrix = homographyMatrix.inv();
    }

    //GETTER MATRIZ DE HOMOGRAFIA
    public Mat getHomographyMatrix(){
        return homographyMatrix;
    }

    //GETTER MATRIZ DE HOMOGRAFIA INVERSA
    public Mat getInverseHomographyMatrix() {
        return inverseHomographyMatrix;
    }
}

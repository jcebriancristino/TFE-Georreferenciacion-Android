package com.cebrian.tfg.geotactical.view;
import com.cebrian.tfg.geotactical.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import com.cebrian.tfg.geotactical.model.TacticalElement;
import com.cebrian.tfg.geotactical.model.GcpPoint;

import java.util.List;

public class OverlayView extends View {

    private List<GcpPoint> gcpPoints;
    private List<TacticalElement> tacticalElements;
    private Paint paint;
    private Bitmap imgInfanteriaAliado;
    private Bitmap imgArtilleriaAliado;
    private Bitmap imgCaballeriaAliado;
    private Bitmap imgTransmisionesAliado;
    private Bitmap imgIngenierosAliado;
    private Bitmap imgInfanteriaEnemigo;
    private Bitmap imgArtilleriaEnemigo;
    private Bitmap imgCaballeriaEnemigo;
    private Bitmap imgTransmisionesEnemigo;
    private Bitmap imgIngenierosEnemigo;


    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setupPaint();
        loadTacticalIcons();
    }

    //SETUP DEL OVERLAY ----------------------------------------------------------------------------------
    private void setupPaint() {
        paint = new Paint();
        paint.setColor(0xFFFF0000);
        paint.setStyle(Paint.Style.FILL);
    }

    //CARGA DE LAS IMAGENES DE LOS ELEMENTOS TACTICOS--------------------------------------------------------
    private void loadTacticalIcons() {
        imgInfanteriaAliado = loadAlliedScaledIcon(R.drawable.infanteria_aliado);
        imgArtilleriaAliado = loadAlliedScaledIcon(R.drawable.artilleria_aliado);
        imgCaballeriaAliado = loadAlliedScaledIcon(R.drawable.caballeria_aliado);
        imgTransmisionesAliado = loadAlliedScaledIcon(R.drawable.transmisiones_aliado);
        imgIngenierosAliado = loadAlliedScaledIcon(R.drawable.ingenieros_aliado);
        imgInfanteriaEnemigo = loadEnemyScaledIcon(R.drawable.infanteria_enemigo);
        imgArtilleriaEnemigo = loadEnemyScaledIcon(R.drawable.artilleria_enemigo);
        imgCaballeriaEnemigo = loadEnemyScaledIcon(R.drawable.caballeria_enemigo);
        imgTransmisionesEnemigo = loadEnemyScaledIcon(R.drawable.transmisiones_enemigo);
        imgIngenierosEnemigo = loadEnemyScaledIcon(R.drawable.ingenieros_enemigo);
    }

    //VALIDACION Y ESCALADO DE IMAGENES ------------------------------------------------------------------
    private Bitmap loadAlliedScaledIcon(int resourceId) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resourceId);

        if (bitmap == null) {
            return null;
        }

        return Bitmap.createScaledBitmap(bitmap, 66, 40, true);
    }


    private Bitmap loadEnemyScaledIcon(int resourceId) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resourceId);

        if (bitmap == null) {
            return null;
        }

        return Bitmap.createScaledBitmap(bitmap, 90, 90, true);
    }

    //BLOQUE DE METODOS 2 ################################################################################

    //ACTUALIZACION DE DATOS EN OVERLAY

    //ACTUALIZACION DE GCP POINT --------------------------------------------------------------------GCP POINT
    public void setGcpPoints(List<GcpPoint> gcpPoints) {
        this.gcpPoints = gcpPoints;
        invalidate();
    }

    //LIMPIAR GCP POINT -----------------------------------------------------------------------------GCP POINT
    public void cleanGcpPoints(){
        this.gcpPoints = null;
        invalidate();
    }

    //ACTUALIZACION DE ELEMENTOS TACTICOS ---------------------------------------------------------- ELEMENTOS TACTICOS
    public void setElements(List<TacticalElement> tacticalElementPoints) {
        this.tacticalElements = tacticalElementPoints;
        invalidate();
    }


    //BLOQUE 3 DE METODOS #############################################################################################

    //METODOS DE DIBUJO EN PANTALLA

    //METODO PRINCIPAL -----------------------------------------------------------------------------------------
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        drawGcpPoints(canvas);
        drawTacticalElements(canvas);
    }

    //DIBUJO DE LISTADO DE ELEMENTOS TACTICOS -----------------------------------------------------------------
    private void drawTacticalElements(Canvas canvas) {
        if (tacticalElements == null) {
            return;
        }
        for (TacticalElement tacticalElement : tacticalElements) {
            if(tacticalElement != null){
                drawTacticalElement(canvas, tacticalElement);
            }
        }
    }

    //DIBUJO DE UN UNICO ELEMENTO TACTICO ----------------------------------------------------------------------
    private void drawTacticalElement(Canvas canvas, TacticalElement tacticalElement) {
        float x = (float) tacticalElement.getXImg();
        float y = (float) tacticalElement.getYImg();

        Bitmap icon = getSymbolForTacticalElement(tacticalElement);

        if (icon != null) {
            canvas.drawBitmap(icon, x - icon.getWidth() / 2f, y - icon.getHeight() / 2f, paint);
        }
    }

    //DIBUJO DEL LISTADO DE GCP POINTS -------------------------------------------------------------------------
    private void drawGcpPoints(Canvas canvas) {
        if (gcpPoints != null) {
            for (GcpPoint gcpPoint : gcpPoints) {
                if(gcpPoint != null) {
                    canvas.drawCircle((float) gcpPoint.getXImg(), (float) gcpPoint.getYImg(), 12, paint);
                }
            }
        }
    }

    //GETTER DE LA IMAGEN CORRESPONDIENTE A UN ELEMENTO TACTICO ------------------------------------------
    private Bitmap getSymbolForTacticalElement(TacticalElement tacticalElement) {

        if (tacticalElement == null || tacticalElement.getType() == null) {
            return null;
        }

        switch (tacticalElement.getType()) {
            case "Infanteria - ALIADO":
                return imgInfanteriaAliado;

            case "Artilleria - ALIADO":
                return imgArtilleriaAliado;

            case "Caballeria - ALIADO":
                return imgCaballeriaAliado;

            case "Transmisiones - ALIADO":
                return imgTransmisionesAliado;

            case "Ingenieros - ALIADO":
                return imgIngenierosAliado;

            case "Infanteria - ENEMIGO":
                return imgInfanteriaEnemigo;

            case "Artilleria - ENEMIGO":
                return imgArtilleriaEnemigo;

            case "Caballeria - ENEMIGO":
                return imgCaballeriaEnemigo;

            case "Transmisiones - ENEMIGO":
                return imgTransmisionesEnemigo;

            case "Ingenieros - ENEMIGO":
                return imgIngenierosEnemigo;

            default:
                return null;
        }

    }
}
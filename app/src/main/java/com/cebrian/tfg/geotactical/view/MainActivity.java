package com.cebrian.tfg.geotactical.view;

import com.cebrian.tfg.geotactical.viewmodel.AppMode;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;

// import de clases visuales//
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import com.cebrian.tfg.geotactical.R;
import com.cebrian.tfg.geotactical.viewmodel.CurrentFormat;
import com.cebrian.tfg.geotactical.viewmodel.MainViewModel;
import com.cebrian.tfg.geotactical.model.TacticalElement;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {
    private MainViewModel viewModel;
    private EditText editTextX;
    private EditText editTextY;
    private EditText editTextZone;
    private Button btnMain;
    private Spinner spinnerElement;
    private Spinner spinnerCoordinateFormat;
    private Button btnExport;
    private Button btnImport;
    private Button btnReset;
    private ImageView loadImageView;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private OverlayView gcpOverlay;
    private long touchStartTime = 0;
    private static final long LONG_PRESS_TIME = 500;
    private ActivityResultLauncher<String> exportCsvLauncher;
    private ActivityResultLauncher<String[]> importCsvLauncher;
    private TacticalElement selectedTacticalElement = null;
    private static final int NORMAL_CONTAINER_COLOR = 0xFFDDDDDD;
    private static final int DELETE_CONTAINER_COLOR = 0x55FF0000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        OpenCVLoader.initLocal();

        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        initUi();

        //LLAMADAS A SELECTO DE IMAGEN
        setupImagePicker();

        //LLAMADAS A SETUPS DE EXPORTADOR E IMPORTADOR
        setupExportCsvLauncher();
        setupImportCsvLauncher();

        //LLAMADAS A SETUPS IMAGEN
        setupImageContainer();
        restoreImageIfExists();
        setupImageTouchListener();

        //LLAMADAS A SETUPS SPINNERS
        setupSpinners();

        //LLAMADAS A SETUPS BOTONES
        setupButtons();

        //INICIALIZACION DE LA UI
        updateUI();
        updateCoordinatesEntryUI();
    }

    //BLOQUE DE METODOS 1 ########################################################################################

    //INICIALIZACION DE COMPONENTES VISUALES

    //LLAMADAS A ELEMENTOS DEL LAYOUT ----------------------------------------------------------------------
    private void initUi() {
        editTextX = findViewById(R.id.editTextX);
        editTextY = findViewById(R.id.editTextY);
        editTextZone = findViewById(R.id.editTextZone);
        btnMain = findViewById(R.id.mainButton);
        spinnerElement = findViewById(R.id.spinnerElement);
        btnImport = findViewById(R.id.btnImport);
        btnExport = findViewById(R.id.btnExport);
        spinnerCoordinateFormat = findViewById(R.id.spinnerCoordinateFormat);
        loadImageView = findViewById(R.id.imageViewMap);
        gcpOverlay = findViewById(R.id.gcpOverlay);
        btnReset = findViewById(R.id.btnReset);
    }

    //SETUP DEL CONTENEDOR DE LA IMAGEN ---------------------------------------------------------------CONTENEDOR IMAGEN
    private void setupImageContainer() {
        loadImageView.setOnClickListener(v -> {
            if (viewModel.getSelectedImageUri() == null) {
                imagePickerLauncher.launch("image/*");
            }
        });
    }

    //SETUP GENERAL DE LOS BOTONES (IMPORT, EXPORT, MAIN BOTON) ----------------------------------------BOTONES
    private void setupButtons() {
        //BOTON PRINCIPAL
        setupBtnMain();
        //BOTON IMPORT
        setupBtnImport();
        //BOTON EXPORT
        setupBtnExport();
        //SETUP RESET
        setupBtnReset();
    }

    //SETUP DEL BOTON IMPORT -------------------------------------------------------------------------- BOTON IMPORT
    private void setupBtnImport() {
        btnImport.setOnClickListener(v -> {
            if (viewModel.getSelectedImageUri() == null) {
                Toast.makeText(this, "Primero debes cargar una imagen", Toast.LENGTH_SHORT).show();
                return;
            }
            importCsvLauncher.launch(new String[]{
                    "text/*",
                    "text/csv",
                    "application/csv",
                    "application/vnd.ms-excel"
            });
        });
    }

    //SETUP DEL BOTON EXPORT -------------------------------------------------------------------------- BOTON EXPORT
    private void setupBtnExport(){
        btnExport.setOnClickListener(v ->
                exportCsvLauncher.launch("datos_georreferenciacion.csv"));
    }

    //SETUP DEL BOTON PRINCIPAL ----------------------------------------------------------------------- BOTON PRINCIPAL
    private void setupBtnMain() {
        btnMain.setOnClickListener(v -> handleMainActionButtonClick());
    }

    //SETUP DE BOTON RESET --------------------------------------------------------------------------BOTON RESET
    private void setupBtnReset() {
        btnReset.setOnClickListener(v -> showResetConfirmationDialog());
    }

    //SETUP GENERAL DE LOS SPINNERS  --------------------------------------------------------------------SPINNERS
    private void setupSpinners() {
        setupSpinnerCoordinateFormat();
        setupSpinnerTacticalElement();
    }

    //SETUP SPINNER DE TIPO DE ELEMENTO TACTIO A INTRODUCIR ------------------------------------- SPINNER ELEMENTO TACTICO
    private void setupSpinnerTacticalElement() {
        String[] elementTypes = {
                "Infanteria - ALIADO",
                "Artilleria - ALIADO",
                "Caballeria - ALIADO",
                "Transmisiones - ALIADO",
                "Ingenieros - ALIADO",
                "Infanteria - ENEMIGO",
                "Artilleria - ENEMIGO",
                "Caballeria - ENEMIGO",
                "Transmisiones - ENEMIGO",
                "Ingenieros - ENEMIGO"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, elementTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerElement.setAdapter((adapter));
    }

    //SETUP SPINNER DE FORMATO DE COORDENADAS --------------------------------------------------- SPINNER FORMATO DE COORDENADAS
    private void setupSpinnerCoordinateFormat() {
        String[] coordinateFormats = {
                "LatLon",
                "UTM",
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, coordinateFormats
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCoordinateFormat.setAdapter((adapter));
        spinnerCoordinateFormat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFormat = parent.getItemAtPosition(position).toString();
                handleCoordinateFormatSelected(selectedFormat);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                handleNoCoordinateFormatSelected();
            }
        });
    }

    //SETUP DE TOQUES SOBRE LA IMAGEN CARGADA --------------------------------------------------------IMAGE TOUCH
    @SuppressLint("ClickableViewAccessibility")
    private void setupImageTouchListener() {
        loadImageView.setOnTouchListener((v, event) ->
                handleImageTouch(event));
    }

    //CUADRO DE DIALOGO CONFIRMACION DE RESET -------------------------------------------------------------------
    private void showResetConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Reiniciar proyecto")
                .setMessage("Se borrarán la imagen, los puntos GCP, los elementos tácticos y la homografía. ¿Quieres continuar?")
                .setPositiveButton("Reiniciar", (dialog, which) -> resetProject())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // BLOQUE 2 DE METODOS ####################################################################################

    //LAUNCHERS DE IMPORTACION Y EXPORTACION

    //LAUNCHER IMPORT --------------------------------------------------------------------------------IMPORT
    private void setupImportCsvLauncher() {
        importCsvLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        try {
                            viewModel.importCsv(this, uri);
                            refreshOverlay();
                            updateUI();
                            Toast.makeText(this, "CSV importado correctamente", Toast.LENGTH_SHORT).show();

                        } catch (Exception e) {
                            Toast.makeText(this, "Error al importar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
    }

    //LAUNCHER EXPORT --------------------------------------------------------------------------------EXPORT
    private void setupExportCsvLauncher() {
        exportCsvLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("text/csv"),
                uri -> {
                    if (uri != null) {
                        try {
                            viewModel.exportCsv(this, uri);
                            Toast.makeText(this, "CSV exportado correctamente", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(this, "Error al exportar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
    }

    //BLOQUE DE METODOS 3 ######################################################################################

    //GESTION DE CARGA DE IMAGEN

    //GUARDADO DE LA IMAGEN CARGADA Y SU INFORMACION DE LAS  VISTAS GENERADAS-------------------------------------
    private void saveLoadedImageInfo(Uri uri) throws Exception {
        if (loadImageView.getDrawable() == null) {
            throw new Exception("No se ha podido cargar la imagen seleccionada.");
        }

        //SE DA ANCHO Y ALTO DEL CONTENEDOR A LA VARIABLE DEL VIEW-MODEL
        int containerWidth = loadImageView.getWidth();
        int containerHeight = loadImageView.getHeight();


        viewModel.setImageContainerSize(containerWidth, containerHeight);

        //SE SETTEA EL OBJETO IMAGE DENTRO DEL OBJETO VIEW-MODEL
        int imageWidth = loadImageView.getDrawable().getIntrinsicWidth();
        int imageHeight = loadImageView.getDrawable().getIntrinsicHeight();

        viewModel.setImage(uri, imageWidth, imageHeight);
    }

    //CARGA DE LA IMAGEN DESDE LOS ARCHIVOS LOCALES-----------------------------------------------------
    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {

                        loadImageView.setImageURI(uri);

                        loadImageView.post(() -> {

                            try {
                                saveLoadedImageInfo(uri);
                                viewModel.setAppMode(AppMode.ADD_GCP);
                                updateUI();
                            } catch (Exception e) {
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                }
        );
    }

    //RE-CARGAR IMAGEN YA CARGADA PREVIAMENTE EN EL PROYECTO ---------------------------------------------
    private void restoreImageIfExists() {
        Uri imageUri = viewModel.getSelectedImageUri();

        if (imageUri != null) {
            loadImageView.setImageURI(imageUri);

            loadImageView.post(() -> {
                try {
                    saveLoadedImageInfo(imageUri);
                    refreshOverlay();
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                }

            });
        }
    }

    //BLOQUE DE METODOS 4 #####################################################################################

    //GESTION DE ACCIONES SOBRE LOS CAMPOS A RELLENAR (EDIT TEXT)

    //GETTER EDIT TEXT "X"------------------------------------------------------------------------------EDITTEXTX
    private String getXText() {
        return editTextX.getText().toString().trim();
    }

    //GETTER EDIT TEXT "Y"------------------------------------------------------------------------------EDITTEXTY
    private String getYText() {
        return editTextY.getText().toString().trim();
    }

    //GETTER EDIT TEXT "ZONE"---------------------------------------------------------------------------EDITTEXTZONE
    private String getZoneText() {
        return editTextZone.getText().toString().trim().toUpperCase();
    }

    //COMPROBACION SI EDIT TEXT X - EDIT TEXT Y ESTAN VACIOS -----------------------------------------EDITTEXTX / EDITTEXTY
    private boolean coordinateInputEmpty(){
        if (getXText().isEmpty() || getYText().isEmpty()) {
            Toast.makeText(this, "Introduce X e Y", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    //MOSTRAR COORDENADAS DE UN TOQUE EN LOS EDIT TEXT CORRESPONDIENTES --------------------------EDITTEXTX / EDITTEXTY / ZONE
    private void showTouchCoordinatesOnEditText(double[] touchOnImageCoordinates) throws Exception {
        String[] coordinate = viewModel.getRealCoordinateFromTouchOnSelectedFormat(touchOnImageCoordinates);
        if (coordinate == null) {
            return;
        }
        applyCoordinatesToEditTexts(coordinate);
    }

    //MUESTRA EN LOS EDIT TEXT LAS CADENAS DE TEXTO QUE LE INTRODUZCAN-----------------------------------------
    private void applyCoordinatesToEditTexts(String[] coordinates) {
        if (coordinates == null || coordinates.length < 3) {
            return;
        }
        editTextX.setText(coordinates[0]);
        editTextY.setText(coordinates[1]);
        editTextZone.setText(coordinates[2]);
    }


    //BLOQUE DE METODOS 5 ###################################################################################

    //METODOS DE ACCIONES PRINCIPALES DE BOTONES

    //GESTION DEL BOTON PRINICIPAL -----------------------------------------------------------------MAIN BOTON
    private void handleMainActionButtonClick() {


        if (viewModel.getAppMode() == null) {
            Toast.makeText(this, "Modo de aplicación no válido.", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (viewModel.getAppMode()) {
            case CONFIRM_GCP:
                confirmGcp();
                break;

            case ADD_GCP:
                if(!hasSelectedImagePoint()){
                    return;
                }
                addGcpFromInputs();
                break;

            case ADD_ELEMENT:
                if(!hasSelectedImagePoint()){
                    return;
                }
                addElementFromInputs();
                break;

            case NO_IMAGE:
                break;
        }
    }

    //AÑADIR GCP A PARTIR DE LAS ENTRADAS DE TEXTO ---------------------------------------------------  GCP
    private void addGcpFromInputs() {
        if (coordinateInputEmpty()) {
            return;
        }

        try {
            viewModel.addGcpPoint(getXText(), getYText(), getZoneText());
            refreshOverlay();
            updateUI();
            editTextX.setText("");
            editTextY.setText("");
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //CONFIRMAR GCP INTRODUCIDOS COMO VALIDOS ----------------------------------------------------- GCP
    private void confirmGcp() {
        try {
            viewModel.confirmGcp();
            gcpOverlay.cleanGcpPoints();
            updateUI();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //AÑADIR ELEMENTOS TACTICOS A PARTIR DE LAS ENTRADAS DE TEXTO -------------------------------- TACTICAL ELEMENT
    private void addElementFromInputs() {
        if (coordinateInputEmpty()) {
            return;
        }
        if (spinnerElement.getSelectedItem() == null) {
            Toast.makeText(this, "Selecciona un tipo de elemento táctico.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            viewModel.addElement(getXText(), getYText(), getZoneText(), spinnerElement.getSelectedItem().toString());
            refreshOverlay();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //BLOQUE DE METODOS 6 ####################################################################################

    //METODOS DE GESTION DEL OVERLAY

    //SETTEA LOS ELEMENTOS TACTICOS O LOS GCP DEL OVERLAY EN FUNCION DEL MODO DE LA APP ----------------------
    private void refreshOverlay() {
        try {
            if (viewModel.getAppMode() == AppMode.CONFIRM_GCP ||
                    viewModel.getAppMode() == AppMode.ADD_GCP) {
                gcpOverlay.setGcpPoints(viewModel.getGcpPointsToPaint());
            } else if (viewModel.getAppMode() == AppMode.ADD_ELEMENT) {
                gcpOverlay.setElements(viewModel.getTacticalElementsToPaint());
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //BLOQUE DE METODOS 7 #####################################################################################

    //METODOS DE GESTION TOQUES SOBRE LA IMAGEN

    //GESTOR PRINCIPAL DEL TOQUE SOBRE LA IMAGEN
    private boolean handleImageTouch(MotionEvent event) {
        if (viewModel.getSelectedImageUri() == null) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                try {
                    return handleTouchDown(event);
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                    return true;
                }
            case MotionEvent.ACTION_MOVE:
                try {
                    return handleTouchMove(event);
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                    return true;
                }
            case MotionEvent.ACTION_UP:
                return handleTouchUp(event);
        }
        return true;
    }

    //GESTION DEL TOQUE SOBRE LA IMAGEN ----------------------------------------------------------TOUCH DOWN
    private boolean handleTouchDown(MotionEvent event) throws Exception {
        if(!viewModel.setLastTouchOnImage(event.getX(), event.getY())){
            return true;
        }
        touchStartTime = System.currentTimeMillis();
        selectedTacticalElement = viewModel.findElementNearTouch();

        if (viewModel.getAppMode() == AppMode.ADD_ELEMENT) {
            showTouchCoordinatesOnEditText(viewModel.getLastTouchImage());
            return selectedTacticalElement != null;
        }
        return false;
    }

    //GESTION DEL MOVIMIENTO DEL TOQUE SIN LEVANTAR EL DEDO SOBRE LA IMAGEN -----------------------TOUCH MOVE
    private boolean handleTouchMove(MotionEvent event) throws Exception {
        if (selectedTacticalElement != null) {
            if (canMoveElement()) {
                boolean touchInsideImage = refreshTacticalElementPosition(event.getX(), event.getY());
                setDeleteModeBackground(!touchInsideImage);
            }
        }
        return true;
    }

    //GESTION DE LEVANTAR EL TOQUE-----------------------------------------------------------------TOUCH UP
    private boolean handleTouchUp(MotionEvent event) {
        if (selectedTacticalElement != null && canMoveElement()) {

            if (!viewModel.setLastTouchOnImage(event.getX(), event.getY())) {
                viewModel.deleteElement(selectedTacticalElement);
                refreshOverlay();
                Toast.makeText(this, "Elemento eliminado", Toast.LENGTH_SHORT).show();
            }
        }
        setDeleteModeBackground(false);
        selectedTacticalElement = null;
        touchStartTime = 0;
        return true;
    }

    //COMPRUEBA SI EXISTE UN TOQUE GUARDADO EN EL VIEWMODEL COMO ULTIMO TOQUE ----------------------LAST TOUCH
    private boolean hasSelectedImagePoint() {
        if (viewModel.getLastTouchImage() == null) {
            Toast.makeText(this, "Primero selecciona un punto en la imagen", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    //REFRESCA LAS COORDENADAS DEL EDIT TEXT Y SU DIBUJO SOBRE EL OVERLAY ----------------------------------
    private boolean refreshTacticalElementPosition(float x, float y) throws Exception {
        if (viewModel.setLastTouchOnImage(x , y)) {
            showTouchCoordinatesOnEditText(viewModel.getLastTouchImage());
            viewModel.updateTacticalElementPosition(selectedTacticalElement);
            refreshOverlay();
            return true;
        }
        return false;
    }

    //BLOQUE DE METODOS 8 ################################################################################

    //METODOS DE GESTION DE LOS SPINNER

    //METODO DE GESTION DEL FORMATO DE COORDENADAS ELEGIDO---------------------------------------CURRENT FORMAT
    private void handleCoordinateFormatSelected(String selectedFormat) {
        if (!viewModel.checkCoordinateFormatChanged(selectedFormat)) {
            return;
        }
        try {
            String[] coordinates = viewModel.convertInputCoordinatesToFormat(getXText(), getYText(), getZoneText());

            if (coordinates != null) {
                applyCoordinatesToEditTexts(coordinates);
            }
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        updateCoordinatesEntryUI();
    }

    //GESTION DE FORMATO DE COORDENADAS NO ELEGIDO ------------------------------------------------CURRENT FORMAT
    private void handleNoCoordinateFormatSelected() {
        viewModel.setCurrentFormat(CurrentFormat.LATLON);
        editTextZone.setText("");
        updateCoordinatesEntryUI();
    }

    //BLOQUE DE METODOS 9 ###############################################################################

    //GESTION DE LA UI

    //ACTUALIZA LA INTERFAZ GENERAL EN FUNCION DEL MODO DE APP ADOPTADO---------------------------MODE APP
    private void updateUI(){
        if (viewModel.getAppMode() == null) {
            return;
        }

        switch (viewModel.getAppMode()) {
            case NO_IMAGE:
                editTextX.setEnabled(false);
                editTextY.setEnabled(false);
                editTextZone.setEnabled(false);
                btnMain.setEnabled(false);
                btnExport.setEnabled(false);
                btnImport.setEnabled(false);
                btnReset.setEnabled(false);
                spinnerElement.setEnabled(false);
                spinnerCoordinateFormat.setEnabled(false);
                btnMain.setAlpha(0.5f);
                btnExport.setAlpha(0.5f);
                btnImport.setAlpha(0.5f);
                btnReset.setEnabled(false);
                btnReset.setAlpha(0.5f);
                spinnerElement.setAlpha(0.5f);
                break;

            case ADD_GCP:
                btnMain.setText(viewModel.getGcpCount() + "/4 puntos");
                btnMain.setEnabled(true);
                btnImport.setEnabled(true);
                btnExport.setEnabled(false);
                spinnerElement.setEnabled(false);
                spinnerCoordinateFormat.setEnabled(true);
                editTextX.setEnabled(true);
                editTextY.setEnabled(true);
                btnImport.setAlpha(1f);
                btnMain.setAlpha(1f);
                btnExport.setAlpha(0.5f);
                btnReset.setEnabled(true);
                btnReset.setAlpha(1f);
                spinnerElement.setAlpha(0.5f);
                break;
            case CONFIRM_GCP:
                btnMain.setText("Confirmar");
                btnMain.setEnabled(true);
                btnImport.setEnabled(true);
                btnExport.setEnabled(false);
                spinnerElement.setEnabled(false);
                spinnerCoordinateFormat.setEnabled(true);
                editTextX.setEnabled(true);
                editTextY.setEnabled(true);
                btnReset.setEnabled(true);
                btnReset.setAlpha(1f);
                btnImport.setAlpha(1f);
                btnMain.setAlpha(1f);
                btnExport.setAlpha(0.5f);
                spinnerElement.setAlpha(0.5f);
                break;

            case ADD_ELEMENT:
                btnMain.setText("Añadir elementos");
                btnMain.setEnabled(true);
                btnImport.setEnabled(true);
                btnExport.setEnabled(true);
                spinnerElement.setEnabled(true);
                spinnerCoordinateFormat.setEnabled(true);
                editTextX.setEnabled(true);
                editTextY.setEnabled(true);
                btnReset.setEnabled(true);
                btnReset.setAlpha(1f);
                btnExport.setAlpha(1f);
                spinnerElement.setAlpha(1f);
                btnImport.setAlpha(1f);
                btnMain.setAlpha(1f);
                break;
        }
    }

    //ACTUALIZA LA INTERFAZ EN FUNCION DEL TIPO DE COORDENADAS ELEGIDO ------------------------FORMATO DE COORDENADAS
    private void updateCoordinatesEntryUI() {
        if (viewModel.getCurrentFormat() == CurrentFormat.UTM) {
            editTextZone.setEnabled(true);
            editTextZone.setFocusable(true);
            editTextZone.setFocusableInTouchMode(true);
            editTextZone.setAlpha(1f);
            editTextZone.setHint("ZONA");
        } else {
            editTextZone.setText("");
            editTextZone.setEnabled(false);
            editTextZone.setFocusable(false);
            editTextZone.setFocusableInTouchMode(false);
            editTextZone.setAlpha(0.5f);
            editTextZone.setHint("-");
        }
    }

    //CAMBIO DE COLOR DEL BACKGROUND -------------------------------------------------------------------------
    private void setDeleteModeBackground(boolean deleteMode) {
        if (deleteMode) {
            loadImageView.setBackgroundColor(DELETE_CONTAINER_COLOR);
        } else {
            loadImageView.setBackgroundColor(NORMAL_CONTAINER_COLOR);
        }
    }

    // RESETEA LA UI AL INICIO ---------------------------------------------------------------------------
    private void resetUI() {
        loadImageView.setImageDrawable(null);
        gcpOverlay.cleanGcpPoints();
        gcpOverlay.setElements(null);

        editTextX.setText("");
        editTextY.setText("");
        editTextZone.setText("");

        selectedTacticalElement = null;
        touchStartTime = 0;

        spinnerCoordinateFormat.setSelection(0);
        viewModel.setCurrentFormat(CurrentFormat.LATLON);

        updateUI();
        updateCoordinatesEntryUI();
    }

    // BLOQUE DE METODOS 10 #################################################################################

    // METODOS AUXILIARES

    //COMPRUEBA SI EL TIEMPO TRANSCURRIDO ES MAYOR QUE EL TIEMPO ESTABLECIDO COMO MINIMO--------------------
    private boolean canMoveElement(){
        return System.currentTimeMillis() - touchStartTime >= LONG_PRESS_TIME;
    }

    //RESET MAINACTIVITY -----------------------------------------------------------------------------------
    private void resetProject() {
        viewModel.resetProject();
        resetUI();

        Toast.makeText(this, "Proyecto reiniciado", Toast.LENGTH_SHORT).show();
    }


}

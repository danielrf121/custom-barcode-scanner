package br.com.mbamobi;

import android.app.Activity;
import android.app.Application;
import android.content.res.Resources;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.journeyapps.barcodescanner.BarcodeView;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.Size;
import com.journeyapps.barcodescanner.camera.CameraManager;


public class ScannerActivity extends AppCompatActivity implements
    DecoratedBarcodeView.TorchListener {

    static String TORCH_ON = "torchOn";
    static String FORMATS = "formats";

    private DecoratedBarcodeView barcodeScannerView;
    private CaptureManager capture;
    private CameraManager cameraManager;

    private boolean isTorchOn = false;
    private Button switchFlashlightButton;
    private Button switchCameraButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getResourceIdentifier("activity_scanner", "layout"));
        Toolbar toolbar = findViewById(getResourceIdentifier("toolbar", "id"));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Ler código");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(getResourceIdentifier("close_camera","drawable"));

        //Initialize barcode scanner view
        barcodeScannerView = findViewById(getResourceIdentifier("zxing_barcode_scanner", "id"));
        calculateFrameSize(barcodeScannerView);

        setTorchButton();
        setSwitchCameraButton();

        //start capture
        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.decode();
    }

    private void setTorchButton() {
        barcodeScannerView.setTorchListener(this);
        this.switchFlashlightButton = findViewById(getResourceIdentifier("switch_flashlight", "id"));
        if (hasFlash()){
            if (getIntent().getBooleanExtra(TORCH_ON, false)) {
                barcodeScannerView.setTorchOn();
            }
        } else {
            switchFlashlightButton.setVisibility(View.GONE);
        }
    }

    private void setSwitchCameraButton() {
        switchCameraButton = findViewById(getResourceIdentifier("switch_camera", "id"));
        if (!hasFrontalCamera()) switchCameraButton.setVisibility(View.GONE);
    }

    public void switchCamera(View view){
        int reqCamId = getIntent().getIntExtra("SCAN_CAMERA_ID", -1);
        getIntent().putExtra("SCAN_CAMERA_ID", reqCamId == 1 ? 0 : 1);
        recreate();
    }

    private boolean hasFlash() {
        return getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    private boolean hasFrontalCamera() {
        return getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    public void switchFlashlight(View view) {
        if (isTorchOn) barcodeScannerView.setTorchOff();
        else barcodeScannerView.setTorchOn();
    }

    @Override
    public void onTorchOn() {
        isTorchOn = true;
        switchFlashlightButton.setBackgroundResource(getResourceIdentifier("lightbulb_on", "drawable"));
    }

    @Override
    public void onTorchOff() {
        isTorchOn = false;
        switchFlashlightButton.setBackgroundResource(getResourceIdentifier("lightbulb_off", "drawable"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    private int getResourceIdentifier(String name, String type) {
        Application app = getApplication();
        String package_name = app.getPackageName();
        Resources resources = app.getResources();

        return resources.getIdentifier(name, type, package_name);
    }

    private void calculateFrameSize(DecoratedBarcodeView decoratedBarcodeView) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = (int) (displayMetrics.widthPixels * .90);
        int height = (int) (displayMetrics.heightPixels * .80);
        Size size = new Size(width, height);

        int barcodeViewId = getResourceIdentifier("zxing_barcode_surface", "id");
        BarcodeView barcodeView = decoratedBarcodeView.findViewById(barcodeViewId);
        barcodeView.setFramingRectSize(size);
    }

}
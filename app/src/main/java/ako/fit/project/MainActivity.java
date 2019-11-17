package ako.fit.project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;

import ako.fit.project.core.BitmapManipulation;
import ako.fit.project.elements.DrawerElement;
import ako.fit.project.elements.MainActivityModal;

import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
import static android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;

public class MainActivity extends AppCompatActivity {

  private int REQUEST_CODE_PERMISSIONS = 10;
  private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA","android.permission.WRITE_EXTERNAL_STORAGE"};
  TextureView textureView;
  MainActivityModal mainModal = null;
  ImageCapture imageCapture = null;
  Preview preview = null;
  ImageView imageView;
  LinearLayout linearLayout;
  ConstraintLayout mainLayout = null;
  DrawerElement drawerElement = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    textureView = findViewById(R.id.view_finder);
    textureView.setSystemUiVisibility(LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES | SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    imageView = findViewById(R.id.img_main);
    mainLayout = findViewById(R.id.cl_mainLayout);
    linearLayout = findViewById(R.id.ll_layout);

    drawerElement = new DrawerElement(this);

    // Start camera
    if(allPermissionsGranted())
      startCamera();
    else
      ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
  }

  public ImageCapture getImageCapture(){ return this.imageCapture; }
  public ImageView getMainImageView(){ return this.imageView; }
  public ConstraintLayout getMainLayout(){ return this.mainLayout; }
  public ConstraintLayout.LayoutParams getMainLayoutParams(){ return (ConstraintLayout.LayoutParams)this.mainLayout.getLayoutParams(); }
  public TextureView getTextureView(){ return this.textureView; }
  public LinearLayout getLinearLayout(){ return this.linearLayout; }


  private void startCamera() {


    CameraX.unbindAll();

    Rational aspectRatio = new Rational (textureView.getWidth(), textureView.getHeight());
    Size screen = new Size(textureView.getWidth(), textureView.getHeight()); //size of the screen


    PreviewConfig pConfig = new PreviewConfig.Builder().setTargetAspectRatio(aspectRatio).setTargetResolution(screen).build();
    preview = new Preview(pConfig);

    preview.setOnPreviewOutputUpdateListener(
      new Preview.OnPreviewOutputUpdateListener() {
        //to update the surface texture we  have to destroy it first then re-add it
        @Override
        public void onUpdated(Preview.PreviewOutput output){
          ViewGroup parent = (ViewGroup) textureView.getParent();
          parent.removeView(textureView);
          parent.addView(textureView, 0);

          textureView.setSurfaceTexture(output.getSurfaceTexture());
          updateTransform();
        }
      });


    ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder().setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
      .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()).build();

    this.imageCapture = new ImageCapture(imageCaptureConfig);

    /*
    findViewById(R.id.capture_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        File file = new File(Environment.getExternalStorageDirectory() + "/" + System.currentTimeMillis() + ".jpg");
        imgCap.takePicture(file, new ImageCapture.OnImageSavedListener() {
          @Override
          public void onImageSaved(@NonNull File file) {
            String msg = "Photo capture succeeded: " + file.getAbsolutePath();
            Toast.makeText(getBaseContext(), msg,Toast.LENGTH_LONG).show();
          }

          @Override
          public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
            String msg = "Photo capture failed: " + message;
            Toast.makeText(getBaseContext(), msg,Toast.LENGTH_LONG).show();
            if(cause != null){
              cause.printStackTrace();
            }
          }
        });
      }
    });
    */

    CameraX.bindToLifecycle((LifecycleOwner)this, preview, imageCapture);
    this.mainModal = new MainActivityModal(this);
  }

  private void updateTransform(){
    /*
     * compensates the changes in orientation for the viewfinder, bc the rest of the layout stays in portrait mode.
     * methinks :thonk:
     * imgCap does this already, this class can be commented out or be used to optimise the preview
     */
    Matrix mx = new Matrix();
    float w = textureView.getMeasuredWidth();
    float h = textureView.getMeasuredHeight();

    float centreX = w / 2f; //calc centre of the viewfinder
    float centreY = h / 2f;

    int rotationDgr;
    int rotation = (int)textureView.getRotation(); //cast to int bc switches don't like floats

    switch(rotation){ //correct output to account for display rotation
      case Surface.ROTATION_0:
        rotationDgr = 0;
        break;
      case Surface.ROTATION_90:
        rotationDgr = 90;
        break;
      case Surface.ROTATION_180:
        rotationDgr = 180;
        break;
      case Surface.ROTATION_270:
        rotationDgr = 270;
        break;
      default:
        return;
    }

    mx.postRotate((float)rotationDgr, centreX, centreY);
    textureView.setTransform(mx); //apply transformations to textureview
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    //start camera when permissions have been granted otherwise exit app
    if(requestCode == REQUEST_CODE_PERMISSIONS){
      if(allPermissionsGranted()){
        startCamera();
      } else{
        Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
        finish();
      }
    }
  }

  private boolean allPermissionsGranted(){
    //check if req permissions have been granted
    for(String permission : REQUIRED_PERMISSIONS){
      if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
        return false;
      }
    }
    return true;
  }



}

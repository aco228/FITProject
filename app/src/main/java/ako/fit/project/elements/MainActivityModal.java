package ako.fit.project.elements;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.os.Environment;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCapture.OnImageCapturedListener;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.impl.utils.executor.CameraXExecutors;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ako.fit.project.MainActivity;
import ako.fit.project.R;
import ako.fit.project.api.ApiResponse;
import ako.fit.project.api.MLServer;
import ako.fit.project.api.SendImageCallback;
import ako.fit.project.core.BitmapManipulation;
import ako.fit.project.core.MainActivityElements;

import static android.widget.Toast.LENGTH_LONG;

public class MainActivityModal extends MainActivityElements {

  // pozicije su u dp
  public static final int ANIMATION_OPEN_DURATION = 250;
  private static final int TRANSLATE_Y_CLOSED = 320;
  private static final int MAIN_IMAGE_ORIGINAL_POSITION = 100;
  private static final int MAIN_IMAGE_TOP_POSITION = MAIN_IMAGE_ORIGINAL_POSITION + TakePictureButton.MARGIN_BUTTON_DISTANCE;

  private TakePictureButton button;
  private ConstraintLayout mainBoxLayout;
  private TextView twHeader;
  private TextView twSubHeader;

  private TextView twWinstonVj;
  private TextView twUpaljacVj;
  private TextView twRestVj;

  public MainActivityModal(MainActivity activity){
    super(activity);
    this.button = new TakePictureButton(this, this.activity);

    this.mainBoxLayout = activity.findViewById(R.id.cl_mainbox);
    this.twHeader = activity.findViewById(R.id.tw_boxHeader);
    this.twSubHeader = activity.findViewById(R.id.tw_subHeader);
    this.twWinstonVj = activity.findViewById(R.id.tw_vjWinston);
    this.twUpaljacVj = activity.findViewById(R.id.tw_vjUpaljac);
    this.twRestVj = activity.findViewById(R.id.tw_vjOstalo);

  }

  // Start process
  public void onCameraButtonClick(){
    this.button.startBlinking();
    new Thread(){ @Override public void run() { takePicture(); } }.run();

  }

  // End process and ready for another
  public void finished(){

  }

  // Take picture from xcamera
  public void takePicture(){

    File file = new File(Environment.getExternalStorageDirectory() + "/" + System.currentTimeMillis() + ".jpg");
    this.activity.getImageCapture().takePicture(file, new ImageCapture.OnImageSavedListener() {
      @Override
      public void onImageSaved(@NonNull File file) {
        activity.runOnUiThread(new Runnable() {
          @Override public void run() {
            try{

              //Toast.makeText(activity.getBaseContext(), file.getAbsolutePath() ,Toast.LENGTH_LONG).show();

              Bitmap currentBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
              Bitmap newBitmap = Bitmap.createScaledBitmap(currentBitmap, currentBitmap.getWidth() / 2, currentBitmap.getHeight() / 2, true);
              currentBitmap = null;
              file.delete();

              File file = new File(Environment.getExternalStorageDirectory() + "/" + System.currentTimeMillis() + ".jpg");
              FileOutputStream fOut = new FileOutputStream(file);
              newBitmap.compress(Bitmap.CompressFormat.JPEG, 50, fOut);
              fOut.flush();
              fOut.close();

              pictureIsTaken(file.getAbsolutePath());
            }
            catch (Exception e){
              onImageError("Greska prilikom optimizacije slike");
            }
          } });
      }

      @Override
      public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) { onImageError("Greska prilikom uzimanja slike");}
    });
  }

  public void onImageError(String reason){
    activity.runOnUiThread(new Runnable() {
      @Override public void run() {
        Toast.makeText(activity.getBaseContext(), reason, LENGTH_LONG).show();

        button.stopBlinking();
        finished();
        button.State = TakePictureButton.TakePictureState.Idle;
      } });
  }

  // If picture is taken, send it to server
  public void pictureIsTaken(String location){
    this.setMainImage(location);
    SendImageCallback sendImageCallback = new SendImageCallback(location) {
      @Override public void onSuccess(ApiResponse response) { activity.runOnUiThread(new Runnable() {
          @Override public void run() {

            prepareInformations(response);
            button.stopBlinking();
            button.animateToTopPosition();
            deleteImage(location);
          }
        });
      }
      @Override public void onError(String reason) { activity.runOnUiThread(new Runnable() {
        @Override public void run() {
          button.stopBlinking();
          finished();
          deleteImage(location);
          }
        });
      }
    };
    MLServer.SendImage(sendImageCallback);
  }

  // if servev responded correctly, update informations
  public void prepareInformations(ApiResponse response){
    this.twHeader.setText(response.Header);
    this.twSubHeader.setText(response.SubHeader);

    this.twWinstonVj.setText(response.WinstonVj);
    this.twUpaljacVj.setText(response.UpaljacVj);
    this.twRestVj.setText(response.OstaloVj);

    this.button.setIsWiston(response.IsWinston);
    if(response.IsWinston){
      this.twHeader.setTextColor(Color.parseColor("#ff99cc00"));
      this.twSubHeader.setTextColor(Color.parseColor("#ff99cc00"));
    }
    else{
      this.twHeader.setTextColor(Color.parseColor("#ffff4444"));
      this.twSubHeader.setTextColor(Color.parseColor("#ffff4444"));
    }
  }

  // update all animations
  public boolean _imageReachedTopPosition = false; // kako ne bi postavljali opacity pri pokretima dugmeta
  public void updateAnimation(float input){
    this.mainBoxLayout.setAlpha(input);
    this.mainBoxLayout.setTranslationY(this.convertDpToPixel((TRANSLATE_Y_CLOSED * (1-input))));

    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)this.activity.getMainImageView().getLayoutParams();
    params.bottomMargin = (int)(this.convertDpToPixel(MAIN_IMAGE_ORIGINAL_POSITION) + (this.convertDpToPixel(TakePictureButton.MARGIN_BUTTON_DISTANCE) * (input)));
    if(_imageReachedTopPosition)
      this.activity.getMainImageView().setAlpha(input);

    this.activity.getLinearLayout().setAlpha(input);
  }

  // set image on main activity
  public void setMainImage(String location){
    File imgFile = new  File(location);
    this._imageReachedTopPosition = false;
    if(imgFile.exists()){

      Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
      myBitmap = BitmapManipulation.rotateImage(myBitmap, 90);
      this.activity.getMainImageView().setImageBitmap(myBitmap);
      this.activity.getMainImageView().animate().alpha(1).setDuration(800).setListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
          super.onAnimationEnd(animation);

        }
      }).start();
    }
  }
  public void deleteImage(String location){
    File file = new File(location);
    file.delete();
  }




}

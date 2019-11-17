package ako.fit.project.elements;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

import ako.fit.project.MainActivity;
import ako.fit.project.R;
import ako.fit.project.core.MainActivityElements;

public class TakePictureButton extends MainActivityElements implements View.OnTouchListener {

  private static final String TAG = "takepicturebutton";
  private static final int MARGIN_BOTTOM_OPEN = 280;
  private static final int MARGIN_BOTTOM_CLOSED = 80;
  public static final int MARGIN_BUTTON_DISTANCE = MARGIN_BOTTOM_OPEN - MARGIN_BOTTOM_CLOSED;

  public enum TakePictureState{
    Idle,
    WaitingServerResponse,
    Animating,
    DisplayedModal
  }

  public TakePictureState State = TakePictureState.Idle;
  ConstraintLayout layout;
  MainActivityModal modal;
  ImageView buttonInsideImage;

  public TakePictureButton(MainActivityModal modal, MainActivity activity) {
    super(activity);
    this.layout = this.activity.findViewById(R.id.cl_cameraButton);
    this.buttonInsideImage = this.activity.findViewById(R.id.img_buttonInside);
    this.modal = modal;

    //this.layout.setOnClickListener(this);

    this.layout.setOnTouchListener(this);

  }

  /*
    +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
    PUBLICS
   */

  public ConstraintLayout.LayoutParams getLayoutParams(){
    return (ConstraintLayout.LayoutParams) layout.getLayoutParams();
  }

  public void setLayoutParams(ConstraintLayout.LayoutParams params){
    this.layout.setLayoutParams(params);
  }

  public float getPrecentigeToBottom(){
    double total = (MARGIN_BOTTOM_OPEN - MARGIN_BOTTOM_CLOSED) * 1.0;
    int currentDistance = (int)this.convertPixelsToDp(this.getLayoutParams().bottomMargin) - MARGIN_BOTTOM_CLOSED;
    return currentDistance / (float)total;
  }

  public void setIsWiston(boolean value){
    if(value)
      this.buttonInsideImage.setImageResource(R.drawable.shape_camerabutton_success);
    else
      this.buttonInsideImage.setImageResource(R.drawable.shape_camerabutton_error);
  }


  /*
    +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
    ANIMATIONS
   */

  public void animateToTopPosition(){
    final TakePictureState cacheState = this.State;
    this.State = TakePictureState.Animating;
    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) layout.getLayoutParams();
    final float startPosition = params.bottomMargin;

    Animation animation = new Animation(){
      @Override
      protected void applyTransformation(float interpolatedTime, Transformation t) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) layout.getLayoutParams();
        params.bottomMargin = (int)(((convertDpToPixel(MARGIN_BOTTOM_OPEN) - startPosition) * interpolatedTime) + startPosition);
        layout.setLayoutParams(params);
        modal.updateAnimation(getPrecentigeToBottom());
        animateInsideImage();
      }

    };

    animation.setAnimationListener(new Animation.AnimationListener() {
      @Override public void onAnimationStart(Animation animation) { }
      @Override public void onAnimationEnd(Animation animation) {
        State = TakePictureState.DisplayedModal;
        modal._imageReachedTopPosition = true;
      }
      @Override public void onAnimationRepeat(Animation animation) { }
    });

    animation.setDuration(MainActivityModal.ANIMATION_OPEN_DURATION);
    this.layout.startAnimation(animation);
  }


  public void animateToBottomPosition(){
    this.State = TakePictureState.Animating;
    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) layout.getLayoutParams();
    final float startPosition = params.bottomMargin;

    Animation animation = new Animation(){
      @Override
      protected void applyTransformation(float interpolatedTime, Transformation t) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) layout.getLayoutParams();
        params.bottomMargin = (int)(((convertDpToPixel(MARGIN_BOTTOM_CLOSED) - startPosition) * interpolatedTime) + startPosition);
        layout.setLayoutParams(params);
        modal.updateAnimation(getPrecentigeToBottom());
        animateInsideImage();
      }
    };

    TakePictureButton self = this;
    animation.setAnimationListener(new Animation.AnimationListener() {
      @Override public void onAnimationStart(Animation animation) { }
      @Override public void onAnimationEnd(Animation animation) {
        State = TakePictureState.Idle;
        modal.finished();
      }
      @Override public void onAnimationRepeat(Animation animation) { }
    });


    animation.setDuration((int)(MainActivityModal.ANIMATION_OPEN_DURATION * this.getPrecentigeToBottom()));
    this.layout.startAnimation(animation);
  }

  public void animateInsideImage(){
    this.buttonInsideImage.setAlpha(this.getPrecentigeToBottom());
  }

  private Animation blinkingAnimation;
  private TakePictureState cacheState;

  public void startBlinking(){
    cacheState = this.State;
    this.State = TakePictureState.WaitingServerResponse;

    blinkingAnimation = new AlphaAnimation(0.0f, 1.0f);
    blinkingAnimation.setDuration(600); //You can manage the blinking time with this parameter
    blinkingAnimation.setStartOffset(20);
    blinkingAnimation.setRepeatMode(Animation.REVERSE);
    blinkingAnimation.setRepeatCount(Animation.INFINITE);
    this.layout.startAnimation(blinkingAnimation);
  }

  public void stopBlinking(){
    this.State = cacheState;
    blinkingAnimation.cancel();
    this.layout.animate().alpha(1).setDuration(50).start();
  }

  /*
    +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
    TOUCH
  */

  private int _yDelta;

  @Override
  public boolean onTouch(View v, MotionEvent event) {

    // ne treba da obradjujemo event
    if(this.State == TakePictureState.Animating || this.State == TakePictureState.WaitingServerResponse)
      return false;

    final ConstraintLayout.LayoutParams params = this.getLayoutParams();
    final int Y = (int) event.getRawY();
    int newValue = 0;
    final int maxValue = (int)this.convertDpToPixel(MARGIN_BOTTOM_OPEN);
    final int minValue = (int)this.convertDpToPixel(MARGIN_BOTTOM_CLOSED);
    final double totalValue = (maxValue - minValue) * 1.0;

    switch (event.getAction() & MotionEvent.ACTION_MASK){
      // release
      case MotionEvent.ACTION_UP:

        if(this.State == TakePictureState.Idle) {
          this.modal.onCameraButtonClick();
          return false;
        }

        // in case that modal is opened

        newValue = (Y - _yDelta) * -1;
        double proc = ((newValue - minValue) * 1.0) / totalValue;
        Log.d(TAG, "PROC=" + proc);

        if(proc == 1)
          this.animateToBottomPosition();
        else{
          if(proc >= 0.6)
            this.animateToTopPosition();
          else
            this.animateToBottomPosition();
        }

        return false;

      case MotionEvent.ACTION_DOWN:

        if(this.State != TakePictureState.DisplayedModal)
          return true;

        _yDelta = Y + params.bottomMargin;
        return true;
      case MotionEvent.ACTION_MOVE:

        if(this.State != TakePictureState.DisplayedModal)
          return true;

        newValue = (Y - _yDelta) * -1;
        boolean isAnimationFinished = false;

        if(newValue >= maxValue)
          newValue = maxValue;

        // zavrsili smo animaciju
        if(newValue <= minValue)
          newValue = minValue;

        Log.d(TAG, "onMove newValue: " + newValue);
        Log.d(TAG, "onMove opened: " + this.convertDpToPixel(MARGIN_BOTTOM_OPEN));
        Log.d(TAG, "onMove closed: " + this.convertDpToPixel(MARGIN_BOTTOM_CLOSED));
        Log.d(TAG, "onMove proc: " + this.getPrecentigeToBottom());

        modal.updateAnimation(this.getPrecentigeToBottom());
        animateInsideImage();
        params.bottomMargin =  newValue;
        this.setLayoutParams(params);

        return true;
      default:
        break;
    }

    return false;
  }


}

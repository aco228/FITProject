package ako.fit.project.core;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.renderscript.RenderScript;
import android.util.DisplayMetrics;
import android.view.View;

import ako.fit.project.MainActivity;

public abstract  class MainActivityElements {

  protected  MainActivity activity = null;

  public MainActivityElements(MainActivity activity){
    this.activity = activity;
  }

  public MainActivity getActivity(){
    return  this.activity;
  }

  public float convertPixelsToDp(final float px) {
    return (int) (px / Resources.getSystem().getDisplayMetrics().density);
  }

  public float convertDpToPixel(final float dp) {
    return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
  }

  public static float convertPixelsToDpS(final float px) {
    return (int) (px / Resources.getSystem().getDisplayMetrics().density);
  }

  public static float convertDpToPixelS(final float dp) {
    return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
  }


}

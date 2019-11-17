package ako.fit.project.elements;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import ako.fit.project.core.MainActivityElements;

public class DrawerOverride extends DrawerLayout {

  private RecyclerView recyclerView;

  public void setRecycler(RecyclerView recyclerView){
    this.recyclerView = recyclerView;
  }

  public DrawerOverride(@NonNull Context context) {
    super(context);
  }

  public DrawerOverride(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public DrawerOverride(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
    int topPositionInDP = (int)MainActivityElements.convertDpToPixelS(160); // totalrno random
    if(ev.getX() >= topPositionInDP && ev.getX() <= (topPositionInDP + recyclerView.getHeight()))
      return false;

    return super.onInterceptTouchEvent(ev);
  }


  /*
  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    if(ev.getX() >= MainActivityElements.convertDpToPixelS(50) && ev.getX() <= MainActivityElements.convertDpToPixelS(150))
      return false;
    return super.onTouchEvent(ev);
  }
   */
}

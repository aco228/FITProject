package ako.fit.project;

import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.transition.Transition;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;

import ako.fit.project.api.MLServer;

public class ImageActivity extends AppCompatActivity {

  ImageView imageView;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_image);


    //this.postponeEnterTransition();
    this.imageView = findViewById(R.id.ai_imageview);

    byte[] byteArray = getIntent().getByteArrayExtra("bitmap");
    Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    this.imageView.setImageBitmap(bmp);

    /*
    Picasso picasso = new Picasso.Builder(this)
      .downloader(new OkHttp3Downloader(MLServer.getClient()))
      .build();
    picasso
      .load(this.getIntent().getExtras().getString("url"))
      //.fit()
      //.centerCrop()
      .into(new Target() {
        @Override public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
          imageView.setImageBitmap(bitmap);
          //startPostponedEnterTransition();

          (new File(getIntent().getExtras().getString("url"))).delete();
        }
        @Override public void onBitmapFailed(Exception e, Drawable errorDrawable) {}
        @Override public void onPrepareLoad(Drawable placeHolderDrawable) { }
      });
     */
  }
}

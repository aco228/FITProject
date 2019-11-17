package ako.fit.project.adapters;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.view.PointerIconCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import ako.fit.project.R;
import ako.fit.project.api.MLServer;
import ako.fit.project.core.RoundedImageView;
import ako.fit.project.elements.DrawerElement;

public class AdapterImageView extends RecyclerView.ViewHolder implements View.OnClickListener {

  private static final String TAG = "adapterImageView";
  public View view = null;
  public CardView cardView = null;
  public ImageView imageView = null;
  DrawerElement drawerElement = null;
  public String imageLocation = "";

  public AdapterImageView(@NonNull View itemView, DrawerElement de) {
    super(itemView);
    this.drawerElement = de;
    this.view = itemView;
    this.cardView = this.view.findViewById(R.id.cardView);
    this.imageView = view.findViewById(R.id.cardViewImageView);

    this.cardView.setOnClickListener(this);
  }

  public void setImage(String location){
    this.imageLocation = MLServer.SERVER + "/api/images/" + location;
    Log.d(TAG, this.imageLocation);

    Picasso picasso = new Picasso.Builder(this.view.getContext())
      .downloader(new OkHttp3Downloader(MLServer.getClient()))
      .build();
      picasso
        .load(this.imageLocation)
        .fit()
        .centerCrop()
        .into(this.imageView);
  }


  @Override
  public void onClick(View v) {
    drawerElement.onCardViewClick(this);
  }
}

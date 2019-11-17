package ako.fit.project.elements;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.navigation.NavigationView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import ako.fit.project.ImageActivity;
import ako.fit.project.MainActivity;
import ako.fit.project.R;
import ako.fit.project.adapters.AdapterImageView;
import ako.fit.project.adapters.RecyclerViewAdapter;
import ako.fit.project.api.GetImagesCallback;
import ako.fit.project.api.MLServer;
import ako.fit.project.core.MainActivityElements;

import static androidx.recyclerview.widget.ItemTouchHelper.LEFT;
import static androidx.recyclerview.widget.ItemTouchHelper.RIGHT;

public class DrawerElement extends MainActivityElements {

  DrawerOverride drawerLayout;
  NavigationView navigationView;
  RecyclerView recyclerView;
  SwipeRefreshLayout swipeRefreshLayout;
  RecyclerViewAdapter recyclerViewAdapter;
  boolean isLoading = false;

  public DrawerElement(MainActivity activity) {
    super(activity);

    navigationView = this.activity.findViewById(R.id.navigationView);
    recyclerView = this.activity.findViewById(R.id.recycler);
    this.recyclerViewAdapter = new RecyclerViewAdapter(this);
    recyclerView.setAdapter(this.recyclerViewAdapter);
    drawerLayout = this.activity.findViewById(R.id.drawerLayout);
    drawerLayout.setRecycler(this.recyclerView);

    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.activity);
    linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
    recyclerView.setLayoutManager(linearLayoutManager);

    swipeRefreshLayout = this.activity.findViewById(R.id.swiperRefresh);
    swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override public void onRefresh() {
        getServerResponse();
      }
    });

    this.drawerListeners();
  }


  public void getServerResponse(){
    if(isLoading)
      return;

    isLoading = true;
    swipeRefreshLayout.setRefreshing(true);
    GetImagesCallback callback = new GetImagesCallback() {
      @Override
      public void onError(String error) {
        activity.runOnUiThread(new Runnable() {
          @Override public void run() {
            Toast.makeText(activity, "ERROR: " + error, Toast.LENGTH_LONG).show();
            swipeRefreshLayout.setRefreshing(false);
            isLoading = false;
          }
        });
      }

      @Override
      public void onSuccess(List<String> result) {
        activity.runOnUiThread(new Runnable() {
          @Override public void run() {
            isLoading = false;
            swipeRefreshLayout.setRefreshing(false);
            recyclerViewAdapter.setData(result);
            recyclerViewAdapter.notifyDataSetChanged();
          }
        });
      }
    };

    MLServer.getImages(callback);
  }

  public void drawerListeners(){
    drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
      @Override public void onDrawerSlide(@NonNull View drawerView, float slideOffset) { }
      @Override public void onDrawerStateChanged(int newState) {}
      @Override public void onDrawerOpened(@NonNull View drawerView) {
        getServerResponse();
      }
      @Override public void onDrawerClosed(@NonNull View drawerView) {
        recyclerViewAdapter.setData(new ArrayList<String>());
        recyclerViewAdapter.notifyDataSetChanged();
      }
    });
  }

  public void onCardViewClick(AdapterImageView cv){
    ActivityOptionsCompat option = ActivityOptionsCompat
      .makeSceneTransitionAnimation(this.activity, cv.imageView, "imageView");

    Bitmap bitmap = ((BitmapDrawable)cv.imageView.getDrawable()).getBitmap();
    String url = createTemporaryFile(bitmap);

    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
    byte[] byteArray = stream.toByteArray();

    Bundle bundle = option.toBundle();

    Intent intent = new Intent(this.activity, ImageActivity.class);
    intent.putExtra("url", url);
    intent.putExtra("bitmap", byteArray);
    this.activity.startActivity(intent, bundle);
  }

  public String createTemporaryFile(Bitmap bitmap) {
    String fileName = "myImage";//no .png or .jpg needed
    try {

      File file = new File(Environment.getExternalStorageDirectory() + "/" + System.currentTimeMillis() + ".jpg");
      FileOutputStream fOut = new FileOutputStream(file);
      bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
      fOut.flush();
      fOut.close();
      fileName = file.getAbsolutePath();

    } catch (Exception e) {
      e.printStackTrace();
      fileName = null;
    }
    return fileName;
  }


}

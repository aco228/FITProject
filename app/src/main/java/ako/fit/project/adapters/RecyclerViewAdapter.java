package ako.fit.project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ako.fit.project.R;
import ako.fit.project.core.RoundedImageView;
import ako.fit.project.elements.DrawerElement;

public class RecyclerViewAdapter extends RecyclerView.Adapter<AdapterImageView> {

  private List<String> data = new ArrayList<>();
  public DrawerElement drawerElement = null;

  public RecyclerViewAdapter(DrawerElement el){
    this.drawerElement = el;
  }

  @NonNull
  @Override
  public AdapterImageView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_image_element, parent, false);
    AdapterImageView adapter = new AdapterImageView(view, this.drawerElement);
    return adapter;
  }

  public void setData(List<String> data){ this.data = data; }

  @Override
  public void onBindViewHolder(@NonNull AdapterImageView holder, int position) {
    holder.setImage(this.data.get(position));
  }

  @Override
  public int getItemCount() {
    return this.data.size();
  }
}

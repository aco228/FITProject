package ako.fit.project.api;

import java.util.List;

public abstract class GetImagesCallback {

  public abstract void onError(String error);
  public abstract void onSuccess(List<String> result);

}

package ako.fit.project.api;

public abstract class SendImageCallback {
  public String fileLocation= "";

  public SendImageCallback(String fileLocation){
    this.fileLocation = fileLocation;
  }

  public abstract void onSuccess(ApiResponse response);
  public abstract void onError(String reason);
}

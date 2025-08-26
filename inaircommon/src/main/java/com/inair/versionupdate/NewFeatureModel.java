package com.inair.versionupdate;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Keep;

@Keep
public class NewFeatureModel implements Parcelable {
   private String title;
   private List<String> desList;

   public NewFeatureModel(String title, List<String> desList) {
      this.title = title;
      this.desList = desList;
   }

   protected NewFeatureModel(Parcel in) {
      title = in.readString();
      desList = new ArrayList<>();
      in.readStringList(desList);
   }

   public static final Creator<NewFeatureModel> CREATOR = new Creator<NewFeatureModel>() {
      @Override
      public NewFeatureModel createFromParcel(Parcel in) {
         return new NewFeatureModel(in);
      }

      @Override
      public NewFeatureModel[] newArray(int size) {
         return new NewFeatureModel[size];
      }
   };

   public String getTitle() {
      return title;
   }

   public List<String> getDesList() {
      return desList;
   }

   @Override
   public int describeContents() {
      return 0;
   }

   @Override
   public void writeToParcel(Parcel dest, int flags) {
      dest.writeString(title);
      dest.writeStringList(desList);
   }
}

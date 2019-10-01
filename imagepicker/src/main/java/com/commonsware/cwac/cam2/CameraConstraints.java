/***
 Copyright (c) 2016 CommonsWare, LLC

 Licensed under the Apache License, Version 2.0 (the "License"); you may
 not use this file except in compliance with the License. You may obtain
 a copy of the License at http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.commonsware.cwac.cam2;

import android.media.CamcorderProfile;
import android.os.Build;
import com.commonsware.cwac.cam2.util.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CameraConstraints {
  private static final ArrayList<CameraConstraints> CONSTRAINTS=
    new ArrayList<>();
  private static final CameraConstraints DEVICE_CONSTRAINT;
  private final Pattern manufacturer;
  private final Pattern product;
  private final Pattern model;
  private final boolean supportsCameraTwo;
  private final int highCamcorderProfile;
  private final boolean disableFocusMode;
  private final int cameraDisplayOrientation;
  private final boolean supportsFFC;
  private final boolean supportsFFCVideo;
  private final boolean supportsRFC;
  private final boolean supportsRFCVideo;
  private final ArrayList<Size> previewFFCSizeWhitelist;
  private final ArrayList<Size> previewRFCSizeWhitelist;
  private final ArrayList<Size> pictureFFCSizeWhitelist;
  private final ArrayList<Size> pictureRFCSizeWhitelist;

  static {
    add(new Builder()
      .product("sdk_phone_x86")
      .supportsCameraTwo(false)
      .build());
    add(new Builder()
      .manufacturer("Amazon")
      .product("full_ford")
      .supportsCameraTwo(false)
      .highCamcorderProfile(CamcorderProfile.QUALITY_1080P)
      .build());
    add(new Builder()
      .manufacturer("asus")
      .product("razor")
      .supportsCameraTwo(false)
      .supportsRFCVideo(false)
      .build());
    add(new Builder()
      .manufacturer("asus")
      .product("US_epad")
      .highCamcorderProfile(CamcorderProfile.QUALITY_480P)
      .build());
    add(new Builder()
      .manufacturer("htc")
      .product("volantis")
      .disableFocusMode(true)
      .build());
    add(new Builder()
      .manufacturer("HTC")
      .product("htc_mecha")
      .supportsFFC(false)
      .supportsFFCVideo(false)
      .supportsRFCVideo(false)
      .build());
    add(new Builder()
      .manufacturer("htc")
      .product("volantisg")
      .disableFocusMode(true)
      .build());
    add(new Builder()
      .manufacturer("HUAWEI")
      .product("KIW-L24")
      .supportsCameraTwo(false)
      .highCamcorderProfile(CamcorderProfile.QUALITY_1080P)
      .build());
    add(new Builder()
      .manufacturer("LGE")
      .product("bullhead")
      .supportsCameraTwo(false)
      .build());
    add(new Builder()
      .manufacturer("LGE")
      .product("g3_tmo_us")
      .supportsFFCVideo(false)
      .supportsRFCVideo(false)
      .build());
    add(new Builder()
      .manufacturer("LGE")
      .product("hammerhead")
      .supportsCameraTwo(false)
      .build());
    add(new Builder()
      .manufacturer("LGE")
      .product("palman")
      .supportsCameraTwo(false)
      .previewFFCSizeWhitelist(new Size(1280,720))
      .build());
    add(new Builder()
      .manufacturer("LGE")
      .product("p1_global_com")
      .supportsCameraTwo(false)
      .build());
    add(new Builder()
      .manufacturer("LGE")
      .product("p1_usc_us")
      .supportsCameraTwo(false)
      .build());
    add(new Builder()
      .manufacturer("LGE")
      .product("pplus_tmo_us")
      .supportsCameraTwo(false)
      .build());
    add(new Builder()
      .manufacturer("motorola")
      .product("surnia_retca")
      .supportsCameraTwo(false)
      .previewRFCSizeWhitelist(new Size(1280,720))
      .build());
    add(new Builder()
      .manufacturer("NVIDIA")
      .product("sb_na_wf")
      .supportsCameraTwo(false)
      .build());
    add(new Builder()
      .manufacturer("OnePlus")
      .model("ONE E1005")
      .supportsCameraTwo(false)
      .build());
    add(new Builder()
      .manufacturer("samsung")
      .product("baffinssvj")
      .previewRFCSizeWhitelist(new Size(1280,720))
      .previewFFCSizeWhitelist(new Size(720,480))
      .build());
    add(new Builder()
      .manufacturer("samsung")
      .product("chagallwifixx")
      .supportsCameraTwo(false)
      .build());
    add(new Builder()
      .manufacturer("samsung")
      .product("ha3gub")
      .supportsCameraTwo(false)
      .build());
    add(new Builder()
      .manufacturer("samsung")
      .product("mantaray")
      .supportsCameraTwo(false)
      .build());
    add(new Builder()
      .manufacturer("samsung")
      .product("m0xx")
      .highCamcorderProfile(CamcorderProfile.QUALITY_QCIF)
      .build());
    add(new Builder()
      .manufacturer("samsung")
      .product("serranoltexx")
      .previewRFCSizeWhitelist(new Size(960, 720))
      .supportsFFC(false)
      .highCamcorderProfile(CamcorderProfile.QUALITY_480P)
      .build());
    add(new Builder()
      .manufacturer("samsung")
      .product("sf2wifixx")
      .cameraDisplayOrientation(0)
      .build());
    add(new Builder()
      .manufacturer("samsung")
      .product("t03gxx")
      .supportsFFCVideo(false)
      .build());
    add(new Builder()
      .manufacturer("samsung")
      .product("zerofltexx")
      .supportsCameraTwo(false)
      .build());
    add(new Builder()
      .manufacturer("Sony")
      .product("C1505")
      .supportsFFCVideo(false)
      .supportsRFCVideo(false)
      .build());
    add(new Builder()
      .manufacturer("Sony")
      .product("C6603")
      .disableFocusMode(true)
      .highCamcorderProfile(CamcorderProfile.QUALITY_480P)
      .build());
    add(new Builder()
      .manufacturer("Sony")
      .product("C6802")
      .disableFocusMode(true)
      .build());
    add(new Builder()
      .manufacturer("Sony")
      .product("D5803")
      .disableFocusMode(true)
      .highCamcorderProfile(CamcorderProfile.QUALITY_480P)
      .build());
    add(new Builder()
      .manufacturer("Sony")
      .product("E2115")
      .highCamcorderProfile(CamcorderProfile.QUALITY_480P)
      .build());
    add(new Builder()
      .manufacturer("Spectralink")
      .product("cyclops_6dq")
      .highCamcorderProfile(CamcorderProfile.QUALITY_1080P)
      .previewRFCSizeWhitelist(new Size(1920,1080))
      .pictureRFCSizeWhitelist(new Size(1920,1080))
      .supportsCameraTwo(false)
      .build());
    add(new Builder()
      .manufacturer("Wileyfox")
      .product("Swift")
      .supportsCameraTwo(false)
      .build());
    add(new Builder()
      .manufacturer("LGE")
      .supportsCameraTwo(false)
      .build());

    CameraConstraints match=null;

    for (CameraConstraints m : CONSTRAINTS) {
      if (m.isMatch()) {
        match=m;
        break;
      }
    }

    DEVICE_CONSTRAINT=match;
  }

  public static void add(CameraConstraints matcher) {
    CONSTRAINTS.add(matcher);
  }

  public static CameraConstraints get() {
    return(DEVICE_CONSTRAINT);
  }

  private CameraConstraints(Pattern manufacturer,
                            Pattern product, Pattern model,
                            boolean supportsCameraTwo,
                            int highCamcorderProfile,
                            boolean disableFocusMode,
                            int cameraDisplayOrientation,
                            boolean supportsFFC,
                            boolean supportsFFCVideo,
                            boolean supportsRFC,
                            boolean supportsRFCVideo,
                            ArrayList<Size> previewFFCSizeWhitelist,
                            ArrayList<Size> previewRFCSizeWhitelist,
                            ArrayList<Size> pictureFFCSizeWhitelist,
                            ArrayList<Size> pictureRFCSizeWhitelist) {
    this.manufacturer=manufacturer;
    this.product=product;
    this.model=model;
    this.supportsCameraTwo=supportsCameraTwo;
    this.highCamcorderProfile=highCamcorderProfile;
    this.disableFocusMode=disableFocusMode;
    this.cameraDisplayOrientation=cameraDisplayOrientation;
    this.supportsFFC=supportsFFC;
    this.supportsFFCVideo=supportsFFCVideo;
    this.supportsRFC=supportsRFC;
    this.supportsRFCVideo=supportsRFCVideo;
    this.previewFFCSizeWhitelist=previewFFCSizeWhitelist;
    this.previewRFCSizeWhitelist=previewRFCSizeWhitelist;
    this.pictureFFCSizeWhitelist=pictureFFCSizeWhitelist;
    this.pictureRFCSizeWhitelist=pictureRFCSizeWhitelist;
  }

  public boolean isMatch() {
    boolean result=true;

/*
android.util.Log.e("20160618", Build.MANUFACTURER);
android.util.Log.e("20160618", Build.PRODUCT);
android.util.Log.e("20160618", Build.MODEL);
*/

    if (manufacturer!=null) {
      result=manufacturer.matcher(Build.MANUFACTURER).matches();
    }

    if (result && product!=null) {
      result=product.matcher(Build.PRODUCT).matches();
    }

    if (result && model!=null) {
      result=model.matcher(Build.MODEL).matches();
    }

    return(result);
  }

  public boolean supportsCameraTwo() {
    return(supportsCameraTwo);
  }

  public int getHighCamcorderProfile() {
    return(highCamcorderProfile);
  }

  public boolean getDisableFocusMode() {
    return(disableFocusMode);
  }

  public int getCameraDisplayOrientation() {
    return(cameraDisplayOrientation);
  }

  public boolean supportsFFC() {
    return(supportsFFC);
  }

  public boolean supportsFFCVideo() {
    return(supportsFFCVideo);
  }

  public boolean supportsRFC() {
    return(supportsRFC);
  }

  public boolean supportsRFCVideo() {
    return(supportsRFCVideo);
  }

  public List<Size> getPreviewFFCSizeWhitelist() {
    return(previewFFCSizeWhitelist);
  }

  public List<Size> getPreviewRFCSizeWhitelist() {
    return(previewRFCSizeWhitelist);
  }

  public List<Size> getPictureFFCSizeWhitelist() {
    return(pictureFFCSizeWhitelist);
  }

  public List<Size> getPictureRFCSizeWhitelist() {
    return(pictureRFCSizeWhitelist);
  }

  public static class Builder {
    private Pattern manufacturer;
    private Pattern product;
    private Pattern model;
    private boolean supportsCameraTwo=false;
    private int highCamcorderProfile=CamcorderProfile.QUALITY_HIGH;
    private boolean disableFocusMode=false;
    private int cameraDisplayOrientation=-1;
    private boolean supportsFFC=true;
    private boolean supportsFFCVideo=true;
    private boolean supportsRFC=true;
    private boolean supportsRFCVideo=true;
    private ArrayList<Size> previewFFCSizeWhitelist;
    private ArrayList<Size> previewRFCSizeWhitelist;
    private ArrayList<Size> pictureFFCSizeWhitelist;
    private ArrayList<Size> pictureRFCSizeWhitelist;

    public Builder manufacturer(String mfr) {
      return(manufacturer(Pattern.compile(mfr)));
    }

    public Builder manufacturer(Pattern manufacturer) {
      this.manufacturer=manufacturer;

      return(this);
    }

    public Builder product(String product) {
      return(product(Pattern.compile(product)));
    }

    public Builder product(Pattern product) {
      this.product=product;

      return(this);
    }

    public Builder model(String model) {
      return(model(Pattern.compile(model)));
    }

    public Builder model(Pattern model) {
      this.model=model;

      return(this);
    }

    public Builder supportsCameraTwo(boolean supportsCameraTwo) {
      this.supportsCameraTwo=supportsCameraTwo;

      return(this);
    }

    public Builder highCamcorderProfile(int highCamcorderProfile) {
      this.highCamcorderProfile=highCamcorderProfile;

      return(this);
    }

    public Builder disableFocusMode(boolean disableFocusMode) {
      this.disableFocusMode=disableFocusMode;

      return(this);
    }

    public Builder cameraDisplayOrientation(int cameraDisplayOrientation) {
      this.cameraDisplayOrientation=cameraDisplayOrientation;

      return(this);
    }

    public Builder supportsFFC(boolean supportsFFC) {
      this.supportsFFC=supportsFFC;

      return(this);
    }

    public Builder supportsFFCVideo(boolean supportsFFCVideo) {
      this.supportsFFCVideo=supportsFFCVideo;

      return(this);
    }

    public Builder supportsRFC(boolean supportsRFC) {
      this.supportsRFC=supportsRFC;

      return(this);
    }

    public Builder supportsRFCVideo(boolean supportsRFCVideo) {
      this.supportsRFCVideo=supportsRFCVideo;

      return(this);
    }

    public Builder previewFFCSizeWhitelist(Size... sizes) {
      if (previewFFCSizeWhitelist==null) {
        previewFFCSizeWhitelist=new ArrayList<>();
      }

      for (Size size : sizes) {
        previewFFCSizeWhitelist.add(size);
      }

      return(this);
    }

    public Builder previewRFCSizeWhitelist(Size... sizes) {
      if (previewRFCSizeWhitelist==null) {
        previewRFCSizeWhitelist=new ArrayList<>();
      }

      for (Size size : sizes) {
        previewRFCSizeWhitelist.add(size);
      }

      return(this);
    }

    public Builder pictureFFCSizeWhitelist(Size... sizes) {
      if (pictureFFCSizeWhitelist==null) {
        pictureFFCSizeWhitelist=new ArrayList<>();
      }

      for (Size size : sizes) {
        pictureFFCSizeWhitelist.add(size);
      }

      return(this);
    }

    public Builder pictureRFCSizeWhitelist(Size... sizes) {
      if (pictureRFCSizeWhitelist==null) {
        pictureRFCSizeWhitelist=new ArrayList<>();
      }

      for (Size size : sizes) {
        pictureRFCSizeWhitelist.add(size);
      }

      return(this);
    }

    public CameraConstraints build() {
      return(new CameraConstraints(manufacturer, product, model,
        supportsCameraTwo, highCamcorderProfile, disableFocusMode,
        cameraDisplayOrientation, supportsFFC, supportsFFCVideo,
        supportsRFC, supportsRFCVideo, previewFFCSizeWhitelist,
        previewRFCSizeWhitelist, pictureFFCSizeWhitelist,
        pictureRFCSizeWhitelist));
    }
  }
}

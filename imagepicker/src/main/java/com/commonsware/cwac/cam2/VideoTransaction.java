/***
 Copyright (c) 2015 CommonsWare, LLC

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

import java.io.File;

public class VideoTransaction {
  File outputPath;
  int quality=1;
  int sizeLimit=0;
  int durationLimit=0;

  private VideoTransaction() {
    // please use the Builder
  }

  public File getOutputPath() {
    return(outputPath);
  }

  public int getQuality() {
    return(quality);
  }

  public int getSizeLimit() {
    return(sizeLimit);
  }

  public int getDurationLimit() {
    return(durationLimit);
  }

  public static class Builder {
    VideoTransaction result=new VideoTransaction();

    public VideoTransaction build() {
      return(result);
    }

    public Builder to(File outputPath) {
      result.outputPath=outputPath;

      return(this);
    }

    public Builder quality(int quality) {
      result.quality=quality;

      return(this);
    }

    public Builder sizeLimit(int sizeLimit) {
      result.sizeLimit=sizeLimit;

      return(this);
    }

    public Builder durationLimit(int durationLimit) {
      result.durationLimit=durationLimit;

      return(this);
    }
  }
}

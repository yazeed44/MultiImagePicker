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

/**
 * Representation of criteria used to select a camera from
 * available cameras on the device. Used with CameraEngine
 * for finding a subset of interesting cameras.
 */
public class CameraSelectionCriteria {
  private Facing facing;
  private boolean facingExactMatch=false;
  private boolean isVideo=false;

  /**
   * Getter method for the facing value, indicating if we
   * want the camera to be front-facing, back-facing, or
   * any direction.
   *
   * @return the facing value
   */
  public Facing getFacing() {
    return(facing);
  }

  /**
   * @return true if the requested facing camera must be an
   * exact match, false otherwise
   */
  public boolean getFacingExactMatch() {
    return(facingExactMatch);
  }

  /**
   * @return true if this will be used for video
   */
  public boolean isVideo() {
    return(isVideo);
  }

  /**
   * Class to create instances of CameraSelectionCriteria via
   * a fluent, builder-style API.
   */
  public static class Builder {
    final private CameraSelectionCriteria criteria=new CameraSelectionCriteria();

    /**
     * Setter for the facing value, indicating if we
     * want the camera to be front-facing, back-facing, or
     * any direction.
     *
     * @param facing the facing value
     * @return the builder, for chained calls
     */
    public Builder facing(Facing facing) {
      criteria.facing=facing;

      return(this);
    }

    /**
     * Setter for indicating if the requested facing value must
     * be an exact match or not. Defaults to false.
     *
     * @param match true if must be an exact match, false otherwise
     * @return the builder, for chained calls
     */
    public Builder facingExactMatch(boolean match) {
      criteria.facingExactMatch=match;

      return(this);
    }

    /**
     * Setting for indicating if we will be using this camera for video
     *
     * @param isVideo true if the camera will be used for video, false otherwise
     * @return the builder, for chained calls
     */
    public Builder isVideo(boolean isVideo) {
      criteria.isVideo=isVideo;

      return(this);
    }

    /**
     * Returns the fabricated criteria
     *
     * @return the criteria defined via calls on the builder
     */
    public CameraSelectionCriteria build() {
      return(criteria);
    }
  }

}

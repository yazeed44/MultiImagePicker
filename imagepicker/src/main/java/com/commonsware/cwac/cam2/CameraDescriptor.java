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

import com.commonsware.cwac.cam2.util.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * A representation of a camera. Specific camera engines will
 * have their own descriptors, which they can use for tracking
 * IDs or other information about the camera. From the standpoint
 * of the public API, a descriptor should be fairly opaque, supplying
 * information about the camera capabilities, but nothing more.
 */
public interface CameraDescriptor {
  /**
   * @return The possible preview sizes for the camera, in no
   * particular order
   */
  List<Size> getPreviewSizes();

  /**
   * @return The possible picture sizes for the camera, in no
   * particular order
   */
  List<Size> getPictureSizes();

  /**
   * Indicates if the camera (and this library) supports a
   * particular image format for pictures.
   *
   * @param format an ImageFormat value (e.g., ImageFormat.JPEG)
   * @return true if supported, false otherwise
   */
  boolean isPictureFormatSupported(int format);
}

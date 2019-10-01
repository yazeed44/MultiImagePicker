/**
 * Copyright (c) 2015 CommonsWare, LLC
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.commonsware.cwac.cam2;

/**
 * Interface for class that post-processes a picture. A
 * PictureTransaction holds a chain of these, which collectively
 * will modify the picture as needed and write it wherever it
 * needs writing to.
 */
public interface ImageProcessor {
  /**
   * Manipulate the picture indicated by the ImageContext.
   * Properties for configuring this particular bit of image
   * processing can be obtained from the PictureTransaction.
   *
   * @param xact the PictureTransaction, containing properties
   * @param imageContext the picture itself
   */
  void process(PictureTransaction xact, ImageContext imageContext);

  /**
   * @return a unique tag to identify this processor among
   * any other processors in the chain
   */
  String getTag();
}

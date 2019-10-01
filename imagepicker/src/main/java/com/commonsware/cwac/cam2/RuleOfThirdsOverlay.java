/***
 Copyright (c) 2017 CommonsWare, LLC
 Licensed under the Apache License, Version 2.0 (the "License"); you may not
 use this file except in compliance with the License. You may obtain a copy
 of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 OF ANY KIND, either express or implied. See the License for the specific
 language governing permissions and limitations under the License.
 */

package com.commonsware.cwac.cam2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import net.yazeed44.imagepicker.library.R;

public class RuleOfThirdsOverlay extends View {
  private Paint paint;
  private float thickness;

  public RuleOfThirdsOverlay(Context context) {
    this(context, null);
  }

  public RuleOfThirdsOverlay(Context context, AttributeSet attrs) {
    super(context, attrs);

    init();
  }

  private void init() {
    thickness=
      getContext().getResources().getDimension(R.dimen.cwac_cam2_gridline_width);

    paint=new Paint();
    paint.setColor(Color.WHITE);
    paint.setAlpha(0x80);
    paint.setStrokeWidth(thickness);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    float width=this.getWidth();
    float height=this.getHeight();

    canvas.drawLine(width/3, 0, width/3, height, paint);
    canvas.drawLine(width*2/3, 0, width*2/3, height, paint);
    canvas.drawLine(0, height/3, width, height/3, paint);
    canvas.drawLine(0, height*2/3, width, height*2/3, paint);
  }
}

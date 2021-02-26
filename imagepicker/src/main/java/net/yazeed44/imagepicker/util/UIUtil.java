package net.yazeed44.imagepicker.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import java.lang.reflect.InvocationTargetException;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by yshrsmz on 15/03/17.
 */
public final class UIUtil {

    public static float dpToPx(@NonNull Context context, float dp) {
        Resources res = context.getResources();
        return dp * (res.getDisplayMetrics().densityDpi / 160f);
    }

    /**
     * Show keyboard and focus to given EditText
     *
     * @param context Context
     * @param target  EditText to focus
     */
    public static void showKeyboard(Context context, EditText target) {
        if (context == null || target == null) {
            return;
        }
        InputMethodManager imm = getInputMethodManager(context);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        target.requestFocus();
    }

    /**
     * Show keyboard and focus to given EditText.
     * Use this method if target EditText is in Dialog.
     *
     * @param dialog Dialog
     * @param target EditText to focus
     */
    public static void showKeyboardInDialog(Dialog dialog, EditText target) {
        if (dialog == null || target == null) {
            return;
        }

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        target.requestFocus();
    }



    /**
     * hide keyboard
     *
     * @param context Context
     * @param target  View that currently has focus
     */
    public static void hideKeyboard(Context context, View target) {
        if (context == null || target == null) {
            return;
        }

        InputMethodManager imm = getInputMethodManager(context);
        imm.hideSoftInputFromWindow(target.getWindowToken(), 0);
    }

    /**
     * hide keyboard
     *
     * @param activity AdvanceLearnActivity
     */
    public static void hideKeyboard(Activity activity) {
        if (activity == null) return;

        View view = activity.getWindow().getDecorView();

        hideKeyboard(activity, view);
    }

    private static InputMethodManager getInputMethodManager(Context context) {
        return (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }


    public static Point getNavigationBarSize(Context context) {
        Point appUsableSize = getAppUsableScreenSize(context);
        Point realScreenSize = getRealScreenSize(context);
        if (appUsableSize == null || realScreenSize == null)
            return new Point();


        // navigation bar on the right
        if (appUsableSize.x < realScreenSize.x) {
            return new Point(realScreenSize.x - appUsableSize.x, appUsableSize.y);
        }

        // navigation bar at the bottom
        if (appUsableSize.y < realScreenSize.y) {
            return new Point(appUsableSize.x, realScreenSize.y - appUsableSize.y);
        }

        // navigation bar is not present
        return new Point();
    }

    @Nullable
    public static Point getAppUsableScreenSize(@NonNull Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) return null;
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static Point getRealScreenSize(Context context) {
        Point size = new Point(1080, 1920);
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) return size;
        Display display = windowManager.getDefaultDisplay();

        if (Build.VERSION.SDK_INT >= 17) {
            display.getRealSize(size);
        } else {
            try {
                size.x = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
                size.y = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
            } catch (IllegalAccessException ignored) {
            } catch (InvocationTargetException ignored) {
            } catch (NoSuchMethodException ignored) {
            }
        }

        return size;
    }

    public static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0 && resources.getBoolean(id)) {
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                return resources.getDimensionPixelSize(resourceId);
            }
        }
        return 0;
    }

    public static int getStatusbarHeight(Context context) {
        int statusBarHeight = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    public static int getActionBarHeight(Context context) {
        int actionBarHeight = 0;
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.actionBarSize}
        );
        actionBarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        return actionBarHeight;
    }

    public static int getNavBarHeight(Context c) {
        int result = 0;
        boolean hasMenuKey = ViewConfiguration.get(c).hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);

        if (!hasMenuKey && !hasBackKey) {
            //The device has a navigation bar
            Resources resources = c.getResources();

            int orientation = resources.getConfiguration().orientation;
            int resourceId;
            if (isTablet(c)) {
                resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape", "dimen", "android");
            } else {
                resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_width", "dimen", "android");
            }

            if (resourceId > 0) {
                return resources.getDimensionPixelSize(resourceId);
            }
        }
        return result;
    }


    private static boolean isTablet(Context c) {
        return (c.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    // Custom method to get screen width in pixels using Context object
    public static int getScreenWidthInPx(Context context) {
        /*
            DisplayMetrics
                A structure describing general information about a display,
                such as its size, density, and font scaling.
        */
        DisplayMetrics dm = new DisplayMetrics();

        /*
            WindowManager
                The interface that apps use to talk to the window manager.
                Use Context.getSystemService(Context.WINDOW_SERVICE) to get one of these.
        */

        /*
            public abstract Object getSystemService (String name)
                Return the handle to a system-level service by name. The class of the returned
                object varies by the requested name. Currently available names are:

                WINDOW_SERVICE ("window")
                    The top-level window manager in which you can place custom windows.
                    The returned object is a WindowManager.
        */

        WindowManager windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        if (windowManager != null)
            windowManager.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    public static Rect getViewRect(View view) {
        Rect scrRec = new Rect();
        view.getGlobalVisibleRect(scrRec);
        return scrRec;
    }

    // Custom method to get screen height in pixels using Context object
    public static int getScreenHeightInPx(Context context) {
        if (context instanceof Activity) {
            Rect scrRec = new Rect();
            ((Activity) context).getWindow().getDecorView().getGlobalVisibleRect(scrRec);
            return scrRec.height();
        }
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        if (windowManager != null)
            windowManager.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    @NonNull
    public static Point getLocationOnScreen(@NonNull View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return new Point(location[0], location[1]);
    }

    public static void changeStrokeColor(View view, @ColorInt int color) {
        if (!(view.getBackground() instanceof GradientDrawable)) return;
        GradientDrawable border = (GradientDrawable) view.getBackground();
        border.setColor(0x00ffffff);
        border.setStroke((int) dpToPx(view.getContext(), 5), color);
        view.setBackground(border);
    }


}

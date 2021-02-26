/*
 * Copyright (C) 2017 MINDORKS NEXTGEN PRIVATE LIMITED
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://mindorks.com/license/apache-v2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package net.yazeed44.imagepicker.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import net.yazeed44.imagepicker.library.BuildConfig;
import net.yazeed44.imagepicker.library.R;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by anhtt on 27/01/17.
 */

public final class CommonUtils {

    private CommonUtils() {
        // This utility class is not publicly instantiable
    }

    public static int random(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public static AlertDialog showLoadingDialog(Context context) {
        AlertDialog proDialog = new AlertDialog.Builder(context).create();
        proDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        proDialog.setCancelable(true);
        try {
            proDialog.show();
        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                e.printStackTrace();
        }
        proDialog.setContentView(R.layout.layout_loading);
        if (proDialog.getWindow() != null) {
            proDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            proDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        return proDialog;
    }

    public static boolean isEmailValid(String email) {
        Pattern pattern;
        Matcher matcher;
        final String EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isPhoneValid(String phone) {
        Pattern pattern;
        Matcher matcher;
        final String PHONE_PATTERN = "(84|\\+84|0)\\d{9}";
        pattern = Pattern.compile(PHONE_PATTERN);
        matcher = pattern.matcher(phone);
        return matcher.matches();
    }

    public static boolean isNetworkConnected(Context context) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        //noinspection deprecation
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }


    public static String unAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replaceAll("Đ", "D").replaceAll("đ", "d").toLowerCase();
    }

    public static void marginLastItem(RecyclerView.ViewHolder holder, int position, int count, int margin) {
        if (holder.itemView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
            if (position == count - 1)
                lp.setMargins(0, 0, 0, (int) UIUtil.dpToPx(holder.itemView.getContext(), margin));
            else
                lp.setMargins(0, 0, 0, 0);
            holder.itemView.setLayoutParams(lp);
        }
    }

    public static String getSampleAssessment(String orig, String studentName) {
        return orig.replaceAll("@tenbe", studentName)
                .replaceAll("@ten-be", studentName)
                .replaceAll("ten-be", studentName)
                .replaceAll("Ten-be", studentName)
                .replaceAll("tenbe", studentName)
                .replaceAll("Tenbe", studentName);
    }

    public static void callPhone(Context context, String phone) {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phone));
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
//            ToastUtils.showToastErrorConfirm(context, R.string.device_not_support_phone_call);
        }
    }


    public static String formatCurrency(Double amount) {
        return new DecimalFormat("###,###,###,###", DecimalFormatSymbols.getInstance(Locale.getDefault())).format(amount);
    }

    public static String formatCurrency(String amount) {
        try {
            return new DecimalFormat("###,###,###,###", DecimalFormatSymbols.getInstance(Locale.getDefault())).format(Double.parseDouble(amount));
        } catch (Exception e) {
            return amount;
        }
    }

    public static void setWebViewContent(WebView webView, String content) {
        if (webView.getSettings() != null) {
            webView.getSettings().setDefaultTextEncodingName("utf-8");
            webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        }

        if (Build.VERSION.SDK_INT >= 19) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        content = content.replaceAll("<!--.*?-->", "")
                .replaceAll("/^\\s+|\\s+$/g", "");
        String htmlPrefix = "<style type=\"text/css\"> @font-face { font-family: MyFont; src: url(\"file:///android_asset/fonts/Montserrat-Regular.ttf\") } body { font-family: MyFont; font-size: 14; text-align: justify;max-width: 100%; word-break: break-all; word-break: break-word}img{display: inline;height: auto;max-width: 100%;}</style>" +
                "<html><body><p align=\"justify\" line-height=\"1.5\" >";
        String htmlPostfix = "</p></body></html>";
        String html;
        if (content.contains("http")) {
            Spannable sp = new SpannableString(Html.fromHtml(content));
            Linkify.addLinks(sp, Linkify.ALL);
            html = htmlPrefix + Html.toHtml(sp) + htmlPostfix;
        } else {
            html = htmlPrefix + content + htmlPostfix;
        }
        webView.loadDataWithBaseURL("", html, "text/html; charset=utf-8", "utf-8", "");
    }

}

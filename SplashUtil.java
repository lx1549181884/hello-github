package com.example.myapplication;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.view.Window;
import android.view.WindowManager;

/**
 * Splash 工具
 */
public class SplashUtil {

    private static BitmapDrawable drawable;

    /**
     * 功能：一张图片适配所有手机
     * 使用要求：图片的宽高比要大些，尽量靠近正方形
     * 使用方法：在 Activity 的 onCreate 里的 super.onCreate() 前调用
     */
    public static void setWindowBackground(Activity activity, int drawableResId) {
        if (activity == null)
            return;
        Window window = activity.getWindow();
        if (window == null)
            return;
        if (drawable == null) {
            WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
            if (windowManager == null)
                return;
            Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(), drawableResId);
            Point point = new Point();
            windowManager.getDefaultDisplay().getSize(point);
            float appScreenRatio = 1f * point.x / point.y; // app 屏幕宽高比
            int bitmapH = bitmap.getHeight();
            int bitmapW = bitmap.getWidth();
            int newBitmapH = bitmapH; // 高度不变
            int newBitmapW = (int) (newBitmapH * appScreenRatio); // 宽度 按 app 屏幕宽高比计算
            // 按 app 屏幕高宽比切图
            Bitmap newBitmap = Bitmap.createBitmap(bitmap,
                    (bitmapW - newBitmapW) / 2, // x 坐标起始位置
                    0, newBitmapW, newBitmapH, null, false);
            drawable = new BitmapDrawable(activity.getResources(), newBitmap);
        }
        window.setBackgroundDrawable(drawable);
    }
}

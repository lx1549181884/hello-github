package com.example.myapplication;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.Utils;
import com.dianyi.jihuibao.R;

import java.text.DateFormat;
import java.util.Date;

/**
 * 国际化工具
 */
public class InternationalUtil {
    /**
     * 时间相关
     */
    public static class Time {
        /**
         * 获取时间
         *
         * @param time   时间戳
         * @param patten 时间样式
         * @return 时间字符串
         */
        public static String getTimeByPatten(long time, @StringRes int patten) {
            return TimeUtils.millis2String(time, Utils.getApp().getString(patten));
        }

        /**
         * 获取时间
         *
         * @param time  时间戳
         * @param style 时间样式
         * @return 时间字符串
         */
        public static String getTimeByStyle(long time, int style) {
            return DateFormat.getDateInstance(style).format(new Date(time));
        }
    }

    /**
     * 富文本相关
     */
    public static class RichText {

        /**
         * 获取加亮富文本
         *
         * @param allText         全部文本
         * @param highlightText   加亮文本
         * @param onClickListener 加亮文本的点击事件
         * @return SpannableString
         */
        public static SpannableString getHighlightText(@StringRes int allText, @StringRes int highlightText, @Nullable View.OnClickListener onClickListener) {
            return getHighlightText(StringUtils.getString(allText), StringUtils.getString(highlightText), onClickListener);
        }

        /**
         * 获取加亮富文本
         *
         * @param allText         全部文本
         * @param highlightText   加亮文本
         * @param onClickListener 加亮文本的点击事件
         * @return SpannableString
         */
        public static SpannableString getHighlightText(@NonNull String allText, @NonNull String highlightText, @Nullable View.OnClickListener onClickListener) {
            SpannableString spannableString = new SpannableString(allText);
            int start = allText.indexOf(highlightText);
            int end = start + highlightText.length();
            spannableString.setSpan(new ClickableSpan() {
                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(ColorUtils.getColor(R.color.ff576b95));
                    ds.setUnderlineText(false);
                }

                @Override
                public void onClick(@NonNull View widget) {
                    if (onClickListener != null) {
                        onClickListener.onClick(widget);
                    }
                }
            }, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            return spannableString;
        }

        /**
         * 设置加亮文本
         *
         * @param textView        文本控件
         * @param allText         全部文本
         * @param highlightText   加亮文本
         * @param onClickListener 加亮文本的点击事件
         */
        public static void setHighlightText(@NonNull TextView textView, @StringRes int allText, @StringRes int highlightText, @Nullable final View.OnClickListener onClickListener) {
            textView.setHighlightColor(Color.TRANSPARENT);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            textView.setText(getHighlightText(allText, highlightText, onClickListener));
        }

        /**
         * 设置加亮文本
         *
         * @param textView        文本控件
         * @param allText         全部文本
         * @param highlightText   加亮文本
         * @param onClickListener 加亮文本的点击事件
         */
        public static void setHighlightText(@NonNull TextView textView, @NonNull String allText, @NonNull String highlightText, @Nullable final View.OnClickListener onClickListener) {
            textView.setHighlightColor(Color.TRANSPARENT);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            textView.setText(getHighlightText(allText, highlightText, onClickListener));
        }
    }
}

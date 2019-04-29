package com.example.linxinggl.hey;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 方向键 工具类
 * <p>
 * 用途：用于需要使用“方向键”（例如电视遥控的上下左右按键）的 APP
 * 效果：可以使用方向键控制焦点在 View 上移动，并且获取焦点的 View 会加亮边框显示
 * <p>
 * 使用方法：
 * 自适应Activity ：在 Application.onCreate() 里调用 {@link DirectionKeyUtil#init(Application, boolean)}
 * 自适应Fragment ：在 BaseFragment.onCreate() 里调用 {@link DirectionKeyUtil#initFragment(Fragment, boolean)}
 * 自适应Diaglog ：在 BaseDialog.onCreate() 里调用 {@link DirectionKeyUtil#initDialog(Dialog, boolean)}
 */
public class DirectionKeyUtil {

    private static final boolean OPEN_LOG = false;

    public static void log(String str) {
        if (OPEN_LOG)
            Log.e("lx", "DirectionKeyUtil---" + str);
    }

    private static Application.ActivityLifecycleCallbacks callback;

    /**
     * 初始化
     *
     * @param forceUseFrame true:强制使用默认的高亮框。 false:若有定义的focus图层，则使用自定义的
     */
    public static synchronized void init(Application application, final boolean forceUseFrame) {
        log("init()");
        if (application == null)
            return;
        if (callback == null)
            callback = new Application.ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                    long t = System.currentTimeMillis();
                    DirectionKeyUtil.initView(activity.getWindow().getDecorView(), forceUseFrame);
                    DirectionKeyUtil.log("初始化 " + activity.getClass().getSimpleName() + "，耗时 " + (System.currentTimeMillis() - t));
                }

                @Override
                public void onActivityStarted(Activity activity) {
                }

                @Override
                public void onActivityResumed(Activity activity) {
                }

                @Override
                public void onActivityPaused(Activity activity) {

                }

                @Override
                public void onActivityStopped(Activity activity) {

                }

                @Override
                public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

                }

                @Override
                public void onActivityDestroyed(Activity activity) {

                }
            };
        application.unregisterActivityLifecycleCallbacks(callback);
        application.registerActivityLifecycleCallbacks(callback);
    }

    /**
     * 初始化Fragment
     *
     * @param forceUseFrame 强制使用默认的高亮框
     */
    public static synchronized void initFragment(Fragment fragment, boolean forceUseFrame) {
        log("initDialog()");
        if (fragment == null)
            return;
        initView(fragment.getView(), forceUseFrame);
    }

    /**
     * 初始化Dialog
     *
     * @param forceUseFrame 强制使用默认的高亮框
     */
    public static synchronized void initDialog(Dialog dialog, boolean forceUseFrame) {
        log("initDialog()");
        if (dialog == null)
            return;
        initView(dialog.getWindow().getDecorView(), forceUseFrame);
    }

    /**
     * 初始化该view及其所有子view
     *
     * @param forceUseFrame 强制使用默认的高亮框
     */
    public static void initView(View view, final boolean forceUseFrame) {
        if (view == null)
            return;
        if (view.hasOnClickListeners() || view instanceof Button) { // 可点击的View才需要focus状态
            addFocusStateToView(view, forceUseFrame); // 添加focused图层给View
            return;
        }
        if (view instanceof AbsListView) { // AbsListView比较特殊，item获取焦点时，是selected状态
            ((AbsListView) view).setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
                @Override
                public void onChildViewAdded(View parent, View child) {
//                    log("ListView---onChildViewAdded---" + parent + "---" + child);
                    addSelectStateToView(child, forceUseFrame); // 给item添加selected图层
                }

                @Override
                public void onChildViewRemoved(View parent, View child) {

                }
            });
        } else if (view instanceof ViewGroup) {

            ViewGroup viewGroup = (ViewGroup) view;
            viewGroup.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
                @Override
                public void onChildViewAdded(View parent, View child) {
//                    log("ViewGroup---onChildViewAdded---" + parent + "---" + child);
                    initView(child, forceUseFrame); // 初始化子View
                }

                @Override
                public void onChildViewRemoved(View parent, View child) {

                }
            });

            // 初始化子View
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                initView(viewGroup.getChildAt(i), forceUseFrame);
            }
        }
    }

    /**
     * 添加focused图层给View
     *
     * @param deleteOriginalFocusStateDrawable 是否删除原focused图层
     */
    private static void addFocusStateToView(View view, boolean deleteOriginalFocusStateDrawable) {
        addStateToView(view, android.R.attr.state_focused, deleteOriginalFocusStateDrawable, true);
    }

    /**
     * 添加selected图层给View
     *
     * @param deleteOriginalSelectStateDrawable 是否删除原selected图层
     */
    private static void addSelectStateToView(View view, boolean deleteOriginalSelectStateDrawable) {
        addStateToView(view, android.R.attr.state_selected, deleteOriginalSelectStateDrawable, false);
    }

    /**
     * 添加指定状态图层给View
     *
     * @param targetState                       指定状态
     * @param deleteOriginalTargetStateDrawable 是否删除原有重复状态的图层
     * @param setFocusable                      是否设置 setFocusable(true)
     */
    private static void addStateToView(View view, int targetState, boolean deleteOriginalTargetStateDrawable, boolean setFocusable) {
        if (setFocusable)
            view.setFocusable(true);
        if (deleteOriginalTargetStateDrawable)
            deleteStateOfView(view, targetState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) // 6.0以上版本，可以setForeground，但对Button无效
            if (view instanceof Button)
                addStateToBg(view, targetState, false);
            else
                view.setForeground(generateDrawable(view.getContext(), view.getForeground(), targetState, false));
        else {
            if (view instanceof ImageView) // 若是ImageView，则添加至Image
                addStateToImg((ImageView) view, targetState, false);
            else
                addStateToBg(view, targetState, false);
        }
    }

    private static void addStateToImg(ImageView imageView, int targetState, boolean deleteOriginalTargetStateDrawable) {
        imageView.setImageDrawable(generateDrawable(imageView.getContext(), imageView.getDrawable(), targetState, deleteOriginalTargetStateDrawable));
    }

    private static void addStateToBg(View view, int targetState, boolean deleteOriginalTargetStateDrawable) {
        view.setBackground(generateDrawable(view.getContext(), view.getBackground(), targetState, deleteOriginalTargetStateDrawable));
    }

    /**
     * 生成带有指定状态图层的Drawable
     *
     * @param originalDrawable                  原先的 Drawable
     * @param targetState                       指定的状态
     * @param deleteOriginalTargetStateDrawable 是否删除原有重复状态的图层
     */
    private static Drawable generateDrawable(Context context, Drawable originalDrawable, int targetState, boolean deleteOriginalTargetStateDrawable) {
        if (originalDrawable instanceof StateListDrawable) { // StateListDrawable 即 drawable-selector 资源文件生成的类
            StateListDrawable sld = (StateListDrawable) originalDrawable;
            try {
                Class<? extends StateListDrawable> sldClass = sld.getClass();
                int count = (int) sldClass.getMethod("getStateCount").invoke(sld);
                Method mGetStateDrawable = sldClass.getMethod("getStateDrawable", int.class);
                Method mGetStateSet = sldClass.getMethod("getStateSet", int.class);

                // 原StateListDrawable中需要使用的图层的容器
                ArrayList<int[]> stateSetList = new ArrayList();
                ArrayList<Drawable> stateDrawableList = new ArrayList();
                // 需要添加的图层的容器
                ArrayList<int[]> newStateSetList = new ArrayList();
                ArrayList<Drawable> newStateDrawableList = new ArrayList();

//                log("修改前");
                // 取出原来的图层
                for (int i = 0; i < count; i++) {
                    int[] stateSet = (int[]) mGetStateSet.invoke(sld, i);
//                    log(Arrays.toString(stateSet));
                    if (deleteOriginalTargetStateDrawable && hasStateInSet(stateSet, targetState)) // 若要删除重复状态图层，则不取出使用
                        continue;
                    stateSetList.add(stateSet);
                    stateDrawableList.add((Drawable) mGetStateDrawable.invoke(sld, i));
                }

                // 生成新的图层
                for (int i = 0; i < stateSetList.size(); i++) {
                    int[] stateSet = stateSetList.get(i);
                    if (!hasStateInSet(stateSet, targetState)) {
                        int[] targetStateSet = new int[stateSet.length + 1];
                        System.arraycopy(stateSet, 0, targetStateSet, 0, stateSet.length);
                        targetStateSet[targetStateSet.length - 1] = targetState;
                        if (!hasStateSetInList(stateSetList, targetStateSet)) {
                            newStateSetList.add(targetStateSet);
                            newStateDrawableList.add(generateFrameLayerDrawable(context, stateDrawableList.get(i)));
                        }
                    }
                }
//                log("修改后");
                // 组装图层
                if (newStateSetList.size() > 0) {
                    sld = new StateListDrawable();
                    for (int i = 0; i < newStateSetList.size(); i++) {
                        sld.addState(newStateSetList.get(i), newStateDrawableList.get(i));
//                        log(Arrays.toString(newStateSetList.get(i)));
                    }
                    for (int i = 0; i < stateSetList.size(); i++) {
                        sld.addState(stateSetList.get(i), stateDrawableList.get(i));
//                        log(Arrays.toString(stateSetList.get(i)));
                    }
                }
                return sld;
            } catch (Exception e) {
                e.printStackTrace();
                log(e.getMessage());
            }
        } else { // 非drawable-selector资源文件生成的Drawable
            StateListDrawable sld = new StateListDrawable();
            if (originalDrawable == null) {
                sld.addState(new int[]{targetState}, getFrameDrawable());
            } else {
                sld.addState(new int[]{targetState}, generateFrameLayerDrawable(context, originalDrawable));
                sld.addState(new int[]{}, originalDrawable);
            }
            return sld;
        }
        return originalDrawable;
    }

    /**
     * 删除View的指定图层
     */
    private static void deleteStateOfView(View view, int state) {
        view.setBackground(deleteState(view.getBackground(), state));
        if (view instanceof ImageView) {
            ImageView img = (ImageView) view;
            img.setImageDrawable(deleteState(img.getDrawable(), state));
        }
    }

    /**
     * 删除指定图层
     */
    private static Drawable deleteState(Drawable drawable, int state) {
        if (drawable instanceof StateListDrawable) {
            StateListDrawable sld = (StateListDrawable) drawable;
            try {
                Class<? extends StateListDrawable> sldClass = sld.getClass();
                int count = (int) sldClass.getMethod("getStateCount").invoke(sld);
                Method mGetStateDrawable = sldClass.getMethod("getStateDrawable", int.class);
                Method mGetStateSet = sldClass.getMethod("getStateSet", int.class);

                ArrayList<int[]> stateSetList = new ArrayList();
                ArrayList<Drawable> stateDrawableList = new ArrayList();

//                log("删除前");
                for (int i = 0; i < count; i++) {
                    int[] stateSet = (int[]) mGetStateSet.invoke(sld, i);
//                    log(Arrays.toString(stateSet));
                    if (hasStateInSet(stateSet, state))
                        continue;
                    stateSetList.add(stateSet);
                    stateDrawableList.add((Drawable) mGetStateDrawable.invoke(sld, i));
                }

//                log("删除后");
                if (stateSetList.size() < count) {
                    sld = new StateListDrawable();
                    for (int i = 0; i < stateSetList.size(); i++) {
//                        log(Arrays.toString(stateSetList.get(i)));
                        sld.addState(stateSetList.get(i), stateDrawableList.get(i));
                    }
                    return sld;
                }
            } catch (Exception e) {
                e.printStackTrace();
                log(e.getMessage());
            }
        }
        return drawable;
    }

    private static Drawable frame; // 边框

    /**
     * 获取边框Drawable
     */
    private static Drawable getFrameDrawable() {
        if (frame == null) {
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setStroke(3, Color.parseColor("#55ff55"));
            frame = gradientDrawable;
        }
        return frame;
    }

    /**
     * 生成带边框的LayerDrawable
     */
    private static LayerDrawable generateFrameLayerDrawable(Context context, Drawable originalDrawable) {
        return new LayerDrawable(new Drawable[]{originalDrawable, context.getResources().getDrawable(R.drawable.focused_frame)});
    }

    /**
     * 是否有 指定的状态集合 在 状态集合列表 里
     *
     * @param stateSetList 状态集合列表
     * @param stateSet     指定的状态集合
     */
    private static boolean hasStateSetInList(ArrayList<int[]> stateSetList, int[] stateSet) {
        boolean hasStateSet = false;
        Arrays.sort(stateSet);
        for (int[] s : stateSetList) {
            Arrays.sort(s);
            if (Arrays.equals(stateSet, s)) {
                hasStateSet = true;
                break;
            }
        }
        return hasStateSet;
    }

    /**
     * 是否有 指定的状态 在 状态集合 里
     *
     * @param stateSet 状态集合
     * @param state    指定的状态
     */
    private static boolean hasStateInSet(int[] stateSet, int state) {
        boolean hasFocus = false;
        for (int s : stateSet) {
            if (s == state) {
                hasFocus = true;
                break;
            }
        }
        return hasFocus;
    }
}

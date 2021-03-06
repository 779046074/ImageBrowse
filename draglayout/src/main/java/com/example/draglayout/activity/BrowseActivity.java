package com.example.draglayout.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.draglayout.DragChangedListener;
import com.example.draglayout.R;
import com.example.draglayout.UpdateSharedElementListener;
import com.example.draglayout.adapter.ImagePagerAdapter;
import com.example.draglayout.bean.TransitionBean;
import com.example.draglayout.utils.SharedElementUtil;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.TransitionSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 图片浏览界面
 * Created by Boqin on 2017/2/21.
 * Modified by Boqin
 *
 * @Version
 */
public class BrowseActivity extends AppCompatActivity {

    public static final String TAG_POSITION = "POSITION";
    public static final String TAG_PATH = "PATH";
    public static final String TAG_SHARE_ELEMENT = "SHARE_ELEMENT";

    private View mLayout;
    private ViewPager mViewPager;
    private TextView mTVCurrent;
    private TextView mTVSlashPage;
    private TextView mTVTotal;
    private List<Uri> mUris;

    private int mPosition;
    private boolean mIsShareElement;
    private ImagePagerAdapter mImagePagerAdapter;

    /**
     * 无共享元素动画的启动模式，启动浏览界面，单图
     *
     * @param activity activity
     * @param uri      图片uri
     */
    public static void launch(Activity activity, Uri uri) {
        List<Uri> uris = new ArrayList<>();
        uris.add(uri);
        launch(activity, uris, 0);
    }

    /**
     * 无共享元素动画的启动模式，启动浏览界面，多图
     *
     * @param activity activity
     * @param uris     图片链接
     * @param position 当前显示位置
     */
    public static void launch(Activity activity, List<Uri> uris, int position) {
        launchWithShareElement(activity, null, uris, position, false, null);
    }

    /**
     * 启动浏览界面 单图
     *
     * @param activity       activity
     * @param transitionView 目标View，在Version大于21的时候实现共享元素
     * @param uri            图片链接
     */
    public static void launchWithShareElement(Activity activity, final ImageView transitionView, Uri uri) {
        List<Uri> uris = new ArrayList<>();
        uris.add(uri);
        launchWithShareElement(activity, transitionView, uris, 0);
    }

    /**
     * 启动浏览界面
     *
     * @param activity       activity
     * @param transitionView 目标View，在Version大于21的时候实现共享元素
     * @param uris           图片uri
     * @param position       当前显示位置
     */
    public static void launchWithShareElement(Activity activity, final ImageView transitionView, List<Uri> uris, int position) {
        launchWithShareElement(activity, transitionView, uris, position, new UpdateSharedElementListener() {
            @Override
            public View onUpdateSharedElement(int position, String url) {
                return transitionView;
            }
        });
    }

    /**
     * 启动浏览界面
     *
     * @param activity                    activity
     * @param transitionView              目标View，在Version大于21的时候实现共享元素
     * @param uris                        图片uri
     * @param position                    当前显示位置
     * @param updateSharedElementListener 回调接口，用于更新列表页回退动画的基准View
     */
    public static void launchWithShareElement(Activity activity, final View transitionView, final List<Uri> uris, int position,
            final UpdateSharedElementListener updateSharedElementListener) {
        launchWithShareElement(activity, transitionView, uris, position, true, updateSharedElementListener);
    }

    /**
     * 启动浏览界面
     *
     * @param activity                    activity
     * @param transitionView              目标View，在Version大于21的时候实现共享元素
     * @param uris                        图片uri
     * @param position                    当前显示位置
     * @param isShareElement              是否使用共享元素动画
     * @param updateSharedElementListener 回调接口，用于更新列表页回退动画的基准View
     */
    private static void launchWithShareElement(Activity activity, final View transitionView, final List<Uri> uris, int position,
            boolean isShareElement, final UpdateSharedElementListener updateSharedElementListener) {
        Intent intent = new Intent();
        intent.setClass(activity, BrowseActivity.class);

        Uri[] paths = new Uri[uris.size()];
        uris.toArray(paths);
        intent.putExtra(TAG_PATH, paths);
        intent.putExtra(TAG_POSITION, position);
        intent.putExtra(TAG_SHARE_ELEMENT, isShareElement);

        // 这里指定了共享的视图元素
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP && isShareElement) {
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(activity, transitionView,
                            SharedElementUtil.getTransitionName(uris.get(position).getPath(), position));

            activity.startActivity(intent, options.toBundle());
            activity.setExitSharedElementCallback(new SharedElementCallback() {
                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                    super.onMapSharedElements(names, sharedElements);
                    //进入时不需要更新SE
                    if (sharedElements.size() != 0 || names.size() == 0) {
                        return;
                    }
                    TransitionBean transitionBean = SharedElementUtil.getTransitionBean(names.get(0));
                    if (updateSharedElementListener != null) {
                        View view = updateSharedElementListener.onUpdateSharedElement(transitionBean.getPosition(), transitionBean.getUrl());
                        sharedElements.clear();
                        if (transitionBean != null) {
                            sharedElements.put(names.get(0), view == null ? transitionView : view);
                        }
                    } else {
                        sharedElements.clear();
                        sharedElements.put(names.get(0), transitionView);
                    }
                }
            });
        } else {
            activity.startActivity(intent);
        }
    }

    @Override
    protected void onCreate(
            @Nullable
                    Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置一个 exit transition
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 允许使用 transitions
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

            TransitionSet transitionSet = new TransitionSet();
            transitionSet.addTransition(new ChangeBounds());
            transitionSet.addTransition(new ChangeImageTransform());
            getWindow().setSharedElementEnterTransition(transitionSet);
            //延时加载
            postponeEnterTransition();

            setEnterSharedElementCallback(new SharedElementCallback() {

                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                    super.onMapSharedElements(names, sharedElements);
                    sharedElements.clear();
                    sharedElements.put(SharedElementUtil
                                    .getTransitionName(mImagePagerAdapter.getBaseName(mViewPager.getCurrentItem()), mViewPager.getCurrentItem()),
                            mImagePagerAdapter.getTransitionView(mViewPager.getCurrentItem()));
                }
            });
        }

        initIntentData();

        setTheme(R.style.translucent);
        setContentView(R.layout.activity_image_browse);

        initView();

        mImagePagerAdapter = new ImagePagerAdapter(getSupportFragmentManager(), mUris, mIsShareElement,
                new DragChangedListener() {
                    @Override
                    public void onViewPositionChanged(View changedView, float scale) {
                        mLayout.setAlpha(scale);
                    }

                    @Override
                    public boolean onViewReleased() {
                        mLayout.setVisibility(View.INVISIBLE);
                        onBackPressed();
                        return true;
                    }
                });

        mViewPager.setAdapter(mImagePagerAdapter);
        mViewPager.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mViewPager.getViewTreeObserver().removeOnPreDrawListener(this);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startPostponedEnterTransition();
                }
                return true;
            }
        });
        mViewPager.setCurrentItem(mPosition);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setIndicator(mViewPager.getCurrentItem()+1, mUris.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (mUris.size()!=0) {
            setIndicator(mViewPager.getCurrentItem()+1, mUris.size());
        }
    }

    private void setIndicator(int current, int total) {
        mTVCurrent.setText(String.valueOf(current));
        mTVTotal.setText(String.valueOf(total));
        mTVSlashPage.setVisibility(View.VISIBLE);
    }

    private void initView() {
        mLayout = findViewById(R.id.root);
        mViewPager = (ViewPager) findViewById(R.id.vp);
        mTVCurrent = (TextView) findViewById(R.id.current_page);
        mTVTotal = (TextView) findViewById(R.id.total_page);
        mTVSlashPage = (TextView) findViewById(R.id.slash_page);
    }

    /**
     * 初始化intent数据
     */
    private void initIntentData() {
        mPosition = getIntent().getIntExtra(TAG_POSITION, 0);
        mIsShareElement = getIntent().getBooleanExtra(TAG_SHARE_ELEMENT, false);
        mUris = new ArrayList<>();
        for (Parcelable parcelable : getIntent().getParcelableArrayExtra(TAG_PATH)) {
            mUris.add((Uri) parcelable);
        }
    }
}

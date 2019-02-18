package com.example.imageviewer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ImageViewPager extends ViewPager {
    private List<ImageView> imgViews;
    private Context mContext;
    private List<String> data;
    private OnImageClickListener mClickListener;
    private OnImageChangeListener mChangeListener;
    private boolean isRolling;

    private Disposable timer; //计时器

    public ImageViewPager(@NonNull Context context) {
        this(context, null);
    }

    public ImageViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        imgViews = new ArrayList<>();
        data = new ArrayList<>();
        isRolling = false;
    }

    /**
     * 输入图片源URL
     */
    public void setData(List<String> srcs) {
        data.clear();
        data.add(srcs.get(srcs.size() - 1));
        data.addAll(srcs);
        data.add(srcs.get(0));
        setAdapter(new Adapter());
        setUpListener();
        setCurrentItem(1, false);
    }

    /**
     * 设置切换监听
     */
    private void setUpListener() {

        //配置滑动监听，确保循环轮播
        addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
//                if (getAdapter() == null) return;
//                int rawMax = getAdapter().getCount();
//
//                if (position == 0) {
//                    position = rawMax - 2;
//                    setCurrentItem(position, false);
//                }
//                if (position == rawMax - 1) {
//                    position = 1;
//                    setCurrentItem(position, false);
//                }
//                if (mChangeListener != null)
//                    mChangeListener.onChange(position - 1);
//
//                Log.d("testtttttt", "" + getCurrentItem());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (getAdapter() == null) return;
                int rawMax = data.size();

                if (state == 0) {
                    int position = getCurrentItem();
                    if (position == 0) {
                        position = rawMax - 2;
                        setCurrentItem(position, false);
                    }
                    if (position == rawMax - 1) {
                        position = 1;
                        setCurrentItem(position, false);
                    }
                    if (mChangeListener != null)
                        mChangeListener.onChange(position - 1);
                }
            }
        });
    }

    /**
     * 设置是否轮播
     */
    public void setRolling(boolean isRolling) {
        this.isRolling = isRolling;
        if (isRolling) {
            startRollingImages();
        } else {
            if (timer != null && !timer.isDisposed())
                timer.dispose();
        }
    }

    /**
     * 开始轮播
     */
    private void startRollingImages() {
        timer = Observable
                .interval(5, 5, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                               @Override
                               public void accept(Long aLong) throws Exception {
                                   showNextImage();
                               }
                           }
                        , new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Toast.makeText(mContext, "轮播图播放失败", Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    /**
     * 播放下一张图
     */
    private void showNextImage() {
        setCurrentItem(getCurrentItem() + 1, true);
    }

    /**
     * 设置图片点击监听
     */
    public void setOnImageClickListener(OnImageClickListener listener) {
        mClickListener = listener;
    }

    /**
     * 设置图片切换监听
     */
    public void setOnImageChangeListener(OnImageChangeListener listener) {
        mChangeListener = listener;
    }

//    ****自定义View相关****

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isRolling) {    //若是轮播状态
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:   //若为手指按下，则暂停轮播
                    if (timer != null && !timer.isDisposed())
                        timer.dispose();
                    break;
                case MotionEvent.ACTION_UP:     //若为手指抬起，则继续轮播
                    if (timer != null && timer.isDisposed())
                        startRollingImages();
                    break;
                default:
                    break;

            }
        }
        return super.dispatchTouchEvent(ev);
    }


//    ****内部类↓****

    /**
     * 内部类适配器
     */
    private class Adapter extends PagerAdapter {

        Adapter() {
            convertViews();
        }

        private void convertViews() {

            for (String url : data) {
                ImageView view = new ImageView(mContext);
                view.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                view.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imgViews.add(view);
            }
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {
            ImageView view = imgViews.get(position);
            //实现点击监听
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mClickListener != null)
                        mClickListener.onClick(position);
                }
            });

            container.addView(view);
            Glide.with(mContext).load(data.get(position)).into(view);

            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            View target = imgViews.get(position);
            target.setOnClickListener(null);
            container.removeView(imgViews.get(position));
        }
    }

    /**
     * 图片切换监听
     */
    public interface OnImageChangeListener {
        void onChange(int position);
    }

    /**
     * 图片点击监听
     */
    public interface OnImageClickListener {
        void onClick(int position);
    }
}

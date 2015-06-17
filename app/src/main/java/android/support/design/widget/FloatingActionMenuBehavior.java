package android.support.design.widget;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;

import java.util.List;

import littlebot.robods.R;

public class FloatingActionMenuBehavior extends CoordinatorLayout.Behavior<com.github.clans.fab.FloatingActionMenu> {
    private static final boolean SNACKBAR_BEHAVIOR_ENABLED;
    private Rect mTmpRect;
    private boolean mIsAnimatingOut;
    private float mTranslationY;

    public FloatingActionMenuBehavior() {
        super();
    }

    public FloatingActionMenuBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean layoutDependsOn(CoordinatorLayout parent, com.github.clans.fab.FloatingActionMenu child, View dependency) {
        return SNACKBAR_BEHAVIOR_ENABLED && dependency instanceof Snackbar.SnackbarLayout;
    }

    public boolean onDependentViewChanged(CoordinatorLayout parent, com.github.clans.fab.FloatingActionMenu child, View dependency) {
        if(dependency instanceof Snackbar.SnackbarLayout) {
            this.updateFabTranslationForSnackbar(parent, child, dependency);
        } else if(dependency instanceof AppBarLayout) {
            AppBarLayout appBarLayout = (AppBarLayout)dependency;
            if(this.mTmpRect == null) {
                this.mTmpRect = new Rect();
            }

            Rect rect = this.mTmpRect;
            ViewGroupUtils.getDescendantRect(parent, dependency, rect);
            if(rect.bottom <= appBarLayout.getMinimumHeightForVisibleOverlappingContent()) {
                if(!this.mIsAnimatingOut && child.getVisibility() == View.VISIBLE) {
                    this.animateOut(child);
                }
            } else if(child.getVisibility() != View.VISIBLE) {
                this.animateIn(child);
            }
        }

        return false;
    }

    private void updateFabTranslationForSnackbar(CoordinatorLayout parent, com.github.clans.fab.FloatingActionMenu fab, View snackbar) {
        float translationY = this.getFabTranslationYForSnackbar(parent, fab);
        if(translationY != this.mTranslationY) {
            ViewCompat.animate(fab).cancel();
            if(Math.abs(translationY - this.mTranslationY) == (float)snackbar.getHeight()) {
                ViewCompat.animate(fab).translationY(translationY).setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR).setListener(null);
            } else {
                ViewCompat.setTranslationY(fab, translationY);
            }

            this.mTranslationY = translationY;
        }

    }

    private float getFabTranslationYForSnackbar(CoordinatorLayout parent, com.github.clans.fab.FloatingActionMenu fab) {
        float minOffset = 0.0F;
        List dependencies = parent.getDependencies(fab);
        int i = 0;

        for(int z = dependencies.size(); i < z; ++i) {
            View view = (View)dependencies.get(i);
            if(view instanceof Snackbar.SnackbarLayout && parent.doViewsOverlap(fab, view)) {
                minOffset = Math.min(minOffset, ViewCompat.getTranslationY(view) - (float)view.getHeight());
            }
        }

        return minOffset;
    }

    private void animateIn(com.github.clans.fab.FloatingActionMenu button) {
        button.setVisibility(View.VISIBLE);
        if(Build.VERSION.SDK_INT >= 14) {
            ViewCompat.animate(button).scaleX(1.0F).scaleY(1.0F).alpha(1.0F).setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR).withLayer().setListener(null).start();
        } else {
            Animation anim = android.view.animation.AnimationUtils.loadAnimation(button.getContext(), R.anim.fab_in);
            anim.setDuration(200L);
            anim.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
            button.startAnimation(anim);
        }

    }

    private void animateOut(final com.github.clans.fab.FloatingActionMenu button) {
        if(Build.VERSION.SDK_INT >= 14) {
            ViewCompat.animate(button).scaleX(0.0F).scaleY(0.0F).alpha(0.0F).setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR).withLayer().setListener(new ViewPropertyAnimatorListener() {
                public void onAnimationStart(View view) {
                    mIsAnimatingOut = true;
                }

                public void onAnimationCancel(View view) {
                    mIsAnimatingOut = false;
                }

                public void onAnimationEnd(View view) {
                    mIsAnimatingOut = false;
                    view.setVisibility(View.GONE);
                }
            }).start();
        } else {
            Animation anim = android.view.animation.AnimationUtils.loadAnimation(button.getContext(), R.anim.fab_out);
            anim.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
            anim.setDuration(200L);
            anim.setAnimationListener(new AnimationUtils.AnimationListenerAdapter() {
                public void onAnimationStart(Animation animation) {
                    mIsAnimatingOut = true;
                }

                public void onAnimationEnd(Animation animation) {
                    mIsAnimatingOut = false;
                    button.setVisibility(View.GONE);
                }
            });
            button.startAnimation(anim);
        }

    }

    static {
        SNACKBAR_BEHAVIOR_ENABLED = Build.VERSION.SDK_INT >= 11;
    }
}
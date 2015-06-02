package littlebot.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import littlebot.robods.R;


/**
 * TODO: document your custom view class.
 */
public class TabDrawer extends ViewGroup implements GestureDetector.OnGestureListener{

    private Rect mBodyBounds, mLayoutBounds, mTabBounds, mViewBounds, mMinViewBounds, mTabIconBounds;
    private Drawable mBodyBackground, mTabBackground, mTabIcon;
    private Context mContext;
    private ViewGroup mDrawerContent;
    private int mTabPadding;
    private int mChildSpacing;
    private boolean opened = false;
    private int mScrollOffset = 0;

    private GestureDetector mGestureDetector;

    public TabDrawer(Context context) {
        super(context);
        init(context, null, 0);
    }

    public TabDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public TabDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {

        mContext = context;
        mBodyBackground = context.getResources().getDrawable(R.drawable.tab_drawer_body_background);
        mTabBackground = context.getResources().getDrawable(R.drawable.tab_drawer_tab_background);


        if(attrs != null){
            // Load attributes
            final TypedArray a = getContext().obtainStyledAttributes(
                    attrs, R.styleable.TabDrawer, defStyle, 0);

            mTabIcon = a.getDrawable(0);

            mTabBounds = new Rect(
                    0,
                    a.getLayoutDimension(1, 0),
                    a.getLayoutDimension(3, 60),
                    a.getLayoutDimension(2, 60)
            );

            mTabPadding = a.getLayoutDimension(4, 5);
            mChildSpacing = a.getLayoutDimension(5, 100);
            mScrollOffset = this.getChildCount()/2;

            a.recycle();
        }

        //For some reason it won't draw if I don't give it a background color
        this.setBackgroundColor(Color.alpha(0));

        mGestureDetector = new GestureDetector(context, this);

    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        int hMode = MeasureSpec.getMode(heightMeasureSpec);

        int maxHeight = 0;
        for(int i = 0 ; i < this.getChildCount() ; i++){
            maxHeight = Math.max(maxHeight, this.getChildAt(i).getLayoutParams().height);
        }

        if((maxHeight < height && hMode == MeasureSpec.AT_MOST) || hMode == MeasureSpec.UNSPECIFIED)
            height = maxHeight;

        mViewBounds = new Rect(0, 0, width, height);
        mTabBounds.offsetTo(mViewBounds.right - mTabBounds.width(), mTabBounds.top);
        mBodyBounds = new Rect(0, 0, mTabBounds.left, mViewBounds.bottom);

        mTabIconBounds = new Rect(mTabBounds.left + mTabPadding,
                                    mTabBounds.top + mTabPadding,
                                    mTabBounds.right - mTabPadding,
                                    mTabBounds.bottom - mTabPadding
                                 );

        mBodyBackground.setBounds(mBodyBounds);
        mTabBackground.setBounds(mTabBounds);
        mTabIcon.setBounds(mTabIconBounds);

        this.setMeasuredDimension(width, height);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        //The area in which the children will be laid out
        mLayoutBounds = new Rect(
                this.getPaddingLeft(),
                this.getPaddingTop(),
                mBodyBounds.right - this.getPaddingRight(),
                mBodyBounds.bottom - this.getPaddingBottom()
        );

        int left = mLayoutBounds.left + mScrollOffset;
        for(int i = 0 ; i < this.getChildCount() ; i++){

            View child = this.getChildAt(i);
            View prevChild = this.getChildAt(i-1);
            int width = child.getLayoutParams().width;
            int height = child.getLayoutParams().height;

            if(child.getVisibility() != GONE){
                int top = mLayoutBounds.centerY() - height/2;
                int right = left + width;
                int bottom = top + height;

                child.layout(left, top, right, bottom);

                left += width + mChildSpacing;
            }
        }
    }



    public void open(){
        if(!opened){
            this.animate().translationX(mBodyBounds.width());
            opened = true;
        }

        if(expansionListener != null)
            expansionListener.opened();
    }

    public void close(){
        if(opened){
            this.animate().translationX(0);
            opened = false;
        }

        if(expansionListener != null)
            expansionListener.closed();
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);
    }


    @Override
    public void draw(Canvas canvas){
        mBodyBackground.draw(canvas);
        mTabBackground.draw(canvas);

        if(mTabIcon != null){
            mTabIcon.draw(canvas);
        }
        canvas.clipRect(mLayoutBounds);
        super.draw(canvas);
    }

    private boolean dragging = false;

    @Override
    public boolean onTouchEvent(MotionEvent event){
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(mTabBounds.contains((int)event.getX(), (int)event.getY()))
                    dragging = true;
                break;
            case MotionEvent.ACTION_UP:
                dragging = false;
        }
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {

        if(mTabBounds.contains((int)e.getX(), (int)e.getY())){
            if(!opened)
                open();
            else
                close();
        }


        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if(mLayoutBounds.contains((int)e1.getX(), (int)e2.getY()) && this.getChildCount() > 0){

            View firstChild = this.getChildAt(0);
            View lastChild = this.getChildAt(this.getChildCount() - 1);
            if((distanceX > 0 && lastChild.getLeft() >= mLayoutBounds.centerX()) ||
                    (distanceX < 0 && firstChild.getLeft() <= mLayoutBounds.centerX())){
                mScrollOffset -= distanceX;
                this.requestLayout();
            }

        }

        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if(velocityX > 500){
            open();
        }else if(velocityX < -500){
            close();
        }

        return true;
    }

    /***********************************Getters*************************************************************/


    /**********************************Setters**************************************************************/

    /**********************************AddView and RemoveView overrides**************************************************/
    private OnTouchListener childListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(childTouchListener != null)
                childTouchListener.onTouch(v, event);
            return false;
        }
    };

    @Override
    public void addView(View view){
        super.addView(view);
        view.setOnTouchListener(childListener);
    }

    @Override
    public void addView(View view, int index){
        super.addView(view, index);
        view.setOnTouchListener(childListener);
    }

    @Override
    public void addView(View view, int index, LayoutParams layoutParams){
        super.addView(view, index, layoutParams);
        view.setOnTouchListener(childListener);
    }

    @Override
    public void addView(View view, LayoutParams layoutParams){
        super.addView(view, layoutParams);
        view.setOnTouchListener(childListener);
    }

    @Override
    public void addView(View view, int width, int height){
        super.addView(view, width, height);
        view.setOnTouchListener(childListener);
    }

    @Override
    public void removeView(View view){
        super.removeView(view);
        view.setOnTouchListener(null);
    }

    /*********************************Listeners and Listener handlers************************************************************/
    private OnChildTouchListener childTouchListener;
    private ExpansionListener expansionListener;

    public void setOnChildTouchListener(OnChildTouchListener ls){
        childTouchListener = ls;
    }

    public void setExpansionListener(ExpansionListener ls){
        expansionListener = ls;
    }

    public OnChildTouchListener getChildTouchListener(){
        return childTouchListener;
    }

    public ExpansionListener getExpansionListener(){
        return expansionListener;
    }


    public interface OnChildTouchListener{
        public void onTouch(View child, MotionEvent evt);
    }

    public interface ExpansionListener{
        public void opened();
        public void closed();
    }


}

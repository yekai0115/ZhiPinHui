package com.zph.commerce.dialog.avloading;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.View;

import com.zph.commerce.R;


/**
 *
 .BallPulse,
 .BallGridPulse,
 .BallClipRotate,
 .BallClipRotatePulse,
 .SquareSpin,
 .BallClipRotateMultiple,
 .BallPulseRise,
 .BallRotate,
 .CubeTransition,
 .BallZigZag,
 .BallZigZagDeflect,
 .BallTrianglePath,
 .BallScale,
 .LineScale,
 .LineScaleParty,
 .BallScaleMultiple,
 .BallPulseSync,
 .BallBeat,
 .LineScalePulseOut,
 .LineScalePulseOutRapid,
 .BallScaleRipple,
 .BallScaleRippleMultiple,
 .BallSpinFadeLoader,
 .LineSpinFadeLoader,
 .TriangleSkewSpin,
 .Pacman,
 .BallGridBeat,
 .SemiCircleSpin
 *
 */
public class AVLoadingIndicatorView extends View{


    //indicators
    public static final int BallPulse=0;
    public static final int BallGridPulse=1;
    public static final int BallClipRotate=2;
    public static final int BallClipRotatePulse=3;
    public static final int SquareSpin=4;
    public static final int BallClipRotateMultiple=5;
    public static final int BallPulseRise=6;
    public static final int BallRotate=7;
    public static final int CubeTransition=8;
    public static final int BallZigZag=9;
    public static final int BallZigZagDeflect=10;
    public static final int BallTrianglePath=11;
    public static final int BallScale=12;
    public static final int LineScale=13;
    public static final int LineScaleParty=14;
    public static final int BallScaleMultiple=15;
    public static final int BallPulseSync=16;
    public static final int BallBeat=17;
    public static final int LineScalePulseOut=18;
    public static final int LineScalePulseOutRapid=19;
    public static final int BallScaleRipple=20;
    public static final int BallScaleRippleMultiple=21;
    public static final int BallSpinFadeLoader=22;
    public static final int LineSpinFadeLoader=23;
    public static final int TriangleSkewSpin=24;
    public static final int Pacman=25;
    public static final int BallGridBeat=26;
    public static final int SemiCircleSpin=27;


    @IntDef(flag = true,
            value = {
                    BallPulse,
                    BallGridPulse,
                    BallClipRotate,
                    BallClipRotatePulse,
                    SquareSpin,
                    BallClipRotateMultiple,
                    BallPulseRise,
                    BallRotate,
                    CubeTransition,
                    BallZigZag,
                    BallZigZagDeflect,
                    BallTrianglePath,
                    BallScale,
                    LineScale,
                    LineScaleParty,
                    BallScaleMultiple,
                    BallPulseSync,
                    BallBeat,
                    LineScalePulseOut,
                    LineScalePulseOutRapid,
                    BallScaleRipple,
                    BallScaleRippleMultiple,
                    BallSpinFadeLoader,
                    LineSpinFadeLoader,
                    TriangleSkewSpin,
                    Pacman,
                    BallGridBeat,
                    SemiCircleSpin
            })
    public @interface Indicator{}

    //Sizes (with defaults in DP)
    public static final int DEFAULT_SIZE=45;

    //attrs
    int mIndicatorId;
    int mIndicatorColor;

    Paint mPaint;

    BaseIndicatorController mIndicatorController;

    private boolean mHasAnimation;
    private Context mContext;

    public AVLoadingIndicatorView(Context context) {
        super(context);
        this.mContext=context;
        init(null, 0);
    }

    public AVLoadingIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext=context;
        init(attrs, 0);
    }

    public AVLoadingIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext=context;
        init(attrs, defStyleAttr);
    }


 
  

    private void init(AttributeSet attrs, int defStyle) {
    	try {
    		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.AVLoadingIndicatorView);
            mIndicatorId=a.getInt(R.styleable.AVLoadingIndicatorView_indicator, BallPulse);
            mIndicatorColor=a.getColor(R.styleable.AVLoadingIndicatorView_indicator_color, mContext.getResources().getColor(R.color.white));
            a.recycle();
            mPaint=new Paint();
            mPaint.setColor(mIndicatorColor);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setAntiAlias(true);
            applyIndicator();
		} catch (Exception e) {
			
		}
        
    }

    private void applyIndicator(){
        switch (mIndicatorId){
            case BallPulse:
                mIndicatorController=new BallPulseIndicator();
                break;
           
            case BallSpinFadeLoader:   //
                mIndicatorController=new BallSpinFadeLoaderIndicator();
                break;
            case LineSpinFadeLoader://
                mIndicatorController=new LineSpinFadeLoaderIndicator();
                break;
           
        }
        mIndicatorController.setTarget(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width  = measureDimension(dp2px(DEFAULT_SIZE), widthMeasureSpec);
        int height = measureDimension(dp2px(DEFAULT_SIZE), heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private int measureDimension(int defaultSize,int measureSpec){
        int result = defaultSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = Math.min(defaultSize, specSize);
        } else {
            result = defaultSize;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawIndicator(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!mHasAnimation){
            mHasAnimation=true;
            applyAnimation();
        }
    }

    void drawIndicator(Canvas canvas){
        mIndicatorController.draw(canvas,mPaint);
    }

    void applyAnimation(){
        mIndicatorController.createAnimation();
    }

    private int dp2px(int dpValue) {
        return (int) getContext().getResources().getDisplayMetrics().density * dpValue;
    }


}

package com.example.sree.assignment3;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sree on 2/16/2018.
 */

public class CircleView extends View implements View.OnTouchListener
{
    private Paint paint = new Paint();
    public static List<Circle> circles = new ArrayList<Circle>();
    float xLocalCoordinate = 0f;
    float yLocalCoordinate = 0f;
    public static float radius = 10f;
    public static boolean growCircle = true;
    long timeStart;
    static int count=0;
    static boolean show = true;

    public CircleView(Context context)
    {
        super(context);
        paint.setColor(Color.GREEN);
        //paint.setStyle(Paint.Style.STROKE);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            xLocalCoordinate = event.getX();
            yLocalCoordinate = event.getY();
            if(this.checkForOverlapping(xLocalCoordinate,yLocalCoordinate))
            {
                show = false;
                timeStart = System.currentTimeMillis();
            }
            else
            {
                show = true;
                growCircle = true;
                Runnable growCircleThread = new growCircleThread(xLocalCoordinate,yLocalCoordinate,circles);
                new Thread(growCircleThread).start();
            }
        }
        if (event.getAction() == MotionEvent.ACTION_UP)
        {
            long timeEnd = System.currentTimeMillis();
            growCircle = false;
            if(this.checkForOverlapping(xLocalCoordinate,yLocalCoordinate))
            {
                SwipeData swipeData = new SwipeData();
                swipeData.fromXCordinate = xLocalCoordinate;
                swipeData.fromYCordinate = yLocalCoordinate;
                swipeData.toXCordinate = event.getX();
                swipeData.toYCordinate = event.getY();
                swipeData.distance = this.calDistance(xLocalCoordinate,yLocalCoordinate,event.getX(),event.getY());
                swipeData.velocity = swipeData.distance/(timeEnd - timeStart);
                Circle circleLocalCopy= new Circle();
                circleLocalCopy = getCircle(xLocalCoordinate,yLocalCoordinate);
                if(circleLocalCopy.motion == false)
                {
                    Runnable moveCircle = new MoveCircle(swipeData, circleLocalCopy);
                    new Thread(moveCircle).start();
                }
            }
            else
            {
                Circle circle = new Circle();
                circle.xCoordinate = xLocalCoordinate;
                circle.yCoordinate = yLocalCoordinate;
                circle.radius = this.radius;
                circle.motion = false;
                this.radius = 10f;
                if (circles.size() < 15)
                {
                    count = count + 1;
                    circle.index = count;
                    circles.add(circle);
                }
            }
            invalidate();
        }
        return true;
    }
    @Override
    protected void onDraw(Canvas canvas)
    {
        for(Circle circle: circles)
        {
            canvas.drawCircle(circle.xCoordinate, circle.yCoordinate,circle.radius, paint);
        }
        if(circles.size() < 15 && show)
            canvas.drawCircle(this.xLocalCoordinate, this.yLocalCoordinate,this.radius, paint);
        invalidate();
    }
    public boolean checkForOverlapping(float xCor,float yCor)
    {
        float x1 = xCor;
        float y1 = yCor;
        for(Circle circle: circles)
        {
            float x2 = circle.xCoordinate;
            float y2 = circle.yCoordinate;
            float r = circle.radius;
            float distance = (float) Math.sqrt(Math.pow((x1-x2),2)+Math.pow((y1-y2),2));
            if(distance<=r)
                return true;
        }
        return false;
    }
    public float calDistance(float x1,float y1, float x2,float y2)
    {
        float distance = (float) Math.sqrt(Math.pow((x1-x2),2)+Math.pow((y1-y2),2));
        return distance;
    }
    public Circle getCircle(float xCor,float yCor)
    {
        float x1 = xCor;
        float y1 = yCor;
        float distance=0;
        for(Circle circle: circles)
        {
            float x2 = circle.xCoordinate;
            float y2 = circle.yCoordinate;
            float r = circle.radius;
            distance = (float) Math.sqrt(Math.pow((x1-x2),2)+Math.pow((y1-y2),2));
            if(distance<=r) {
                Log.i("Radius",String.valueOf(r));
                Log.i("Radius",String.valueOf(distance));
                return circle;
            }
        }
        return null;
    }
}
class growCircleThread implements Runnable
{
    float xCor;
    float yCor;
    List<Circle> circlesCopy = new ArrayList<Circle>();
    growCircleThread(float xCordinate,float yCordinate,List<Circle> circles)
    {
        xCor = xCordinate;
        yCor = yCordinate;
        for (Circle circle: circles)
        {
            circlesCopy.add(circle);
        }
    }
    public void run()
    {
        boolean checkOverlap = false;
        while(CircleView.growCircle)
        {
            for(Circle circle: circlesCopy)
            {
                float x2 = circle.xCoordinate;
                float y2 = circle.yCoordinate;
                float r1 = circle.radius;
                float distance = (float) Math.sqrt(Math.pow((xCor-x2),2)+Math.pow((yCor-y2),2));
                if(!(CircleView.radius+r1 < distance))
                    checkOverlap = true;
            }
            if(!checkOverlap)
            {
                if(circlesCopy.size() > 10)
                    CircleView.radius = CircleView.radius + 0.001f;
                else if(circlesCopy.size() > 5 && circlesCopy.size() > 10)
                    CircleView.radius = CircleView.radius + 0.0008f;
                else
                    CircleView.radius = CircleView.radius + 0.00007f;
            }
            else
                CircleView.growCircle = false;
            if(circlesCopy.size() == 0)
                CircleView.radius = CircleView.radius + 0.00000001f;
            //invalidate();
        }
    }
}
class Circle
{
    float xCoordinate;
    float yCoordinate;
    float radius;
    int index;
    boolean motion;
}
class SwipeData
{
    float fromXCordinate;
    float fromYCordinate;
    float toXCordinate;
    float toYCordinate;
    float distance;
    float velocity;
}
class MoveCircle implements Runnable
{
    SwipeData data = new SwipeData();
    Circle localCircleCopy = new Circle();
    public MoveCircle(SwipeData swipeData, Circle originalCircleCopy)
    {
        data.toXCordinate = swipeData.toXCordinate;
        data.toYCordinate = swipeData.toYCordinate;
        data.fromXCordinate = swipeData.fromXCordinate;
        data.fromYCordinate = swipeData.fromYCordinate;
        data.velocity = swipeData.velocity;
        localCircleCopy.xCoordinate = originalCircleCopy.xCoordinate;
        localCircleCopy.yCoordinate = originalCircleCopy.yCoordinate;
        localCircleCopy.index = originalCircleCopy.index;
        localCircleCopy.radius = originalCircleCopy.radius;
    }
    public void run()
    {
        for(Circle circle: CircleView.circles)
        {
            if(localCircleCopy.xCoordinate == circle.xCoordinate && localCircleCopy.yCoordinate == circle.yCoordinate)
            {
                int position = localCircleCopy.index - 1;
                Circle circleToSave = new Circle();
                circleToSave.index = localCircleCopy.index;
                circleToSave.radius = localCircleCopy.radius;
                circleToSave.motion = true;

                float x = circle.xCoordinate;
                float y = circle.yCoordinate;
                float radius = circleToSave.radius;

                float x0 = circle.xCoordinate;
                float x1 = data.toXCordinate;
                float y0 = circle.yCoordinate;
                float y1 = data.toYCordinate;
                float velocity = data.velocity;
                while(true)
                {
                    if(x0>x1)
                    {
                        circleToSave.xCoordinate = x;
                        circleToSave.yCoordinate = ((y0) * ((1 - ((x - x0)) / (x1 - x0))) + (y1 * ((x - x0) / (x1 - x0))));
                        x = (x - (velocity));
                    }
                    else if(x0<x1)
                    {
                        circleToSave.xCoordinate = x;
                        circleToSave.yCoordinate = ((y0) * ((1 - ((x - x0)) / (x1 - x0))) + (y1 * ((x - x0) / (x1 - x0))));
                        x = (x + (velocity));
                    }
                    else if(x0==x1 && y0>y1)
                    {
                        circleToSave.xCoordinate = x;
                        circleToSave.yCoordinate = y;
                        y = y - velocity;
                    }
                    else if(x0==x1 && y0<y1)
                    {
                        circleToSave.xCoordinate = x;
                        circleToSave.yCoordinate = y;
                        y = y + velocity;
                    }
                    Log.i("Helper!! Dont Remove","-------------");
                    CircleView.circles.set(position, circleToSave);
                }
            }
        }
    }
}
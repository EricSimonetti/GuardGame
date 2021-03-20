package com.guard.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.ai.utils.Location;

public class Player implements Location<Vector2> {
    final static private float DEFAULT_X = 200f, DEFAULT_Y = 600f;
    private Vector2 linearVelocity, position;
    private float orientation, angularVelocity, angularVelocityMax, maxLinearSpeed;
    private Color color;

    public Player(Vector2 position, float orientation){
        this.linearVelocity = new Vector2();
        this.position = position;
        this.orientation = orientation;
        this.angularVelocity = 0;
        this.maxLinearSpeed = 4;
        this.angularVelocityMax = .1f;
        this.color = Color.CORAL;
    }

    public Player(){
        this.linearVelocity = new Vector2();
        this.position = new Vector2(DEFAULT_X, DEFAULT_Y);
        this.angularVelocity = 0;
        this.maxLinearSpeed = 7;
        this.angularVelocityMax = .1f;
        this.color = Color.CORAL;
    }

    public void render(ShapeRenderer shapeRenderer){
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(color);
        shapeRenderer.circle(position.x, position.y, 10);

        shapeRenderer.setColor(Color.BLACK);

        Vector2 eyeVec = getEyeposition((orientation-.4f),position);
        shapeRenderer.circle(eyeVec.x, eyeVec.y, 3f);
        eyeVec = getEyeposition((orientation+.4f),position);
        shapeRenderer.circle(eyeVec.x, eyeVec.y, 3f);

        shapeRenderer.end();
    }
    private Vector2 getEyeposition(float ang, Vector2 position){
        return new Vector2(position.x+7*(float)Math.cos(ang), position.y+7*(float)Math.sin(ang));
    }

    public void setSpeed(float speed){
        linearVelocity.x = (float)(speed*Math.cos(orientation));
        linearVelocity.y = (float)(speed*Math.sin(orientation));
    }

    public void forward(){
        setSpeed(linearVelocity.len()+.3f);
    }

    public void backward(){
        if(linearVelocity.len()-.1<0){
            setSpeed(0);
        }
        else {
            setSpeed(linearVelocity.len()-.1f);
        }
    }

    public void right(){
        linearVelocity.x+=.3f;
    }

    public void left(){
        linearVelocity.x-=.3f;
    }

    public void up(){
        linearVelocity.y+=.3f;
    }

    public void down(){
        linearVelocity.y-=.3f;
    }

    public void act(){
        if(linearVelocity.len()!=0) {
            orientation = linearVelocity.angleRad();
        }
        if(linearVelocity.len()-.1<0) setSpeed(0);
        if(linearVelocity.len()>maxLinearSpeed){
            linearVelocity = linearVelocity.limit(maxLinearSpeed);
        }
        position.x += linearVelocity.x;
        position.y += linearVelocity.y;
        linearVelocity.x/=1.05f;
        linearVelocity.y/=1.05f;

        fixWallCollide();

    }

    private void fixWallCollide(){
        if(position.x > (758)){
            wallHitX(758);
        }
        else if(position.x<15){
            wallHitX(15);
        }
        if(position.y > (662)){
            wallHitY(662);
        }
        else if(position.y<15){
            wallHitY(15);
        }
        xBoundary(586, 514, 158);
        xBoundary(680, 410, 320);

        if(position.x<535 && position.x>440 && position.y<110 && position.y>25){
            float diffy1 = Math.abs(110-position.y), diffy2 = Math.abs(25-position.y);
            float diffx1 = Math.abs(535-position.x), diffx2 = Math.abs(440-position.x);
            float min = Math.min(Math.min(diffx1, diffx2), Math.min(diffy1, diffy2));
            if(min==diffx1){
                wallHitX(535);
            }
            else if(min==diffx2){
                wallHitX(440);
            }
            else if(min==diffy1){
                wallHitY(110);
            }
            else if(min==diffy2){
                wallHitY(25);
            }
        }if(position.x > (758)){
            wallHitX(758);
        }
        else if(position.x<15){
            wallHitX(15);
        }
        if(position.y > (662)){
            wallHitY(662);
        }
        else if(position.y<15){
            wallHitY(15);
        }
        xBoundary(586, 514, 158);
        xBoundary(680, 410, 320);

        if(position.x<535 && position.x>440 && position.y<110 && position.y>25){
            float diffy1 = Math.abs(110-position.y), diffy2 = Math.abs(25-position.y);
            float diffx1 = Math.abs(535-position.x), diffx2 = Math.abs(440-position.x);
            float min = Math.min(Math.min(diffx1, diffx2), Math.min(diffy1, diffy2));
            if(min==diffx1){
                wallHitX(535);
            }
            else if(min==diffx2){
                wallHitX(440);
            }
            else if(min==diffy1){
                wallHitY(110);
            }
            else if(min==diffy2){
                wallHitY(25);
            }
        }
    }

    private void xBoundary(int x, int y1, int y2){
        if(position.x<x && position.y<y1 && position.y>y2){
            float diffy1 = Math.abs(y1-position.y), diffy2 = Math.abs(y2-position.y);
            float diffx = Math.abs(x-position.x);
            if(diffx<diffy1&&diffx<diffy2){
                wallHitX(x);
            }
            else {
                wallHitY(diffy1 < diffy2 ? y1 : y2);
            }
        }
    }

    public void wallHitX(float wallLoc){
        position.x = wallLoc;
        linearVelocity.x = 0;
    }

    public void wallHitY(float wallLoc){
        position.y = wallLoc;
        linearVelocity.y = 0;
    }

    public void turnRight(){
        if(angularVelocity>-angularVelocityMax){
            angularVelocity -= .01f;
        }
    }

    public void turnLeft(){
        if(angularVelocity<angularVelocityMax){
            angularVelocity += .01f;
        }
    }

    @Override
    public Vector2 angleToVector(Vector2 outVector, float angle){
        outVector.x = -(float)Math.sin(angle);
        outVector.y = (float)Math.cos(angle);
        return outVector;
    }

    @Override
    public float getOrientation(){
        return orientation;
    }
    @Override
    public Vector2 getPosition(){
        return position;
    }
    @Override
    public Location newLocation(){
        return this;
    }
    @Override
    public void setOrientation(float orientation){
        this.orientation = orientation;
    }
    @Override
    public float vectorToAngle(Vector2 vector){
        return (float)Math.atan(vector.y/vector.x);
    }
}

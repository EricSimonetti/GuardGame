package com.guard.game;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.ai.steer.behaviors.CollisionAvoidance;
import com.badlogic.gdx.ai.steer.behaviors.Seek;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class Guard implements Steerable<Vector2> {
    final static private float DEFAULT_X = 590f, DEFAULT_Y = 155f;
    private Vector2 linearVelocity, position;
    private float orientation, angularVelocity, maxLinearSpeed,
                  maxLinearAcceleration, maxAngularSpeed, maxAngularAcceleration;
    private float goalOrientation;
    private Color color;
    private int state = 0;
    private boolean seePlayer;
    private Player player;
    private Player lastKnownLocation;
    private Vector2[][] edgeEndPoints;

    private static final SteeringAcceleration<Vector2> steeringOutput = new SteeringAcceleration<Vector2>(new Vector2());
    SteeringBehavior<Vector2> steeringBehavior;
    Seek<Vector2> seek;
    Arrive<Vector2> arrive;
    CollisionAvoidance<Vector2> avoid;


    public Guard(Player player, Vector2[][] edgeEndPoints){
        this.position = new Vector2(DEFAULT_X, DEFAULT_Y);
        this.linearVelocity = new Vector2();
        this.maxLinearSpeed = 2;
        this.maxLinearAcceleration = 2;
        this.orientation = (float)(Math.PI);
        this.angularVelocity = 0;
        this.maxAngularSpeed = .08f;
        this.maxAngularAcceleration = .1f;

        this.edgeEndPoints = edgeEndPoints;
        this.color = Color.NAVY;
        this.seePlayer = false;
        this.player = player;
        this.seek = new Seek<>(this, player);
        this.arrive = new Arrive<>(this, player).setArrivalTolerance(1);
        //this.avoid = new CollisionAvoidance<Vector2>();
    }


    @Override
    public float vectorToAngle (Vector2 vector) {
        return (float)Math.atan2(vector.x, vector.y);
    }

    @Override
    public Vector2 getLinearVelocity(){
        return linearVelocity;
    }

    @Override
    public float getAngularVelocity(){
        return angularVelocity;
    }

    @Override
    public float getBoundingRadius(){
        return 10;
    }

    @Override
    public boolean isTagged(){
        return false;
    }

    @Override
    public void setTagged(boolean tagged){ }

    @Override
    public Vector2 getPosition(){
        return position;
    }

    @Override
    public float getOrientation(){
        return orientation;
    }

    @Override
    public Location<Vector2> newLocation(){
       return null;
    }

    @Override
    public void setOrientation(float orientation){
        this.orientation = orientation;
    }

    @Override
    public float getMaxLinearSpeed () {
        return maxLinearSpeed;
    }

    @Override
    public void setMaxLinearSpeed (float maxLinearSpeed) {
        this.maxLinearSpeed = maxLinearSpeed;
    }

    @Override
    public float getMaxLinearAcceleration () {
       return maxLinearAcceleration;
    }

    @Override
    public void setMaxLinearAcceleration (float maxLinearAcceleration) {
        this.maxLinearAcceleration = maxLinearAcceleration;
    }

    @Override
    public float getMaxAngularSpeed () {
        return maxAngularSpeed;
    }

    @Override
    public void setMaxAngularSpeed (float maxAngularSpeed) {
        this.maxAngularSpeed = maxAngularSpeed;
    }

    @Override
    public float getMaxAngularAcceleration () {
        return maxAngularAcceleration;
    }

    @Override
    public void setMaxAngularAcceleration (float maxAngularAcceleration) {
        this.maxLinearAcceleration = maxAngularAcceleration;
    }

    @Override
    public float getZeroLinearSpeedThreshold () {
        return 0.001f;
    }

    @Override
    public void setZeroLinearSpeedThreshold (float value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Vector2 angleToVector (Vector2 outVector, float angle) {
        outVector.x = -(float)Math.sin(angle);
        outVector.y = (float)Math.cos(angle);
        return outVector;
    }

    public void render(ShapeRenderer shapeRenderer, SpriteBatch batch) {
        shapeRenderer.setColor(new Color(1, 1, 0, 0.1f));
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        float x = position.x, y = position.y;

        float l = 25f, w = (float) Math.tan(1.0472) * l;

        float firstX = (float) (l * Math.cos(orientation) - ((-w) * Math.sin(orientation)) + x),
                firstY = (float) (l * Math.sin(orientation) + ((-w) * Math.cos(orientation)) + y),
                lastX = (float) (l * Math.cos(orientation) - w * Math.sin(orientation) + x),
                lastY = (float) (l * Math.sin(orientation) + w * Math.cos(orientation) + y);
        Vector2 previous = findAnglePoint(new Vector2(firstX, firstY));
        Vector2 last = findAnglePoint(new Vector2(lastX, lastY));


        ArrayList<Vector2> inVision = new ArrayList<>();
        ArrayList<Vector2> points = new ArrayList<>();
        for (Vector2[] arr : edgeEndPoints) {
            for(Vector2 curr: arr){
                if(pointInVision(curr)) {
                    inVision.add(curr);
                }
            }
        }

        for(float ang = orientation-1.0472f; ang<orientation+1.0472f; ang+=.00005f){  //orders points in vision
            Vector2 ray = new Vector2((float)Math.cos(ang), (float)Math.sin(ang));
            ray = ray.add(position);
            float slope = (float)(Math.sin(ang)/Math.cos(ang));
            Vector2 remove = null;

            for(Vector2 curr: inVision){
                float dist = Math.abs(curr.y - (slope*(curr.x-ray.x)+ray.y));
                if(dist<1){
                    remove = curr;
                    points.add(curr);
                }
            }
            inVision.remove(remove);
        }


        if(seePlayer){
            shapeRenderer.setColor(Color.ORANGE);
        }
        else{
            shapeRenderer.setColor(Color.YELLOW);
        }

        for(Vector2 curr: points){ //draw vision with triangles using three rays in order to see past corners

            Vector2 prevDist = new Vector2(position.x-(curr.x), position.y-curr.y);
            prevDist.setAngleRad(prevDist.angleRad()-.001f);
            Vector2 prevRay = new Vector2(position.x, position.y);
            prevRay = prevRay.sub(prevDist);
            Vector2 prevAnglePoint = findAnglePoint(prevRay);
            shapeRenderer.triangle(x, y, previous.x, previous.y, prevAnglePoint.x, prevAnglePoint.y);

            Vector2 ray = new Vector2(curr.x, curr.y);
            ray = ray.setAngleRad(ray.angleRad());
            Vector2 anglePoint = findAnglePoint(ray);
            shapeRenderer.triangle(x, y, prevAnglePoint.x, prevAnglePoint.y, anglePoint.x, anglePoint.y);

            Vector2 nextDist = new Vector2(position.x-(curr.x), position.y-curr.y);
            nextDist.setAngleRad(nextDist.angleRad()+.001f);
            Vector2 nextRay = new Vector2(position.x, position.y);
            nextRay = nextRay.sub(nextDist);
            Vector2 nextAnglePoint = findAnglePoint(nextRay);
            shapeRenderer.triangle(x, y, anglePoint.x, anglePoint.y, nextAnglePoint.x, nextAnglePoint.y);
            previous = nextAnglePoint;

        }
        shapeRenderer.triangle(x, y, previous.x, previous.y, last.x, last.y);



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

    public void update(){
        seePlayer = calcVision();
        tick(); //updates position and orientation
        if (steeringBehavior != null) {
            steeringBehavior.calculateSteering(steeringOutput);
            applySteering(steeringOutput, .1f);
        }
        act();
    }

    public void act(){
        switch (state) {
            case 0: // Guarding turing left
                steeringBehavior = null;
                if(seePlayer){
                    steeringBehavior = seek;
                    state = 2;
                }else {
                    if (orientation - Math.PI <= 0) {
                        state = 1;
                        angularVelocity = 0;
                    }
                    else {
                        turnRightPatrol();
                    }
                }
                break;
            case 1: // Guarding turing right
                if(seePlayer){
                    turnRightPatrol();
                    steeringBehavior = seek;
                    state = 2;
                }else {
                    turnLeftPatrol();
                    if (orientation - (2f*Math.PI) > Math.PI/2f) {
                        state = 0;
                    }
                }
                break;
            case 2: //seek player
                //seek
                if(!seePlayer){
                    lastKnownLocation = new Player(new Vector2(player.getPosition()), player.getOrientation());
                    arrive.setTarget(lastKnownLocation);
                    steeringBehavior = arrive;
                    state = 3;
                }
                break;
            case 3: //arrive at players last known pos
                if(seePlayer){
                    steeringBehavior = seek;
                    state = 2;
                }else {
                    if(Math.abs(position.x-lastKnownLocation.getPosition().x)<1 &&
                       Math.abs(position.y-lastKnownLocation.getPosition().y)<1){
                        state = 4;
                        arrive.setTarget(new Player(new Vector2(DEFAULT_X, DEFAULT_Y), (float)Math.PI));
                    }
                }
                break;
            case 4:
                if(seePlayer){
                    steeringBehavior = seek;
                    state = 2;
                }
                else if(Math.abs(position.x-DEFAULT_X)<2 &&
                        Math.abs(position.y-DEFAULT_Y)<2){
                    steeringBehavior = null;
                    linearVelocity.x = 0;
                    linearVelocity.y = 0;
                    orientation = fixedOrientation();
                    if(orientation>Math.PI/2&&orientation<(7.0*Math.PI/4.0))
                        turnRightPatrol();
                    else
                        turnLeftPatrol();
                    if(Math.abs(orientation-Math.PI/2.0)>=.1||Math.abs(orientation-Math.PI/2.0)<=.1){
                        state = 0;
                    }
                }
                break;
        }
    }

    public float fixedOrientation(){
        float temp = orientation;
        while(temp<0){
            temp+=(float)Math.PI*2f;
        }
        while(temp>Math.PI*2){
            temp-=(float)Math.PI*2f;
        }
        return temp;
    }

    public void turnRightPatrol(){
        if(angularVelocity>-.01){
            angularVelocity -= .001f;
        }
    }

    public void turnLeftPatrol(){
        if(angularVelocity<.01){
            angularVelocity += .001f;
        }
    }

    private void applySteering (SteeringAcceleration<Vector2> steering, float time) {
        linearVelocity.mulAdd(steering.linear, time).limit(this.getMaxLinearSpeed());
        if(linearVelocity.len()-.1>0) {
            goalOrientation = calculateOrientationFromLinearVelocity();
            if (goalOrientation != orientation) {
                float diff = goalOrientation - orientation;
                if(diff>Math.PI)
                    diff-=2*Math.PI;
                if(diff<-Math.PI)
                    diff+=2*Math.PI;

                angularVelocity = (diff) * time;
                if(angularVelocity>maxAngularSpeed)
                    angularVelocity = maxAngularSpeed;
                if(angularVelocity<-maxAngularSpeed)
                    angularVelocity = -maxAngularSpeed;
            }
        }
    }

    public float calculateOrientationFromLinearVelocity(){
        return (float)Math.atan2(linearVelocity.y, linearVelocity.x);
    }

    public void tick(){
        if(linearVelocity.len()>maxLinearSpeed){
            linearVelocity = linearVelocity.limit(maxLinearSpeed);
            //linearVelocity.x = (float)(maxLinearVelocity*Math.cos(orientation));
            //linearVelocity.y = (float)(maxLinearVelocity*Math.sin(orientation));
        }
        position.x += linearVelocity.x;
        position.y += linearVelocity.y;
        if(steeringBehavior!=null&&Math.abs(orientation - goalOrientation) < .01){
            angularVelocity = 0;
            orientation = goalOrientation;
        }
        orientation += angularVelocity;
        fixWallCollide();
    }



    public boolean calcVision(){
        return calcVisionPos()||calcVisionNeg();
    }

    public boolean calcVisionPos(){
        Vector2 guardOrientation = new Vector2((float)Math.cos(getOrientation()), (float)Math.sin(getOrientation()));
        Vector2 perp = PerpendicularClockwise(guardOrientation).scl(10);
        Vector2 edge = perp.add(player.getPosition());

        if(pointInVision(edge)) {
            Vector2 collisionPoint = findAnglePoint(edge);
            Vector2 dist = new Vector2(position.x-(edge.x), position.y-edge.y);
            Vector2 collisionDist = new Vector2(position.x - collisionPoint.x, position.y - collisionPoint.y);
            return dist.len() < collisionDist.len();
        }
        return false;
    }
    public boolean calcVisionNeg(){
        Vector2 guardOrientation = new Vector2((float)Math.cos(getOrientation()), (float)Math.sin(getOrientation()));
        Vector2 perp = PerpendicularCounterClockwise(guardOrientation).scl(10);
        Vector2 edge = perp.add(player.getPosition());

        if(pointInVision(edge)) {
            Vector2 collisionPoint = findAnglePoint(edge);
            Vector2 dist = new Vector2(position.x-(edge.x), position.y-edge.y);
            Vector2 collisionDist = new Vector2(position.x - collisionPoint.x, position.y - collisionPoint.y);
            return dist.len() < collisionDist.len();
        }
        return false;
    }

    public boolean pointInVision(Vector2 point){
        Vector2 dist = new Vector2(position.x-(point.x), position.y-(point.y)).nor();
        Vector2 guardOrientation = new Vector2((float)Math.cos(getOrientation()), (float)Math.sin(getOrientation()));
        float dot = dist.dot(guardOrientation);
        return dot<=-.49;
    }

    public static Vector2 PerpendicularClockwise(Vector2 vector2)
    {
        return new Vector2(vector2.y, -(vector2.x));
    }

    public static Vector2 PerpendicularCounterClockwise(Vector2 vector2)
    {
        return new Vector2(-vector2.y, vector2.x);
    }

    public Vector2 findAnglePoint(Vector2 edgeEndPoint){
        Vector2 anglePoint = new Vector2(0, 100);
        boolean firstFound = false;
        for(int i = 0; i<edgeEndPoints.length; i++){
            for(int j = 0; j<edgeEndPoints[i].length; j++){
                int secondIndex = j+1;
                if(secondIndex >= edgeEndPoints[i].length) secondIndex = 0;
                Vector2 firstPoint = edgeEndPoints[i][j];
                Vector2 secondPoint = edgeEndPoints[i][secondIndex];

                Vector2 potentialPoint = intersection(position, edgeEndPoint, firstPoint, secondPoint);

                float greaterX = Math.max(firstPoint.x, secondPoint.x), lesserX = Math.min(firstPoint.x, secondPoint.x);
                float greaterY = Math.max(firstPoint.y, secondPoint.y), lesserY = Math.min(firstPoint.y, secondPoint.y);


                if(potentialPoint.x<=greaterX&&potentialPoint.y<=greaterY&&
                   potentialPoint.x>=lesserX&&potentialPoint.y>=lesserY &&
                   pointInVision(potentialPoint)){
                    if(!firstFound){
                        anglePoint = potentialPoint;
                        firstFound = true;
                    }
                    else {
                        Vector2 ppDist = new Vector2(position.x - (potentialPoint.x), position.y - (potentialPoint.y));
                        Vector2 apDist = new Vector2(position.x - (anglePoint.x), position.y - (anglePoint.y));
                        if (apDist.len() > ppDist.len())
                            anglePoint = potentialPoint;
                    }
                }
            }
        }
        return anglePoint;
    }

    public Vector2 intersection(Vector2 p1, Vector2 p2, Vector2 p3, Vector2 p4){
        if((p1.x-p2.x) == 0){
            if((p3.x-p4.x) == 0){
                return new Vector2(-1, -1);
            }

            float slope2 = (p3.y-p4.y)/(p3.x-p4.x);
            float x = p1.x;
            float y = slope2*(x-p3.x)+p3.y;
            return new Vector2(x,y);
        }
        if((p3.x-p4.x) == 0){
            float slope1 = (p1.y-p2.y)/(p1.x-p2.x);
            float x = p3.x;
            float y = slope1*(x-p1.x)+p1.y;
            return new Vector2(x,y);
        }


        float slope1 = (p1.y-p2.y)/(p1.x-p2.x), slope2 = (p3.y-p4.y)/(p3.x-p4.x);
        if(slope1==slope2){
            return new Vector2(-1, -1);
        }

        if(slope1 == 0){
            float x = (p1.y-p3.y)/slope2 + p3.x;
            float y = p1.y;
            return new Vector2(x,y);
        }
        if(slope2 == 0){
            float x = (p3.y-p1.y)/slope1 + p1.x;
            float y = p3.y;
            return new Vector2(x,y);
        }

        float x = (slope1*p2.x + slope2*p3.x + p2.y - p3.y)/(slope2-slope1);
        float y = (slope1*(slope2*p3.x-p3.y) - slope2*(slope1*p2.x-p2.y))/(slope2-slope1);
        return new Vector2(x,y);
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
                linearVelocity.x+=.2f;
            }
        }
    }

    public void wallHitX(float wallLoc){
        position.x = wallLoc;
        linearVelocity.x = 0;
        if(linearVelocity.y>0){
            linearVelocity.y+=.1f;
        }
        else{
            linearVelocity.y-=.1f;
        }
    }

    public void wallHitY(float wallLoc){
        position.y = wallLoc;
        linearVelocity.y = 0;
        if(linearVelocity.x>0){
            linearVelocity.x+=.1f;
        }
        else{
            linearVelocity.x-=.1f;
        }
    }
}

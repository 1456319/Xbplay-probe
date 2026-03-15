package com.studio08.xbgamestream.Controller;
/* loaded from: /app/base.apk/classes3.dex */
public class Vector2d {
    public static final Vector2d ZERO = new Vector2d();
    private double magnitude;
    private float x;
    private float y;

    public Vector2d() {
        initialize(0.0f, 0.0f);
    }

    public void initialize(float f, float f2) {
        this.x = f;
        this.y = f2;
        this.magnitude = Math.sqrt(Math.pow(f, 2.0d) + Math.pow(f2, 2.0d));
    }

    public double getMagnitude() {
        return this.magnitude;
    }

    public void getNormalized(Vector2d vector2d) {
        double d = this.magnitude;
        vector2d.initialize((float) (this.x / d), (float) (this.y / d));
    }

    public void scalarMultiply(double d) {
        initialize((float) (this.x * d), (float) (this.y * d));
    }

    public void setX(float f) {
        initialize(f, this.y);
    }

    public void setY(float f) {
        initialize(this.x, f);
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }
}

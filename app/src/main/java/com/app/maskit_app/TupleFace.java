package com.app.maskit_app;

public class TupleFace<X, Y> {
    public final X x;
    public final Y y;
    public TupleFace(X x, Y y) {
        this.x = x;
        this.y = y;
    }

    /**
     *
     * @return face location
     */
    public X getLocation() {
        return x;
    }

    /**
     * A face with a positive Euler Y angle is looking to the right of the camera,
     * or looking to the left if negative.
     * @return face angle
     */
    public Y getFaceAngle() {
        return y;
    }


}

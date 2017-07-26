package raytracing.photon;

import raytracing.Vec3d;
import raytracing.kdtree.IMultiPoint;

/**
 * Created by THU73 on 17/7/26.
 */
public class Photon implements IMultiPoint{
    private Vec3d position;
    private double strength;
    private double[] raw;

    public Photon() {
        position = new Vec3d();
        strength = 0;
    }

    public Vec3d getPosition() {
        return position;
    }

    public void setPosition(Vec3d position) {
        raw = new double[3];
        this.position = position;
        raw[0] = position.x;
        raw[1] = position.y;
        raw[2] = position.z;
    }

    public double getStrength() {
        return strength;
    }

    public void setStrength(double strength) {
        this.strength = strength;
    }

    @Override
    public int dimensionality() {
        return 3;
    }

    @Override
    public double getCoordinate(int dx) {
        if(dx == 1) {
            return position.x;
        } else if (dx == 2) {
            return position.y;
        } else {
            return position.z;
        }

    }

    @Override
    public double distance(IMultiPoint imp) {
        Photon another = (Photon) imp;
        return Math.pow((position.x - another.position.x), 2) +
                Math.pow((position.y - another.position.y), 2) +
                Math.pow((position.z - another.position.z), 2);
    }

    @Override
    public double[] raw() {
        return raw;
    }
}

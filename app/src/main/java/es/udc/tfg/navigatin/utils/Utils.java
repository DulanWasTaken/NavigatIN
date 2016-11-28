package es.udc.tfg.navigatin.utils;

/**
 * Created by Usuario on 14/11/2016.
 */

public class Utils {

    public static float radToDegrees(double angle){
        double result = angle*180/Math.PI;
        return (float) result;
    }
}

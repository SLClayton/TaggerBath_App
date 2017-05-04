package uk.co.claytapp.taggerbath;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created by Sam on 10/02/2017.
 */

public class Bounds {

    private static final String logtag = "BoundsClass";

    private LatLngBounds latLngBounds;


    public Bounds(LatLng latLng1, LatLng latLng2) {
        latLngBounds = new LatLngBounds(latLng1, latLng2);
    }

    public Bounds(LatLngBounds bounds) {
        latLngBounds = bounds;
    }

    public Bounds(LatLng centre, double height, double width) {
        //Log.i(logtag, "centre " + centre.toString() + " h:" + Double.toString(height) + " w:" + Double.toString(width));

        latLngBounds = new LatLngBounds(new LatLng(centre.latitude - (height/2f),
                                                   centre.longitude - (width/2f)),
                                        new LatLng(centre.latitude + (height/2f),
                                                   centre.longitude + (width/2f)));

    }

    public double getWidth(){
        return latLngBounds.northeast.longitude - latLngBounds.southwest.longitude;
    }

    public double getHeight(){
        return latLngBounds.northeast.latitude - latLngBounds.southwest.latitude;
    }

    public Bounds multiplyBound(double scale_mulitple){

        LatLng ne = latLngBounds.northeast;
        LatLng sw = latLngBounds.southwest;
        double height = this.getHeight();
        double width = this.getWidth();

        //Log.i(logtag, "width " + String.valueOf(width) + " height " + String.valueOf(height));

        double m = (scale_mulitple - 1)  / 2.0d;

        Bounds new_bounds = new Bounds(new LatLng(sw.latitude - (m * height),
                                                  sw.longitude - (m * width)),
                                       new LatLng(ne.latitude + (m * height),
                                                  ne.longitude + (m * width)));

        return new_bounds;
    }

    public LatLngBounds getLatLngBounds(){
        return latLngBounds;
    }

    public boolean contains(LatLng point){
        return latLngBounds.contains(point);
    }

    public boolean contains(LatLng point, double lat_allowance, double lng_allowance){
        LatLngBounds llb = new LatLngBounds(new LatLng(latLngBounds.southwest.latitude - lat_allowance,
                                                       latLngBounds.southwest.longitude - lng_allowance),
                                            new LatLng(latLngBounds.northeast.latitude + lat_allowance,
                                                       latLngBounds.northeast.longitude + lng_allowance));

        return llb.contains(point);
    }

    public LatLng getNE(){
        return latLngBounds.northeast;
    }

    public LatLng getSW(){
        return latLngBounds.southwest;
    }

    public LatLng getNW(){
        return new LatLng(latLngBounds.northeast.latitude,
                          latLngBounds.southwest.longitude);
    }

    public LatLng getSE(){
        return new LatLng(latLngBounds.southwest.latitude,
                          latLngBounds.northeast.longitude);
    }

    public LatLng getCentre(){
        return latLngBounds.getCenter();
    }


}

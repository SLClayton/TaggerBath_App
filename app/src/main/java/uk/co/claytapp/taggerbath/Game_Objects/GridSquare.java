package uk.co.claytapp.taggerbath.Game_Objects;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import uk.co.claytapp.taggerbath.Bounds;
import uk.co.claytapp.taggerbath.R;

/**
 * Created by Sam on 30/01/2017.
 */

public class GridSquare {

    private GoogleMap mMap;

    private Bounds bounds;
    private String team;
    private int level;
    private String item;

    private GroundOverlay groundOverlay;
    private GroundOverlay itemOverlay;


    public GridSquare(GoogleMap map, LatLngBounds BOUNDS, String TEAM, int LEVEL, String ITEM){
        mMap = map;
        bounds = new Bounds(BOUNDS);
        team = TEAM;
        level = LEVEL;
        item = ITEM;

        addSquareOverlay();
        addItemOverlay();
    }

    public void addSquareOverlay(){
        if (groundOverlay != null){
            groundOverlay.remove();
            groundOverlay = null;
        }
        GroundOverlayOptions goo = new GroundOverlayOptions()
                    .positionFromBounds(bounds.getLatLngBounds())
                    .image(getImage());

        groundOverlay = mMap.addGroundOverlay(goo);
    }

    public void addItemOverlay(){
        if (itemOverlay != null){
            itemOverlay.remove();
            itemOverlay = null;
        }

        if (item != null){
            GroundOverlayOptions goo = new GroundOverlayOptions()
                    .positionFromBounds(bounds.multiplyBound(0.75d).getLatLngBounds())
                    .image(getItemImage());

            itemOverlay = mMap.addGroundOverlay(goo);
        }
    }

    public void setValues(String TEAM, int LEVEL, String ITEM){
        if (TEAM != team || LEVEL != level){
            team = TEAM;
            level = LEVEL;

            addSquareOverlay();
        }

        if (ITEM != item){
            item = ITEM;

            addItemOverlay();
        }
    }



    public LatLngBounds getBounds(){
        return bounds.getLatLngBounds();
    }

    public LatLng getPosition(){
        return bounds.getLatLngBounds().getCenter();
    }

    public String getTeam(){
        return team;
    }

    public String getItem() {
        return item;
    }

    public BitmapDescriptor getImage(){
        return getImage(team, level);
    }

    public static BitmapDescriptor getImage(String TEAM, int LEVEL){
        if (TEAM == null){
            return BitmapDescriptorFactory.fromResource(R.drawable.grey_square_low);
        }

        switch (TEAM){

            case "red":

                switch (LEVEL){

                    case 0:
                        return BitmapDescriptorFactory.fromResource(R.drawable.red_contested);
                    case 1:
                        return BitmapDescriptorFactory.fromResource(R.drawable.red_square_low);
                    case 2:
                        return BitmapDescriptorFactory.fromResource(R.drawable.red_square_high);
                    default:
                        return BitmapDescriptorFactory.fromResource(R.drawable.unknown_square);
                }

            case "green":

                switch (LEVEL){

                    case 0:
                        return BitmapDescriptorFactory.fromResource(R.drawable.green_contested);
                    case 1:
                        return BitmapDescriptorFactory.fromResource(R.drawable.green_square_low);
                    case 2:
                        return BitmapDescriptorFactory.fromResource(R.drawable.green_square_high);
                    default:
                        return BitmapDescriptorFactory.fromResource(R.drawable.unknown_square);
                }

            case "blue":

                switch (LEVEL){

                    case 0:
                        return BitmapDescriptorFactory.fromResource(R.drawable.blue_contested);
                    case 1:
                        return BitmapDescriptorFactory.fromResource(R.drawable.blue_square_low);
                    case 2:
                        return BitmapDescriptorFactory.fromResource(R.drawable.blue_square_high);
                    default:
                        return BitmapDescriptorFactory.fromResource(R.drawable.unknown_square);
                }

            case "null":

                switch (LEVEL){

                    case 0:
                        return BitmapDescriptorFactory.fromResource(R.drawable.grey_square_low);
                    default:
                        return BitmapDescriptorFactory.fromResource(R.drawable.unknown_square);
                }

            default:
                return BitmapDescriptorFactory.fromResource(R.drawable.unknown_square);
        }
    }

    public BitmapDescriptor getItemImage(){
        return BitmapDescriptorFactory.fromResource(Item.getItemImage(item));
    }

    public void setVisible(boolean visible){
        if (groundOverlay != null){
            groundOverlay.setVisible(visible);
        }
        if (itemOverlay != null){
            itemOverlay.setVisible(visible);
        }
    }

    public void removeFromMap(){
        if (groundOverlay != null){
            groundOverlay.remove();
        }
        if (itemOverlay != null){
            itemOverlay.remove();
        }
    }

}

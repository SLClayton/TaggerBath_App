package uk.co.claytapp.taggerbath.Game_Objects;

import android.content.Context;
import android.graphics.drawable.Drawable;

import uk.co.claytapp.taggerbath.R;

/**
 * Created by Sam on 08/03/2017.
 */

public class Item {

    Context context;

    String item;
    int amount;

    public Item(Context c, String ITEM, int AMOUNT){

        context = c;

        item = ITEM;
        amount = AMOUNT;
    }

    public String getItem(){
        return item;
    }

    public int getAmount(){
        return amount;
    }

    public void setAmount(int AMOUNT){
        amount = AMOUNT;
    }

    public Drawable getItemImage(){
        return  context.getResources().getDrawable(getItemImage(item));
    }

    public static int getItemImage(String TEAM){
        if (TEAM == null){
            return R.drawable.unknown_square;
        }
        switch (TEAM){
            case "duck":
                return R.drawable.duck;
            case "uni":
                return  R.drawable.uni;
            case "rugby":
                return  R.drawable.rugby;
            case "bath":
                return  R.drawable.bath;
            case "lollipop":
                return  R.drawable.lollipop;
            case "star":
                return R.drawable.star;
            case "trophy":
                return  R.drawable.trophy;
            case "beer":
                return  R.drawable.beer;
            default:
                return R.drawable.unknown_square;
        }
    }
}

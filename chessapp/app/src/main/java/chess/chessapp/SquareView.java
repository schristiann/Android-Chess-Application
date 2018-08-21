package chess.chessapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.ImageView;

@SuppressLint("AppCompatCustomView")
public class SquareView extends ImageView {

    public Square getSquare() {
        return square;
    }

    @Override
    public String toString() {
        return "SquareView{" +
                "square=" + square +
                '}';
    }

    private Square square;

    public SquareView(Context context) {
        super(context);
    }

    public SquareView(Context context, Square square) {
        super(context);
        this.square = square;

    }

}

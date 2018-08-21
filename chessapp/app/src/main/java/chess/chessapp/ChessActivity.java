package chess.chessapp;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;

import android.graphics.drawable.Drawable;

import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;

import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static chess.chessapp.ChessConstants.BLACK;
import static chess.chessapp.ChessConstants.WHITE;

public class ChessActivity extends AppCompatActivity {

    public static final String REPLAY_URI = "replayUri";
    protected static Map<Piece, Square> whitePieceLocation;

    protected static  Map<Piece, Square> blackPieceLocation;

    protected static Map<Piece, Drawable> drawableMap;
    protected static Square[][] board;

    protected static Map<Class, Integer> whitePieceCount = new HashMap<>();

    protected static Map<Class, Integer> blackPieceCount = new HashMap<>();

    protected static List<ChessMove> moveList;
    protected int moveCounter = 0;
    private static final String TAG = "ChessActivity";

    private boolean replayMode;
    private ChessGame chessGame;


    public static List<ChessMove> getMoveList() {
        return Collections.unmodifiableList(moveList);
    }

    public boolean isMatchOver() {
        return matchOver;
    }

    public void setMatchOver(boolean matchOver) {
        this.matchOver = matchOver;
    }

    protected boolean matchOver = false;
    private static Square lastStartSquare;
    private static Square lastEndSquare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chess_activity);
        board = new Square[8][8];
        chessGame = new ChessGame();
        whitePieceLocation = new HashMap<>();
        blackPieceLocation = new HashMap<>();
        drawableMap = new HashMap<>();
        moveList = new ArrayList<>();
        String replayUriString ;
        createBoard(this);

        Bundle bundle = getIntent().getExtras();

        replayMode = false;
        if (bundle != null) {   //
            replayUriString = bundle.getString(REPLAY_URI);
            Uri replayUri = Uri.parse(replayUriString);
            replayMode = true;
            loadGame(replayUri);
            Log.i(TAG, "Passed replayPath of: " + replayUriString);
        }

        final Button undoButton = findViewById(R.id.undo_button);
        undoButton.setEnabled(false);
        undoButton.setOnClickListener((view) -> {

            doUndo();

        });

        final Button aiButton = findViewById(R.id.ai_button);

        aiButton.setOnClickListener((view) -> {

            doAiMove();

        });

        final Button resignButton = findViewById(R.id.resign_button);

        resignButton.setOnClickListener((view) -> {

            doResign();

        });

        final Button drawButton = findViewById(R.id.draw_button);

        drawButton.setOnClickListener((view) -> {

            doDraw();

        });

        final Button nextButton = findViewById(R.id.next_button);

        nextButton.setOnClickListener((view) -> {

            doReplayMove();

        });


        final Button endPlaybackButton = findViewById(R.id.end_playback_button);

        endPlaybackButton.setOnClickListener((view) -> {

            doFinish();

        });

        TableRow playerButtonRow = findViewById(R.id.player_buttons);
        TableRow replayButtonRow = findViewById(R.id.replay_buttons);


        if (replayMode)
            playerButtonRow.setVisibility(View.INVISIBLE);
        else
            replayButtonRow.setVisibility(View.INVISIBLE);

        TextView textView = findViewById(R.id.playerInfo);
        textView.setText(this.getString(R.string.white_move));
        showPlayerMove(moveCounter);
    }

    private void createBoard(Context context) {

        TableLayout table = findViewById(R.id.chessboard);
        Resources resource = context.getResources();

        int instanceCount = 0;

        char  color = BLACK;
        for (int r = 0; r < 8; r++) {
            TableRow tr = new TableRow(this);
            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            for (int c = 0; c < 8; c++) {

                final int row = r;
                final int col = c;
                Square square = new Square(r,c);
                board[r][c] = square;
                SquareView squareView = new SquareView(this, square);

                squareView.setId( (r * 8) + c); // give the square an id between 0 and 63

                squareView.setOnDragListener(new myDragEventListener());

                squareView.setOnLongClickListener( (view) -> {
                    ClipData clipData = ClipData.newPlainText("pieceId", "r" + row + "c" + col);

                    Square clickSquare = square;
                    view.startDrag(clipData, new View.DragShadowBuilder(view), clickSquare, 0);
                    return true;
                });

                if (r == 0 || r == 1) {
                    color= BLACK;
                } else if (r == 6 || r == 7) {
                    color= WHITE;
                }
                if (r == 0 || r == 7) {
                    if (c == 0 || c == 7) {

                        instanceCount = getInstanceCount(color, Rook.class);

                        Rook rook = new Rook(color, instanceCount);
                        square.setPiece(rook);

                        if (color == BLACK) {
                            squareView.setImageResource(R.drawable.blackrook);

                        }
                        else {
                            squareView.setImageResource(R.drawable.whiterook);
                        }

                        drawableMap.put(rook, squareView.getDrawable());

                    }
                    if (c == 1 || c == 6) {

                        instanceCount = getInstanceCount(color,Knight.class);
                        Knight knight = new Knight(color, instanceCount);
                        square.setPiece(knight);

                        if (color == BLACK)
                            squareView.setImageResource(R.drawable.blackknight);
                        else
                            squareView.setImageResource(R.drawable.whiteknight);

                        drawableMap.put(knight, squareView.getDrawable());
                    }
                    if (c == 2 || c == 5) {
                        instanceCount = getInstanceCount(color,Bishop.class);
                        Bishop bishop = new Bishop(color, instanceCount);
                        square.setPiece(bishop);

                        if (color == BLACK)
                            squareView.setImageResource(R.drawable.blackbishop);
                        else
                            squareView.setImageResource(R.drawable.whitebishop);

                        drawableMap.put(bishop, squareView.getDrawable());
                    }
                    if (c == 3) {
                        instanceCount = getInstanceCount(color,Queen.class);
                        Queen queen = new Queen(color, instanceCount);
                        square.setPiece(queen);

                        if (color == BLACK)
                            squareView.setImageResource(R.drawable.blackqueen);
                        else
                            squareView.setImageResource(R.drawable.whitequeen);

                        drawableMap.put(queen, squareView.getDrawable());

                    }
                    if (c == 4) {

                        King king = new King(color);
                        square.setPiece(king);
                        if (color == BLACK)
                            squareView.setImageResource(R.drawable.blackking);
                        else
                            squareView.setImageResource(R.drawable.whiteking);

                        drawableMap.put(king, squareView.getDrawable());
                    }
                } else if (r == 1|| r == 6) {
                    instanceCount = getInstanceCount(color,Pawn.class);
                    Pawn pawn = new Pawn(color, instanceCount);
                    square.setPiece(pawn);
                    if (color == BLACK)
                        squareView.setImageResource(R.drawable.blackpawn);
                    else
                        squareView.setImageResource(R.drawable.whitepawn);
                    drawableMap.put(pawn, squareView.getDrawable());

                } else {
                    setEmptySquareColor(squareView);
                }

                tr.addView(squareView);
                setBackgroundColor(squareView);
                squareView.setClickable(true);

                if (!square.isEmpty()) {
                    if (square.getPiece().getColor() == BLACK) {
                        blackPieceLocation.put(square.getPiece(), square);
                    } else {
                        whitePieceLocation.put(square.getPiece(), square);
                    }
                }
            }
            table.addView(tr);
        }
    }



    protected class myDragEventListener implements View.OnDragListener {

        // This is the method that the system calls when it dispatches a drag event to the
        // listener.
        public boolean onDrag(View v, DragEvent event) {

            // Defines a variable to store the action type for the incoming event
            final int action = event.getAction();

            // Handles each of the expected events
            switch(action) {

                case DragEvent.ACTION_DRAG_STARTED:

                    // Determines if this View can accept the dragged data
                    if (event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {


                        // Invalidate the view to force a redraw in the new tint
                        v.invalidate();

                        // returns true to indicate that the View can accept the dragged data.
                        return true;

                    }

                    // Returns false. During the current drag and drop operation, this View will
                    // not receive events again until ACTION_DRAG_ENDED is sent.
                    return false;

                case DragEvent.ACTION_DRAG_ENTERED:


                    // Invalidate the view to force a redraw in the new tint
                    v.invalidate();

                    return true;

                case DragEvent.ACTION_DRAG_LOCATION:

                    // Ignore the event
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:


                    // Invalidate the view to force a redraw in the new tint
                    v.invalidate();

                    return true;

                case DragEvent.ACTION_DROP:

                    // Gets the item containing the dragged data
                    ClipData.Item item = event.getClipData().getItemAt(0);

                    // Gets the text data from the item.
                    CharSequence dragData = item.getText();

                    // Displays a message containing the dragged data.
                    System.out.println("Dragged data is " + dragData);

                    Square square = (Square) event.getLocalState();

                    System.out.println("Localstate is: " + square);
                    SquareView toSquareView = (SquareView) v;

                    SquareView fromSquareView = findViewById( (square.getRow() * 8) + square.getCol());

                    boolean result = true;


                    lastStartSquare = new Square(fromSquareView.getSquare());
                    lastEndSquare = new Square(toSquareView.getSquare());

                    result = executeMove(fromSquareView, toSquareView);

                    if (result) {
                        // Invalidates the view to force a redraw
                        v.invalidate();

                        Button undoButton = findViewById(R.id.undo_button);
                        undoButton.setEnabled(true);

                        showPlayerMove(moveCounter);
                    } else {
                        showMessage("Invalid Move", "Invalid Move");
                    }



                    return result;

                case DragEvent.ACTION_DRAG_ENDED:

                    // Invalidates the view to force a redraw
                    v.invalidate();

                    // returns true; the value is ignored.
                    return true;

                // An unknown action type was received.
                default:
                    Log.e("DragDrop Example","Unknown action type received by OnDragListener.");
                    break;
            }

            return false;
        }
    }

    protected void setEmptySquareColor( Square square) {
        setEmptySquareColor( findViewById( (square.getRow() * 8) + square.getCol()));
    }

    /**
     * Sets an empty square to the appropriate color
     * @param squareView SquareView to set
     */
    protected void setEmptySquareColor( SquareView squareView) {
        Square square = squareView.getSquare();

        int c = square.getCol();
        int r = square.getRow();
        if ((c % 2 == 1 && r% 2 == 0) || (c % 2 == 0 && r % 2 == 1))
            squareView.setImageResource(R.drawable.greysquare);
        else
            squareView.setImageResource(R.drawable.whitesquare);
    }

    /**
     * Sets the background color of an occupied square
     * @param squareView Sqaureview to set
     */
    protected void setBackgroundColor(SquareView squareView) {
        Square square = squareView.getSquare();

        int c = square.getCol();
        int r = square.getRow();
        if ((c % 2 == 1 && r% 2 == 0) || (c % 2 == 0 && r % 2 == 1))
            squareView.setBackgroundColor(Color.GRAY);
        else
            squareView.setBackgroundColor(Color.WHITE);
    }

    /**
     * Displays a message to the user
     * @param title Title of message
     * @param message Message to display
     */
    protected void showMessage( String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(ChessActivity.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    protected void showGameOver(String title, String message) {
        new AlertDialog.Builder(ChessActivity.this).
                setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, (dialog, whichbutton) -> {
                    doFinish();
                }).show();
    }

    protected void doDraw() {
        String message = ((moveCounter % 2 == 0) ? "White" : "Black") + " offers to Draw?";

        new AlertDialog.Builder(ChessActivity.this).
                setTitle("Draw?")
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, (dialog, whichbutton) -> {
                    showGameOver("Match Over", "Match ends in a draw");
                    setMatchOver(true);
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void doResign() {
        if (moveCounter %2 == 0) {
            showGameOver("Game Over", "White player resigns, black player wins");
        } else {
            showGameOver("Game Over", "Black player resigns, white player wins");
        }
        setMatchOver(true);
    }

    /**
     * Performs an undo of the previous action
     */
    public void doUndo() {

        System.out.println("doUndo: lastStart: "  + lastStartSquare
                + " lastEnd: " + lastEndSquare);
        if (lastStartSquare == null) {  // should never happen
            System.err.println("lastStartSquare is null, cannot perform undo");
            return;
        }

        int startRow = lastStartSquare.getRow();
        int startCol = lastStartSquare.getCol();

        int endRow = lastEndSquare.getRow();
        int endCol = lastEndSquare.getCol();

        SquareView startSquareView = findViewById( (startRow * 8) + startCol);
        SquareView endSquareView = findViewById( (endRow * 8) + endCol);

        board[endRow][endCol].setPiece(lastEndSquare.getPiece());
        board[startRow][startCol].setPiece(lastStartSquare.getPiece());


        // reset the images
        startSquareView.setImageDrawable(drawableMap.get(lastStartSquare.getPiece()));

        if (! lastEndSquare.isEmpty())
            endSquareView.setImageDrawable(drawableMap.get(lastEndSquare.getPiece()));
        else
            setEmptySquareColor(endSquareView);


        if(lastEndSquare.getColor()== BLACK) {
            blackPieceLocation.put(lastEndSquare.getPiece(), lastEndSquare);
        }
        else {
            whitePieceLocation.put(lastEndSquare.getPiece(), lastEndSquare);
        }

        if (lastStartSquare.getColor() == BLACK) {
            blackPieceLocation.put(lastStartSquare.getPiece(), lastStartSquare);
        } else {
            whitePieceLocation.put(lastStartSquare.getPiece(), lastStartSquare);
        }

        final Button undoButton = findViewById(R.id.undo_button);

        undoButton.setEnabled(false);
        moveCounter--;

        // remove last move from the Movelist if not empty

        if (! moveList.isEmpty()) {
            moveList.remove(moveList.size()-1);
        }
        showPlayerMove(moveCounter);
    }

    /**
     * Perfoms a basic AI piece move
     */
    private void doAiMove() {

        char color = BLACK;
        if (moveCounter % 2 == 0) {
            color = WHITE;
        }

        Map<Piece, Square> locationMap;

        if (color == BLACK) {
            locationMap = blackPieceLocation;
        } else {
            locationMap = whitePieceLocation;
        }

        // try pawns first


        boolean successfulMove = false;
        for (Piece piece : getPieces(locationMap, Pawn.class)) {

            successfulMove = aiMovePawn((Pawn)piece, locationMap.get(piece));

            if (successfulMove) {
                break;  // successful move
            }
        }

        // try knights next
        if (! successfulMove) {
            for (Piece piece : getPieces(locationMap, Knight.class)) {
                successfulMove = aiMoveKnight((Knight) piece , locationMap.get(piece));

                if (successfulMove) {
                    break;  // successful move
                }

            }

        }
    }

    private boolean aiMoveKnight(Knight knight, Square square) {

        // Advance knight forward 2 and across 1

        int row = square.getRow();
        int col = square.getCol();
        int nextRow = 0;
        boolean result = false;
        if (knight.getColor() == BLACK && row < board.length -2 ||
                knight.getColor() == WHITE && row > 2) {

            if (knight.getColor() == BLACK)
                nextRow = row + 2;
            else
                nextRow = row -2;

            if (col > 0)
                col--;
            else if (col < 7)
                col++;

            SquareView fromSquareView = findViewById((row * 8) + square.getCol());
            SquareView toSquareView = findViewById((nextRow * 8) + col);

            result = executeMove(fromSquareView, toSquareView);
        }

        return result;
    }


    /**
     * Gets a list of pieces from the location map for a given class type
     * @param locationMap Location map containing pieces
     * @param classType Class type of requested piece
     * @param <T> Type of class
     * @return A list of objects that are of type class&lt;T&gt;
     */
    private <T> List<Piece> getPieces(Map<Piece, Square> locationMap, Class<T> classType) {

        List<Piece> pieces = new ArrayList<>();
        for (Piece piece : locationMap.keySet()) {
            if (piece.getClass().equals(classType)) {
                pieces.add(piece);
            }
        }

        return pieces;
    }


    /**
     * Perfoms an automated move for a pawn piece
     * @param pawn Pawn to move
     * @param square Starting square
     * @return true (move is successful) or false
     */
    private boolean aiMovePawn(Pawn pawn, Square square) {

        int row = square.getRow();
        int nextRow = 0;

        // check if we can move forwards one square

        if (pawn.getColor() == BLACK && row == board.length -1 ||
                pawn.getColor() == WHITE && row == 0) {
            return false;
        }

        if (pawn.getColor() == BLACK ) {
            nextRow = row + 1;
        } else if (pawn.getColor() == WHITE) {
            nextRow = row - 1;
        }

        SquareView fromSquareView = findViewById((row * 8) + square.getCol());
        SquareView toSquareView = findViewById((nextRow * 8) + square.getCol());

        return executeMove(fromSquareView, toSquareView);

    }

    /**
     * Displays the next move message to the user
     * @param moveCounter
     */
    private void showPlayerMove(int moveCounter) {

        TextView textView = findViewById(R.id.playerInfo);

        if (moveCounter %2 == 0)
            textView.setText(this.getString(R.string.white_move));
        else
            textView.setText(this.getString(R.string.black_move));
    }

    /**
     * Gets a count of pieces of a given type
     * @param player Player type WHITE or BLACK
     * @param piece class of piece
     * @param <T> Type of class
     * @return Count of pieces for a given type
     */
    protected <T> int getInstanceCount(char player, Class<T>  piece) {

        int count = 0;
        if (player == WHITE) {
            if (whitePieceCount.containsKey(piece)) {
                count = whitePieceCount.get(piece);
            }
            count++;
            whitePieceCount.put(piece, count);
        } else {
            if (blackPieceCount.containsKey(piece)) {
                count = blackPieceCount.get(piece);
            }
            count++;
            blackPieceCount.put(piece, count);
        }

        return count;
    }

    /**
     * Executes a move from a user, AI or playback request
     * @param fromSquareView
     * @param toSquareView
     * @return
     */
    boolean executeMove(SquareView fromSquareView, SquareView toSquareView) {
        boolean result = true;
        try {

            // Save the beginning and ending squares for possible undo

            lastStartSquare = new Square(fromSquareView.getSquare());
            lastEndSquare = new Square(toSquareView.getSquare());

            // If not in replay mode, add current move to the list

            if (! replayMode)
                moveList.add(new ChessMove(lastStartSquare, lastEndSquare));

            MoveResults moveResults = chessGame.movePiece(moveCounter, fromSquareView, toSquareView);

            if (moveResults.gameOver ) {

                setMatchOver(true);
//                saveGame();
                showGameOver("Game Over", "Game over " +
                        ((moveCounter %2 == 0)  ? "white" : "black"  ) + " wins");


            } else {
                if (moveResults.checkMessage != null) {
                    showMessage("Check", moveResults.checkMessage);
                }

                if (moveResults.getNewRookSquare() != null) {
                    setPieceImage(moveResults.getNewRookSquare());
                    setEmptySquareColor(moveResults.getOldRookSquare());
                }
                else if (moveResults.getEnpassantSquare() != null) {
                    setEmptySquareColor(moveResults.getEnpassantSquare());
                }
            }

            // Invalidates the view to force a redraw
//            v.invalidate();

            Button undoButton = findViewById(R.id.undo_button);
            undoButton.setEnabled(true);
            moveCounter++;
            showPlayerMove(moveCounter);

        }
        catch (IllegalMoveException ime) {
            if (! moveList.isEmpty()) {
                moveList.remove(moveList.size()-1);
            }
            result = false;
        }


        return result;
    }

    private void loadGame(Uri uri) {

        try (ParcelFileDescriptor fileDescriptor = this.getContentResolver().openFileDescriptor(uri, "r");
             FileInputStream fileIn = new FileInputStream(fileDescriptor.getFileDescriptor());
             ObjectInputStream oin = new ObjectInputStream(fileIn)) {
            moveList = (List<ChessMove>) oin.readObject();
            Log.i(TAG, "Loaded movelist of size: " + moveList.size());

        } catch (FileNotFoundException e) {
            Log.e(TAG, "Chess game does not exist");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
    /**
     * Saves last game
     */
    private void saveGame() {
        try (
                FileOutputStream fileOut =
                        openFileOutput("lastGame.ser", Context.MODE_PRIVATE);
                ObjectOutputStream out = new ObjectOutputStream(fileOut)) {

            Log.i(TAG, "Saving moveList of size: " + moveList.size());
            out.writeObject(moveList);
            Log.i(TAG, "File saved");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doReplayMove() {

        ChessMove chessMove = moveList.get(moveCounter);

        Square startSquare = chessMove.getFromSquare();
        Square endSquare = chessMove.getToSquare();

        Log.i(TAG, "Executing move: " + moveCounter + chessMove);

        SquareView startSquareView = findViewById((startSquare.getRow() * 8) + startSquare.getCol());
        SquareView endSquareView = findViewById((endSquare.getRow() * 8) + endSquare.getCol());

        executeMove(startSquareView, endSquareView);

        if (moveCounter == moveList.size()) {
            final Button nextButton = findViewById(R.id.next_button);
            nextButton.setEnabled(false);
        }
    }

    private void doFinish() {
        Intent data = new Intent();

//---set the data to pass back---
        setResult(RESULT_OK, data);
//---close the activity---
        finish();
    }


    protected void setPieceImage(Square square) {

        Piece piece = square.getPiece();

        SquareView squareView = findViewById( (square.getRow() * 8) + square.getCol());

        Drawable drawable = drawableMap.get(piece);

        squareView.setImageDrawable(drawable);

    }

}
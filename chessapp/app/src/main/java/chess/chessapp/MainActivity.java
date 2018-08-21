package chess.chessapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int PLAY_GAME_CODE = 1;
    public static final int REPLAY_GAME_CODE = 2;
    public static final int READ_REQUEST_CODE = 3;
    public static final int WRITE_REQUEST_CODE = 4;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView welcome = findViewById(R.id.welcome);

        welcome.setText("Welcome to the chess app");

        final Button nextButton = findViewById(R.id.play_button);

        nextButton.setOnClickListener((view) -> {

            doPlayGame();

        });

        final Button replayButton = findViewById(R.id.replay_button);

        replayButton.setOnClickListener((view) -> {

            performFileSearch();

        });
    }

    private void doReplayGame(Uri uri) {

        Bundle bundle = new Bundle();
        bundle.putString(ChessActivity.REPLAY_URI, uri.toString());
        Intent intent = new Intent(this, ChessActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, REPLAY_GAME_CODE);
    }

    private void doPlayGame() {

        Intent intent = new Intent(this, ChessActivity.class);
//        intent.putExtras(bundle);
        startActivityForResult(intent, PLAY_GAME_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        //
        Log.i(TAG, "OnActivityResult request code: " + requestCode + " resultCode: " + resultCode);
        if (resultCode == RESULT_OK) {
            if (requestCode == PLAY_GAME_CODE) {
                // Make sure the request was successful
                if (resultCode == RESULT_OK) {

                    Log.i(TAG, "Return OK from chess game. move count: " + ChessGame.getMoveList().size());
                    promptForSave();
                }

            } else if (requestCode == READ_REQUEST_CODE) {
                // The document selected by the user won't be returned in the intent.
                // Instead, a URI to that document will be contained in the return intent
                // provided to this method as a parameter.
                // Pull that URI using resultData.getData().
                Uri uri = null;
                if (resultData != null) {
                    uri = resultData.getData();
                    Log.i(TAG, "Uri: " + uri.toString());
                    doReplayGame(uri);

                }
            } else if (requestCode == WRITE_REQUEST_CODE) {
                Uri uri = resultData.getData();
                Log.i(TAG, "Uri: " + uri.toString());
                storeGame(uri, ChessActivity.getMoveList());
            }
        } else {
            Log.e(TAG, "Received result code of: " + resultCode);
        }

    }

    private void storeGame(Uri uri, List<ChessMove> moveList) {
        try (
            ParcelFileDescriptor fileDescriptor = this.getContentResolver().openFileDescriptor(uri, "w");
            FileOutputStream fileOut = new FileOutputStream(fileDescriptor.getFileDescriptor());
            ObjectOutputStream out = new ObjectOutputStream(fileOut))
        {
            Log.i(TAG, "Storing moveList of size: " + moveList.size());
            out.writeObject(moveList);
            Log.i(TAG, "File " + uri.toString() + " saved");
        } catch ( IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves last game
     */
    private void saveGameOld(List<ChessMove> moveList) {
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

    private void saveGame() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/java-serialized-object");
        intent.putExtra(Intent.EXTRA_TITLE, "savedGame.ser");
        startActivityForResult(intent, WRITE_REQUEST_CODE);
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void performFileSearch() {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra("android.content.extra.SHOW_ADVANCED", true);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("application/java-serialized-object");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    protected void promptForSave() {
        String message = "Save last game?";

        new AlertDialog.Builder(MainActivity.this).
                setTitle("Save Game?")
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, (dialog, whichbutton) -> {
                    saveGame();
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

}

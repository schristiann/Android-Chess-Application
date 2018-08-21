package chess.chessapp;



import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static chess.chessapp.ChessConstants.BLACK;
import static chess.chessapp.ChessConstants.WHITE;

public class ChessGame extends ChessActivity {

    private static boolean blackKingCheck = false;

    private static boolean whiteKingCheck= false;
    private static Square enpassantSquare=new Square();

    private List<Square> checkList= new ArrayList<>();

    private static final String TAG = "ChessGme";


    public MoveResults movePiece(int moveCounter, SquareView startSquareView, SquareView endSquareView) throws IllegalMoveException{

        char currentPlayer = ( moveCounter %2 == 0 ) ? WHITE : BLACK;

        MoveResults moveResults = new MoveResults();

        moveResults.gameOver = false;
        moveResults.checkMessage = null;

        boolean gameOver = false;
        boolean result = basicValidations(currentPlayer, startSquareView.getSquare(), endSquareView.getSquare());

        if (! result) {
            throw new IllegalMoveException("Invalid Move");
        }

        Square startSquare = startSquareView.getSquare();
        Square endSquare = endSquareView.getSquare();


        int startCol = startSquare.getCol();
        int endCol = endSquare.getCol();

        int startRow = startSquare.getRow();
        int endRow = endSquare.getRow();

        //TODO Draw edit

        if (! isValidMove(startSquare, endSquare)) {
            throw new IllegalMoveException("Invalid Move");
        }

        Piece chessPiece = startSquare.getPiece();
        chessPiece.setPieceMoved(true);

        if(chessPiece instanceof King && (( (King) chessPiece).isKingSideCastle()|| ((King) chessPiece).isQueenSideCastle())) {
            doCastle(chessPiece, startRow, moveResults);
        }
        else {
            doPreCheck(chessPiece, currentPlayer, startRow, startCol, endRow, endCol);
        }

        if(!endSquare.isEmpty()) {
            Piece endPiece=endSquare.getPiece();

            if(endPiece.getColor()==BLACK) {
                blackPieceLocation.remove(endPiece);
            }
            else {
                whitePieceLocation.remove(endPiece);
            }

            if(endPiece instanceof King) {  //todo check if possible
                gameOver=true;
            }
        }

        if(chessPiece instanceof Pawn) {
            if ((currentPlayer == WHITE && endRow == 0) || (currentPlayer == BLACK && endRow == 7)) {
                chessPiece = promotePawn(chessPiece, endSquare);
                if (chessPiece.getColor() == WHITE)
                    startSquareView.setImageResource(R.drawable.whitequeen);
                else
                    startSquareView.setImageResource(R.drawable.blackqueen);
            }

            if(enpassantSquare!=null) {
                if(endSquare.equals(enpassantSquare)&&enpassantSquare.enpassantPlayer!=currentPlayer) {
                    if(currentPlayer==WHITE) {
                        Square square=board[enpassantSquare.getRow()+1][enpassantSquare.getCol()];
                        Piece piece=square.getPiece();
                        blackPieceLocation.remove(piece);
                        square.setPiece(null);
                        moveResults.setEnpassantSquare(square);
                    }
                    else {
                        Square square=board[enpassantSquare.getRow()-1][enpassantSquare.getCol()];
                        Piece piece=square.getPiece();
                        whitePieceLocation.remove(piece);
                        square.setPiece(null);
                        moveResults.setEnpassantSquare(square);
                    }
                    enpassantSquare=null;
                }
                else {
                    enpassantSquare=null;
                }
            }
            if(Math.abs(endRow-startRow)==2) {
                if(currentPlayer==WHITE) {
                    enpassantSquare=board[endSquare.getRow()+1][endSquare.getCol()];
                    enpassantSquare.enpassantPlayer='w';
                }
                else {
                    enpassantSquare=board[endRow-1][endCol];
                    enpassantSquare.enpassantPlayer='b';
                }
            }
        }
        else {
            enpassantSquare = null;
        }


        // Move the pieces and set the images

        board[endRow][endCol].setPiece(chessPiece);
        board[startRow][startCol].setPiece(null);
        endSquareView.setImageDrawable(startSquareView.getDrawable());
//        endSquare.setPiece(startSquare.getPiece());
//        startSquare.setPiece(null);

        setEmptySquareColor(startSquareView);

        setBackgroundColor(startSquareView);

        if(chessPiece.getColor()== BLACK) {
            blackPieceLocation.put(chessPiece, endSquare);
        }
        else {
            whitePieceLocation.put(chessPiece, endSquare);
        }


        Log.i(TAG, "Target square: " + endSquareView);
        Log.i(TAG, "Origin row: " + startSquareView);

//check for white king in check, isKingInCheck takes the opposing player color and checks pieces

        if(isKingInCheck('b')) {

            Log.i(TAG, "White king is in check");
            //check if a white piece can block check
            boolean isCheckMate=false;
            boolean canBlock=checkBlock('b');
            if(!canBlock) {
                System.out.println("White cannot block");
                if(isCheckMate=isCheckmate('w')) {
                    gameOver=true;
                    Log.i(TAG, "Game over black wins");
                }
            }

            whiteKingCheck=true;
            moveResults.checkMessage  = "White king is in check";
        }
        else {
            whiteKingCheck=false;
        }

        checkList.clear();

        if(isKingInCheck('w')){

            boolean canBlock=checkBlock('w');
            boolean isCheckMate=false;
            if(!canBlock) {
                if(isCheckMate=isCheckmate('b')) {
                    gameOver=true;
                }
            }

            blackKingCheck=true;
            moveResults.checkMessage  = "Black king is in check";
        }
        else {
            blackKingCheck=false;
        }

        moveResults.gameOver = gameOver;
        return moveResults;
    }

    /**
     * Sees if the checking move made by the current player can be blocked by the opposing player, taking the opposing player's king out of check
     * @param checkingPlayer  The current player attacking the opposing player's king
     * @return returns true if an opposing player's piece can block the attacking player's check, false if no pieces can
     */
    private boolean checkBlock(char checkingPlayer) {
        Map<Piece, Square> locationMap;

        Square kingSquare;

        //see which player is in check and get king square

        if(checkingPlayer=='b') {
            kingSquare= whitePieceLocation.get(new King('w'));
        }
        else {
            kingSquare=blackPieceLocation.get(new King('b'));
        }

        //go through list of checking pieces and see if path to king can be blocked

        for(Square blockingSquare: checkList) {
            Piece piece= blockingSquare.getPiece();

            int startRow = blockingSquare.getRow();
            int startCol=blockingSquare.getCol();

            int endRow= kingSquare.getRow();
            int endCol= kingSquare.getCol();

            if(piece instanceof Queen) {
                if(startRow==endRow) {
                    if(blockHorizontal(blockingSquare, kingSquare)) {
                        Log.i(TAG, "Horizontal block of: " + piece.toString() );
                        return true;
                    }
                }
                else if(startCol==endCol) {
                    if(blockVertical(blockingSquare, kingSquare)) {
                        Log.i(TAG, "Vertical block of: " + piece + " can block");
                        return true;
                    }
                }
                else {
                    if(blockDiagonal(blockingSquare, kingSquare)) {
                        Log.i(TAG, "Diagonal block of: " + piece + " can block");
                        return true;
                    }
                }
            }
            else if(piece instanceof Bishop || piece instanceof Pawn) {
                if(blockDiagonal(blockingSquare, kingSquare)) {
                    Log.i(TAG, "Piece: " + piece + " can block");
                    return true;
                }
            }
            else if (piece instanceof Rook) {
                if(startCol==endCol) {
                    if(blockVertical(blockingSquare, kingSquare)) {
                        Log.i(TAG, "Piece: " + piece + " can block");
                        return true;
                    }
                }
                else {
                    if(blockHorizontal(blockingSquare, kingSquare)) {
                        Log.i(TAG, "Piece: " + piece + " can block");
                        return true;
                    }
                }
            }

            else if(piece instanceof Knight) {
                if(isValidMove(blockingSquare, kingSquare)) {
                    Log.i(TAG, "Piece: " + piece + " can block");
                    return true;
                }
            }

        }

        return false;

    }

    /**
     * Assesses if the opposing player's king is in check
     * @param attackingPlayer the color of the current player
     * @return returns true if the opposing player's king is in check, false if not
     */

    private boolean isKingInCheck(char attackingPlayer) {
        //if white move, check position of black king and vice versa

        if(attackingPlayer=='w') {
            Square blackKingSquare=blackPieceLocation.get(new King('b'));


            if (checkScan(blackKingSquare, whitePieceLocation)) {
                return true;
            }
        }
        else {
            Square whiteKingSquare=whitePieceLocation.get(new King('w'));


            if (checkScan(whiteKingSquare, blackPieceLocation)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Promotes the pawn if it has reached the opposite side of the board
     * @param pawn the pawn Piece being tested
     * @param endSquare the square being moved to
     * @return returns the type of piece that has been designated to be promoted to.
     */
    private Piece promotePawn(Piece pawn,  Square endSquare) {

        Piece promotedPiece;

        char player= pawn.getColor();

        int instanceCount = getInstanceCount(player, Queen.class);
        promotedPiece = new Queen(player, instanceCount);

        endSquare.setPiece(promotedPiece);

        promotedPiece.setPieceMoved(true);

        if(player=='w') {
            whitePieceLocation.put(promotedPiece, endSquare);
        }
        else {
            blackPieceLocation.put(promotedPiece, endSquare);
        }
        return promotedPiece;
    }

    private void doCastle(Piece chessPiece, int startRow, MoveResults moveResults) {
        King king= (King) chessPiece;
        Square newRookSquare;
        Piece rookPiece;
        Square oldRookSquare;

        if(king.kingSideCastle) {
            rookPiece=board[startRow][7].getPiece();
            newRookSquare=board[startRow][5];

            newRookSquare.setPiece(rookPiece);

            oldRookSquare = board[startRow][7];
            oldRookSquare.setPiece(null);


        }
        else {
            rookPiece=board[startRow][0].getPiece();
            newRookSquare=board[startRow][3];
            newRookSquare.setPiece(rookPiece);

            oldRookSquare = board[startRow][0];
            oldRookSquare.setPiece(null);
        }

        if(rookPiece.getColor()==BLACK) {
            blackPieceLocation.put(rookPiece, newRookSquare);

        }
        else {
            whitePieceLocation.put(rookPiece, newRookSquare);

        }

        // Set rooks previous position to an empty square

        moveResults.setNewRookSquare(newRookSquare);
        moveResults.setOldRookSquare(oldRookSquare);

        king.setQueenSideCastle(false);
        king.setKingSideCastle(false);
    }

    private void doPreCheck(Piece chessPiece, char currentPlayer, int startRow, int startCol,
                            int endRow, int endCol) throws IllegalMoveException {
        boolean check;

        Square kingSquare;
        Map<Piece, Square> locationMap;

        if(currentPlayer=='b') {
            kingSquare=blackPieceLocation.get(new King('b'));
            locationMap=whitePieceLocation;
        }
        else {
            kingSquare=whitePieceLocation.get(new King('w'));
            locationMap=blackPieceLocation;
        }

        //store the current piece of the "end square"
        // and put the start piece in the "to square" for check testing

        Piece tempTo=board[endRow][endCol].getPiece();
        Piece tempStart=chessPiece;
        board[endRow][endCol].setPiece(board[startRow][startCol].getPiece());
        board[startRow][startCol].setPiece(null);
        if(chessPiece instanceof King) {        //todo check logic ?
            kingSquare= board[endRow][endCol];
        }
        check=checkScan(kingSquare, locationMap);
        board[endRow][endCol].setPiece(tempTo);
        board[startRow][startCol].setPiece(tempStart);
        if(check) {
            throw new IllegalMoveException("King in check");
        }

    }
    private boolean basicValidations(char currentPlayer, Square fromSquare, Square toSquare) {

        boolean result = true;
        if (fromSquare.isEmpty()) {
            result = false;
        } else {
            // check to make sure piece color matches current player color
            char color = fromSquare.getPiece().getColor();

            result = color == currentPlayer;
        }

        return result;
    }

    /**
     * Checks if the move entered by the player is valid.
     * @param startSquare The square of the piece to be moved.
     * @param endSquare The destination square of the piece being moved.
     * @return returns true if the move is valid, false if the move is not.
     */
    private boolean isValidMove(Square startSquare, Square endSquare) {
        boolean validMove=false;

        Piece startPiece=startSquare.getPiece();

        if(startPiece==null) {
            return false;
        }

        // can't move to the same spot.  Can happen if the
        // drag action does not leave the current square

        if (startSquare.getRow() == endSquare.getRow() &&
                startSquare.getCol() == endSquare.getCol()) {
            return false;
        }

        if(startPiece instanceof Pawn) {
            validMove=isValidPawnMove(startSquare, endSquare);
        }
        else if(startPiece instanceof Knight) {
            validMove=isValidKnightMove(startSquare, endSquare);
        }
        else if(startPiece instanceof King) {
            validMove=isValidKingMove(startSquare, endSquare);
        }
        else if(startPiece instanceof Queen) {
            validMove = isValidBishopMove(startSquare, endSquare) || isValidRookMove(startSquare, endSquare);
        }
        else if (startPiece instanceof Bishop) {
            validMove = isValidBishopMove(startSquare, endSquare);
        }
        else if(startPiece instanceof Rook) {
            validMove = isValidRookMove(startSquare, endSquare);
        }

        return validMove;
    }

    /**
     * If the piece in the square is of type rook, checks to see if the move is a valid rook move.
     * @param startSquare The square to be moved from.
     * @param endSquare the Square to be moved to.
     * @return returns true if the move is a valid rook move, false if it is not.
     */
    private boolean isValidRookMove(Square startSquare, Square endSquare) {
        int rowDiff=endSquare.getRow()-startSquare.getRow();
        int colDiff=endSquare.getCol()-startSquare.getCol();

        Piece startPiece=startSquare.getPiece();

        int src_xIndex= startSquare.getCol();
        int src_yIndex= startSquare.getRow();

        int dest_xIndex = endSquare.getCol();
        int dest_yIndex=endSquare.getRow();

        //if the rook is on a new row make sure it didnt move the column and vice versa
        if(!((Math.abs(rowDiff)==0 && Math.abs(colDiff)!=0) || (Math.abs(rowDiff)!= 0 && Math.abs(colDiff)==0))) {
            return false;
        }

        if(!endSquare.isEmpty() && endSquare.getPieceColor()==startSquare.getPieceColor()) {
            return false;
        }

        //Moving horizontally
        if(src_yIndex == dest_yIndex) {
            if(src_xIndex > dest_xIndex) {
                for(int i = dest_xIndex + 1; i < src_xIndex; i++) {
                    if(! board[dest_yIndex][i].isEmpty()) {
                        return false;
                    }
                }
            }
            else {
                for(int i = src_xIndex + 1; i < dest_xIndex; i++) {
                    if(! board[dest_yIndex][i].isEmpty()) {
                        return false;
                    }
                }
            }

        }
        // Moving vertically
        else {
            if(src_yIndex > dest_yIndex) {
                for(int i = dest_yIndex + 1; i < src_yIndex; i++) {
                    if(! board[i][dest_xIndex].isEmpty()) {
                        return false;
                    }
                }
            }
            else {
                for(int i = src_yIndex + 1; i < dest_yIndex; i++) {
                    if(! board[i][dest_xIndex].isEmpty()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * If the piece in the square is of type pawn, checks to see if the move is a valid pawn move.
     * @param startSquare The square to be moved from.
     * @param endSquare the Square to be moved to.
     * @return returns true if the move is a valid pawn move, false if it is not.
     */
    private boolean isValidPawnMove(Square startSquare, Square endSquare) {
        boolean invalidMove=false;

        int rowDiff=endSquare.getRow()-startSquare.getRow();
        int colDiff=endSquare.getCol()-startSquare.getCol();

        Piece startPiece=startSquare.getPiece();

        invalidMove |= startPiece.getColor()== WHITE && rowDiff!=-1 && startPiece.isPieceMoved();

        invalidMove |= startPiece.getColor()== BLACK && rowDiff!=1 && startPiece.isPieceMoved();

        invalidMove |= Math.abs(rowDiff)>2 ;

        //maybe have move count field for piece

        invalidMove |= Math.abs(rowDiff) ==1 && Math.abs(colDiff)==0 &&  !endSquare.isEmpty();

        invalidMove |= Math.abs(rowDiff) == 2 && Math.abs(colDiff)==0 &&  !endSquare.isEmpty() && ! startPiece.isPieceMoved();
        invalidMove |= Math.abs(colDiff) >1;

        invalidMove |= Math.abs(colDiff) == 1 && ((endSquare.getPiece()!=null && endSquare.getPieceColor()==startSquare.getPieceColor())||
                (endSquare.isEmpty()&&!(endSquare.equals(enpassantSquare))));

        return !invalidMove;
    }

    /**
     * Scans the board for any pieces putting a player's king in check.
     * @param kingSquare The square of the king being tested for a check.
     * @param locationMap The map of pieces of the king's opposing color.
     * @return returns true if the king in question can be put in check, false if not.
     */
    private boolean checkScan(Square kingSquare, Map<Piece, Square> locationMap) {

        checkList.clear();

        for(Piece piece : locationMap.keySet()) {
            if (canCheckKing(locationMap.get(piece), kingSquare)) {
                checkList.add(locationMap.get(piece));
            }
        }
        return ! checkList.isEmpty();
    }

    private boolean canCheckKing(Square oppSquare, Square kingSquare) {
        return isValidMove(oppSquare, kingSquare);
    }

    /**
     * If the piece in the square is of type knight, checks to see if the move is a valid pawn move.
     * @param startSquare The square to be moved from.
     * @param endSquare the Square to be moved to.
     * @return returns true if the move is a valid knight move, false if it is not.
     */
    private boolean isValidKnightMove(Square startSquare, Square endSquare) {

        int rowDiff=endSquare.getRow()-startSquare.getRow();
        int colDiff=endSquare.getCol()-startSquare.getCol();

        Piece startPiece=startSquare.getPiece();

        if(!((Math.abs(rowDiff)==1 && Math.abs(colDiff)==2)||(Math.abs(colDiff)==1 && Math.abs(rowDiff)==2))) {
            return false;
        }
        if(!endSquare.isEmpty() && endSquare.getPieceColor()==startSquare.getPieceColor()) {
            return false;
        }



        return true;
    }

    /**
     * Sees if a king is in check-mate.
     * @param playerInCheck contains the color of the player being checked for a check-mate
     * @return returns true if the player's king is in check-mate, false if it is not.
     */
    private boolean isCheckmate(char playerInCheck) {
        // check if all open spaces that the king can move to are in check
        Square kingSquare;

        Map<Piece, Square> locationMap;

        if(playerInCheck=='w') {
            locationMap=blackPieceLocation;
            kingSquare=whitePieceLocation.get(new King('w'));
        }
        else {
            locationMap=whitePieceLocation;
            kingSquare=blackPieceLocation.get(new King('b'));
        }
        int row=kingSquare.getRow();
        int col=kingSquare.getCol();


        boolean checked= checkScan(kingSquare, locationMap);

        if(!checked) {
            return false;
        }

        //horizontal checks

        if(col>0) {
            checked= checkSquareChecked(row, col-1, playerInCheck);
        }
        if(!checked) {
            return false;
        }
        if(col<7) {
            checked=checkSquareChecked(row, col+1, playerInCheck);
        }

        if(!checked) {
            return false;
        }

        if(row>0) {
            checked=checkSquareChecked(row-1, col, playerInCheck);
        }

        if(!checked) {
            return false;
        }

        if(row<7) {
            checked=checkSquareChecked(row+1, col, playerInCheck);
        }

        if(!checked) {
            return false;
        }

        // diagonal checks

        if(row>0) {
            if(col>0) {
                checked=checkSquareChecked(row-1, col-1, playerInCheck);
            }

            if(!checked) {
                return false;
            }

            if(col<7) {
                checked=checkSquareChecked(row-1, col+1, playerInCheck);
            }

            if(!checked) {
                return false;
            }
        }

        if(row<7) {
            if(col>0) {
                checked=checkSquareChecked(row+1, col-1, playerInCheck);
            }

            if(!checked) {
                return false;
            }

            if(col<7) {
                checked=checkSquareChecked(row+1, col+1, playerInCheck);
            }

            if(!checked) {
                return false;
            }
        }

        return checked;

    }
    /**
     * Sees if the move being made puts the player's king in check.
     * @param row row of the square.
     * @param col column of the square.
     * @param player The player who's king is in check.
     * @return returns true if the move still puts the king in check, false if it does not.
     */
    private boolean checkSquareChecked(int row, int col, char player) {
        Square square=board[row][col];

        Map<Piece, Square> locationMap;

        boolean checked;

        Square tempKingSquare;
        if(player=='b') {
            locationMap=whitePieceLocation;
            tempKingSquare=blackPieceLocation.get(new King('b'));
        }
        else {
            locationMap=blackPieceLocation;
            tempKingSquare=whitePieceLocation.get(new King('w'));
        }

        //see if square to move to is in check or not

        if(square.isEmpty()|| !(square.getPieceColor()==player)) {
            Piece temp= square.getPiece();
            board[row][col].setPiece(tempKingSquare.getPiece());
            checked=checkScan(square, locationMap);
            board[row][col].setPiece(temp);
        }
        else {
            checked=true;
        }
        return checked;

    }
    /**
     * If the piece in the square is of type king, checks to see if the move is a valid pawn move.
     * @param startSquare The square to be moved from.
     * @param endSquare the Square to be moved to.
     * @return returns true if the move is a valid king move, false if it is not.
     */

    private boolean isValidKingMove(Square startSquare, Square endSquare) {
        int rowDiff=endSquare.getRow()-startSquare.getRow();
        int colDiff=endSquare.getCol()-startSquare.getCol();

        boolean horizontalMove=false;
        boolean verticalMove=false;
        boolean diagonalMove=false;


		/*
        Castling rules:
         King has never moved  (move count 0)
         Rook has never moved.
         Rook is original
         Squares between pieces are unoccupied
         King is not in check
         King does not land in check
         */


        if(isHorizontalMove(rowDiff, colDiff)) {

            horizontalMove=true;

        }

        if(isVerticalMove(rowDiff, colDiff)){
            if(Math.abs(rowDiff)>1) {
                return false;
            }
            else {
                verticalMove=true;
            }
        }

        if(isDiagonalMove(rowDiff, colDiff)) {
            if(Math.abs(colDiff)>1) {
                return false;
            }
            else {
                diagonalMove=true;
            }
        }
        if(!(verticalMove||diagonalMove||horizontalMove)) {
            return false;
        }

        boolean castleMove= horizontalMove && isCastleMove(startSquare, endSquare);

        if(!castleMove) {
            if(horizontalMove) {
                if(Math.abs(colDiff)>1) {
                    return false;
                }
            }
            if(!endSquare.isEmpty() && endSquare.getPieceColor()==startSquare.getPieceColor()) {
                return false;
            }
        }


        return true;
    }
    /**
     * checks if the move is a valid horizontal move.
     * @param rowDiff number of rows the start and end square differ by.
     * @param colDiff number of columns the start and end square differ by.
     * @return returns false if the move is an invalid horizontal move.
     */
    private boolean isHorizontalMove(int rowDiff, int colDiff) {

        return(rowDiff==0 && colDiff!=0);
    }


    /**
     * checks if the move is a valid vertical move.
     * @param rowDiff number of rows the start and end square differ by.
     * @param colDiff number of columns the start and end square differ by.
     * @return returns false if the move is an invalid vertical move.
     */
    private boolean isVerticalMove(int rowDiff, int colDiff) {
        return(rowDiff!=0 && colDiff==0);
    }

    /**
     * checks if the move is a valid diagonal move.
     * @param rowDiff number of rows the start and end square differ by.
     * @param colDiff number of columns the start and end square differ by.
     * @return returns false if the move is an invalid diagonal move.
     */
    private boolean isDiagonalMove(int rowDiff, int colDiff) {
        return(Math.abs(rowDiff)==Math.abs(colDiff) && rowDiff!=0);
    }

    /**
     * Checks if the move being made s a valid castle move.
     * @param startSquare  The starting square
     * @param endSquare The end square
     * @return True if the move is a valid castle move, false if it is not.
     */

    private boolean isCastleMove(Square startSquare, Square endSquare) {
        boolean castleMove=false;

        King king=(King) startSquare.getPiece();

        char player=startSquare.getPieceColor();
        if(Math.abs(startSquare.getCol()-endSquare.getCol())>2){
            return false;
        }

        if(!(king.isCheck() && king.isPieceMoved())){
            Square rookSquare= getCastleSquare(startSquare, endSquare);
            if(rookSquare.isEmpty() || !(rookSquare.getPiece() instanceof Rook)) {
                return false;
            }

            Rook castleRook=(Rook) rookSquare.getPiece();
            if(castleRook.getColor()==king.getColor()) {
                if(!castleRook.isPieceMoved()) {
                    int startRow=startSquare.getRow();
                    int startCol=startSquare.getCol();
                    int endRow=endSquare.getRow();
                    int endCol=endSquare.getCol();

                    int targetKingCol;
                    int col;

                    boolean kingSideCastle=true;

                    if(endCol<startCol) {
                        kingSideCastle=false;
                        for(col=startCol-1;col>endCol;col--) {
                            if(!board[startRow][col].isEmpty()) {
                                break;
                            }
                        }
                    } else {
                        for(col=startCol+1;col<endCol;col++) {
                            if(!board[startRow][col].isEmpty()) {
                                break;
                            }
                        }

                    }

                    if(col==endCol) {
                        targetKingCol=col;
                        //ADD CHECK TO SEE IF MOVE PUTS KING IN CHECK
                        Square targetSquare = board[startRow][targetKingCol];
                        if (player=='b') {
                            castleMove = ! checkScan(targetSquare, whitePieceLocation);
                        } else {
                            castleMove = ! checkScan(targetSquare, blackPieceLocation);
                        }

                        if(castleMove) {
                            if(kingSideCastle) {
                                king.kingSideCastle=true;
                            }
                            else {
                                king.queenSideCastle=true;
                            }
                        }
                    }

                }
            }

        }
        return castleMove;

    }
    /**
     * Determines if the move is a king side or queen side castle
     * @param startSquare The start square
     * @param endSquare The end square
     * @return The Square containing the rook involved in the castling move.
     */
    private Square getCastleSquare(Square startSquare, Square endSquare) {
        int row= startSquare.getRow();
        int startCol= startSquare.getCol();
        int endCol= endSquare.getCol();

        if(startCol>endCol) {
            return board[row][0];
        }
        else {
            return board[row][7];
        }
    }


    /**
     * Validates the movement of a Bishop between squares
     * @param fromSquare The starting square containing the Bishop
     * @param toSquare Destination square for the Bishop to land
     * @return true (Bishop can move between squares) or false
     */
    private boolean isValidBishopMove(Square fromSquare, Square toSquare) {


        boolean invalidMove = false;
        int rowDiff = toSquare.getRow() - fromSquare.getRow();
        int colDiff = toSquare.getCol() - fromSquare.getCol();

        if ( ! isDiagonalMove(rowDiff, colDiff))
            return false;

        int r = 0;
        int c = 0;
        if (rowDiff > 0) {  // moving up in rows
            c = fromSquare.getCol();
            rowLoop:
            if (colDiff > 0) {  // moving right
                for (r = fromSquare.getRow() + 1, c = fromSquare.getCol() + 1; r < toSquare.getRow(); r++, c++) {
                    Square square = board[r][c];

                    if (! square.isEmpty()) {
                        invalidMove = true;
                        break rowLoop;
                    }
                }

            } else {   // moving left
                for (r = fromSquare.getRow() + 1, c = fromSquare.getCol() - 1; r < toSquare.getRow(); r++, c--) {
                    Square square = board[r][c];

                    if (square.getPiece() != null) {
                        invalidMove = true;
                        break rowLoop;
                    }

                }
            }
        } else {    // moving down in rows

            rowLoop:

            if (colDiff > 0) {  // moving right
                for (r = fromSquare.getRow() -1, c = fromSquare.getCol() +1; r > toSquare.getRow(); r--, c++) {

                    Square square = board[r][c];

                    if (!square.isEmpty()) {
                        invalidMove = true;
                        break rowLoop;
                    }
                }

            } else {  // moving left
                for (r = fromSquare.getRow() -1, c = fromSquare.getCol() -1; r > toSquare.getRow(); r--, c--) {
                    Square square = board[r][c];

                    if (! square.isEmpty()) {
                        invalidMove = true;
                        break rowLoop;
                    }
                }
            }
        }

        Square square = board[r][c];  // last piece must be empty or an opponent piece

        if ( ! square.isEmpty() && square.getPieceColor()==fromSquare.getPieceColor()) {
            invalidMove = true;
        }
        return !invalidMove;
    }

    /**
     * Sees if the player being attacked can block a diagonal check made by the current player.
     * @param startSquare The piece of the current player
     * @param endSquare    The square containing the opposing player's king being attacked.
     * @return returns true if a piece of the opposing player can block a diagonal check made by the current player
     */
    private boolean blockDiagonal(Square startSquare, Square endSquare) {
        int startCol=startSquare.getCol();
        int startRow=startSquare.getRow();

        int endCol=endSquare.getCol();
        int endRow=endSquare.getRow();

        char checkedPlayer= endSquare.getPieceColor();

        Map<Piece, Square> locationMap;

        if(checkedPlayer=='b') {
            locationMap=blackPieceLocation;
        }
        else {
            whitePieceLocation.get(new King('w'));
            locationMap=whitePieceLocation;
        }

        int rowDiff = endRow-startRow;

        int colDiff= endCol- startCol;

        int r,c=0;

        if(rowDiff> 0) {
            c=startSquare.getCol();
            rowLoop:
            if(colDiff> 0) {
                for(r= startSquare.getRow(), c=startSquare.getCol(); r< endSquare.getRow();r++,c++) {
                    if(checkPath(r, c, locationMap)) {
                        return true;
                    }
                }
            }
            else {
                for(r=startSquare.getRow(), c =startSquare.getCol(); r< endSquare.getRow(); r++,c--) {
                    if(checkPath(r, c, locationMap)) {
                        return true;
                    }
                }
            }
        }
        else {
            rowLoop:

            if(colDiff>0) {
                for(r=startSquare.getRow(), c =startSquare.getCol(); r> endSquare.getRow(); r--,c++) {
                    if(checkPath(r, c, locationMap)) {
                        return true;
                    }
                }
            }
            else {
                for(r= startSquare.getRow(), c= startSquare.getCol(); r> endSquare.getRow(); r--, c--) {
                    if(checkPath(r, c, locationMap)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    /**
     Sees if the player being attacked can block a horizontal check made by the current player.
     * @param startSquare The piece of the current player
     * @param endSquare    The square containing the opposing player's king being attacked.
     * @return returns true if a piece of the opposing player can block a horizontal check made by the current player
     */
    private boolean blockHorizontal(Square startSquare, Square endSquare) {
        int startCol= startSquare.getCol();
        int startRow= startSquare.getRow();

        int endCol=endSquare.getCol();

        char checkedPlayer= endSquare.getPieceColor();

        Map<Piece,Square> locationMap;

        if(checkedPlayer=='b') {
            locationMap=blackPieceLocation;
        }
        else {
            whitePieceLocation.get(new King('w'));
            locationMap=whitePieceLocation;
        }

        int r,c;

        if (startCol>endCol) {
            for(c=startCol;c>endCol+1;c--) {
                if(checkPath(startRow, c, locationMap)) {
                    return true;
                }
            }
        }
        else {
            for(c=startCol;c<endCol+1;c++) {
                if(checkPath(startRow,c, locationMap)) {
                    return true;
                }
            }
        }

        return false;

    }
    /**
     Sees if the player being attacked can block a vertical check made by the current player.
     * @param startSquare The piece of the current player
     * @param endSquare    The square containing the opposing player's king being attacked.
     * @return returns true if a piece of the opposing player can block a vertical check made by the current player
     */
    private boolean blockVertical(Square startSquare, Square endSquare) {
        int startCol=startSquare.getCol();
        int startRow=startSquare.getRow();

        int endRow=endSquare.getRow();

        char checkedPlayer= endSquare.getPieceColor();

        Map<Piece, Square> locationMap;

        if(checkedPlayer=='b') {
            locationMap=blackPieceLocation;
        }
        else {
            whitePieceLocation.get(new King('w'));
            locationMap=whitePieceLocation;
        }

        int r,c;

        if(startRow>endRow) {
            for(r=startRow;r>endRow+1;r--) {
                if(checkPath(r,startCol, locationMap)) {
                    return true;
                }
            }
        }
        if(startRow<endRow) {
            for(r=startRow;r<endRow+1;r++) {
                if(checkPath(r, startCol, locationMap)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks the path between a row/col square on the board and all pieces of players side
     * @param row The rows of the target square
     * @param col The column of the target square
     * @param locationMap The location map containing all the pieces to test the path on
     * @return returns true if a valid path exists between the target square and a piece in the location map, false if not
     */
    private boolean checkPath(int row, int col, Map<Piece, Square> locationMap) {
        Square thisSquare=board[row][col];

        for(Piece piece:locationMap.keySet()) {
            if(piece instanceof King) {
                continue;
            }
            if(isValidMove(locationMap.get(piece), thisSquare)) {
                return true;
            }
        }
        return false;
    }

}
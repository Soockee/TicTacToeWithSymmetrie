package V7;

import java.util.*;
import java.util.stream.IntStream;


/**
 * Created by Simon on 19.04.2017.
 */
public class TicTacToe {
    //Board:
    //012
    //345
    //678

    //246_048_258_147_036_876_543_321; boards[0] = 'x' boards[1] = 'o'
    int[] boards;
    int bitfilter = 0b001_001_001_001_001_001_001_001;
    int bitfilterForBoard = 0b111_111_111;
    int moveCounter = 0;
    int node = 0;
    int leaf = 0;
    Stack<Integer> history = new Stack<>();
    Collection<Integer> set = new HashSet<>();

    int[] bitpattern = {
            0b000_001_000_000_001_000_000_001,
            0b000_000_000_001_000_000_000_010,
            0b001_000_001_000_000_000_000_100,
            0b000_000_000_000_010_000_001_000,
            0b010_010_000_010_000_000_010_000,
            0b000_000_010_000_000_000_100_000,
            0b100_000_000_000_100_001_000_000,
            0b000_000_000_100_000_010_000_000,
            0b000_100_100_000_000_100_000_000
    };

    public TicTacToe() {
        boards = new int[2];
        boards[0] = 0b000_000_000_000_000_000_000_000;
        boards[1] = 0b000_000_000_000_000_000_000_000;
    }


    public boolean isWin() {
        if (moveCounter < 5) return false;
        int currentBoard = boards[moveCounter - 1 & 1];
        return ((currentBoard & (currentBoard >> 1) & (currentBoard >> 2)) & bitfilter) > 0;
    }

    public void makeMove(int move) {
        int turn = moveCounter & 1;
        history.push(boards[turn]);
        boards[turn] |= bitpattern[move];
        moveCounter++;
    }

    public void makeMove(int... move) {
        for (int i : move) {
            makeMove(i);
        }
    }

    public ArrayList<Integer> moves() {
        ArrayList<Integer> lst = new ArrayList<>();
        int res = ~((boards[0] & bitfilterForBoard) | (boards[1] & bitfilterForBoard));
        IntStream.range(0,9).forEach(i->{
            if (((res>>i) & 1) == 1){
                lst.add(i);
            }
        });
        return lst;
    }

    public void undoMove() {
        moveCounter--;
        boards[moveCounter & 1] = history.pop();
    }

   /* public int boardToHash() {
        //bitfilterForBoard: Damit sind die ersten 8 Bit der Boardrepresäntation gemeint, also von 0 (oben links) bis 8 (unten rechts)
        int board0ToHash = (boards[0] & bitfilterForBoard) << 9;
        int board1ToHash = (boards[1] & bitfilterForBoard);
        return (board0ToHash | board1ToHash);
    }*/

    public int boardToHash(int b1, int b2) {
        int board0ToHash = (b1 & bitfilterForBoard) << 9;
        int board1ToHash = (b2 & bitfilterForBoard);
        return (board0ToHash | board1ToHash);

    }

    public void generateMoves() {
        for (int i : moves()) {
            makeMove(i);
            if (!(set.add(boardToHash(boards[1], boards[0])))) {
                excludeRedundancy();
                undoMove();
                continue;
            }
            if (isWin() || moveCounter == 9) {
                excludeRedundancy();
                leaf++;
                undoMove();
                continue;
            }
            excludeRedundancy();
            node++;
            generateMoves();
            undoMove();
        }
    }

    public int flipDiagonal(int board) {
        int res = board & bitfilterForBoard;
        res = (res << 8) & 100_000_000
                | (res >> 8) & 0b1
                | (res << 4) & 0b100_000
                | (res >> 4) & 0b10
                | (res << 4) & 0b10_000_000
                | (res >> 4) & 0b1_000;
        res = res | (board & 0b001_010_100);
        return res;
    }

    public int flipAntiDiagonal(int board) {
        int res = board & bitfilterForBoard;
        res = (res << 4) & 0b1_000_000
                | (res >> 4) & 0b100
                | (res << 2) & 0b10_000_000
                | (res >> 2) & 0b100_000
                | (res << 2) & 0b1_000
                | (res >> 2) & 0b10;
        res = res | (board & 0b100_010_001);
        return res;
    }

    public int flipHorizontal(int board) {
        int res = board & bitfilterForBoard;
        res = ((res << 6) & 0b111_000_000) | ((res >> 6) & 0b000_000_111);
        res = res | (board & 0b000_111_000);
        return res;
    }

    public int flipVertical(int board) {
        int res = board & bitfilterForBoard;
        res = ((res << 2) & 0b100_100_100) | ((res >> 2) & 0b001_001_001);
        res = res | (board & 0b010_010_010);
        return res;
    }

    public int rotate90DegreesClockwise(int board) {
        return flipHorizontal(flipDiagonal(board));
    }

    public int rotate90DegreesAntiClockwise(int board) {
        return flipHorizontal(flipAntiDiagonal(board));
    }

    public int rotate180Degrees(int board) {
        return flipHorizontal(flipVertical(board));
    }

    public int rotate180DegreesMirror(int board) {
        return rotate180Degrees(flipVertical(board));
    }

    public int rotate90DegreesClockWiseMirror(int board) {
        return rotate90DegreesClockwise(flipVertical(board));
    }

    public int rotate90DegreesAntiClockWiseMirror(int board) {
        return rotate90DegreesAntiClockwise(flipVertical(board));
    }

    public void excludeRedundancy() {
        int b1 = boards[1];
        int b2 = boards[0];

        set.add(boardToHash(rotate90DegreesAntiClockwise(b1), rotate90DegreesAntiClockwise(b2)));
        set.add(boardToHash(rotate90DegreesClockwise(b1), rotate90DegreesClockwise(b2)));
        set.add(boardToHash(rotate180Degrees(b1), rotate180Degrees(b2)));

        set.add(boardToHash(flipVertical(b1), flipVertical(b2)));
        set.add(boardToHash(rotate90DegreesAntiClockWiseMirror(b1), rotate90DegreesAntiClockWiseMirror(b2))); // rotate90DegreesAntiClockWiseMirror(b2) War vorher rotate90DegreesClockWiseMirror(b2)
        set.add(boardToHash(rotate90DegreesClockWiseMirror(b1), rotate90DegreesClockWiseMirror(b2)));
        set.add(boardToHash(rotate180DegreesMirror(b1), rotate180DegreesMirror(b2)));
}

    //not finished yet
    public int getCurrentPlayer() {
        // x starts
        //-1 = 'x' ; 1 = 'o';
        int res = moveCounter & 1;
        if (res == 0) res = -1;
        return res;
    }

    public int minimax(int node) {
        int bestValue;
        makeMove(node);
        if (isWin()) {
            undoMove();
            return 1 * getCurrentPlayer();
        }
        if (getCurrentPlayer() == 1) {
            bestValue = Integer.MIN_VALUE;
            for (int i : moves()) {
                int v = minimax(i);
                bestValue = Math.max(bestValue, v);
            }
        } else { /*minimizing player*/
            bestValue = Integer.MIN_VALUE;
            for (int i : moves()) {
                int v = minimax(i);
                bestValue = Math.min(bestValue, v);
            }
        }
        undoMove();
        return bestValue;
    }

    @Override
    public String toString() {
        char[] board = new char[9];
        int boardX = boards[0] & bitfilterForBoard;
        int boardO = boards[1] & bitfilterForBoard;
        for (int i = 0; i < 9; i++) {
            if (((boards[0] >> i) & 1) > 0) board[i] = 'x';
            else if (((boards[1] >> i) & 1) > 0) board[i] = 'o';
            else {
                board[i] = '=';
            }
        }
        StringBuilder sb = new StringBuilder();
        char[] field = board;
        for (int j = 0; j < field.length; j++) {
            sb.append(field[j] + " ");
            if (j == 2 || j == 5) sb.append("\n");
        }
        sb.append("\n");
        return "\n" + sb.toString();
    }
}

/*
TicTacToe t = new TicTacToe();
t.rotate180Degrees(1);
t.rotate90DegreesClockwise(1);
t.rotate90DegreesAntiClockwise(1);
t.flipVertical(1);
t.rotate180DegreesMirror(1);
t.rotate90DegreesClockWiseMirror(1);
t.rotate90DegreesAntiClockWiseMirror(1);*/

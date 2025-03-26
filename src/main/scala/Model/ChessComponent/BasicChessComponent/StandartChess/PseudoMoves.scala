package Model.ChessComponent.BasicChessComponent.StandartChess

import Model.*
import Model.ChessComponent.BasicChessComponent.StandartChess.Color.WHITE
import Model.ChessComponent.BasicChessComponent.StandartChess.PieceType.{BISHOP, EMPTY, KING, KNIGHT, PAWN, QUEEN, ROOK}
import Model.ChessComponent.BasicChessComponent.StandartChess.{EmptySquareHandler, EnemySquareHandler, OnBoardHandler}
import Model.ChessComponent.RealChess.*

import scala.annotation.tailrec

object PseudoMoves {
    
    def pseudoLegalPawnAttack(board: Vector[Piece], piecePosition: Int, rowDirection: Int, columDirection: Int, attackColor: Color): Boolean = {
        onBoard(piecePosition, rowDirection, columDirection) && board(piecePosition + rowDirection * 8 + columDirection).color == attackColor
    }
    
    def pseudoLegalPawnMove(board: Vector[Piece], piecePosition: Int, rowDirection: Int, columDirection: Int): Boolean = {
        if (Math.abs(rowDirection) == 2) {
            onBoard(piecePosition, rowDirection, columDirection) && board(piecePosition + rowDirection * 8 + columDirection).color == Color.EMPTY && board(piecePosition + (rowDirection/2) * 8 + columDirection).color == Color.EMPTY
        } else {
            onBoard(piecePosition, rowDirection, columDirection) && board(piecePosition + rowDirection * 8 + columDirection).color == Color.EMPTY
        }
    }
    
    def pseudoLegalMove(board: Vector[Piece], piecePosition: Int, rowDirection: Int, columdirection: Int, attackColor: Color): Boolean = {
        val handler = new OnBoardHandler(Some(new EmptySquareHandler(Some(new EnemySquareHandler(None)))))
        handler.handle(piecePosition, rowDirection, columdirection, board, attackColor)
    }
    
    def extractColor(color: String): (Int, Color, Color) = {
        color match {
            case "w" => (-1, Color.WHITE, Color.BLACK);
            case "b" => (1, Color.BLACK, Color.WHITE);
        }
    }
    
    def onBoard(beginningPosition: Int, rowDirection: Int, columDirection: Int): Boolean = {
        val newRow = beginningPosition + rowDirection * 8
        if (newRow < 0 || newRow > 63) {
            return false
        }
        val newColum = beginningPosition + columDirection
        val oldColum = beginningPosition % 8
        if (oldColum + columDirection < 0 || oldColum + columDirection > 7) {
            return false
        }
        true
    }
        
    def piecesPositions(board: Vector[Piece], pieces: List[Piece]): List[Int] = {

        @tailrec def pieceChecker(board: List[Piece], accumulator: List[Int], index: Int): List[Int] = {
            board match {
                case Nil => accumulator
                case h :: t => {
                    if (pieces.contains(h)) {
                        pieceChecker(t, index :: accumulator, index + 1);
                    } else {
                        pieceChecker(t, accumulator, index + 1);
                    }
                }
            }
        }

        pieceChecker(board.toList, List(), 0);
    }

    def piecePositions(board: Vector[Piece], piece: Piece): List[Int] = {
        val boardMonad = BoardMonad(board)
        var index = -1
        val index_boardMonad = boardMonad.map(e =>
            index += 1
            (e, index)
        )
        val correct_pieces_with_index = index_boardMonad.filter(
            (currentPiece, i) => currentPiece match {
                case None => false
                case Some(a) if a.pieceType == piece.pieceType && a.color == piece.color => true
                case _ => false
            }
        )
        correct_pieces_with_index.map((piece, i) => i).toList
    }
    
    @tailrec def checkPawnMoves(board: Vector[Piece], pawnPostion: Int, moves: List[(Int, Int)], attacks: List[(Int, Int)], accumulator: List[(Int, Int)], attackColor: Color): List[(Int, Int)] = {
        attacks match {
            case Nil => {
                moves match {
                    case Nil => accumulator
                    case (rowDirection, columDirection) :: t => {
                        if (pseudoLegalPawnMove(board, pawnPostion, rowDirection, columDirection)) {
                            checkPawnMoves(board, pawnPostion, t, attacks, (pawnPostion, pawnPostion + rowDirection * 8 + columDirection) :: accumulator, attackColor)
                        } else {
                            checkPawnMoves(board, pawnPostion, t, attacks, accumulator, attackColor)
                        }
                    }
                }
            }
            case (rowDirection, columDirection) :: t => {
                if (pseudoLegalPawnAttack(board, pawnPostion, rowDirection, columDirection, attackColor)) {
                    checkPawnMoves(board, pawnPostion, moves, t, (pawnPostion, pawnPostion + rowDirection * 8 + columDirection) :: accumulator, attackColor)
                } else {
                    checkPawnMoves(board, pawnPostion, moves, t, accumulator, attackColor)
                }
            }
        }

    }
    
    def checkEnPassant(board: Vector[Piece], enPassantPosition: Int, attackColorNumber: Int, moveColor: Color): List[(Int, Int)] = {
        val toCheck = List((attackColorNumber * -1, attackColorNumber * -1), (attackColorNumber * -1, attackColorNumber))

        @tailrec def checkTakingPossibility(movelist: List[(Int, Int)], accumulator: List[(Int, Int)]): List[(Int, Int)] = {
            movelist match {
                case Nil => accumulator
                case (rowdirection, columdirection) :: t => {
                    if (onBoard(enPassantPosition, rowdirection, columdirection) && board(enPassantPosition + 8 * rowdirection + columdirection) == Piece(PieceType.PAWN, moveColor)) {
                        checkTakingPossibility(t, (enPassantPosition + 8 * rowdirection + columdirection, enPassantPosition) :: accumulator)
                    } else {
                        checkTakingPossibility(t, accumulator)
                    }
                }
            }
        }
        checkTakingPossibility(toCheck, List());
    }
    
    def readyingPseudoMoveData(fen: String, pieceTypes: List[PieceType]) : (Vector[Piece], List[String],
        Int, Color, Color, List[Int]) = {
        val board: Vector[Piece] = ChessBoard.fenToBoard(fen)
        val fenSplit: List[String] = fen.split(" ").toList

        val (attackColorNum, moveColor, attackColor): (Int, Color, Color) = extractColor(fenSplit(1))
        @tailrec
        def createColoredList(pieceTypes: List[PieceType], color: Color, accumulator: List[Piece]) : List[Piece] = {
            pieceTypes match {
                case Nil => accumulator
                case h :: t => {
                    createColoredList(t, color, Piece(h, color) :: accumulator)
                }
            }
        }
        val pieces : List[Piece] = createColoredList(pieceTypes, moveColor, List())
        val piecePos = piecesPositions(board, pieces)

        (board, fenSplit, attackColorNum, moveColor, attackColor, piecePos)
    }

    def pseudoPawnMoves(fen: String): List[(Int, Int)] = {
        val (board, fenSplit, attackColorNum, moveColor, attackColor, piecePos) = readyingPseudoMoveData(fen, List(PAWN))

        val attackMoves = List((attackColorNum, attackColorNum), (attackColorNum, attackColorNum * -1));
        val straightMovesBase = List((attackColorNum, 0), (attackColorNum * 2, 0));
        val straightMoves = List((attackColorNum, 0));

        @tailrec def checkPawnMove(acc: List[(Int, Int)], piecePos: List[Int]): List[(Int, Int)] = {
            piecePos match {
                case Nil => acc
                case h :: t => {
                    if ((h > 7 && h < 16 && attackColorNum == 1) || (h > 47 && h < 56 && attackColorNum == -1)) {
                        checkPawnMove(checkPawnMoves(board, h, straightMovesBase, attackMoves, acc, attackColor), t)
                    } else {
                        checkPawnMove(checkPawnMoves(board, h, straightMoves, attackMoves, acc, attackColor), t)
                    }
                }
            }
        }

        if (fenSplit(3) != "-") {
            checkPawnMove(checkEnPassant(board, ChessBoard.coordinatesToIndex(fenSplit(3)), attackColorNum, moveColor), piecePos);
        } else {
            checkPawnMove(List(), piecePos);
        }

    }
    
    @tailrec def checkMoves(board: Vector[Piece], piecePosition: Int, moves: List[(Int, Int)], accumulator: List[(Int, Int)], attackColor: Color): List[(Int, Int)] = {
        moves match {
            case Nil => accumulator;
            case (rd, cd) :: t => {
                if (pseudoLegalMove(board, piecePosition, rd, cd, attackColor)) {
                    checkMoves(board, piecePosition, t, (piecePosition, piecePosition + rd * 8 + cd) :: accumulator, attackColor)
                } else {
                    checkMoves(board, piecePosition, t, accumulator, attackColor)
                }
            }
        }

    }

    def pieceMoves(pieceTypes: List[PieceType]): List[(Int, Int)] = {
        pieceTypes match {
            case x: List[PieceType] if x.contains(PieceType.KNIGHT) => List((-2, 1), (-2, -1), (-1, 2), (1, 2), (2, 1), (2, -1), (1, -2), (-1, -2))
            case x: List[PieceType] if x.contains(PieceType.KING) => List((1, 1), (-1, 1), (-1, -1), (1, -1), (-1, 0), (1, 0), (0, 1), (0, -1))
            case x: List[PieceType] if x.contains(List(PieceType.ROOK, PieceType.QUEEN)) => List((-1, 0), (1, 0), (0, 1), (0, -1))
            case x: List[PieceType] if x.contains(List(PieceType.BISHOP, PieceType.QUEEN)) => List((-1, 1), (1, 1), (1, -1), (-1, -1))
            case _ => List()
        }
    }
    
    def pseudoKnightMoves(result: List[(Int, Int)], fen: String): List[(Int, Int)] = {
        val (board, fenSplit, attackColorNum, moveColor, attackColor, piecePos) = readyingPseudoMoveData(fen, List(KNIGHT))
        val moves = pieceMoves(List(KNIGHT))
        
        @tailrec def checkKnighMove(accumulator: List[(Int, Int)], piecePosition: List[Int]): List[(Int, Int)] = {
            piecePosition match {
                case Nil => accumulator
                case h :: t => {
                    checkKnighMove(checkMoves(board, h, moves, accumulator, attackColor), t)
                }
            }
        }
        checkKnighMove(result, piecePos);
    }
    
    def pseudoKingMoves(result: List[(Int, Int)], fen: String): List[(Int, Int)] = {
        val (board, fenSplit, attackColorNum, moveColor, attackColor, piecePos) = readyingPseudoMoveData(fen, List(KING))
        val moves = pieceMoves(List(KING))
        
        @tailrec def checkKingMove(accumulator: List[(Int, Int)], piecePosistions: List[Int]): List[(Int, Int)] = {
            piecePosistions match {
                case Nil => accumulator
                case h :: t => {
                    checkKingMove(checkMoves(board, h, moves, accumulator, attackColor), t)
                }
            }
        }

        val mapKastleToMove: Map[Char, (Int, Int)] = Map(
            'K' -> (-1, -1),
            'Q' -> (-2, -1),
            'k' -> (-3, -1),
            'q' -> (-4, -1)
        )

        def emptyForKastle(c: Char): Boolean = {
            c match {
                case 'K' => board(61).pieceType == PieceType.EMPTY && board(62).pieceType == PieceType.EMPTY
                case 'Q' => board(59).pieceType == PieceType.EMPTY && board(58).pieceType == PieceType.EMPTY && board(57).pieceType == PieceType.EMPTY
                case 'k' => board(5).pieceType == PieceType.EMPTY && board(6).pieceType == PieceType.EMPTY
                case 'q' => board(1).pieceType == PieceType.EMPTY && board(2).pieceType == PieceType.EMPTY && board(3).pieceType == PieceType.EMPTY

            }
        }

        @tailrec
        def checkCastleMove(accumulator: List[(Int, Int)], fenCastle: List[Char]): List[(Int, Int)] = {
            fenCastle match {
                case Nil => accumulator;
                case h :: t => {
                    h match {
                        case '-' => accumulator
                        case castleType => {
                            if (((moveColor == WHITE && castleType.isUpper) || (moveColor == Color.BLACK && castleType.isLower)) && emptyForKastle(castleType)) {
                                checkCastleMove(mapKastleToMove(castleType)::accumulator, t);
                            } else {
                                checkCastleMove(accumulator, t);
                            }
                        }
                    }
                }
            }
        }

        checkKingMove(checkCastleMove(result, fenSplit(2).toList), piecePos);
    }

    @tailrec
    private def checkGeneralMoveInDirection(board: Vector[Piece], attackColor: Color, accumulator: List[(Int, Int)], piecePos: Int, startingPosition: Int, moveDir: (Int, Int)): List[(Int, Int)] = {
        val (rowDirection, columDirection) = moveDir
        if (!PseudoMoves.onBoard(piecePos, rowDirection, columDirection)) {
            return accumulator
        }
        board(piecePos + 8 * rowDirection + columDirection) match {
            case Piece(_, `attackColor`) => (startingPosition, piecePos + 8* rowDirection + columDirection) :: accumulator
            case Piece(PieceType.EMPTY, Color.EMPTY) => checkGeneralMoveInDirection(board, attackColor, (startingPosition, piecePos + 8* rowDirection + columDirection) :: accumulator, piecePos + 8* rowDirection + columDirection, startingPosition, moveDir);
            case _ => accumulator;
        }
    }

    @tailrec
    private def checkGeneralDirection(board: Vector[Piece], attackColor: Color, accumulator: List[(Int, Int)], piecePos: Int, moveDirections: List[(Int, Int)]): List[(Int, Int)] = {
        moveDirections match {
            case Nil => accumulator;
            case h :: t => checkGeneralDirection(board, attackColor, checkGeneralMoveInDirection(board, attackColor, accumulator, piecePos, piecePos, h), piecePos, t);
        }
    }

    @tailrec
    def moveGeneralRecursive(board: Vector[Piece], attackColor: Color, accumulator: List[(Int, Int)], piecePos: List[Int]): List[(Int, Int)] = {
        piecePos match {
            case Nil => accumulator
            case h :: t => {
                moveGeneralRecursive(board, attackColor, accumulator, t);
            }
        }
    }

    def pseudoHorizontalMoves(resultAccumulator: List[(Int, Int)], fen: String): List[(Int, Int)] = {
        val (board, fenSplit, attackColorNum, moveColor, attackColor, piecePos) = readyingPseudoMoveData(fen, List(ROOK, QUEEN))
        val directions: List[(Int, Int)]=  pieceMoves(List(ROOK, QUEEN))

        @tailrec
        def checkMoveInDirection(accumulator: List[(Int, Int)], piecePos: Int, startingPosition: Int, moveDir: (Int, Int)): List[(Int, Int)]
        = checkGeneralMoveInDirection(board, attackColor, accumulator, piecePos, startingPosition, moveDir)

        @tailrec
        def checkDirection(accumulator: List[(Int, Int)], piecePos: Int, moveDirections: List[(Int, Int)]): List[(Int, Int)]
        = checkGeneralDirection(board, attackColor, accumulator, piecePos, moveDirections)

        @tailrec
        def rookAndQueenMovesRecursive(accumulator: List[(Int, Int)], piecePos: List[Int]): List[(Int, Int)]
        = moveGeneralRecursive(board, attackColor, accumulator, piecePos)

        rookAndQueenMovesRecursive(resultAccumulator, piecePos);
    }

    def pseudoVerticalMoves(resultAccumulator: List[(Int, Int)], fen: String): List[(Int, Int)] = {
        val (board, fenSplit, attackColorNum, moveColor, attackColor, piecePos) = readyingPseudoMoveData(fen, List(BISHOP, QUEEN))
        val directions: List[(Int, Int)] = pieceMoves(List(BISHOP, QUEEN))

        @tailrec
        def checkMoveInDirection(accumulator: List[(Int, Int)], piecePos: Int, startingPosition: Int, moveDir: (Int, Int)): List[(Int, Int)]
        = checkGeneralMoveInDirection(board, attackColor, accumulator, piecePos, startingPosition, moveDir)

        @tailrec
        def checkDirection(accumulator: List[(Int, Int)], piecePos: Int, moveDirections: List[(Int, Int)]): List[(Int, Int)]
        = checkGeneralDirection(board, attackColor, accumulator, piecePos, moveDirections)

        @tailrec
        def verticalMovesRecursive(accumulator: List[(Int, Int)], piecePos: List[Int]): List[(Int, Int)]
        = moveGeneralRecursive(board, attackColor, accumulator, piecePos)

        verticalMovesRecursive(resultAccumulator, piecePos);
    }
}
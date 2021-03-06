package com.dummies.androidgame.crazyeights;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class GameView extends View {

	private int screenW;
	private int screenH;
	private Context myContext;
	private List<Card> deck = new ArrayList<Card>();
	private int scaledCardW;
	private int scaledCardH;
	private Paint whitePaint;
	private List<Card> myHand = new ArrayList<Card>();
	private List<Card> oppHand = new ArrayList<Card>();
	private int myScore = 0;
	private int oppScore = 0;
	private float scale;
	private Bitmap cardBack;
	private List<Card> discardPile = new ArrayList<Card>();
	private boolean myTurn;
	private ComputerPlayer computerPlayer = new ComputerPlayer();
	private int movingCardIdx = -1;
	private int movingX;
	private int movingY;
	private int validRank = 8;
	private int validSuit = 0;
	private Bitmap nextCardButton;
	private int scoreThisHand = 0;

	public GameView(Context context) {
		super(context);
		this.myContext = context;
		
		// Prepara un Paint para el texto. El texto se ve igual en cualquier
		// dispositivo, tiene siempre el mismo tama�o aparente ya que se elige
		// su tama�o en funci�n de la densidad de pantalla (scale)
		this.scale = myContext.getResources().getDisplayMetrics().density;
		this.whitePaint = new Paint();
		this.whitePaint.setAntiAlias(true);
		this.whitePaint.setColor(Color.WHITE);
		this.whitePaint.setStyle(Paint.Style.STROKE);
		this.whitePaint.setTextAlign(Paint.Align.LEFT);
		this.whitePaint.setTextSize(this.scale * 15);
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		this.screenW = w;
		this.screenH = h;
		
		// Calcula el reverso de las cartas
		Bitmap tempBitmap = BitmapFactory.decodeResource(
				this.myContext.getResources(), R.drawable.card_back);
		this.scaledCardW = (int) (this.screenW / 8);
		this.scaledCardH = (int) (this.scaledCardW * 1.28);
		this.cardBack = Bitmap.createScaledBitmap(tempBitmap, this.scaledCardW,
				this.scaledCardH, false);
		
		// Calcula el bot�n de pasar a la siguiente carta cuando hay m�s de 7 en la mano
		this.nextCardButton = BitmapFactory.decodeResource(super.getResources(),
				R.drawable.arrow_next);
		
		// Inicializa el mazo de cartas
		this.initCards();
		
		// Baraja las cartas del mazo y reparte a los 2 jugadores
		this.dealCards();
		
		// Extrae una carta para el centro, para la pila de descarte, que empieza vac�a,
		this.drawCard(this.discardPile);
		
		// Inicializa las variables
		this.validSuit = this.discardPile.get(0).getSuit();
		this.validRank = this.discardPile.get(0).getRank();
		this.myTurn = new Random().nextBoolean();
		if (!this.myTurn) {
			this.makeComputerPlay();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// Dibuja las puntuaciones en la pantalla.
		canvas.drawText("Computer Score: " + Integer.toString(this.oppScore), 10,
				this.whitePaint.getTextSize() + 10, this.whitePaint);
		canvas.drawText("My Score: " + Integer.toString(this.myScore), 10, this.screenH
				- this.whitePaint.getTextSize() - 10, this.whitePaint);
		
		// Dibuja la mano del adversario (el ordenador). Cartas del rev�s. En la
		// esquina superior izquierda, debajo de la puntuaci�n del oponente
		for (int i = 0; i < this.oppHand.size(); i++) {
			canvas.drawBitmap(this.cardBack, i * (this.scale * 5),
					this.whitePaint.getTextSize() + (50 * scale), null);
		}
		
		// Dibuja las cartas de la pila de descartadas en el centro de la pantalla
		canvas.drawBitmap(this.cardBack, (this.screenW / 2) - this.cardBack.getWidth() - 10,
				(this.screenH / 2) - (this.cardBack.getHeight() / 2), null);
		if (!this.discardPile.isEmpty()) {
			canvas.drawBitmap(this.discardPile.get(0).getBitmap(),
					(this.screenW / 2) + 10, (this.screenH / 2)
							- (this.cardBack.getHeight() / 2), null);
		}
		
		// Dibuja las cartas de la mano del jugador. Si hay m�s de 7, dibuja boton ver mas cartas
		if (this.myHand.size() > 7) { // Dibuja el bot�n de ver ciclo de cartas
			canvas.drawBitmap(this.nextCardButton, this.screenW - 
					this.nextCardButton.getWidth() - (30 * this.scale), this.screenH
					- this.nextCardButton.getHeight() - this.scaledCardH
					- (90 * this.scale), null);
		}
		// Dibuja mis cartas
		for (int i = 0; i < this.myHand.size(); i++) {
			// Si se est� desplazando una carta, esta se pinta en una posicion especial
			if (i == this.movingCardIdx) {
				canvas.drawBitmap(this.myHand.get(i).getBitmap(), this.movingX, 
						this.movingY,
						null);
			} else {
				// Si la carta no se est� moviendo, se pinta en su sitio parte inferior
				// de la pantalla
				if (i < 7) {
					canvas.drawBitmap(this.myHand.get(i).getBitmap(), i
							* (this.scaledCardW + 5), this.screenH - this.scaledCardH
							- this.whitePaint.getTextSize() - (50 * this.scale), null);
				}
			}
		}
		invalidate();
	}

	public boolean onTouchEvent(MotionEvent event) {
		int eventaction = event.getAction();
		int X = (int) event.getX();
		int Y = (int) event.getY();

		switch (eventaction) {

		case MotionEvent.ACTION_DOWN:
			if (this.myTurn) {
				for (int i = 0; i < 7; i++) {
					if (X > i * (this.scaledCardW + 5)
							&& X < i * (this.scaledCardW + 5) + this.scaledCardW
							&& Y > this.screenH - this.scaledCardH
									- this.whitePaint.getTextSize() - (50 * this.scale)) {
						this.movingCardIdx = i;
						this.movingX = X - (int) (30 * this.scale);
						this.movingY = Y - (int) (70 * this.scale);
					}
				}
			}
			break;

		case MotionEvent.ACTION_MOVE:
			this.movingX = X - (int) (30 * this.scale);
			this.movingY = Y - (int) (70 * this.scale);
			break;

		case MotionEvent.ACTION_UP:
			if (this.movingCardIdx > -1
					&& X > (this.screenW / 2) - (100 * this.scale)
					&& X < (this.screenW / 2) + (100 * this.scale)
					&& Y > (this.screenH / 2) - (100 * this.scale)
					&& Y < (this.screenH / 2) + (100 * this.scale)
					&& (this.myHand.get(this.movingCardIdx).getRank() == 8
							|| myHand.get(this.movingCardIdx).getRank() == this.validRank 
							|| this.myHand.get(this.movingCardIdx).getSuit() == this.validSuit)) {
				// Dejo una carta v�lida en el centro
				this.validRank = this.myHand.get(this.movingCardIdx).getRank();
				this.validSuit = this.myHand.get(this.movingCardIdx).getSuit();
				this.discardPile.add(0, this.myHand.get(this.movingCardIdx));
				this.myHand.remove(this.movingCardIdx);
				if (this.myHand.isEmpty()) {
					this.endHand();
				} else {
					if (this.validRank == 8) {
						this.showChooseSuitDialog();
					} else {
						this.myTurn = false;
						this.makeComputerPlay();
					}
				}
			}
			
			if (this.movingCardIdx == -1 && this.myTurn
					&& X > (this.screenW / 2) - (100 * this.scale)
					&& X < (this.screenW / 2) + (100 * this.scale)
					&& Y > (this.screenH / 2) - (100 * this.scale)
					&& Y < (this.screenH / 2) + (100 * this.scale)) {
				// Pincho en el centro para robar una carta, porque no tengo jugada v�lida
				if (this.checkForValidDraw()) {
					this.drawCard(this.myHand);
				} else {
					Toast.makeText(myContext, "You have a valid play.",
							Toast.LENGTH_SHORT).show();
				}
			}
			
			if (this.myHand.size() > 7
					&& X > this.screenW - this.nextCardButton.getWidth() - (30 * this.scale)
					&& Y > this.screenH - this.nextCardButton.getHeight() - this.scaledCardH
							- (90 * this.scale)
					&& Y < this.screenH - this.nextCardButton.getHeight() - this.scaledCardH
							- (60 * this.scale)) {
				// Pincho en el bot�n de rotar mi mazo de cartas para ver las ocultas
				Collections.rotate(this.myHand, 1);
			}
			this.movingCardIdx = -1;
			break;
		}
		invalidate();
		return true;
	}

	private void showChooseSuitDialog() {
		final Dialog chooseSuitDialog = new Dialog(this.myContext);
		chooseSuitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		chooseSuitDialog.setContentView(R.layout.choose_suit_dialog);
		
		final Spinner suitSpinner = (Spinner) chooseSuitDialog.findViewById(R.id.suitSpinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.myContext, R.array.suits, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		suitSpinner.setAdapter(adapter);
		
		Button okButton = (Button) chooseSuitDialog.findViewById(R.id.okButton);
		okButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				validSuit = (suitSpinner.getSelectedItemPosition() + 1) * 100;
				String suitText = "";
				if (validSuit == 100) {
					suitText = "Diamonds";
				} else if (validSuit == 200) {
					suitText = "Clubs";
				} else if (validSuit == 300) {
					suitText = "Hearts";
				} else if (validSuit == 400) {
					suitText = "Spades";
				}
				chooseSuitDialog.dismiss();
				
				Toast.makeText(myContext, "You chose " + suitText, Toast.LENGTH_SHORT).show();
				myTurn = false;
				makeComputerPlay();
			}
		});
		chooseSuitDialog.show();
	}

	private void initCards() {
		for (int i = 0; i < 4; i++) {
			for (int j = 102; j < 115; j++) {
				int tempId = j + (i * 100);
				Card tempCard = new Card(tempId);
				int resourceId = super.getResources().getIdentifier(
						"card" + tempId, "drawable", myContext.getPackageName());
				Bitmap tempBitmap = BitmapFactory.decodeResource(
						this.myContext.getResources(), resourceId);
				this.scaledCardW = (int) (this.screenW / 8);
				this.scaledCardH = (int) (this.scaledCardW * 1.28);
				Bitmap scaledBitmap = Bitmap.createScaledBitmap(tempBitmap,
						this.scaledCardW, this.scaledCardH, false);
				tempCard.setBitmap(scaledBitmap);
				this.deck.add(tempCard);
			}
		}
	}

	private void drawCard(List<Card> handToDraw) {
		handToDraw.add(0, this.deck.get(0));
		this.deck.remove(0);
		if (this.deck.isEmpty()) {
			for (int i = this.discardPile.size() - 1; i > 0; i--) {
				this.deck.add(this.discardPile.get(i));
				this.discardPile.remove(i);
				Collections.shuffle(this.deck, new Random());
			}
		}
	}

	private void dealCards() {
		// Baraja las cartas
		Collections.shuffle(this.deck, new Random());
		
		// Reparte 7 cartas a cada jugador
		for (int i = 0; i < 7; i++) {
			this.drawCard(this.myHand);
			this.drawCard(this.oppHand);
		}
	}

	private boolean checkForValidDraw() {
		boolean canDraw = true;
		for (int i = 0; i < this.myHand.size(); i++) {
			int tempId = this.myHand.get(i).getId();
			int tempRank = this.myHand.get(i).getRank();
			int tempSuit = this.myHand.get(i).getSuit();
			if (this.validSuit == tempSuit || this.validRank == tempRank || tempId == 108
					|| tempId == 208 || tempId == 308 || tempId == 408) {
				canDraw = false;
			}
		}
		return canDraw;
	}

	private void makeComputerPlay() {
		int tempPlay = 0;
		while (tempPlay == 0) {
			tempPlay = this.computerPlayer.makePlay(this.oppHand, this.validSuit, this.validRank);
			if (tempPlay == 0) {
				drawCard(this.oppHand);
			}
		}
		if (tempPlay == 108 || tempPlay == 208 || tempPlay == 308 || tempPlay == 408) {
			this.validRank = 8;
			this.validSuit = this.computerPlayer.chooseSuit(this.oppHand);
			String suitText = "";
			if (this.validSuit == 100) {
				suitText = "Diamonds";
			} else if (this.validSuit == 200) {
				suitText = "Clubs";
			} else if (this.validSuit == 300) {
				suitText = "Hearts";
			} else if (this.validSuit == 400) {
				suitText = "Spades";
			}
			Toast.makeText(this.myContext, "Computer chose " + suitText, Toast.LENGTH_SHORT).show();
		} else {
			this.validSuit = Math.round((tempPlay / 100) * 100);
			this.validRank = tempPlay - validSuit;
		}
		for (int i = 0; i < this.oppHand.size(); i++) {
			Card tempCard = this.oppHand.get(i);
			if (tempPlay == tempCard.getId()) {
				this.discardPile.add(0, this.oppHand.get(i));
				this.oppHand.remove(i);
			}
		}
		if (this.oppHand.isEmpty()) {
			this.endHand();
		}
		this.myTurn = true;
	}

	private void endHand() {
		final Dialog endHandDialog = new Dialog(myContext);
		endHandDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		endHandDialog.setContentView(R.layout.end_hand_dialog);
		
		this.updateScores();
		
		TextView endHandText = (TextView) endHandDialog.findViewById(R.id.endHandText);
		if (this.myHand.isEmpty()) {
			// He ganado yo la mano
			if (this.myScore >= 300) {
				// He ganado tambi�n la partida
				endHandText.setText("You reached " + this.myScore
						+ " points. You won! Would you like to play again?");
			} else {
				endHandText.setText("You went out and got " + this.scoreThisHand + " points!");
			}
		} else if (this.oppHand.isEmpty()) {
			// El ordenador ha ganado la mano
			if (this.oppScore >= 300) {
				endHandText
						.setText("The computer reached "
								+ this.oppScore
								+ " points. Sorry, you lost. Would you like to play again?");
			} else {
				endHandText.setText("The computer went out and got "
						+ this.scoreThisHand + " points.");
			}
		}
		
		Button nextHandButton = (Button) endHandDialog.findViewById(R.id.nextHandButton);
		if (this.oppScore >= 300 || this.myScore >= 300) {
			nextHandButton.setText("New Game");
		}
		nextHandButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (oppScore >= 300 || myScore >= 300) {
					myScore = 0;
					oppScore = 0;
				}
				initNewHand();
				endHandDialog.dismiss();
			}
		});
		endHandDialog.show();
	}

	private void updateScores() {
		for (int i = 0; i < this.myHand.size(); i++) {
			this.oppScore += this.myHand.get(i).getScoreValue();
			this.scoreThisHand += this.myHand.get(i).getScoreValue();
		}
		for (int i = 0; i < this.oppHand.size(); i++) {
			this.myScore += this.oppHand.get(i).getScoreValue();
			this.scoreThisHand += this.oppHand.get(i).getScoreValue();
		}
	}

	private void initNewHand() {
		this.scoreThisHand = 0;
		if (this.myHand.isEmpty()) {
			this.myTurn = true;
		} else if (oppHand.isEmpty()) {
			this.myTurn = false;
		}
		this.deck.addAll(this.discardPile);
		this.deck.addAll(this.myHand);
		this.deck.addAll(this.oppHand);
		this.discardPile.clear();
		this.myHand.clear();
		this.oppHand.clear();
		this.dealCards();
		this.drawCard(this.discardPile);
		this.validSuit = this.discardPile.get(0).getSuit();
		this.validRank = this.discardPile.get(0).getRank();
		if (!this.myTurn) {
			makeComputerPlay();
		}
	}
}

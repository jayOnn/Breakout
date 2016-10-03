import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.*;
import java.util.*;
import java.lang.Math;


// top-level container class
class Breakout {

	int level = 1;
	int Maxlevel = 3;
	boolean pause = true;
	boolean GameOver;
	boolean Winner;
	int Score = 0;
	int levScore = 0;
	int Life = 3;
	double BallRat = 1;


	String msg;
	JFrame frame;
	JPanel mainPanel;
	View view;
	Model model;
	ArrayList<Ball> balls;
	ArrayList<Block> blocks;
	Paddle paddle;

	 public static class SplashScreen extends JWindow{
		SplashScreen(){
			JPanel content = (JPanel)getContentPane();
			//content.setBackground(Color.white);

			//window Bounds
			int width = 600;
			int height = 450;
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			int x = (screen.width - width)/2;
			int y = (screen.height -height)/2;
			setBounds(x,y,width,height);
			ImageIcon pic = new ImageIcon(getClass().getResource("Splash.png"));
			// Build splash
			content.add(new JLabel(pic), BorderLayout.CENTER);
			Color oraRed = new Color(156, 20, 20, 255);
			content.setBorder(BorderFactory.createLineBorder(oraRed,7));

			setVisible(true);

			try{Thread.sleep(6500);}
			catch(Exception e){}
			setVisible(false);
		}
	}
	// constructor for the game
	// instantiates all of the top-level classes (model, view)
	// and tells the model to start the game
	Breakout(int frameRate, int ballRate) {
		BallRat = ballRate/100;
		view = new View();
		frame = new JFrame("Breakout Game");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(new Dimension(600, 600));
		frame.setMinimumSize(new Dimension(400, 300));
		frame.setContentPane(view);
		frame.setVisible(true);


	//	System.out.println("line 1");

		model = new Model();

		startGame(level,frameRate);

	}

	public void startGame(int lev,int frameRate) {
		model.newGame(lev);
		ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
		exec.scheduleAtFixedRate(new Runnable() {
			public void run() {
				view.refresh();
				//System.out.println("gg");
			}
		}, 0, 1000/frameRate, TimeUnit.MILLISECONDS);

		ScheduledExecutorService exec2 = Executors.newSingleThreadScheduledExecutor();
		exec2.scheduleAtFixedRate(new Runnable() {
			public void run() {
				model.refresh();
			}
		}, 0, 25, TimeUnit.MILLISECONDS);
	}

	// game elements
	class Block {
		public double Blockx;
		public double Blocky;
		public double locX;
		public double locY;
		public int health;
		public int points;
		Color col;

		Block(double X, double Y , int HP, int point, double bx, double by, Color color){
			locX = X;
			locY = Y;
			health = HP;
			points = point;
			Blockx = bx;
			Blocky = by;
			col = color;
		}

		public void hit(){
			health--;
			Score += points;
			if( health == 0)
				levScore++;
			if(health == 1){
				col = new Color(0,0,0,100);
			}

			//System.out.println(levScore + "\n");
			// add points
		}

	}

	class Ball {
		public double Ballx = 0.03;
		public double Bally = 0.03;
		public double ballSpeed;
		public int ballDirection = 330;
		public double locX;
		public double locY;

		Ball(double X, double Y, double speed){
			locX = X;
			locY = Y;
			ballSpeed = speed;
		}

		public void ballBounce(int side) { // 0 top, 1 right , 2 bottom, 3 left
			Score += 10;
			if (side == 0) {    // top side
				if (ballDirection >= 0 && ballDirection < 90) {
					ballDirection += (90 - ballDirection) * 2;
				} else if (ballDirection > 270 && ballDirection < 360) {
					ballDirection = 180 + (360 - ballDirection);
				}
			} else if (side == 2) {// bottom side
				if (ballDirection <= 180 && ballDirection > 90) {
					ballDirection -= 2 * (ballDirection - 90);
				} else if (ballDirection > 180 && ballDirection < 270) {
					ballDirection += 2 * (270 - ballDirection);
				}
			} else if (side == 1) { // right side
				if (ballDirection > 0 && ballDirection <= 90) {
					ballDirection = 360 - ballDirection;
				} else if (ballDirection > 90 && ballDirection < 180) {
					ballDirection = 360 - ballDirection;
				}
			} else if (side == 3) {    // left side
				if (ballDirection < 360 && ballDirection >= 270) {
					ballDirection = 360 - ballDirection;
				} else if (ballDirection < 270 && ballDirection > 180) {
					ballDirection = 90 + (270 - ballDirection);
				}
			}
		}

		public void ballTravelx(){
			if(ballDirection == 90){
				locX += ballSpeed;
			}
			else if(ballDirection == 270){
				locX -=ballSpeed;
			}
			else if(ballDirection > 0 && ballDirection < 360 ){
				locX += ballSpeed*Math.sin(Math.toRadians(ballDirection));
			}

		}

		public void ballTravely(){
			if( ballDirection == 0){
				locY -= ballSpeed;
			}
			else if (ballDirection == 180){
				locY +=ballSpeed;
			}
			else if(ballDirection > 0 && ballDirection < 360 ){
				locY -= (ballSpeed*Math.cos(Math.toRadians(ballDirection)));
			}

		}

	}

	class Paddle {

		public double width = 0.16;
		public double height = 0.02;
		public double PaddleX = 0.43;
		public double PaddleY = 0.90;
		public double PaddleX2 = 0.58;

		Paddle(){
		}
	}

	// model keeps track of game state (objects in the game)
	// contains a Timer that ticks periodically to advance the game
	// AND calls an update() method in the View to tell it to redraw
	class Model {

		Model(){
			mainPanel = new JPanel();
			mainPanel.setBackground(Color.BLACK);
			//mainPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			frame.add(mainPanel,BorderLayout.CENTER);
			mainPanel.setVisible(true);
			msg = "Press Space or Click to Start";
			view.refresh();
		}

		public void reset(){
			if(balls.size() == 1) {
				balls.get(0).locX = 0.49;
				balls.get(0).locY = 0.88;
				balls.get(0).ballDirection = 45;
				paddle.PaddleX = 0.43;
				paddle.PaddleX2 = 0.58;
			}else if(balls.size() == 2){
				balls.get(0).locX = 0.48;
				balls.get(0).locY = 0.87;
				balls.get(0).ballDirection = 30;
				balls.get(1).locX = 0.52;
				balls.get(1).locY = 0.88;
				balls.get(1).ballDirection = 330;
				paddle.PaddleX = 0.43;
				paddle.PaddleX2 = 0.58;
			}
		}

		public void death(){

			if(Life == 0){
				GameOver = true;
			}
			else if(Life > 0){
				reset();
			}
			if(Life > 0)
			Life--;
		}

		public void ballPhy(Ball ball1){
			Rectangle r = frame.getBounds();
			double ballWidth = ball1.Ballx;
			double ballHeight = ball1.Bally;
			double ballTLx = (ball1.locX) * view.getWidth();
			double ballTLy = (ball1.locY) * view.getHeight();
			double ballTRx = ballTLx + view.getWidth() * ballWidth;
			double ballTRy = ballTLy;
			double ballBLx = ballTLx;
			double ballBLy = ballTLy + view.getHeight() * ballHeight;
			double ballBRx = ballTRx;
			double ballBRy = ballBLy;
			if (ballTLx <= 0) { //left wall contact
				ball1.ballBounce(3);
			} else if (ballTLy <= 0) // top wall contact
			{
				ball1.ballBounce(0);
			} else if (ballTRx >= view.getWidth()) { // right wall contact
				ball1.ballBounce(1);
			} else if (ballBLy >= view.getHeight() + view.getHeight() * 0.02) { // bottom wall contact
				//ball1.ballBounce(2);
				death();
				//if (flag == 1) {
				pause = true;
				//}
				//flag = 1;
			} else if (((ballBLx >= adjustX(paddle.PaddleX) && ballBLx <= adjustX(paddle.PaddleX2)
					&& ballBLy >= view.getHeight() * paddle.PaddleY) || (ballBRx >= adjustX(paddle.PaddleX)
					&& ballBLx <= adjustX(paddle.PaddleX2) && ballBRy >= view.getWidth() * paddle.PaddleY))
					&& ballBLy <= view.getHeight() * (paddle.PaddleY + paddle.height)) {
				ball1.ballBounce(2);
			} else { // block hit bot
				for (int m = 0; m < blocks.size(); m++) {
					if (blocks.get(m).health > 0) {
						double b0x = blocks.get(m).locX * view.getWidth();
						double b0y = blocks.get(m).locY * view.getHeight();
						double b1x = b0x + (blocks.get(m).Blockx * view.getWidth());
						double b1y = b0y;
						double b3x = b0x;
						double b3y = b0y + (blocks.get(m).Blocky * view.getHeight());
						double b2x = b1x;
						double b2y = b3y;

						if (((ballTLx >= b3x && ballTLx <= b2x) || (ballTRx >= b3x && ballTRx <= b2x)) && (ballTLy <= b3y && ballTLy > b3y - balls.get(0).ballSpeed * view.getHeight())) { // bottom check
							blocks.get(m).hit();
							ball1.ballBounce(0);
						//	System.out.println("bottom block");
						} else if (((ballTLx >= b3x && ballTLx <= b2x) || (ballTRx >= b3x && ballTRx <= b2x)) && (ballBLy >= b0y && ballBLy < b0y + balls.get(0).ballSpeed * view.getHeight())) { //top check
							blocks.get(m).hit();
							ball1.ballBounce(2);
						//	System.out.println("top block");
						} else if (((ballTLy >= b1y && ballTLy <= b2y) || (ballBLy >= b1y && ballBLy <= b2y)) && (ballTLx <= b1x && ballTLx > b1x - balls.get(0).ballSpeed * view.getWidth())) { // bottom check
							blocks.get(m).hit();
							ball1.ballBounce(3);
						//	System.out.println("right block");
						} else if (((ballTRy >= b0y && ballTRy <= b3y) || (ballBRy >= b0y && ballBRy <= b3y)) && (ballTRx >= b0x && ballTRx < b0x + balls.get(0).ballSpeed * view.getWidth())) { // bottom check
							blocks.get(m).hit();
							ball1.ballBounce(1);
						//	System.out.println("left block " + view.getWidth());
						}
					}
				}
			}

			//balls.get(0).locY-= balls.get(0).ballSpeed;
			ball1.ballTravelx();
			ball1.ballTravely();
		}

		public double adjustX(double cord){
			return (int)(cord*view.getWidth());
		}
		// advances the game
		public void refresh(){
			if((levScore == 54 && level == 1)||(levScore == 36 && level ==2)){
				level ++;
				newGame(level);
				pause = true;
			}
			else if(levScore == 54 && level == 3){
				//pause = true;
				Winner = true;
				pause = true;
			}
			if(!pause) {
				Ball ball1 = balls.get(0);
				ballPhy(ball1);
				if(balls.size() == 2){
					Ball ball2 = balls.get(1);
					ballPhy(ball2);
				}


				//System.out.println(view.getHeight());
				//	System.out.println("Border: "+ view.getWidth());
				//	System.out.println("ball: "+ballTRx);
			}
		}

		public void newGame(int lev){ // indicates level it starts
			Winner = false;
			GameOver = false;
			pause = true;
			levScore =0;
			//Life = 3;
			if(lev <= 1){
				blocks = new ArrayList<Block>(40);
				balls = new ArrayList<Ball>(2);
				for (double i = 0.05; i < 0.55 ; i+=0.10){
					for(double j = 0.05; j < 0.85; j+=0.1) {
						blocks.add(new Block(j, i, 1, 100,0.06,0.09,new Color((int)(i*255),164,212,255)));

					}
				}

				balls.add(new Ball(0.49, 0.87, 0.01*BallRat));
				paddle = new Paddle();
			}
			else if( lev == 2 ){
				blocks = new ArrayList<Block>(40);
				balls = new ArrayList<Ball>(2);
				for (double i = 0.05; i < 0.25 ; i+=0.05){
					for(double j = 0.05; j < 0.85; j+=0.1) {
						if(i <0.20)
							blocks.add(new Block(j, i, 1, 100 , 0.09, 0.03,new Color((int)(j*255),164,93,202)));
						else if(i == 0.20)
							blocks.add(new Block(j, i, 2, 100 , 0.09, 0.03,Color.BLACK));

					}
				}
				balls.add(new Ball(0.49, 0.87, 0.015*BallRat));
				paddle = new Paddle();
			}else if(lev == 3){
				blocks = new ArrayList<Block>(40);
				balls = new ArrayList<Ball>(2);
				for (double i = 0.05; i < 0.55 ; i+=0.10){
					for(double j = 0.05; j < 0.85; j+=0.1) {
						blocks.add(new Block(j, i, 1, 100,0.03,0.03,new Color((int)(i*255*1.5),(int)(j*255),(int)(j*255),255)));

					}
				}
				balls.add(new Ball(0.48, 0.87, 0.013*BallRat));
				balls.add(new Ball(0.52, 0.87, 0.015*BallRat));
				paddle = new Paddle();
			}
		}
	}

	// game window
	// draws everything based on the game state
	// receives notification from the model when something changes, and
	// draws components based on the model.
	class View extends JComponent {
		public KeyLis listener;
		double xval = getWidth()*0.5;

		View(){



			//System.out.println("view start");
			repaint();
			listener = new KeyLis();
			this.setFocusable(true);
			this.requestFocus();
			this.addKeyListener(listener);
			this.addMouseMotionListener(new MouseMotionAdapter(){
				@Override
				public void mouseMoved(MouseEvent e) {
					super.mouseMoved(e);
					//if(xval != (double)(e.getX()/getWidth())){
						xval = (double)(e.getX())/getWidth();
						if(xval-(paddle.width/2) < 0){
							paddle.PaddleX = 0;
						}
						else if((xval-(paddle.width/2)+0.16)*getWidth() > getWidth()){
							paddle.PaddleX = 0.85;
						}
						else {
							if(pause) {
								if(balls.size() == 1) {
									balls.get(0).locX = xval-0.01;
								}else if(balls.size()==2){
									balls.get(0).locX = xval-0.02;
									balls.get(1).locX = xval+0.02;
								}
							}
							paddle.PaddleX = xval - (paddle.width / 2);
						}
						paddle.PaddleX2 = paddle.PaddleX+0.16;
						//System.out.print((double)(e.getX()/getWidth())+"\n");
				//	}
					//System.out.print((xval!=(double)(e.getX())/getWidth()) +"\n");
				}
			});
			this.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent me) {
					if (!GameOver) {
						if (pause)
							pause = !pause;
					} else if (GameOver) {
						level = 1;
						Score = 0;
						Life = 3;
						levScore = 0;
						model.newGame(level);
					}
					if(Winner){
						level = 1;
						Score = 0;
						Life = 3;
						levScore = 0;
						model.newGame(level);
					}
				}
			});
			//frame.add(this);
			//this.add(new JLabel("Hello"));
			this.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e)
				{
					repaint();
					//System.out.println("resized");
				}
			});
			Point p = MouseInfo.getPointerInfo().getLocation();
		}

		private class KeyLis extends KeyAdapter {
			@Override
			public void keyPressed(KeyEvent e) {
				//System.out.println(paddle.PaddleX2);
				switch (e.getKeyCode()) {
					case KeyEvent.VK_LEFT:
						if(paddle.PaddleX > 0) {
							if(pause == true){
								balls.get(0).locX -= 0.02;
								if(balls.size()==2)
									balls.get(1).locX -= 0.02;
							}
							paddle.PaddleX -= 0.02;
							paddle.PaddleX2 -= 0.02;
						}
						break;
					case KeyEvent.VK_RIGHT:
						if(getWidth()*paddle.PaddleX2 < getWidth()) {
							//System.out.println(paddle.PaddleX2);
							//System.out.println(getWidth());
							if(pause == true){
								balls.get(0).locX += 0.02;
								if(balls.size()==2)
									balls.get(1).locX += 0.02;
							}
							paddle.PaddleX2 += 0.02;
							paddle.PaddleX += 0.02;
						}
						break;
					case KeyEvent.VK_SPACE:
						if(!GameOver) {
							if(pause)
								pause = !pause;
						}
						else if(GameOver || Winner){
							level = 1;
							Score = 0;
							Life = 3;
							levScore = 0;
							model.newGame(level);
					}
						break;
					case KeyEvent.VK_PAGE_DOWN:
						if(level > 1){
							level --;
							model.newGame(level);
						}
						break;
					case KeyEvent.VK_PAGE_UP:
						if(level < Maxlevel){
							level ++;
							model.newGame(level);

						}
						break;
					case KeyEvent.VK_ESCAPE:
						System.exit(0);
						break;

				}
			}
		}


		public void refresh(){
			repaint();
		}
		//final Component myComponent = makeTheFrameOrWhatever();



		@Override
		protected void paintComponent(Graphics g){
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  // antialiasing look nicer
					RenderingHints.VALUE_ANTIALIAS_ON);
			//setOpaque(true);
			setBackground(Color.BLACK);
			//g2.setColor(Color.BLUE);
			for(int m = 0; m < blocks.size(); m++){
				//System.out.print(m);
				g2.setColor(blocks.get(m).col);
				if(blocks.get(m).health > 0) {
					g2.fillRect((int) (getWidth() * blocks.get(m).locX),
							(int) (getHeight() * blocks.get(m).locY),
							(int) (blocks.get(m).Blockx * getWidth()),
							(int) (blocks.get(m).Blocky * getHeight()));
				}
			}

			Font fontl = new Font("Arial",Font.PLAIN,(int)(getWidth()*0.02));
			g2.setFont(fontl);
			g2.setColor(Color.BLACK);
			g2.drawString("Level: "+ level,(int)(getWidth()*0.01),(int)(getHeight()*0.02));

			g2.setColor(new Color(255,148,54,255));
			g2.fillOval((int)(getWidth()* balls.get(0).locX),(int)(getHeight()*balls.get(0).locY),(int)(getWidth()* balls.get(0).Ballx),(int)(getHeight()*balls.get(0).Bally));
			if(level == 3){
				g2.setColor(new Color(255,199,54,255));
				g2.fillOval((int)(getWidth()* balls.get(1).locX),(int)(getHeight()*balls.get(1).locY),(int)(getWidth()* balls.get(1).Ballx),(int)(getHeight()*balls.get(1).Bally));
			}
			g2.setColor(Color.BLACK);

			g2.fillRect((int)(getWidth()*paddle.PaddleX),(int)(getHeight()* paddle.PaddleY), (int)(getWidth()*paddle.width),(int)(getHeight()*paddle.height));

			Font font = g2.getFont().deriveFont((float)(getWidth()*0.03));
			g2.setFont(font);
			g2.drawString("Life: "+Integer.toString(Life),(int)(getWidth()*0.90),(int)(getHeight()*0.98));
			g2.drawString(Integer.toString(Score),(int)(getWidth()*0.01),(int)(getHeight()*0.98));

			if(pause && !GameOver && !Winner){
				Font font2 = new Font("Arial",Font.PLAIN,(int)(getWidth()*0.03));
				g2.setFont(font2);
				g2.setColor(Color.MAGENTA);
				g2.drawString("Press Space or Click to Start",(int)(getWidth()*0.33),(int)(getHeight()*0.97));
			}
			else if(GameOver){
				Font font3 = g2.getFont().deriveFont((float)(getWidth()*0.1));
				g2.setFont(font3);
				g2.setColor(Color.RED);
				g2.drawString("GAME OVER",(int)(getWidth()*0.20),(int)(getHeight()*0.50));
				Font font4 = g2.getFont().deriveFont((float)(getHeight()*0.03));
				g2.setFont(font4);
				g2.setColor(Color.MAGENTA);
				g2.drawString("Press Space or Click to Continue",(int)(getWidth()*0.33),(int)(getHeight()*0.97));
			}
			if(Winner){
				Font font3 = g2.getFont().deriveFont((float)(getWidth()*0.1));
				g2.setFont(font3);
				g2.setColor(new Color(104,255,54,255));
				g2.drawString("WINNER",(int)(getWidth()*0.30),(int)(getHeight()*0.50));
				Font font4 = g2.getFont().deriveFont((float)(getHeight()*0.03));
				g2.setFont(font4);
				g2.setColor(Color.MAGENTA);
				g2.drawString("Press Space or Click to Restart",(int)(getWidth()*0.33),(int)(getHeight()*0.97));
			}
			//System.out.print("frame: "+ frame.getWidth() + "\n");
			//System.out.print("view: "+ getHeight() + "\n");
			//System.out.print("Rect: "+ getWidth()*blocks.get(0).locX + "\n");
		}
	}

	// entry point for the application
	public static void main(String[] args) {
		int frameRate = Integer.parseInt(args[0]);
		int ballRate = Integer.parseInt(args[1]);
		SplashScreen splashscreen = new SplashScreen();
		Breakout game = new Breakout(frameRate,ballRate);
	}
}

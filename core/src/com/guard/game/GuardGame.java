package com.guard.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class GuardGame extends ApplicationAdapter {
	private final double FRAME_PERIOD = (1 / 60) * 1000;
	final int SCREEN_WIDTH = 768, SCREEN_HEIGHT = 672;
	SpriteBatch batch;
	ShapeRenderer shapeRenderer;
	Vector2[][] obsticles;
	Guard guard;
	Player player;
	boolean gameOver;
	BitmapFont font;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();
		font = new BitmapFont();
		gameOver = false;
		obsticles = new Vector2[4][4];

		obsticles[0][0] = new Vector2(5, 168);
		obsticles[0][1] = new Vector2(5, 504);
		obsticles[0][2] = new Vector2(576, 504);
		obsticles[0][3] = new Vector2(576, 168);

		obsticles[1][0] = new Vector2(576, 330);
		obsticles[1][1] = new Vector2(576, 400);
		obsticles[1][2] = new Vector2(670, 400);
		obsticles[1][3] = new Vector2(670, 330);

		obsticles[2][0] = new Vector2(450, 100);
		obsticles[2][1] = new Vector2(450, 35);
		obsticles[2][2] = new Vector2(525, 35);
		obsticles[2][3] = new Vector2(525, 100);

		obsticles[3][0] = new Vector2(5, 5);
		obsticles[3][1] = new Vector2(5, SCREEN_HEIGHT);
		obsticles[3][2] = new Vector2(SCREEN_WIDTH, SCREEN_HEIGHT);
		obsticles[3][3] = new Vector2(SCREEN_WIDTH, 5);

		player = new Player();
		guard = new Guard(player, obsticles);
	}

	@Override
	public void render () {
		long beginTime = System.currentTimeMillis();
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		guard.render(shapeRenderer, batch);
		player.render(shapeRenderer);
		drawBuilding();

		if(gameOver){
			batch.begin();
			font.setColor(Color.FIREBRICK);
			font.getData().setScale(5);
			font.draw(batch, "Game Over", SCREEN_WIDTH/2f-150, SCREEN_HEIGHT/2f+20);
			font.getData().setScale(2);
			font.draw(batch, "Press Enter to Reset", SCREEN_WIDTH/2f-100, SCREEN_HEIGHT/2f-60);
			batch.end();
		}
		else {
			player.act();
			guard.update();
		}

		update();

		long timeDiff = System.currentTimeMillis() - beginTime;
		long sleepTime = (long) (FRAME_PERIOD - timeDiff);
		if (sleepTime > 0) {
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
			}
		}
	}

	private void drawBuilding(){
		shapeRenderer.setColor(Color.DARK_GRAY);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		for(int i = 0; i<3; i++){
			float height = obsticles[i][0].y - obsticles[i][1].y;
			float width = obsticles[i][1].x - obsticles[i][2].x;
			shapeRenderer.rect(obsticles[i][2].x, obsticles[i][2].y, width, height);
		}
		shapeRenderer.rect(0,0, 5, 672);
		shapeRenderer.rect(0,0, 768, 5);
		shapeRenderer.end();
	}

	public void update() {
		if(gameOver){
			if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
				restart();
			}
		}
		else {
			if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
				player.right();
			}
			if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
				player.left();
			}
			if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
				player.down();
			}
			if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
				player.up();
			}
			Vector2 positionHolder = new Vector2(player.getPosition());
			if (positionHolder.sub(guard.getPosition()).len() < 20) {
				gameOver = true;
			}
		}
	}

	private void restart(){
		gameOver = false;
		player = new Player();
		guard = new Guard(player, obsticles);
	}

	@Override
	public void dispose () {
		batch.dispose();
		shapeRenderer.dispose();
		font.dispose();
	}
}

package com.company.android.arduinoadk;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl;
import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl.IAnalogOnScreenControlListener;
import org.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.touch.controller.MultiTouch;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import android.opengl.GLES20;
import android.util.Log;
import android.widget.Toast;

public class AnalogOnScreenControls extends SimpleBaseGameActivity {

	private static final String TAG = AnalogOnScreenControls.class.getSimpleName();

	private static final int CAMERA_WIDTH = 480;
	private static final int CAMERA_HEIGHT = 320;

	private Camera mCamera;

	private BitmapTextureAtlas mBitmapTextureAtlas;

	private BitmapTextureAtlas mOnScreenControlTexture;
	private ITextureRegion mOnScreenControlBaseTextureRegion;
	private ITextureRegion mOnScreenControlKnobTextureRegion;

	private boolean mPlaceOnScreenControlsAtDifferentVerticalLocations = false;


	@Override
	public EngineOptions onCreateEngineOptions() {
		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		final EngineOptions engineOptions = new EngineOptions(true,
				ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(
						CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera);
		engineOptions.getTouchOptions().setNeedsMultiTouch(true);

		if (MultiTouch.isSupported(this)) {
			if (MultiTouch.isSupportedDistinct(this)) {
				Toast.makeText(
						this,
						"MultiTouch detected --> Both controls will work properly!",
						Toast.LENGTH_SHORT).show();
			} else {
				this.mPlaceOnScreenControlsAtDifferentVerticalLocations = true;
				Toast.makeText(
						this,
						"MultiTouch detected, but your device has problems distinguishing between fingers.\n\nControls are placed at different vertical locations.",
						Toast.LENGTH_LONG).show();
			}
		} else {
			Toast.makeText(
					this,
					"Sorry your device does NOT support MultiTouch!\n\n(Falling back to SingleTouch.)\n\nControls are placed at different vertical locations.",
					Toast.LENGTH_LONG).show();
		}

		return engineOptions;
	}

	@Override
	public void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		this.mBitmapTextureAtlas = new BitmapTextureAtlas(
				this.getTextureManager(), 32, 32, TextureOptions.BILINEAR);
//		this.mFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory
//				.createFromAsset(this.mBitmapTextureAtlas, this,
//						"face_box.png", 0, 0);
		this.mBitmapTextureAtlas.load();

		this.mOnScreenControlTexture = new BitmapTextureAtlas(
				this.getTextureManager(), 256, 128, TextureOptions.BILINEAR);
		this.mOnScreenControlBaseTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mOnScreenControlTexture, this,
						"onscreen_control_base.png", 0, 0);
		this.mOnScreenControlKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mOnScreenControlTexture, this,
						"onscreen_control_knob.png", 128, 0);
		this.mOnScreenControlTexture.load();
	}

	@Override
	public Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		final Scene scene = new Scene();
		scene.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));

		/* Velocity control (left). */
		final float x1 = 0;
		final float y1 = CAMERA_HEIGHT
				- this.mOnScreenControlBaseTextureRegion.getHeight();
		final AnalogOnScreenControl velocityOnScreenControl = new AnalogOnScreenControl(
				x1, y1, this.mCamera, this.mOnScreenControlBaseTextureRegion,
				this.mOnScreenControlKnobTextureRegion, 0.1f,
				this.getVertexBufferObjectManager(),
				new IAnalogOnScreenControlListener() {
					@Override
					public void onControlChange(
							final BaseOnScreenControl pBaseOnScreenControl,
							final float pValueX, final float pValueY) {
						Log.i(TAG, "x=" + pValueX + " ,y= " + pValueY);
					}

					@Override
					public void onControlClick(
							final AnalogOnScreenControl pAnalogOnScreenControl) {
						/* Nothing. */
					}
				});
		velocityOnScreenControl.getControlBase().setBlendFunction(
				GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		velocityOnScreenControl.getControlBase().setAlpha(0.5f);

		scene.setChildScene(velocityOnScreenControl);

		/* Rotation control (right). */
		final float y2 = (this.mPlaceOnScreenControlsAtDifferentVerticalLocations) ? 0
				: y1;
		final float x2 = CAMERA_WIDTH
				- this.mOnScreenControlBaseTextureRegion.getWidth();
		final AnalogOnScreenControl rotationOnScreenControl = new AnalogOnScreenControl(
				x2, y2, this.mCamera, this.mOnScreenControlBaseTextureRegion,
				this.mOnScreenControlKnobTextureRegion, 0.1f,
				this.getVertexBufferObjectManager(),
				new IAnalogOnScreenControlListener() {
					@Override
					public void onControlChange(
							final BaseOnScreenControl pBaseOnScreenControl,
							final float pValueX, final float pValueY) {
						Log.i(TAG, "x=" + pValueX + " ,y= " + pValueY);
					}

					@Override
					public void onControlClick(
							final AnalogOnScreenControl pAnalogOnScreenControl) {
						/* Nothing. */
					}
				});
		rotationOnScreenControl.getControlBase().setBlendFunction(
				GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		rotationOnScreenControl.getControlBase().setAlpha(0.5f);

		velocityOnScreenControl.setChildScene(rotationOnScreenControl);

		return scene;
	}

}

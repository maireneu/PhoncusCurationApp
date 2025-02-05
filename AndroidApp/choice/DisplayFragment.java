package com.phoncus.app.alpha.choice;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.phoncus.app.alpha.R;

public class DisplayFragment extends Fragment implements View.OnClickListener,ChoiceActivity.onKeyBackPressedListener {

	private ImageView nextbutton = null;
	private LinearLayout footer = null;
	DrawingView ov=null;
	private ImageView diplay_image=null;
	private String current_display_save;
	private String select_display_save;

	SharedPreferences prefs = null;


	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_choice_display2, container, false);

		nextbutton = (ImageView) view.findViewById(R.id.next);
		nextbutton.setOnClickListener(this);
		footer = (LinearLayout) view.findViewById(R.id.displayseekbar);
		diplay_image = (ImageView) view.findViewById(R.id.diplay_image);
		prefs = getActivity().getSharedPreferences("__setting",
				Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		current_display_save = prefs.getString("PPI", "0");
		Log.d("test", current_display_save+"");
		if (prefs.getString("SELECT_PPI","0") == "") {
			if (Integer.parseInt(current_display_save) < 300) {
				select_display_save = "250";
				diplay_image.setImageResource(R.mipmap.display_hd);
			} else if (Integer.parseInt(current_display_save) < 450) {
				select_display_save = "400";
				diplay_image.setImageResource(R.mipmap.display_fhd);
			} else{
				select_display_save = "550";
				diplay_image.setImageResource(R.mipmap.display_qhd);
			}
		}else {
			select_display_save = prefs.getString("SELECT_PPI","0");
			if(Integer.parseInt(select_display_save) < 300){
				diplay_image.setImageResource(R.mipmap.display_hd);
			}else if(Integer.parseInt(select_display_save) < 450){
				diplay_image.setImageResource(R.mipmap.display_fhd);
			}else if(Integer.parseInt(select_display_save) < 1000){
				diplay_image.setImageResource(R.mipmap.display_qhd);
			}else{
				diplay_image.setImageResource(R.mipmap.no_image);
			}
		}
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		ov = new DrawingView(this.getActivity());
		footer.addView(ov);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.next) {
			footer.removeView(ov);
			SharedPreferences prefs = getActivity().getSharedPreferences("__setting",
					Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("SELECT_PPI",select_display_save + "");
			editor.commit();
			((ChoiceActivity)getActivity()).nextFragmentReplace();
		}
	}

	class DrawingView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

		private float w;
		private float h;
		private float seekbarh; //seekbar 높이
		private float seekbarw; //seekbar 너비
		private float buttonh; //seekbar 버튼 높이
		private float buttonw; //seekbar 버튼 너비
		private boolean touch=true; //touch 인식
		private int n = 5; // seekbar의 갯수 변수
		private Bitmap img1; //button
		private Bitmap img2; //seekbar
		Canvas canvas = null;
		float myX = 10;
		boolean first = true;
		boolean checkdraw = true;
		float[] ncoordinate = new float[n];
		Paint paint = new Paint();
		private float tmp = 0;


		private SurfaceHolder holder = null;
		private boolean isRunning = false;
		private Thread thread = null;
		private int tmpi;

		public DrawingView(Context context) {
			super(context);

			holder= this.getHolder();
			holder.addCallback(this);
			img1 = BitmapFactory.decodeResource(getResources(), R.mipmap.seekbar_button);
			img2 = BitmapFactory.decodeResource(getResources(), R.mipmap.seekbar_1);

			DisplayMetrics metrics = new DisplayMetrics();
			WindowManager windowManager = (WindowManager) getActivity().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
			windowManager.getDefaultDisplay().getMetrics(metrics);

			img2 = Bitmap.createScaledBitmap(img2, metrics.widthPixels*711/1080 , metrics.heightPixels*21/1920 ,false);
			img1 = Bitmap.createScaledBitmap(img1, metrics.widthPixels*65/1080 , metrics.heightPixels*65/1920 ,false);
			seekbarw = img2.getWidth();
			seekbarh = img2.getHeight();
			buttonh = img1.getWidth();
			buttonw = img1.getHeight();

			paint.setAntiAlias(true);
			paint.setColor(Color.argb(255, 255, 255, 255));
		}


		@Override
		public boolean onTouchEvent(MotionEvent event) {
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_MOVE:
					Log.d("aa", "m" + event.getX() + "");
					myX = event.getX();
					break;
				case MotionEvent.ACTION_UP:
					Log.d("aa", "u" + event.getX() + "");
					touch=false;
					break;
				case MotionEvent.ACTION_DOWN:
					Log.d("aa", "d" + event.getX() + "");
					myX = event.getX();
					touch=true;
					checkdraw = true;
					break;
			}
			return true;// super.onTouchEvent(event);
		}


		@Override
		public void draw(Canvas canvas) {
			super.draw(canvas);
			this.canvas = canvas;
			paint.setTextSize(w/40);
			canvas.drawText("지금이좋다", (w-seekbarw)/2-w/17, (h - seekbarh)/48*13, paint);
			canvas.drawText("보통", w/2-seekbarw/4-w/40, (h - seekbarh)/48*13, paint);
			canvas.drawText("좋음", w/2-w/29, (h - seekbarh)/48*13, paint);
			canvas.drawText("매우좋음", w/2+seekbarw/4-w/31, (h - seekbarh)/48*13, paint);
			canvas.drawText("상관없음", (w+seekbarw)/2-w/26, (h - seekbarh)/48*13, paint);

			canvas.drawBitmap(img2, (w-seekbarw)/2, (h - seekbarh)/12*4, null);


			if(touch==true) {
				if ((w - seekbarw) / 2 > myX) {
					myX = (w - seekbarw) / 2;
				}
				else if ((w + seekbarw) / 2 < myX) {
					myX = (w + seekbarw) / 2;
				}

			}
			else {
				tmp = 999999;
				for(int i=0; i<n; i++){
					if(Math.abs(tmp-myX) > Math.abs(myX-ncoordinate[i])){
						tmp=ncoordinate[i];
						tmpi = i;
					}
				}

				Log.d("bb", tmp+"");
				if(tmp > myX+w/400){
					myX = myX + w/200;
				}else if(tmp<myX-w/400){
					myX = myX - w/200;
				}else{
					checkdraw = false;
					//int progress =(int) tmp/10;
					//bitmap.getWidth();
					//bitmap1 = Bitmap.createScaledBitmap(bitmap, progress , progress , false);
					getActivity().runOnUiThread (new Thread(new Runnable() {
						public void run() {
							switch (tmpi){
								case 0:
									select_display_save = current_display_save;
									if (Integer.parseInt(select_display_save) < 300) {
										diplay_image.setImageResource(R.mipmap.display_hd);
									} else if (Integer.parseInt(select_display_save) < 450) {
										diplay_image.setImageResource(R.mipmap.display_fhd);
									} else{
										diplay_image.setImageResource(R.mipmap.display_qhd);
									}
									break;

								case 1:
									select_display_save = "250";
									diplay_image.setImageResource(R.mipmap.display_hd);
									break;
								case 2:
									select_display_save = "400";
									diplay_image.setImageResource(R.mipmap.display_fhd);
									break;
								case 3:
									select_display_save = "550";
									diplay_image.setImageResource(R.mipmap.display_qhd);
									break;
								case 4:
									select_display_save = "10000";
									diplay_image.setImageResource(R.mipmap.no_image);
									break;
							}
						}

					}));



				}



			}

			canvas.drawBitmap(img1, myX - buttonw / 2, (h / 12 - buttonh / 12)*4, null);

			if(first==true){
				first = false;
				checkdraw = false;
			}


		}


		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			//this.holder = holder;
			isRunning = true;
			thread = new Thread(this);
			thread.start();
		}


		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		}


		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			isRunning = false;
			boolean retry = true;
			while (retry) {
				try {
					thread.join();
					retry = false;
				} catch (Exception e) {
				}
			}
			thread = null;
		}


		@Override
		public void run() {
			Canvas canvas = null;

			while (isRunning) {
				if(checkdraw==true) {
					canvas = holder.lockCanvas(null);

					if (canvas != null && first == true) {
						w = canvas.getWidth();
						h = canvas.getHeight();
						for (int i = 0; i < n; i++) {
							ncoordinate[i] = (w - seekbarw) / 2 * (n - i - 1) / (n - 1) + (w + seekbarw) / 2 * i / (n - 1);
						}

						switch (select_display_save){
							case "250":
								myX=ncoordinate[1];
								break;
							case "400":
								myX=ncoordinate[2];
								break;
							case "550":
								myX=ncoordinate[3];
								break;
							case "10000":
								myX=ncoordinate[4];
								break;
							default:
								myX=ncoordinate[0];
								break;
						}

						Log.d("cc", "onDraw");

					}
					synchronized (holder) {
						if (canvas != null) {
							draw(canvas);
						}
						try {
							Thread.sleep(10);
						} catch (Exception e) {
						}
					}
					if (canvas != null) {
						holder.unlockCanvasAndPost(canvas);
					}
				}
			}
		}

	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		((ChoiceActivity) activity).setOnKeyBackPressedListener(this);
	}

	@Override
	public void onBack(){

		ChoiceActivity activity = (ChoiceActivity) getActivity();
		footer.removeView(ov);
		SharedPreferences prefs = getActivity().getSharedPreferences("__setting",
				Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("SELECT_PPI",select_display_save + "");
		editor.commit();
		activity.pastFragment();

		//activity.setOnKeyBackPressedListener(null);

	}

}
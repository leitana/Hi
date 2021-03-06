package com.ly.hi.im.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.RelativeLayout;
import cn.bmob.im.BmobUserManager;

import com.ly.hi.R;
import com.ly.hi.im.im.bean.User;
import com.ly.hi.im.ui.listener.ShakeListener;
import com.ly.hi.im.view.HeaderLayout.onRightTextViewClickListener;
import com.ly.hi.lbs.biz.SendModel;
import com.ly.hi.lbs.biz.base.BaseModel;
import com.ly.hi.lbs.response.BaseResponseParams;
import com.ly.hi.lbs.response.DeletePoiRes;
import com.ly.hi.lbs.response.DetailTablesRes;

/**
 * 摇一摇activity
 * 
 * @author liuy
 * 
 */
public class ShakeActivity extends BaseActivity {

	ShakeListener mShakeListener = null;
	Vibrator mVibrator;
	private RelativeLayout mImgUp;
	private RelativeLayout mImgDn;
	private RelativeLayout mTitle;

	// private SlidingDrawer mDrawer;
	private Button mDrawerBtn;

	private SendModel mModel = null;// 发送请求
	private BmobUserManager mUserManager;
	private User mUser;

	private Handler mDetailTableHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BaseModel.MSG_SUC:
				BaseResponseParams<DetailTablesRes> response = (BaseResponseParams<DetailTablesRes>) msg.obj;
				if (BaseModel.REQ_SUC.equals(response.getStatus())) {
					if (!TextUtils.isEmpty(response.getObj().getPois().get(0).getId())) {
						String geoId = response.getObj().getPois().get(0).getId();
						deleteGeo(geoId);
					}
				}
				break;
			}

		}
	};
	
	private Handler mDeleteGeoHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BaseModel.MSG_SUC:
				BaseResponseParams<DeletePoiRes> response = (BaseResponseParams<DeletePoiRes>) msg.obj;
				if (BaseModel.REQ_SUC.equals(response.getStatus())) {
					ShowToast("delete");
				}
				break;
			}

		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shake);
		ShowToast("摇一摇后将自动共享位置信息!");
		mUserManager = BmobUserManager.getInstance(this);
		// drawerSet ();//设置 drawer监听 切换 按钮的方向
		// initTopBarForLeft("摇一摇");
		// mHeaderLayout.getRightImageButton().setEnabled(false);

		initTopBarForBoth("摇一摇", "清除位置", new onRightTextViewClickListener() {

			@Override
			public void onClick() {
				// TODO Auto-generated method stub
				// Intent intent = new Intent(ShakeActivity.this, NearPeopleActivity.class);
				// startAnimActivity(intent);
				getDetailTable();

			}
		});
		mHeaderLayout.getRightTextView().setEnabled(true);

		mVibrator = (Vibrator) getApplication().getSystemService(VIBRATOR_SERVICE);

		mImgUp = (RelativeLayout) findViewById(R.id.shakeImgUp);
		mImgDn = (RelativeLayout) findViewById(R.id.shakeImgDown);
		// mTitle = (RelativeLayout) findViewById(R.id.shake_title_bar);

		// mDrawer = (SlidingDrawer) findViewById(R.id.slidingDrawer1);
		// mDrawerBtn = (Button) findViewById(R.id.handle);
		/*
		 * mDrawer.setOnDrawerOpenListener(new OnDrawerOpenListener() { public void onDrawerOpened() {
		 * mDrawerBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.shake_report_dragger_down)); TranslateAnimation titleup = new
		 * TranslateAnimation(Animation.RELATIVE_TO_SELF,0f,Animation.RELATIVE_TO_SELF,0f,Animation.RELATIVE_TO_SELF,0f,Animation.RELATIVE_TO_SELF,-1.0f); titleup.setDuration(200);
		 * titleup.setFillAfter(true); mTitle.startAnimation(titleup); } });
		 */
		/* 设定SlidingDrawer被关闭的事件处理 */
		/*
		 * mDrawer.setOnDrawerCloseListener(new OnDrawerCloseListener() { public void onDrawerClosed() {
		 * mDrawerBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.shake_report_dragger_up)); TranslateAnimation titledn = new
		 * TranslateAnimation(Animation.RELATIVE_TO_SELF,0f,Animation.RELATIVE_TO_SELF,0f,Animation.RELATIVE_TO_SELF,-1.0f,Animation.RELATIVE_TO_SELF,0f); titledn.setDuration(200);
		 * titledn.setFillAfter(false); mTitle.startAnimation(titledn); } });
		 */
		mShakeListener = new ShakeListener(this);
		mShakeListener.setOnShakeListener(new ShakeListener.OnShakeListener() {
			public void onShake() {
				// Toast.makeText(getApplicationContext(), "抱歉，暂时没有找到在同一时刻摇一摇的人。\n再试一次吧！", Toast.LENGTH_SHORT).show();
				startAnim(); // 开始 摇一摇手掌动画
				mShakeListener.stop();
				startVibrato(); // 开始 震动
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						// Toast.makeText(getApplicationContext(), "抱歉，暂时没有找到\n在同一时刻摇一摇的人。\n再试一次吧！", 500).setGravity(Gravity.CENTER,0,0).show();
						// Toast mtoast;
						// mtoast = Toast.makeText(getApplicationContext(),
						// "抱歉，暂时没有找到\n在同一时刻摇一摇的人。\n再试一次吧！", 10);
						// //mtoast.setGravity(Gravity.CENTER, 0, 0);
						// mtoast.show();
						Intent intent = new Intent(ShakeActivity.this, NearLocationActivity.class);
						startActivity(intent);
						mVibrator.cancel();
						mShakeListener.start();
						finish();
					}
				}, 2000);
			}
		});
	}

	/**
	 * 删除数据
	 */
	protected void deleteGeo(String id) {
		mModel = new SendModel(mDeleteGeoHandler, getApplicationContext(), getTag(), getRequestQueue());
		mModel.deletePoi(id);
	}

	/**
	 * 获取列表详细
	 */
	protected void getDetailTable() {
		mModel = new SendModel(mDetailTableHandler, getApplicationContext(), getTag(), getRequestQueue());

		mUser = mUserManager.getCurrentUser(User.class);
		mModel.detailGeotable(mUser.getUsername());
	}

	public void startAnim() { // 定义摇一摇动画动画
		AnimationSet animup = new AnimationSet(true);
		TranslateAnimation mytranslateanimup0 = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
				Animation.RELATIVE_TO_SELF, -0.5f);
		mytranslateanimup0.setDuration(1000);
		TranslateAnimation mytranslateanimup1 = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
				Animation.RELATIVE_TO_SELF, +0.5f);
		mytranslateanimup1.setDuration(1000);
		mytranslateanimup1.setStartOffset(1000);
		animup.addAnimation(mytranslateanimup0);
		animup.addAnimation(mytranslateanimup1);
		mImgUp.startAnimation(animup);

		AnimationSet animdn = new AnimationSet(true);
		TranslateAnimation mytranslateanimdn0 = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
				Animation.RELATIVE_TO_SELF, +0.5f);
		mytranslateanimdn0.setDuration(1000);
		TranslateAnimation mytranslateanimdn1 = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
				Animation.RELATIVE_TO_SELF, -0.5f);
		mytranslateanimdn1.setDuration(1000);
		mytranslateanimdn1.setStartOffset(1000);
		animdn.addAnimation(mytranslateanimdn0);
		animdn.addAnimation(mytranslateanimdn1);
		mImgDn.startAnimation(animdn);
	}

	public void startVibrato() { // 定义震动
		mVibrator.vibrate(new long[] { 500, 200, 500, 200 }, -1); // 第一个｛｝里面是节奏数组， 第二个参数是重复次数，-1为不重复，非-1俄日从pattern的指定下标开始重复
	}

	public void shake_activity_back(View v) { // 标题栏 返回按钮
		this.finish();
	}

	// public void linshi(View v) { //标题栏
	// startAnim();
	// }
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mShakeListener != null) {
			mShakeListener.stop();
		}
	}
}
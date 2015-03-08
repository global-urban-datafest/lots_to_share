package com.baidu.lbs.duanzu;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.lbs.duanzu.R;
import com.baidu.location.BDLocation;
import com.baidu.mapapi.cloud.CloudManager;
import com.baidu.mapapi.cloud.CloudPoiInfo;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.DotOptions;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.model.LatLngBounds.Builder;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.navisdk.BNaviPoint;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.BaiduNaviManager.OnStartNavigationListener;
import com.baidu.navisdk.comapi.routeplan.RoutePlanParams.NE_RoutePlan_Mode;

/**
 * 小猪短租tab地图类
 * 
 * @author Lu.Jian
 * 
 */
public class LBSMapActivity extends Activity {
	private Context context;

	public BaiduMap mBaiduMap = null;
	public MapView mMapView = null;
	public final View mapViewitem = null;
	ViewHolder holder=null;
	public static final String strKey = "63418012748CD126610D926A0546374D0BFC86D5";
	OnMarkerClickListener onMarkerClickListener;
	/* class ViewHolder */
	private class ViewHolder {
		TextView addr;
		TextView name;
		TextView distance;
		TextView price;
		TextView count;
		ImageView icon;
		RelativeLayout item;
		ContentModel content;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.map);
		mMapView = (MapView) findViewById(R.id.bmapView);

		holder = new ViewHolder();
		holder.name = (TextView) findViewById(R.id.name);
		holder.addr = (TextView) findViewById(R.id.addr);
		holder.distance = (TextView) findViewById(R.id.distance);
		holder.price = (TextView) findViewById(R.id.price);
		holder.icon = (ImageView) findViewById(R.id.icon);
		holder.count = (TextView) findViewById(R.id.count);
		holder.item = (RelativeLayout) findViewById(R.id.item);
		holder.item.setVisibility(View.INVISIBLE);
		holder.content=null;
		
		OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				
				DemoApplication app = (DemoApplication) getApplication();
				BDLocation location = app.currlocation;
				BNaviPoint startPoint = new BNaviPoint(location.getLongitude(), location.getLatitude(), "我的位置",
						BNaviPoint.CoordinateType.BD09_MC);
				BNaviPoint endPoint = new BNaviPoint(holder.content.getLongitude(), holder.content.getLatitude(), 
						holder.content.getName(),
						BNaviPoint.CoordinateType.BD09_MC);
				BaiduNaviManager.getInstance().launchNavigator(LBSMapActivity.this,
						startPoint, // 起点（可指定坐标系）
						endPoint, // 终点（可指定坐标系）
						NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_TIME,// 算路方式
						true, // 真实导航
						BaiduNaviManager.STRATEGY_FORCE_ONLINE_PRIORITY,
						new OnStartNavigationListener() {
							@Override
							public void onJumpToNavigator(Bundle configParams) {
								Intent intent = new Intent(LBSMapActivity.this, BNavigatorActivity.class);
		                        intent.putExtras(configParams);
		                        startActivity(intent);
							}
							@Override
							public void onJumpToDownloader() {
								
							}

						} // 跳转监听
					);
				
			}};
		holder.item.setOnClickListener(onClickListener);
		mBaiduMap = mMapView.getMap();

		LatLng latLng = new LatLng(39.915, 116.404);
		float zoom = 13f;

		MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(latLng, zoom);
		mBaiduMap.animateMapStatus(u);
		mBaiduMap.setMyLocationEnabled(true);

		DemoApplication.getInstance().setMapActivity(this);

		onMarkerClickListener = new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {
				DemoApplication app = (DemoApplication) getApplication();
				List<ContentModel> list = app.getList();
				for (ContentModel content : list) {
					LatLng ll;
					ll = new LatLng(content.getLatitude(),
							content.getLongitude());
					// same marker
					double dis = DistanceUtil.getDistance(marker.getPosition(), ll);
					Toast.makeText(context, String.valueOf(dis), Toast.LENGTH_SHORT).show();
					if (dis < 1) {
						setView(content);
						holder.item.setVisibility(View.VISIBLE);
						break;
					}
				}
				return false;
			}

			private void setView(ContentModel content) {
				// holder.index.setText((String)
				// items.get(position).getIndex());
				holder.content=content;
				holder.addr
						.setText((String) content.getAddr() + "");
				holder.name
						.setText((String) content.getName() + "");
				holder.distance.setText((String) content.getDistance() + "");
				holder.price.setText(String.format("%.2f",content.getPrice()));
				holder.count.setText(String.format("%d/%d",content
						.getOccupCount(), content.getCount()));
				// holder.icon.setImageBitmap(getBitmapFromUrl((String)
				// items.get(position).getImageurl()));
			}
		
		};
		mBaiduMap.setOnMarkerClickListener(onMarkerClickListener);
	}

	@Override
	// 建议在您app的退出之前调用mapadpi的destroy()函数，避免重复初始化带来的时间消耗
	public void onDestroy() {
		// TODO Auto-generated method stub
		mMapView.onDestroy();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		removeAllMarker();
		holder.item.setVisibility(View.INVISIBLE);
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		addAllMarker();
		super.onResume();
	}

	/**
	 * 删除所有标记
	 */
	public void removeAllMarker() {
		mBaiduMap.clear();
		holder.item.setVisibility(View.INVISIBLE);
	}

	/**
	 * 添加所有标记
	 */
	public void addAllMarker() {
		DemoApplication app = (DemoApplication) getApplication();
		List<ContentModel> list = app.getList();
		mBaiduMap.clear();

		BitmapDescriptor bd = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_marka);
		LatLng ll;
		LatLngBounds.Builder builder = new Builder();
		for (ContentModel content : list) {
			ll = new LatLng(content.getLatitude(), content.getLongitude());
			OverlayOptions oo;
			oo = new MarkerOptions().icon(bd).position(ll);
			mBaiduMap.addOverlay(oo);
			String s = String.format("%d/%d", content.getOccupCount(),
					content.getCount());
			oo = new DotOptions()
					.center(ll)
					.color(0x5500ffff)
					.radius(30);
			mBaiduMap.addOverlay(oo);
			builder.include(ll);
		}
		LatLngBounds bounds = builder.build();
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLngBounds(bounds);
		mBaiduMap.animateMapStatus(u);

		// 如果定位成功，添加当前坐标点
		if (app.currlocation != null) {
			mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
					LocationMode.NORMAL, true, null));
			BDLocation location = app.currlocation;
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			ll = new LatLng(location.getLatitude(), location.getLongitude());
			u = MapStatusUpdateFactory.newLatLng(ll);
			mBaiduMap.animateMapStatus(u);
		}

		// 北京的中心，无定位时的地图中心

		if (app.currlocation == null) {
			LatLng latLng = new LatLng(39.915, 116.404);
			u = MapStatusUpdateFactory.newLatLng(latLng);
			mBaiduMap.animateMapStatus(u);
		} else if (list != null && list.size() >= 1) {
			ContentModel c = (ContentModel) list.get(0);
			LatLng latLng = new LatLng(c.getLatitude(), c.getLongitude());
			u = MapStatusUpdateFactory.newLatLng(latLng);
			mBaiduMap.animateMapStatus(u);
		}
	}
}

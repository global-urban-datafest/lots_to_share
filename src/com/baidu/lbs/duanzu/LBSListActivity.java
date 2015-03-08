package com.baidu.lbs.duanzu;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.navisdk.BNaviPoint;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.BaiduNaviManager.OnStartNavigationListener;
import com.baidu.navisdk.comapi.routeplan.RoutePlanParams.NE_RoutePlan_Mode;

/**
 * 
 * 
 * @author Wei, Vincent W., Xiong, Shun  
 * 
 */
public class LBSListActivity extends ListActivity implements OnScrollListener {

	private ContentAdapter adapter;
	private List<ContentModel> list = new ArrayList<ContentModel>();
	public View loadMoreView;
	public ProgressBar progressBar;
	private int visibleLastIndex = 0; // ���Ŀ���������
	public int totalItem = -1;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		loadMoreView = getLayoutInflater().inflate(R.layout.list_item_footer,
				null);
		progressBar = (ProgressBar) loadMoreView.findViewById(R.id.progressBar);

		loadMoreView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loadMoreData();
				progressBar.setVisibility(View.VISIBLE);
			}
		});

		final ListView listView = getListView();
		listView.setItemsCanFocus(false);
		// listView.addFooterView(loadMoreView);
		listView.setOnScrollListener(this);

		adapter = new ContentAdapter(LBSListActivity.this, list);
		setListAdapter(adapter);

		DemoApplication app = (DemoApplication) getApplication();
		app.setList(list);
		app.setAdapter(adapter);
		app.setListActivity(this);

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

	}

	/**
	 * �б�item����ص�
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		DemoApplication app = (DemoApplication) getApplication();
		BDLocation location = app.currlocation;
		BNaviPoint startPoint = new BNaviPoint(location.getLongitude(), location.getLatitude(), "�ҵ�λ��",
				BNaviPoint.CoordinateType.BD09_MC);
		BNaviPoint endPoint = new BNaviPoint(list.get(position).getLongitude(), list.get(position).getLatitude(), 
				list.get(position).getName(),
				BNaviPoint.CoordinateType.BD09_MC);
		BaiduNaviManager.getInstance().launchNavigator(this,
				startPoint, // ��㣨��ָ������ϵ��
				endPoint, // �յ㣨��ָ������ϵ��
				NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_TIME,// ��·��ʽ
				true, // ��ʵ����
				BaiduNaviManager.STRATEGY_FORCE_ONLINE_PRIORITY,
				new OnStartNavigationListener() {
					@Override
					public void onJumpToNavigator(Bundle configParams) {
						Intent intent = new Intent(LBSListActivity.this, BNavigatorActivity.class);
                        intent.putExtras(configParams);
                        startActivity(intent);
					}
					@Override
					public void onJumpToDownloader() {
						
					}

				} // ��ת����
			);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		int itemsLastIndex = adapter.getCount() - 1; // ���ݼ����һ�������
		int lastIndex = itemsLastIndex + 1;
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
				&& visibleLastIndex == lastIndex) {
			// ������Զ�����,��������������첽�������ݵĴ���
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		visibleLastIndex = firstVisibleItem + visibleItemCount - 1;
		if (totalItemCount == totalItem) {
			getListView().removeFooterView(loadMoreView);
		}
	}

	public class ContentAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private List<ContentModel> items;

		private ViewHolder holder;

		public ContentAdapter(Context context, List<ContentModel> list) {
			mInflater = LayoutInflater.from(context);

			items = list;
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Object getItem(int position) {
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.contentitem, null);
				holder = new ViewHolder();

				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.addr = (TextView) convertView.findViewById(R.id.addr);
				holder.distance = (TextView) convertView
						.findViewById(R.id.distance);
				holder.price = (TextView) convertView.findViewById(R.id.price);
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
				holder.count = (TextView) convertView.findViewById(R.id.count);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// holder.index.setText((String) items.get(position).getIndex());
			holder.addr.setText((String) items.get(position).getAddr() + "");
			holder.name.setText((String) items.get(position).getName() + "");
			holder.distance.setText((String) items.get(position).getDistance()
					+ "");
			holder.price.setText(String.format("%.2f", items.get(position)
					.getPrice()));
			holder.count.setText(String.format("%d/%d", items.get(position)
					.getOccupCount(), items.get(position).getCount()));
			// holder.icon.setImageBitmap(getBitmapFromUrl((String)
			// items.get(position).getImageurl()));

			return convertView;
		}

		/* class ViewHolder */
		private class ViewHolder {
			TextView addr;
			TextView name;
			TextView distance;
			TextView price;
			TextView count;
			ImageView icon;
		}
	}

	private Bitmap getBitmapFromUrl(String imgUrl) {
		URL url;
		Bitmap bitmap = null;
		try {
			url = new URL(imgUrl);
			InputStream is = url.openConnection().getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			bitmap = BitmapFactory.decodeStream(bis);
			bis.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	/**
	 * ���ظ�������
	 */
	private void loadMoreData() {
		HashMap<String, String> filterParams = DemoApplication.getInstance()
				.getFilterParams();
		filterParams.put("page_index", (list.size() / 10 + 1) + "");
		// search type Ϊ -1�������ֵ�ǰ����������
		LBSCloudSearch.request(-1, filterParams, DemoApplication.getInstance()
				.getHandler(), DemoApplication.networkType);
	}

}

package com.baidu.lbs.duanzu;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.baidu.lbs.duanzu.LBSListActivity.ContentAdapter;
import com.baidu.location.BDLocation;
import com.baidu.navisdk.BNaviEngineManager.NaviEngineInitListener;
import com.baidu.navisdk.BaiduNaviManager;

/**
 * 小猪短租首页tab类
 * 
 * @author Lu.Jian
 * 
 */
public class MainActivity extends TabActivity {

	private Context context;

	public static final int MSG_NET_TIMEOUT = 100;
	public static final int MSG_NET_STATUS_ERROR = 200;
	public static final int MSG_NET_SUCC = 1;
	
	private static String ak = "sz86YlIdUacCpUD8uG2gAnzt";
	private static String geotable_id = "95675";
	private boolean initSearchFlag = false;

	private RelativeLayout progress;

	/*
	 * 处理网络请求
	 */
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			progress.setVisibility(View.INVISIBLE);
			switch (msg.what) {
			case MSG_NET_TIMEOUT:
				break;
			case MSG_NET_STATUS_ERROR:
				break;
			case MSG_NET_SUCC:
				initSearchFlag = true;
				String result = msg.obj.toString();
				try {
					JSONObject json = new JSONObject(result);
					parser(json);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;

			}
		}
	};
	private boolean mIsEngineInitSuccess = false;
	private NaviEngineInitListener mNaviEngineInitListener = new NaviEngineInitListener() {
		public void engineInitSuccess() {
			// 导航初始化是异步的，需要一小段时间，以这个标志来识别引擎是否初始化成功，为true时候才能发起导航
			mIsEngineInitSuccess = true;
		}

		public void engineInitStart() {
		}

		public void engineInitFail() {
		}
	};

	private String getSdcardDir() {
		if (Environment.getExternalStorageState().equalsIgnoreCase(
				Environment.MEDIA_MOUNTED)) {
			return Environment.getExternalStorageDirectory().toString();
		}
		return null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;

		setContentView(R.layout.activity_main);
		progress = (RelativeLayout) findViewById(R.id.progress);

		// 初始化tab页（列表，附近）
		initTabHost();

		// 初始化筛选条件（区域，价格，类型）
		initSpinner();

		// 初始化切换按钮（筛选，附近）
		initToggleButton();

		// 初始化附近范围（1000米，3000米，5000米）
		initRadioGroup();
		// 初始化添加车库按钮
		initAddpoiButton();
		// 发起搜索请求
		search();

		DemoApplication.getInstance().setHandler(mHandler);
		BaiduNaviManager.getInstance().initEngine(this, getSdcardDir(),
				mNaviEngineInitListener, null);
	}
	EditText ed_name = null;
	Spinner s_scale = null;
	String currlocation = "";
	private void initAddpoiButton() {
		Button button = (Button) findViewById(R.id.btn_add_poi);

		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);   
			     LayoutInflater factory = LayoutInflater.from(MainActivity.this);  
			     final View textEntryView = factory.inflate(R.layout.add_poi_dialog, null);
			     
			     ed_name =  (EditText) textEntryView.findViewById(R.id.etName);
			     DemoApplication app = (DemoApplication) getApplication();
				 BDLocation location = app.currlocation;
				 currlocation = location.getAddrStr();
				 ed_name.setText(location.getAddrStr());
				 
				 s_scale = (Spinner) textEntryView.findViewById(R.id.et_scale);

			     ArrayAdapter adapter = ArrayAdapter.createFromResource(
			    		 MainActivity.this, R.array.scale, android.R.layout.simple_spinner_item);

			     adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			     s_scale.setAdapter(adapter);
			     
		         builder.setIcon(R.drawable.marker_red_1);  
		         builder.setTitle("当前位置上报新车库");  
		         builder.setView(textEntryView);  
		         builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {  
		             public void onClick(DialogInterface dialog, int whichButton) {  
		            	String url = "http://api.map.baidu.com/geodata/v3/poi/create";
		         		//ak=sz86YlIdUacCpUD8uG2gAnzt&geotable_id=95675&id=685079141&coord_type=1&occupy_count=0
						DemoApplication app = (DemoApplication) getApplication();
						BDLocation location = app.currlocation;
						
						int scale = s_scale.getSelectedItemPosition();
						if(scale==0)scale=2;
						int radis = scale*100;
						int count = radis*radis;
						
						Map<String,Object> params = new HashMap<String,Object>();
						params.put("ak",ak);
						params.put("geotable_id",geotable_id);
						
						params.put("latitude",location.getLatitude());
						params.put("longitude",location.getLongitude());
						params.put("address",currlocation);
						params.put("title",ed_name.getText());
						params.put("owner","test");
						params.put("count",count);
						params.put("occup_count",count);
						params.put("price",0);
						params.put("level",scale);
						params.put("radis",radis);
						params.put("coord_type",3);

		         		String result = RestClient.post(url,params);
		         		
		         		Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
		         		search();
		             //EditText userName = (EditText) textEntryView.findViewById(R.id.etUserName);  
		             //EditText password = (EditText) textEntryView.findViewById(R.id.etPassWord);  
		             //showDialog("姓名 ："  + userName.getText().toString()  + "密码：" + password.getText().toString() );  
		             }  
		         });  
			       AlertDialog dialog = builder.create(); 
			       dialog.setCanceledOnTouchOutside(true);
			       dialog.show();
			}
		});
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	/*
	 * 添加对back按钮的处理，点击提示退出 (non-Javadoc)
	 * 
	 * @see android.app.Activity#dispatchKeyEvent(android.view.KeyEvent)
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& event.getAction() != 1) {
			exit();
			return true;
		}

		return super.dispatchKeyEvent(event);
	}

	private void initRadioGroup() {
		RadioGroup filter2 = (RadioGroup) findViewById(R.id.filter2);

		// 附近搜索 选择不同距离的点击事件
		filter2.setOnCheckedChangeListener(new android.widget.RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(android.widget.RadioGroup arg0,
					int arg1) {
				search(LBSCloudSearch.SEARCH_TYPE_NEARBY);
			}
		});
	}

	private void initToggleButton() {
		ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggleButton1);
		// 添加toggle button点击事件
		toggleButton
				.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(
							android.widget.CompoundButton arg0, boolean arg1) {
						TextView textView1 = (TextView) findViewById(R.id.toggle_text1);
						TextView textView2 = (TextView) findViewById(R.id.toggle_text2);
						HorizontalScrollView filter1 = (HorizontalScrollView) findViewById(R.id.filter1);
						RadioGroup filter2 = (RadioGroup) findViewById(R.id.filter2);

						if (arg1) {
							// 显示附近
							textView1.setVisibility(View.INVISIBLE);
							filter1.setVisibility(View.INVISIBLE);

							textView2.setVisibility(View.VISIBLE);
							filter2.setVisibility(View.VISIBLE);
							search(LBSCloudSearch.SEARCH_TYPE_NEARBY);
						} else {
							// 显示筛选
							textView1.setVisibility(View.VISIBLE);
							filter1.setVisibility(View.VISIBLE);

							textView2.setVisibility(View.INVISIBLE);
							filter2.setVisibility(View.INVISIBLE);
							search();
						}

					}
				});

	}

	private void initTabHost() {
		final TabHost tabHost = getTabHost();

		// 添加列表tab和地图tab
		tabHost.addTab(tabHost.newTabSpec("tab1")
				.setIndicator(getString(R.string.tab_1_text))
				.setContent(new Intent(this, LBSListActivity.class)));
		tabHost.addTab(tabHost.newTabSpec("tab2")
				.setIndicator(getString(R.string.tab_2_text))
				.setContent(new Intent(this, LBSMapActivity.class)));

		TabWidget tabWidget = tabHost.getTabWidget();

		// 将tab的图文组合改为文字显示并调整tab高度
		for (int i = 0; i < tabWidget.getChildCount(); i++) {
			View child = tabWidget.getChildAt(i);

			final TextView tv = (TextView) child
					.findViewById(android.R.id.title);

			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tv
					.getLayoutParams();
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0); // 取消文字底边对齐
			params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE); // 设置文字居中对齐

			child.getLayoutParams().height = 120; // hard code
			child.getLayoutParams().width = 500;
		}

	}

	private void initSpinner() {

		final Spinner s1 = (Spinner) findViewById(R.id.spinner1);
		final Spinner s2 = (Spinner) findViewById(R.id.spinner2);
		final Spinner s3 = (Spinner) findViewById(R.id.spinner3);

		// 设置区域过滤下拉框
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.region, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s1.setAdapter(adapter);
		s1.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if (initSearchFlag) {
					search();
				}
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		// 设置价格过滤下拉框
		adapter = ArrayAdapter.createFromResource(this, R.array.price,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s2.setAdapter(adapter);
		s2.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if (initSearchFlag) {
					search();
				}
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		// 设置短租类型下拉框
		adapter = ArrayAdapter.createFromResource(this, R.array.category,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s3.setAdapter(adapter);
		s3.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if (initSearchFlag) {
					search();
				}
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}

	/*
	 * 云检索发起
	 */
	private void search() {
		search(LBSCloudSearch.SEARCH_TYPE_LOCAL);
	}

	/*
	 * 根据搜索类型发起检索
	 */
	private void search(int searchType) {
		progress.setVisibility(View.VISIBLE);
		DemoApplication app = DemoApplication.getInstance();
		app.getList().clear(); // 搜索钱清空列表
		app.getListActivity().loadMoreView.setVisibility(View.INVISIBLE);
		if (app.getListActivity().getListView().getFooterViewsCount() == 0) {
			// 点击查看更多按钮添加
			app.getListActivity().getListView()
					.addFooterView(app.getListActivity().loadMoreView);
		}

		app.getListActivity().getListView().setAdapter(app.getAdapter());

		// 云检索发起
		LBSCloudSearch.request(searchType, getRequestParams(), mHandler,
				DemoApplication.networkType);
	}

	/*
	 * 获取云检索参数
	 */
	private HashMap<String, String> getRequestParams() {
		HashMap<String, String> map = new HashMap<String, String>();

		Spinner s1 = (Spinner) findViewById(R.id.spinner1);
		Spinner s2 = (Spinner) findViewById(R.id.spinner2);
		Spinner s3 = (Spinner) findViewById(R.id.spinner3);

		ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggleButton1);

		try {
			map.put("region", URLEncoder.encode("杭州", "utf-8"));

			String filter = "";

			if (toggleButton.isChecked()) {
				// 附件，周边搜索
				RadioGroup filter2 = (RadioGroup) findViewById(R.id.filter2);
				RadioButton rb = (RadioButton) findViewById(filter2
						.getCheckedRadioButtonId());
				String radius = rb.getText().toString();
				radius = radius.substring(0, radius.length() - 1);
				map.put("radius", radius);

				DemoApplication app = DemoApplication.getInstance();
				if (app.currlocation != null) {
					map.put("location", app.currlocation.getLongitude() + ","
							+ app.currlocation.getLatitude());
				} else {
					// 无定位数据默认北京中心
					double cLat = 30.909230;
					double cLon = 120.397428;
					map.put("location", cLat + "," + cLon);
				}
			} else {
				// 筛选，本地搜索
				if (s1.getSelectedItemPosition() > 0) {
					String s1String = s1.getSelectedItem().toString();
					map.put("q", URLEncoder.encode(s1String, "utf-8"));
				}

				if (s2.getSelectedItemPosition() > 0) {
					String s2String = s2.getSelectedItem().toString();
					String[] priceArray = context.getResources()
							.getStringArray(R.array.price);
					for (int i = 0; i < priceArray.length; i++) {
						if (s2String.equals(priceArray[i])) {
							if (i == priceArray.length - 1) {
								filter = filter
										+ URLEncoder.encode("|", "utf-8")
										+ "dayprice:" + (i - 1) * 50
										+ URLEncoder.encode(",", "utf-8")
										+ 10000;
							} else {
								filter = filter
										+ URLEncoder.encode("|", "utf-8")
										+ "dayprice:" + (i - 1) * 50
										+ URLEncoder.encode(",", "utf-8") + i
										* 50;
							}

							break;
						}
					}
				}

				if (s3.getSelectedItemPosition() > 0) {
					String s3String = s3.getSelectedItem().toString();
					String[] categoryArray = context.getResources()
							.getStringArray(R.array.category);
					for (int i = 0; i < categoryArray.length; i++) {
						if (s3String.equals(categoryArray[i])) {
							filter = filter + URLEncoder.encode("|", "utf-8")
									+ "leasetype:" + i
									+ URLEncoder.encode(",", "utf-8") + i;
							break;
						}
					}
				}
			}

			map.put("filter", filter);
			Log.i("filterParams", Integer.toString(map.size()));
			Iterator iter = map.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				String key = entry.getKey().toString();
				String value = entry.getValue().toString();
				Log.i("filterParams", key + "," + value);
			}

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		DemoApplication.getInstance().setFilterParams(map);

		return map;
	}

	/*
	 * 解析返回数据
	 */
	private void parser(JSONObject json) {
		DemoApplication app = (DemoApplication) getApplication();
		List<ContentModel> list = app.getList();

		try {
			app.getListActivity().totalItem = json.getInt("total");

			JSONArray jsonArray = json.getJSONArray("contents");
			if (jsonArray != null && jsonArray.length() <= 0) {
				ContentModel contentt = new ContentModel();
				contentt.setName("title");
				contentt.setAddr("address");

				contentt.setDistance("distance" + "米");

				double la = 120.0;
				double lo = 30.0;
				contentt.setLatitude(la);
				contentt.setLongitude(lo);

				float r[] = new float[1];

				if (app.currlocation != null) {
					Location.distanceBetween(app.currlocation.getLatitude(),
							app.currlocation.getLongitude(), la, lo, r);
				}
				contentt.setDistance((int) r[0] + "米");

				// JSONObject jsonObject3 = jsonObject2.getJSONObject("ext");
				// content.setPrice(jsonObject3.getString("dayprice"));
				// content.setImageurl(jsonObject3.getString("mainimage"));
				// content.setWebUrl(jsonObject3.getString("roomurl"));//
				contentt.setPrice(9);
				contentt.setCount(10);
				contentt.setOwner("owner");
				contentt.setOccupCount(2);
				contentt.setIconStyleID("icon_style_id");

				list.add(contentt);
				Toast.makeText(context, "没有符合要求的数据", Toast.LENGTH_SHORT).show();
			} else {
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject2 = (JSONObject) jsonArray.opt(i);
					ContentModel content = new ContentModel();
					content.setName(jsonObject2.getString("title"));
					content.setAddr(jsonObject2.getString("address"));

					content.setDistance(jsonObject2.getString("distance") + "米");

					// double latitude = jsonObject2.getDouble("latitude");
					// double longitude = jsonObject2.getDouble("longitude");
					JSONArray locArray = jsonObject2.getJSONArray("location");
					double latitude = locArray.getDouble(1);
					double longitude = locArray.getDouble(0);
					content.setLatitude(latitude);
					content.setLongitude(longitude);

					float results[] = new float[1];

					if (app.currlocation != null) {
						Location.distanceBetween(
								app.currlocation.getLatitude(),
								app.currlocation.getLongitude(), latitude,
								longitude, results);
					}
					content.setDistance((int) results[0] + "米");

					// JSONObject jsonObject3 =
					// jsonObject2.getJSONObject("ext");
					// content.setPrice(jsonObject3.getString("dayprice"));
					// content.setImageurl(jsonObject3.getString("mainimage"));
					// content.setWebUrl(jsonObject3.getString("roomurl"));//
					content.setPrice(jsonObject2.getDouble("price"));
					content.setCount(jsonObject2.getInt("count"));
					content.setOwner(jsonObject2.getString("owner"));
					content.setOccupCount(jsonObject2.getInt("occup_count"));
					content.setIconStyleID(jsonObject2
							.getString("icon_style_id"));

					list.add(content);

				}

			}
			if (list.size() < 10) {
				app.getListActivity().getListView()
						.removeFooterView(app.getListActivity().loadMoreView);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ContentAdapter adapter = ((DemoApplication) getApplication())
				.getAdapter();
		if (adapter != null) {
			adapter.notifyDataSetChanged();
			app.getListActivity().loadMoreView.setVisibility(View.VISIBLE);
			app.getListActivity().progressBar.setVisibility(View.INVISIBLE);
		}
		if (app.getMapActivity() != null) {
			app.getMapActivity().removeAllMarker();
			app.getMapActivity().addAllMarker();
		}
	}

	/*
	 * 退出应用程序
	 */
	private void exit() {
		new AlertDialog.Builder(MainActivity.this)
				.setMessage(R.string.exit_confirm)
				.setPositiveButton(R.string.button_ok,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int whichButton) {
								finish();
								android.os.Process
										.killProcess(android.os.Process.myPid());
							}
						}).setNegativeButton(R.string.button_cancel, null)
				.show();
	}

}


package com.baidu.lbs.duanzu;
import java.util.*;
import java.io.*;
import java.net.*;
/**
 * 
 * 
 * @author Shun  
 * 
 */
public class RestClient
{
	public static String get(String url, Map<String,Object> params) throws Exception {
		URL u = null;
		HttpURLConnection con = null;
		// 构建请求参数
		StringBuffer sb = new StringBuffer();
		if (params != null)
		{
			for (Map.Entry<String,Object> e : params.entrySet())
			{
				sb.append(e.getKey());
				sb.append("=");
				sb.append(URLEncoder.encode((e.getValue() == null ? "" : e.getValue().toString()),"UTF-8"));
				sb.append("&");
			}
			sb.substring(0, sb.length() - 1);
		}
		url += "?" + sb.toString();
		System.out.println("send_url:" + url);
		// 尝试发送请求
		u = new URL(url);
		con = (HttpURLConnection)u.openConnection();
		con.setRequestMethod("GET");

		StringBuffer buffer = new StringBuffer();
		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			String temp;
			while ((temp = br.readLine()) != null)
			{
				buffer.append(temp);
				buffer.append("\n");
			}
			System.out.println(buffer.toString());
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		return buffer.toString();
	}
	
	public static String post(String url, Map<String,Object> params) {
		URL u = null;
		HttpURLConnection con = null;
		// 构建请求参数
		StringBuffer sb = new StringBuffer();
		if (params != null)
			{
				for (Map.Entry<String,Object> e : params.entrySet())
					{
						sb.append(e.getKey());
						sb.append("=");
						sb.append(e.getValue() == null ? "" : e.getValue().toString());
						sb.append("&");
					}
				sb.substring(0, sb.length() - 1);
			}
		System.out.println("send_url:" + url);
		System.out.println("send_data:" + sb.toString());
		// 尝试发送请求
		try
		{
			u = new URL(url);
			con = (HttpURLConnection) u.openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setUseCaches(false);
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
			osw.write(sb.toString());
			osw.flush();
			osw.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			if (con != null)
				{
					con.disconnect();
				}
		}

		StringBuffer buffer = new StringBuffer();
		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			String temp;
			while ((temp = br.readLine()) != null)
			{
				buffer.append(temp);
				buffer.append("\n");
			}
			System.out.println(buffer.toString());
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		return buffer.toString();
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("更新车位信息");
		String url = "http://api.map.baidu.com/geodata/v3/poi/update";
		//ak=sz86YlIdUacCpUD8uG2gAnzt&geotable_id=95675&id=685079141&coord_type=1&occupy_count=0
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("ak","sz86YlIdUacCpUD8uG2gAnzt");
		params.put("geotable_id",95675);
		params.put("id",685079141);
		params.put("coord_type",1);
		params.put("occupy_count",5);
		
		String result = RestClient.post(url,params);
		System.out.println(result);
		
		System.out.println("查询附近停车位");
		url = "http://api.map.baidu.com/geosearch/v3/nearby";
		params = new HashMap<String,Object>();
		//ak=sz86YlIdUacCpUD8uG2gAnzt&geotable_id=95675&location=120.119321,30.322477
		params.put("ak","sz86YlIdUacCpUD8uG2gAnzt");
		params.put("geotable_id",95675);
		params.put("location","120.119321,30.322477");
		
		result = RestClient.get(url, params);
		System.out.println(result);
		
		int count = 1;
		int radis = 1;
		int scale = 1;
		System.out.println("增加附近停车位");
		url = "http://api.map.baidu.com/geosearch/v3/create";
		params = new HashMap<String,Object>();
		//ak=sz86YlIdUacCpUD8uG2gAnzt&geotable_id=95675&location=120.119321,30.322477
		params.put("ak","sz86YlIdUacCpUD8uG2gAnzt");
		params.put("geotable_id",95675);
		params.put("location",String.format("%f,%f",120.119321,30.322477));
		params.put("title","ed_name.getText()");
		params.put("owner","test");
		params.put("count",count);
		params.put("occupy_count",count);
		params.put("price",0);
		params.put("level",scale);
		params.put("radis",radis);
		
		result = RestClient.post(url, params);
		System.out.println(result);
	}
}
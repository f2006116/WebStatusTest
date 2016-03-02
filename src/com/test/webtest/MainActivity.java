package com.test.webtest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Entity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	public static final int SHOW_RESPONSE = 0;
	public static final int Disconnect = 1;
	// 设置网络连接状态标志
	private int mResponseStatus = 0;
	private Button mButtonSendRequest;
	private TextView mTextViewResponseText;

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SHOW_RESPONSE:
				String response = (String) msg.obj;
				// 在这里进行UI操作，将结果显示在界面上
				mTextViewResponseText.setText(response);
				break;
			case Disconnect:
				Toast.makeText(MainActivity.this, "Disconnect!",
						Toast.LENGTH_LONG).show();
				break;
			// default:
			// break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mButtonSendRequest = (Button) findViewById(R.id.button_sendRquest);
		mButtonSendRequest.setOnClickListener(this);
		mTextViewResponseText = (TextView) findViewById(R.id.TextView_responseText);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		// if (R.id.button_sendRquest == v.getId()) {
		// sendRequestWithHttpURLConnection();
		// }
		if (v.getId() == R.id.button_sendRquest) {
			// sendRequestWithHttpURLConnection();
			// Toast.makeText(MainActivity.this,
			// "sendRequestWithHttpURLConnection", Toast.LENGTH_LONG)
			// .show();
			sendRequestWithHttpClient();
			Toast.makeText(MainActivity.this, "sendRequestWithHttpClient",
					Toast.LENGTH_LONG).show();
		}

	}

	private void sendRequestWithHttpClient() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				mResponseStatus = -1;
				try {
					HttpClient httpClient = new DefaultHttpClient();
//					HttpGet httpGet = new HttpGet("http://www.baidu.com");
					HttpGet httpGet = new HttpGet("http://192.168.0.121:8081/AppStoreTV");
					HttpResponse httpResponse = httpClient.execute(httpGet);
					if (200 == httpResponse.getStatusLine().getStatusCode()) {
						// 请求和响应都成功了
						mResponseStatus = 0;
						HttpEntity httpEntity = httpResponse.getEntity();
						// 指定字符集
						String response = EntityUtils.toString(httpEntity,
								"utf-8");
						Message message = new Message();
						message.what = SHOW_RESPONSE;
						// // 将服务器返回的结果存放到Message中
						message.obj = response.toString();
						mHandler.sendMessage(message);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (-1 == mResponseStatus) {
						mHandler.sendEmptyMessage(1);
					}

				}
			}
		}).start();

	}

	private void sendRequestWithHttpURLConnection() {
		// 开线程来发起网络请求
		new Thread(new Runnable() {

			@Override
			public void run() {
				HttpURLConnection connection = null;
				mResponseStatus = -1;
				try {
					URL url = new URL("http://www.baidu.com");
					// URL url = new
					// URL("http://192.168.0.121:8081/AppStoreTV");
					connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");
					connection.setConnectTimeout(8000);
					connection.setReadTimeout(8000);
					int status = connection.getResponseCode();
					if (status == 200) {
						mResponseStatus = 0;
					}
					// connection.
					// connection.
					InputStream in = connection.getInputStream();
					// 下面对获得到的输入流进行读取
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(in));
					StringBuilder response = new StringBuilder();
					String line;
					while ((line = reader.readLine()) != null) {
						response.append(line);
					}
					Message message = new Message();
					message.what = SHOW_RESPONSE;
					// // 将服务器返回的结果存放到Message中
					message.obj = response.toString();
					// message.obj = String.valueOf(status);
					mHandler.sendMessage(message);

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (-1 == mResponseStatus) {
						mHandler.sendEmptyMessage(1);
					}
					if (connection != null) {
						connection.disconnect();
					}
				}
			}
		}).start();

	}
}
// if (HttpURLConnection.HTTP_OK != connection
// .getResponseCode()) {
// Message message = new Message();
// message.what = SHOW_RESPONSE;
// // 将服务器返回的结果存放到Message中
// message.obj = "连接失败";
// mHandler.sendMessage(message);
// } else {